package com.snapswap.siftscience.model

import com.snapswap.siftscience.model.ErrorCodeEnum.ErrorCode
import spray.json.DefaultJsonProtocol


object json extends DefaultJsonProtocol {

  import spray.json._
  import com.snapswap.siftscience.utils.JsonHelper._

  sealed trait WriteOnly[T] {
    def read(json: JsValue): T = {
      deserializationError(s"write only")
    }
  }


  implicit val AbuseDetailSeqFormat = new RootJsonReader[Seq[AbuseDetail]] {
    override def read(json: JsValue): Seq[AbuseDetail] = {

      val reasons = json match {
        case JsArray(r) =>
          r
        case other =>
          deserializationError(s"Expected array but got: ${other.prettyPrint}")
      }

      reasons.map { r =>
        val name = (r / "name").convertTo[String]
        val value = (r / "value").convertTo[String]
        val details = (r / "details").map(_.prettyPrint)

        (name, value, details) match {
          case (Some(n), Some(v), d) =>
            AbuseDetail(n, v, d)
          case _ =>
            deserializationError(s"Can't deserialize to AbuseDetail: ${r.prettyPrint}")
        }
      }

    }
  }

  implicit val AbuseSeqFormat = new RootJsonReader[Seq[Abuse]] {
    override def read(json: JsValue): Seq[Abuse] = {

      json.asJsObject.fields.toSeq.map { case (name, abuse) =>
        val score = (abuse / "score").convertTo[BigDecimal]
        val detail = (abuse / "reasons").convertTo[Seq[AbuseDetail]].getOrElse(Seq.empty)

        (name, score, detail) match {
          case (n, Some(s), d) =>
            Abuse(n, s, d)
          case _ =>
            deserializationError(s"Can't deserialize to Abuse: ${json.prettyPrint}")
        }
      }

    }
  }

  implicit val ResponseFormat = new RootJsonReader[Response] {
    override def read(json: JsValue): Response = {

      val timestamp = (json / "time").convertTo[Long]
      val userId = (json / "score_response" / "user_id").convertTo[String]
      val errorCode = (json / "status").convertTo[Int]
      val errorMessages = (json / "error_message").convertTo[String]
      val abuses = (json / "score_response" / "scores").convertTo[Seq[Abuse]]

      (timestamp, userId, abuses, errorCode, errorMessages) match {
        case (Some(t), Some(u), Some(a), Some(err), Some(msg)) if a.nonEmpty =>
          Response(t, u, a, err, msg)
        case _ =>
          deserializationError(s"Can't deserialize to Response: ${json.prettyPrint}")
      }

    }
  }


  implicit val MoneyFormat = new RootJsonFormat[Money] with WriteOnly[Money] {
    override def write(obj: Money): JsValue =
      JsObject(
        "$amount" -> Micros.micros(obj.amount).toJson,
        "$currency_code" -> obj.ccy.toJson
      )
  }

  implicit val updateLuxtrustFormat = new RootJsonFormat[UpdateLuxtrust] with WriteOnly[UpdateLuxtrust] {
    override def write(obj: UpdateLuxtrust): JsValue = {
      JsObject(
        "$name" -> s"${obj.givenName} ${obj.familyName}".toJson
      )
    }
  }

  implicit val updateIdDataFormat = new RootJsonFormat[UpdateIdData] with WriteOnly[UpdateIdData] {
    override def write(obj: UpdateIdData): JsValue = {
      JsObject(
        "$name" -> Seq(obj.givenName, obj.familyName).flatten.mkString(" ").toJson
      )
    }
  }

  implicit val updateEmailFormat = new RootJsonFormat[UpdateEmail] with WriteOnly[UpdateEmail] {
    override def write(obj: UpdateEmail): JsValue = {
      JsObject(
        "$email" -> obj.email.toJson
      )
    }
  }

  implicit val updateAddressFormat = new RootJsonFormat[UpdateAddress] with WriteOnly[UpdateAddress] {
    override def write(obj: UpdateAddress): JsValue = {
      JsObject(
        "$address_1" -> obj.address1.toJson,
        "$address_2" -> obj.address2.toJson,
        "$city" -> obj.city.toJson,
        "$region" -> obj.region.toJson,
        "$country" -> obj.country.toJson,
        "$zipcode" -> obj.zip.toJson
      )
    }
  }

  implicit val updateNicknameFormat = new RootJsonFormat[UpdateNickname] with WriteOnly[UpdateNickname] {
    override def write(obj: UpdateNickname): JsValue = {
      JsObject(
        "user_name" -> obj.nickname.toJson
      )
    }
  }

  implicit val updateCardAccountFormat = new RootJsonFormat[UpdateCardAccount] with WriteOnly[UpdateCardAccount] {
    override def write(obj: UpdateCardAccount): JsValue = {
      JsObject(
        "$payment_type" -> "$credit_card".toJson,
        "$card_bin" -> obj.bin.toJson,
        "$card_last4" -> obj.last4.toJson
      )
    }
  }

  implicit val updateBankAccountFormat = new RootJsonFormat[UpdateBankAccount] with WriteOnly[UpdateBankAccount] {
    override def write(obj: UpdateBankAccount): JsValue = {
      JsObject(
        "$payment_type" -> "$electronic_fund_transfer".toJson,
        "$routing_number" -> obj.routingNumber.toJson
      )
    }
  }

