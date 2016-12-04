package com.snapswap.siftscience


package object model {
  val responseRawJson =
    """{
      |  "score_response": {
      |    "workflow_statuses": [],
      |    "user_id": "011111112",
      |    "status": 0,
      |    "scores": {
      |      "payment_abuse": {
      |        "score": 0.06322920114013307,
      |        "reasons": [{
      |          "name": "Latest name",
      |          "value": "Samantha Carter"
      |        }, {
      |          "name": "Latest payment methods count",
      |          "value": "1"
      |        }]
      |      }
      |    },
      |    "error_message": "OK",
      |    "latest_labels": {
      |
      |    }
      |  },
      |  "status": 0,
      |  "error_message": "OK",
      |  "time": 1480725986
      |}""".stripMargin

  val abuseRawJson =
    """{
      |  "payment_abuse": {
      |    "score": 0.898391231245,
      |    "reasons": [
      |      {
      |        "name": "UsersPerDevice",
      |        "value": "4",
      |        "details": {
      |          "users": "a, b, c, d"
      |        }
      |      }
      |    ]
      |  }
      |}""".stripMargin

  val abuseDetailSeqRawJson =
    """[
      |  {
      |    "name": "UsersPerDevice",
      |    "value": "4",
      |    "details": {
      |      "users": "a, b, c, d"
      |    }
      |  }
      |]""".stripMargin
}
