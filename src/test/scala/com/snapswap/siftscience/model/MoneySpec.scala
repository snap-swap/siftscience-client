package com.snapswap.siftscience.model

import org.scalatest.{Matchers, WordSpecLike}

class MoneySpec
  extends WordSpecLike
    with Matchers {

  "Money" should {
    "correct convert amount to micros" when {
      "amount 1 cent" in {
        val amount = Money(0.01, "USD")

        amount.micros shouldBe 10000L
      }

      "amount 5 USD" in {
        val amount = Money(5, "USD")

        amount.micros shouldBe 5000000L
      }
    }
  }
}