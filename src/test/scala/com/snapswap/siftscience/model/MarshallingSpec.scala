package com.snapswap.siftscience.model

import com.snapswap.siftscience.model.json._
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.{Matchers, WordSpecLike}
import spray.json._

class MarshallingSpec extends WordSpecLike with Matchers {

  "unmarshaller" should {
    "parse Response" in {
      val result = responseRawJson.parseJson.convertTo[Response]
      result.time shouldBe new DateTime(1480725986.toLong * 1000, DateTimeZone.UTC)
    }

    "parse Abuse" in {
      val result = abuseRawJson.parseJson.convertTo[Seq[Abuse]]
      result.head.name shouldBe "payment_abuse"
    }

    "parse sequence of AbuseDetail" in {
      val result = abuseDetailSeqRawJson.parseJson.convertTo[Seq[AbuseDetail]]
      result.head.name shouldBe "UsersPerDevice"
    }

    "parse 'This feature is not enabled in your feature plan' response" in {
      intercept[FeatureIsNotEnabled.type] {
        featureDisabled.parseJson.convertTo[Response]
      }
    }
  }
}