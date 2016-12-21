package com.snapswap.siftscience.model

import scala.util.control.NoStackTrace

case class RetryableError(error: Error) extends NoStackTrace

case class FatalError(error: Error) extends NoStackTrace

case object FeatureIsNotEnabled extends NoStackTrace