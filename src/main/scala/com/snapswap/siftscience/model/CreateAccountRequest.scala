package com.snapswap.siftscience.model

private[siftscience] case class RequestCommon(`type`: String,
                                              apiKey: String,
                                              userId: String,
                                              sessionId: String,
                                              accountState: String,
                                              ip: String,
                                              time: Long)