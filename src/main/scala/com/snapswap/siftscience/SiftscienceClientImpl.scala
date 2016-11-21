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
import com.snapswap.siftscience.retry.{ExponentialBackOff, RetryConfig}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class SiftscienceClientImpl(apiKey: String,
                            retryConfig: RetryConfig,
                            verboseLogging: Boolean = true,
                            compactRequestPrint: Boolean = true)
                           (implicit system: ActorSystem, ec: ExecutionContext, materializer: Materializer)
  extends SiftscienceClient with SiftscienceRequestGenerator {

  import spray.json._
  import com.snapswap.siftscience.model.json._

  private val log = Logging(system, this.getClass)
  private val baseURL = "/v204/events"
  private val connectionFlow =
    Http()
      .outgoingConnectionHttps("api.siftscience.com")
      .log("siftscience")

  override def accountCreated(profile: String,
                              clientId: String,
                              profileState: String,
                              givenName: String,
                              familyName: String,
                              phone: String,
                              inviter: Option[String],
                              accounts: Seq[PaymentMethod],
                              promotions: Seq[Promotion],
                              ip: String,
                              time: Long): Future[Unit] = {
    val common: RequestCommon = RequestCommon("$create_account", apiKey, profile, clientId, profileState, ip, time)

    delivery(
      accountCreatedRequest(common, givenName, familyName, phone, inviter, accounts, promotions)
    )
  }

  override def updateAccount(profile: String,
                             clientId: String,
                             profileState: String,
                             ip: String,
                             time: Long,
                             update: UpdateSiftAccount): Future[Unit] = {
    val common: RequestCommon = RequestCommon("$update_account", apiKey, profile, clientId, profileState, ip, time)

    delivery(
      accountUpdateRequest(common, update)
    )
  }

  override def transaction(profile: String,
                           clientId: String,
                           profileState: String,
                           ip: String,
                           time: Long,
                           tx: Transaction): Future[Unit] = {
    val common: RequestCommon = RequestCommon("$transaction", apiKey, profile, clientId, profileState, ip, time)

    delivery(
      transactionRequest(common, tx)
    )
  }

  private def delivery(data: Map[String, JsValue], backOffCfg: Option[ExponentialBackOff] = None): Future[Unit] = {
    send[Unit](post(data)) { _ => () }
      .map(_ => ())
      .recover {
        case RetryableError(error) =>
          log.warning(s"Can't send '$data' to Sift Science because '$error', attempt retry")

          backOffCfg match {
            case Some(cfg) =>
              if (cfg.restartCount > retryConfig.maxAttempts) {
                log.error(s"After '${retryConfig.maxAttempts} stop send retry")
              } else {
                val next = cfg.nextBackOff()

                log.debug(s"Schedule resend event after '${next.calculateDelay.toSeconds}' seconds")
                system.scheduler.scheduleOnce(next.calculateDelay) {
                  delivery(data, Some(next))
                }
              }
            case None =>
              val cfg = ExponentialBackOff(retryConfig.minBackoff, retryConfig.maxBackoff, 0.1)

              system.scheduler.scheduleOnce(cfg.calculateDelay) {
                delivery(data, Some(cfg))
              }
          }
        case FatalError(error) =>
          log.error(s"Can't resend '${data.toJson.compactPrint}' to Sift Science because error code '${error.status.code}' with message '${error.errorMessage}'")
        case NonFatal(ex) =>
          log.error(ex, s"Can't resend '${data.toJson.compactPrint}' to Sift Science because '${ex.getMessage}'")
      }
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
    http(request).flatMap { response =>
      Unmarshal(response.entity).to[String].map { asString =>
        if (response.status.isSuccess()) {
          log.debug(s"SUCCESS ${request.method} -> ${response.status} '$asString'")
          asString
        } else {
          if(asString.isEmpty) {
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