package com.snapswap.siftscience.model

object Money {
  private val delimeter = 1000000

  def fromMicros(amount: Long, ccy: String): Money = {
    new Money(BigDecimal(amount / delimeter), ccy)
  }
}

case class Money(amount: BigDecimal, ccy: String) {
  // amount in micros, e.g. 1 cent = 10000 micros, $5 = 5000000
  def micros(): Long = {
    (amount * Money.delimeter).toLong
  }
}