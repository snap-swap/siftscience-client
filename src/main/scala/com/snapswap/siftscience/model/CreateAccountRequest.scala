package com.snapswap.siftscience.model

private[siftscience] case class RequestCommon(`type`: String,
                                              apiKey: String,
                                              userId: String,
                                              sessionId: Option[String],
                                              accountState: String,
                                              ip: Option[String],
                                              time: Long)