  implicit val paymentMethodFormat = new RootJsonFormat[PaymentMethod] with WriteOnly[PaymentMethod] {
    override def write(obj: PaymentMethod): JsValue = {
      obj match {
        case update: UpdateCardAccount =>
          update.toJson
        case update: UpdateBankAccount =>
          update.toJson
      }
    }
  }
  implicit val updateAccountFormat = new RootJsonFormat[UpdateSiftAccount] with WriteOnly[UpdateSiftAccount] {
    override def write(obj: UpdateSiftAccount): JsValue = {
      obj match {
        case update: UpdateLuxtrust =>
          update.toJson
        case update: UpdateIdData =>
          update.toJson
        case update: UpdateEmail =>
          update.toJson
        case update: UpdateAddress =>
          update.toJson
        case update: UpdateNickname =>
          update.toJson
        case update: UpdateCardAccount =>
          update.toJson
        case update: UpdateBankAccount =>
          update.toJson
      }
    }
  }

  implicit val bankDepositFormat = new RootJsonFormat[BankDeposit] with WriteOnly[BankDeposit] {
    override def write(obj: BankDeposit): JsValue = {
      val micros = Micros.micros(obj.amount)

      JsObject(
        "$transaction_type" -> "$deposit".toJson,
        "$transaction_status" -> obj.status.toJson,
        "$amount" -> micros.toJson,
        "$currency_code" -> obj.ccy.toJson,
        "$order_id" -> obj.transferId.toJson,
        "$transaction_id" -> obj.transactionId.toJson,
        "$payment_method" -> UpdateBankAccount(obj.bankCode).toJson
      )
    }
  }
  implicit val cardDepositFormat = new RootJsonFormat[CardDeposit] with WriteOnly[CardDeposit] {
    override def write(obj: CardDeposit): JsValue = {
      val micros = Micros.micros(obj.amount)

      JsObject(
        "$transaction_type" -> "$deposit".toJson,
        "$transaction_status" -> obj.status.toJson,
        "$amount" -> micros.toJson,
        "$currency_code" -> obj.ccy.toJson,
        "$order_id" -> obj.transferId.toJson,
        "$transaction_id" -> obj.transactionId.toJson,
        "$payment_method" -> UpdateCardAccount(obj.bin, obj.last4).toJson
      )
    }
  }
  implicit val bankwithdrawalformat = new RootJsonFormat[BankWithdrawal] with WriteOnly[BankWithdrawal] {
    override def write(obj: BankWithdrawal): JsValue = {
      val micros = Micros.micros(obj.amount)

      JsObject(
        "$transaction_type" -> "$withdrawal".toJson,
        "$transaction_status" -> obj.status.toJson,
        "$amount" -> micros.toJson,
        "$currency_code" -> obj.ccy.toJson,
        "$order_id" -> obj.transferId.toJson,
        "$transaction_id" -> obj.transactionId.toJson,
        "$payment_method" -> UpdateBankAccount(obj.bankCode).toJson
      )
    }
  }
  implicit val sendPaymentFormat = new RootJsonFormat[SendPayment] with WriteOnly[SendPayment] {
    override def write(obj: SendPayment): JsValue = {
      val micros = Micros.micros(obj.amount)

      JsObject(
        "$transaction_type" -> "$transfer".toJson,
        "$transaction_status" -> obj.status.toJson,
        "$amount" -> micros.toJson,
        "$currency_code" -> obj.ccy.toJson,
        "$order_id" -> obj.transferId.toJson,
        "$transaction_id" -> obj.transactionId.toJson,
        "$recipient_user_id" -> obj.payeeId.toJson
      )
    }
  }
  implicit val receivePaymentFormat = new RootJsonFormat[ReceivePayment] with WriteOnly[ReceivePayment] {
    override def write(obj: ReceivePayment): JsValue = {
      val micros = Micros.micros(obj.amount)

      JsObject(
        "$transaction_type" -> "$transfer".toJson,
        "$transaction_status" -> obj.status.toJson,
        "$amount" -> micros.toJson,
        "$currency_code" -> obj.ccy.toJson,
        "$order_id" -> obj.transferId.toJson,
        "$transaction_id" -> obj.transactionId.toJson,
        "$sender_user_id" -> obj.payerId.toJson
      )
    }
  }

  implicit val transactionFormat = new RootJsonFormat[Transaction] with WriteOnly[Transaction] {
    override def write(obj: Transaction): JsValue = {
      obj match {
        case tx: BankDeposit =>
          tx.toJson
        case tx: CardDeposit =>
          tx.toJson
        case tx: BankWithdrawal =>
          tx.toJson
        case tx: SendPayment =>
          tx.toJson
        case tx: ReceivePayment =>
          tx.toJson
      }
    }
  }

  implicit val RequestCommonFormat = jsonFormat(RequestCommon,
    "$type",
    "$api_key",
    "$user_id",
    "$session_id",
    "account_state",
    "$ip",
    "time")

  implicit val PromotionFormat = jsonFormat(Promotion,
    "$promotion_id",
    "$status",
    "$description",
    "$referrer_user_id",
    "$discount")

  implicit val errorCodeFormat = new RootJsonFormat[ErrorCodeEnum.ErrorCode] {
    override def read(json: JsValue): ErrorCode = {
      ErrorCodeEnum.withCode(json.convertTo[Int])
    }

    override def write(obj: ErrorCode): JsValue = {
      obj.code.toJson
    }
  }

  implicit val errorFormat = jsonFormat(Error, "status", "error_message", "time", "request")
}