package com.snapswap.siftscience.model

import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import com.snapswap.siftscience.model.json._
import org.joda.time.{DateTime, DateTimeZone}

class MarshallingSpec extends FlatSpec with Matchers {

  "unmarshaller" should "parse Response" in {
    val result = responseRawJson.parseJson.convertTo[Response]
    result.time shouldBe new DateTime(1480725986.toLong * 1000, DateTimeZone.UTC)
  }
  it should "parse Abuse" in {
    val result = abuseRawJson.parseJson.convertTo[Seq[Abuse]]
    result.head.name shouldBe "payment_abuse"
  }
  it should "parse sequence of AbuseDetail" in {
    val result = abuseDetailSeqRawJson.parseJson.convertTo[Seq[AbuseDetail]]
    result.head.name shouldBe "UsersPerDevice"
  }
}
