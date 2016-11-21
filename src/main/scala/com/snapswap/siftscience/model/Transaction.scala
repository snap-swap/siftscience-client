package com.snapswap.siftscience.model

sealed trait Transaction {
  def profile: String

  def transferId: String

  def amount: Money

  def status: String

  def transactionId: String
}

case class BankDeposit(profile: String, transferId: String, transactionId: String, amount: Money, bankCode: String, status: String = "$success") extends Transaction

case class CardDeposit(profile: String, transferId: String, transactionId: String, amount: Money, bin: String, last4: String, status: String = "$success") extends Transaction

case class BankWithdrawal(profile: String, transferId: String, transactionId: String, amount: Money, bankCode: String, status: String = "$success") extends Transaction

case class SendPayment(profile: String, transferId: String, transactionId: String, amount: Money, payeeId: String, status: String = "$success") extends Transaction

case class ReceivePayment(profile: String, transferId: String, transactionId: String, amount: Money, payerId: String, status: String = "$success") extends Transaction