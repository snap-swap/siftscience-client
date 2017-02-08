package com.snapswap.siftscience.model

import scala.concurrent.duration.FiniteDuration

case class RetryConfig(minBackoff: FiniteDuration, maxBackoff: FiniteDuration, maxAttempts: Int)

