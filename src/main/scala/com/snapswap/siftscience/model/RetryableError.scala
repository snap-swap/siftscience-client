package com.snapswap.siftscience.model

import com.snapswap.retry.RetryableException

import scala.util.control.NoStackTrace

case class RetryableError(error: Error, message: Option[String] = None) extends RetryableException {
  override def getMessage: String =
    message.getOrElse(error.errorMessage)
}

object RetryableError {
  def apply(error: Error, message: String): RetryableError =
    new RetryableError(error, Some(message))
}


case class FatalError(error: Error, message: Option[String] = None) extends NoStackTrace {
  override def getMessage: String =
    message.getOrElse(error.errorMessage)
}

object FatalError {
  def apply(error: Error, message: String): FatalError =
    new FatalError(error, Some(message))
}


case object FeatureIsNotEnabled extends NoStackTrace


case class NonSiftScienceError(details: String) extends NoStackTrace {
  override def getMessage: String =
    details
}