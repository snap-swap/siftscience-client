package com.snapswap.siftscience.retry

import scala.concurrent.duration.FiniteDuration

case class RetryConfig(minBackoff: FiniteDuration, maxBackoff: FiniteDuration, maxAttempts: Int)