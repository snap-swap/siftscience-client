package com.snapswap.siftscience

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.snapswap.siftscience.model._
import com.snapswap.retry.{RetryableAction, RetryableException}
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class SiftscienceClientImpl(apiKey: String,
                            retryConfig: RetryConfig,
                            verboseLogging: Boolean = true,
                            compactRequestPrint: Boolean = true)
                           (implicit system: ActorSystem, ec: ExecutionContext, materializer: Materializer)
  extends SiftscienceClient with SiftscienceRequestGenerator {

  import com.snapswap.siftscience.model.json._
  import spray.json._

  private val log = Logging(system, this.getClass)
  private val baseURL = "/v204/events?return_score=true"
  private val connectionFlow =
    Http()
      .outgoingConnectionHttps("api.siftscience.com")
      .log("siftscience")

  private def deliver(delivery: => Future[Unit], actionType: String): Future[Unit] = {

    val whenRetry: (String, RetryableException, Int) => Future[Unit] =
      (actionName: String, ex: RetryableException, attemptNumber: Int) =>
        Future(log.warning(s"[$actionName] failed at attempt number $attemptNumber with retryable exception: ${ex.getMessage}, attempt retry"))

    val whenFatal: (String, Throwable, Int) => Future[Unit] =
      (actionName: String, ex: Throwable, attemptNumber: Int) =>
        Future(log.error(ex, s"[$actionName] failed at attempt number $attemptNumber with ${ex.getClass.getSimpleName}: ${ex.getMessage}, can't retry"))

    val whenSuccess: (String, Int) => Future[Unit] =
      (_: String, _: Int) =>
        Future.successful(())


    RetryableAction(delivery, actionType, retryConfig.minBackoff, retryConfig.maxBackoff, retryConfig.maxAttempts, 0.1)(
      whenRetryAction = whenRetry,
      whenFatalAction = whenFatal,
      whenSuccessAction = whenSuccess
    )
  }

  override def accountCreated(profile: String,
                              clientId: Option[String],
                              profileState: String,
                              givenName: Option[String],
                              familyName: Option[String],
                              phone: String,
                              inviter: Option[String],
                              ip: Option[String],
                              accounts: Seq[PaymentMethod] = Seq.empty[PaymentMethod],
                              promotions: Seq[Promotion] = Seq.empty[Promotion],
                              time: Long = nowUTC(),
                              postCallAction: Response => Future[Unit]): Future[Unit] = {
    val actionType = "$create_account"
    val common: RequestCommon = RequestCommon(actionType, apiKey, profile, clientId, profileState, ip, time)

    deliver(
      delivery(
        accountCreatedRequest(common, givenName, familyName, phone, inviter, accounts, promotions)
      )(postCallAction), actionType)
  }

  override def updateAccount(profile: String,
                             clientId: Option[String],
                             profileState: String,
                             ip: Option[String],
                             update: UpdateSiftAccount,
                             time: Long = nowUTC(),
                             postCallAction: Response => Future[Unit]): Future[Unit] = {
    val actionType = "$update_account"
    val common: RequestCommon = RequestCommon(actionType, apiKey, profile, clientId, profileState, ip, time)

    deliver(
      delivery(
        accountUpdateRequest(common, update)
      )(postCallAction), actionType)
  }

  override def transaction(profile: String,
                           clientId: Option[String],
                           profileState: String,
                           ip: Option[String],
                           tx: Transaction,
                           time: Long = nowUTC(),
                           postCallAction: Response => Future[Unit]): Future[Unit] = {
    val actionType = "$transaction"
    val common: RequestCommon = RequestCommon(actionType, apiKey, profile, clientId, profileState, ip, time)

    deliver(
      delivery(
        transactionRequest(common, tx)
      )(postCallAction), actionType)
  }

  override protected def nowUTC(): Long = {
    new DateTime(DateTimeZone.UTC).getMillis
  }

  private def delivery(data: Map[String, JsValue])(responseAction: Response => Future[Unit]): Future[Unit] = {
    for {
      response <- send[Response](post(data))(_.parseJson.convertTo[Response])
      _ <- responseAction(response).recover {
        case NonFatal(ex) =>
          log.error(ex, s"Error occurred due to processing response action: ${response.toString}")
      }
    } yield ()
  }.recover {
    case RetryableError(error, _) =>
      throw RetryableError(error, s"Can't send '$data' to Sift Science because '$error', attempt retry")
    case FatalError(error, _) =>
      throw FatalError(error, s"Can't resend '${data.toJson.compactPrint}' to Sift Science because error code '${error.status.code}' with message '${error.errorMessage}'")
    case FeatureIsNotEnabled =>
      log.warning(s"Request send, but response is 'This feature is not enabled in your feature plan', skip response action")
    case NonFatal(ex) =>
      throw NonSiftScienceError(s"Can't resend '${data.toJson.compactPrint}' to Sift Science because '${ex.getMessage}'")
  }


  private def http(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(connectionFlow).runWith(Sink.head)

  private def post(data: Map[String, JsValue]): HttpRequest = {
    val content = if (compactRequestPrint) {
      data.toJson.compactPrint
    } else {
      data.toJson.prettyPrint
    }

    if (verboseLogging) {
      log.debug(s"Prepare request: POST $baseURL with $content")
    } else {
      log.debug(s"Prepare request: POST $baseURL")
    }

    Post(baseURL)
      .withEntity(HttpEntity(`application/json`, content))
  }


  private def send[T](request: HttpRequest)(handler: String => T): Future[T] = {
    http(request).flatMap {
      response =>
        Unmarshal(response.entity).to[String].map {
          asString =>
            if (response.status.isSuccess()) {
              log.debug(s"SUCCESS ${request.method} -> ${response.status} '$asString'")
              asString
            } else {
              if (asString.isEmpty) {
                throw new RuntimeException(s"Unknown response: '$response'")
              } else {
                val error = asString.parseJson.convertTo[Error]

                if (error.status.code < 0) {
                  log.error(s"FAILURE ${request.method} -> ${response.status} '$asString', attempt send request again")
                  throw RetryableError(error)
                } else {
                  log.error(s"FAILURE ${request.method} -> ${response.status} '$asString'")
                  throw FatalError(error)
                }
              }
            }
        }
    }.map(handler)
  }
}