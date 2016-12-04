package com.snapswap.siftscience.model

import org.joda.time.{DateTime, DateTimeZone}


case class Response(time: DateTime, userId: String, abuses: Seq[Abuse], errorCode: Int, errorMessage: String) {
  override def toString: String =
    s"timestamp => $time, userId => $userId, abuses => [${abuses.map(_.toString).mkString(", ")}]"
}

object Response {
  def apply(time: Long, userId: String, abuses: Seq[Abuse], errorCode: Int, errorMessage: String): Response =
    new Response(new DateTime(time * 1000, DateTimeZone.UTC), userId, abuses, errorCode, errorMessage)
}


case class Abuse(name: String, score: BigDecimal, details: Seq[AbuseDetail]) {
  def detailsAsString(): Option[String] =
    if (details.isEmpty) None else Some(details.map(_.toString).mkString("; "))

  override def toString: String =
    s"name => $name, score => $score, details => ${detailsAsString()}"
}


case class AbuseDetail(name: String, value: String, details: Option[String]) {
  override def toString: String = s"$name: $value${details.map(d => s"($d)").getOrElse("")}"
}



