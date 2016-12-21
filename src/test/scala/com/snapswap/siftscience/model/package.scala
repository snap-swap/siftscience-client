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

  val featureDisabled =
    """{
      |  "status": 0,
      |  "error_message": "OK",
      |  "time": 1482226137,
      |  "request": "{\"$phone\":\"353209111111\",\"$api_key\":\"e94ab48cc94bf95a\",\"$type\":\"$create_account\",\"$name\":\"Samantha Carter\",\"$referrer_user_id\":null,\"account_state\":\"newbie\",\"$ip\":\"127.0.0.1\",\"$session_id\":\"111111112\",\"time\":1482226136736,\"$user_id\":\"011111112\"}",
      |  "score_response": {
      |    "status": 111,
      |    "error_message": "This feature is not enabled in your feature plan."
      |  }
      |}""".stripMargin
}
