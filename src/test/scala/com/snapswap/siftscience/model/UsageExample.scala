package com.snapswap.siftscience.model

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.snapswap.siftscience.retry.RetryConfig
import com.snapswap.siftscience.{SiftscienceClient, SiftscienceClientImpl}
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.Await
import scala.concurrent.duration._

object UsageExample {
  def main(args: Array[String]): Unit = {
    require(args.length == 1, "You must provide API key as run parameter")

    implicit val system = ActorSystem("UsageSample")
    implicit val materializer = ActorMaterializer()

    import system.dispatcher

    val config = RetryConfig(1.second, 60.seconds, 10)
    val client: SiftscienceClient = new SiftscienceClientImpl(args(0), config, true, true)

    val bankAccount = UpdateBankAccount("LU780030276335160000")
    val promotion = Promotion("Bonus", "success", "Signup", "system", Money(5, "USD"))

    val request = client.accountCreated(
      profile = "011111112",
      clientId = "111111112",
      profileState = "newbie",
      givenName = "Samantha",
      familyName = "Carter",
      phone = "353209111111",
      inviter = None,
      accounts = Seq(bankAccount),
      promotions = Seq(promotion),
      ip = "127.0.0.1",
      time = new DateTime(DateTimeZone.UTC).getMillis
    )

    Await.result(request, Duration.Inf)
    system.log.info("Example completed")
  }
}