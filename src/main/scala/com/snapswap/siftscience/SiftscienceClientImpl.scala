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
import com.snapswap.siftscience.retry.RetryConfig

import scala.concurrent.{ExecutionContext, Future}

class SiftscienceClientImpl(apiKey: String,
                            retryConfig: RetryConfig,
                            verboseLogging: Boolean = true,
                            compactRequestPrint: Boolean = true)
                           (implicit system: ActorSystem, ec: ExecutionContext, materializer: Materializer)
  extends SiftscienceClient with SiftscienceRequestGenerator {

  import spray.json._
  import com.snapswap.siftscience.model.json._

  private val log = Logging(system, this.getClass)
  private val baseURL = "v204/events"
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

  private def delivery(data: Map[String, JsValue]): Future[Unit] = {
//    system.scheduler.scheduleOnce(retryConfig.initialDelay) {
//
//    }
    send[String](post(data)) { response =>
      response
    }.map(_ => ())
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
          log.debug(s"SUCCESS ${request.method} ${request.uri} -> ${response.status} '$asString'")
          asString
        } else {
          RetryableError
          log.error(s"FAILURE ${request.method} ${request.uri} -> ${response.status} '$asString'")
          null
        }
      }
    }.map(handler)
  }
}