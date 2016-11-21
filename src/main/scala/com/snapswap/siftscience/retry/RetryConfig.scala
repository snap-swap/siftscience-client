package com.snapswap.siftscience.retry

import scala.concurrent.duration.FiniteDuration

case class RetryConfig(initialDelay: FiniteDuration, maxAttempts: Int)