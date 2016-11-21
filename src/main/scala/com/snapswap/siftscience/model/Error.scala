package com.snapswap.siftscience.model

case class Error(status: ErrorCodeEnum.ErrorCode, errorMessage: String, time: Long, request: String)