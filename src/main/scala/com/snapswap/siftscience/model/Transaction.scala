package com.snapswap.siftscience.model

sealed trait Transaction {
  def transferId: String

  def amount: BigDecimal

  def ccy: String

  def status: String

  def transactionId: String
}

case class BankDeposit(transferId: String, transactionId: String, amount: BigDecimal, ccy: String, bankCode: String, status: String = "$success") extends Transaction

case class CardDeposit(transferId: String, transactionId: String, amount: BigDecimal, ccy: String, bin: String, last4: String, status: String = "$success") extends Transaction

case class BankWithdrawal(transferId: String, transactionId: String, amount: BigDecimal, ccy: String, bankCode: String, status: String = "$success") extends Transaction

case class SendPayment(transferId: String, transactionId: String, amount: BigDecimal, ccy: String, payeeId: String, status: String = "$success") extends Transaction

case class ReceivePayment(transferId: String, transactionId: String, amount: BigDecimal, ccy: String, payerId: String, status: String = "$success") extends Transaction