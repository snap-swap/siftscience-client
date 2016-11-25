package com.snapswap.siftscience.model

import org.scalatest.{Matchers, WordSpecLike}

class MicrosSpec
  extends WordSpecLike
    with Matchers {

  "Micros" should {
    "correct convert amount to micros" when {
      "amount 1 cent" in {
        val result = Micros.micros(BigDecimal(0.01))

          result shouldBe 10000L
      }

      "amount 5 USD" in {
        val result = Micros.micros(BigDecimal(5))

        result shouldBe 5000000L
      }
    }
    "correct convert micros to amount" when {
      "amount 1 cent" in {
        val result = Micros.micros(10000L)

        result shouldBe BigDecimal(0.01)
      }

      "amount 5 USD" in {
        val result = Micros.micros(5000000L)

        result shouldBe BigDecimal(5)
      }
    }
  }
}