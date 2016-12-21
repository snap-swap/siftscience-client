package com.snapswap.siftscience.model

object ErrorCodeEnum extends Enumeration {
  val `Service currently unavailable` = ErrorCode(-4, "Service currently unavailable. Please try again later.")
  val `Server-side timeout processing request` = ErrorCode(-3, "Server-side timeout processing request. Please try again later.")
  val `Unexpected server-side error -1` = ErrorCode(-2, "Unexpected server-side error")
  val `Unexpected server-side error -2` = ErrorCode(-1, "Unexpected server-side error")
  val `OK` = ErrorCode(0, "OK")
  val `Invalid API key` = ErrorCode(51, "Invalid API key")
  val `Invalid characters in field name` = ErrorCode(52, "Invalid characters in field name")
  val `Invalid characters in field value` = ErrorCode(53, "Invalid characters in field value")
  val `Missing required field` = ErrorCode(55, "Missing required fieldvalue")
  val `Invalid JSON in request` = ErrorCode(57, "Invalid JSON in request")
  val `Rate limited` = ErrorCode(60, "Rate limited")
  val `Invalid API version` = ErrorCode(104, "Invalid API version")
  val `Not a valid reserved field` = ErrorCode(105, "Not a valid reserved field")

  case class ErrorCode(code: Int, name: String) extends super.Val() {
    override def toString(): String = {
      s"'$code' -> '$name'"
    }
  }

  def withCode(code: Int): ErrorCodeEnum.ErrorCode = {
    values
      .find(_.asInstanceOf[ErrorCode].code == code)
      .map(_.asInstanceOf[ErrorCode])
      .getOrElse(throw new NoSuchElementException(s"Unknown error code '$code'"))
  }
}