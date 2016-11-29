package com.snapswap.siftscience.model

import com.snapswap.siftscience.SiftscienceRequestGenerator
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.{Matchers, WordSpecLike}

class SiftscienceRequestGeneratorSpec extends WordSpecLike with Matchers {

  import spray.json._
  import com.snapswap.siftscience.model.json._

  "SiftscienceRequestGenerator" should {
    "correct compose common part" in new setup {
      val result: Map[String, JsValue] = common.toJson.asJsObject.fields

      result should contain {
        // Common part
        "$type" -> "$create_account".toJson
        "$api_key" -> "582ad99ce4b014f72ff13308".toJson
        "$user_id" -> "011111112".toJson
        "$session_id" -> "111111112".toJson
        "account_state" -> "newbie".toJson
        "$ip" -> "127.0.0.1".toJson
        "time" -> now.toJson
      }
    }

    "correct compose promotion part" in new setup {
      val result: Map[String, JsValue] = promotion.toJson.asJsObject.fields

      result should contain {
        "$promotion_id" -> "GlonetaRegistrationBonus".toJson
        "$status" -> "success".toJson
        "$description" -> "Signup bonus for new Gloneta customers in 2016".toJson
        "$referrer_user_id" -> "S#INVITER".toJson
        "$discount" -> JsObject(
          "$amount" -> 5000000.toJson,
          "$currency_code" -> "USD".toJson
        )
      }
    }

    "correct compose update bank account" in new setup {
      val result: Map[String, JsValue] = bankAccount.toJson.asJsObject.fields

      result should have size 2
      result should contain {
        "$payment_type" -> "$electronic_fund_transfer".toJson
        "$routing_number" -> "LU780030276335160000".toJson
      }
    }

    "correct compose 'update card' account" in new setup {
      val result: Map[String, JsValue] = updateCardAccount.toJson.asJsObject.fields

      result should have size 3
      result should contain {
        "$payment_type" -> "$credit_card".toJson
        "$card_bin" -> "123456".toJson
        "$card_last4" -> "7890".toJson
      }
    }

    "correct compose 'update Luxtrust' account" in new setup {
      val result: Map[String, JsValue] = updateLuxtrust.toJson.asJsObject.fields

      result should have size 1
      result should contain {
        "$name" -> "Samantha Carter".toJson
      }
    }

    "correct compose 'update ID data' account" in new setup {
      val result: Map[String, JsValue] = updateIdData.toJson.asJsObject.fields

      result should have size 1
      result should contain {
        "$name" -> "Samantha Carter".toJson
      }
    }

    "correct compose 'update address' account" in new setup {
      val result: Map[String, JsValue] = updateAddress.toJson.asJsObject.fields

      result should have size 6
      result should contain {
        "$address_1" -> "2100 Main Street".toJson
        "$address_2" -> "Apt 3B".toJson
        "$city" -> "Luxemburg".toJson
        "$country" -> "LU".toJson
        "$zipcode" -> "1623".toJson
      }
    }

    "correct compose 'update nickname' account" in new setup {
      val result: Map[String, JsValue] = updateNickname.toJson.asJsObject.fields

      result should have size 1
      result should contain {
        "$user_name" -> "johndoe".toJson
      }
    }

    "correct compose 'create account' request" in new setup {
      val result: Map[String, JsValue] = accountCreatedRequest(common, Some("Samantha"), Some("Carter"), "353209111111", Some("S#INVITER"), Seq(bankAccount), Seq(promotion))

      result should contain {
        "$name" -> "Samantha Carter".toJson
        "$phone" -> "353209111111".toJson
        "$referrer_user_id" -> "S#INVITER".toJson
      }

      result should contain key "$promotions"
      val promo: JsArray = result("$promotions").asInstanceOf[JsArray]
      promo.elements should have size 1

      result should contain key "$payment_methods"
      val accs: JsArray = result("$payment_methods").asInstanceOf[JsArray]
      accs.elements should have size 1
    }

    "correct compose receiving a payment 'transaction' request" in new setup {
      val tx = ReceivePayment("transfer id", "transaction id", 0.01, "EUR", "011111113")
      val result: Map[String, JsValue] =  transactionRequest(common, tx)

      result should contain {
        "$amount" -> 10000.toJson
        "$sender_user_id" -> "011111113".toJson
        "time" -> now.toJson
      }
    }

    "correct compose sending a payment 'transaction' request" in new setup {
      val tx = SendPayment("transfer id", "transaction id", 0.01, "EUR", "011111113")
      val result: Map[String, JsValue] =  transactionRequest(common, tx)

      result should contain {
        "$amount" -> 10000.toJson
        "$recipient_user_id" -> "011111113".toJson
        "time" -> now.toJson
      }
    }

    "correct compose 'bank deposit' transaction request" in new setup {
      val result: Map[String, JsValue] = bankDeposit.toJson.asJsObject.fields

      result should have size 7
      result should contain {
        "$transaction_type" -> "$deposit".toJson
      }

      result should contain key "$payment_method"
    }

    "correct compose 'card deposit' transaction request" in new setup {
      val result: Map[String, JsValue] = cardDeposit.toJson.asJsObject.fields

      result should have size 7
      result should contain {
        "$transaction_type" -> "$deposit".toJson
      }

      result should contain key "$payment_method"
    }

    "correct compose 'bank withdrawal' transaction request" in new setup {
      val result: Map[String, JsValue] = bankWithdrawal.toJson.asJsObject.fields

      result should have size 7
      result should contain {
        "$transaction_type" -> "$withdrawal".toJson
      }

      result should contain key "$payment_method"
    }

    "correct compose 'receive payment' transaction request" in new setup {
      val result: Map[String, JsValue] = receivePayment.toJson.asJsObject.fields

      result should have size 7
      result should contain {
        "$transaction_type" -> "$transfer".toJson
      }

      result should contain key "$sender_user_id"
    }

    "correct compose 'send payment' transaction request" in new setup {
      val result: Map[String, JsValue] = sendPayment.toJson.asJsObject.fields

      result should have size 7
      result should contain {
        "$transaction_type" -> "$transfer".toJson
      }

      result should contain key "$recipient_user_id"
    }
  }

