package com.aiadvertisement.core.exception

sealed class AppException : Exception() {
    data class NetworkException(override val message: String) : AppException()
    data class ServerException(val code: Int, override val message: String) : AppException()
    data class BusinessException(val code: Int, override val message: String) : AppException()
    data class ParseException(override val message: String) : AppException()
    data class UnknownException(override val message: String) : AppException()
}
