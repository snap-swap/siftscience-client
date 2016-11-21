package com.snapswap.siftscience

import com.snapswap.siftscience.model._

private[siftscience] trait SiftscienceRequestGenerator {

  import com.snapswap.siftscience.model.json._
  import spray.json._

  protected def accountCreatedRequest(common: RequestCommon,
                                      givenName: String,
                                      familyName: String,
                                      // parameter must be named 'phones', but "-Xfatal-warnings" force me to rename it to 'phoneParam'
                                      phoneParam: String,
                                      inviter: Option[String],
                                      accounts: Seq[PaymentMethod],
                                      // parameter must be named 'promotions', but "-Xfatal-warnings" force me to rename it to 'promotionsParam'
                                      promotionsParam: Seq[Promotion]): Map[String, JsValue] = {
    common.toJson.asJsObject.fields ++
      Map(
        "$name" -> s"$givenName $familyName".toJson,
        "$phone" -> phoneParam.toJson,
        "$referrer_user_id" -> inviter.toJson,
        "$promotions" -> promotionsParam.toJson,
        "$payment_methods" -> accounts.toJson
      )
  }

  protected def accountUpdateRequest(common: RequestCommon,
                                     update: UpdateSiftAccount): Map[String, JsValue] = {
    common.toJson.asJsObject.fields ++ update.toJson.asJsObject.fields
  }

  protected def transactionRequest(common: RequestCommon,
                                   tx: Transaction): Map[String, JsValue] = {
    common.toJson.asJsObject.fields ++ tx.toJson.asJsObject.fields
  }
}