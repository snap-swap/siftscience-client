package com.snapswap.siftscience.model

sealed trait UpdateSiftAccount

sealed trait PaymentMethod

case class UpdateLuxtrust(givenName: String, familyName: String) extends UpdateSiftAccount

case class UpdateIdData(givenName: String, familyName: String) extends UpdateSiftAccount

case class UpdateEmail(email: String) extends UpdateSiftAccount

case class UpdateAddress(address1: String, address2: Option[String], city: String, region: Option[String], country: String, zip: String) extends UpdateSiftAccount

case class UpdateNickname(nickname: String) extends UpdateSiftAccount

case class UpdateCardAccount(bin: String, last4: String) extends PaymentMethod with UpdateSiftAccount

case class UpdateBankAccount(routingNumber: String) extends PaymentMethod with UpdateSiftAccount