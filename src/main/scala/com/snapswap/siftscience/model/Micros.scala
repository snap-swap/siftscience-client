package com.snapswap.siftscience.model

object Micros {
  private val delimeter: Double = 1000000d

  def micros(amount: Long): BigDecimal = {
    BigDecimal(amount / delimeter)
  }

  def micros(amount: BigDecimal): Long = {
    (amount * delimeter).toLong
  }
}