  abstract class setup extends SiftscienceRequestGenerator {
    val now: Long = new DateTime(DateTimeZone.UTC).getMillis

    val promotion = Promotion("GlonetaRegistrationBonus", "success", "Signup bonus for new Gloneta customers in 2016", "S#INVITER", Money(5, "USD"))

    val updateLuxtrust: UpdateSiftAccount = UpdateLuxtrust("Samantha", "Carter")
    val updateIdData: UpdateSiftAccount = UpdateIdData(Some("Samantha"), Some("Carter"))
    val updateEmail: UpdateSiftAccount = UpdateEmail("test@test.com")
    val updateAddress: UpdateSiftAccount = UpdateAddress("2100 Main Street", Some("Apt 3B"), "Luxemburg", None, "LU", "1623")
    val updateNickname: UpdateSiftAccount = UpdateNickname("johndoe")
    val updateCardAccount: PaymentMethod = UpdateCardAccount("123456", "7890")
    val bankAccount: PaymentMethod = UpdateBankAccount("LU780030276335160000")

    val bankDeposit = BankDeposit("transfer id", "transaction id", 0.01, "EUR", "011111113")
    val cardDeposit = CardDeposit("transfer id", "transaction id", 0.01, "EUR", "123456", "7890")
    val bankWithdrawal = BankWithdrawal("transfer id", "transaction id", 0.01, "EUR", "LU780030276335160000")
    val receivePayment = ReceivePayment("transfer id", "transaction id", 0.01, "EUR", "011111113")
    val sendPayment = SendPayment("transfer id", "transaction id", 0.01, "EUR", "011111113")

    val common = RequestCommon(
      `type` = "$create_account",
      apiKey = "582ad99ce4b014f72ff13308",
      userId = "011111112",
      sessionId = Some("111111112"),
      accountState = "newbie",
      ip = Some("127.0.0.1"),
      time = now)
  }

}