package com.aiadvertisement.core.exception

import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ExceptionHandler {

    fun handle(throwable: Throwable): AppException {
        return when (throwable) {
            is AppException -> throwable
            is CancellationException -> throw throwable
            is UnknownHostException -> AppException.NetworkException("网络不可用，请检查网络连接")
            is SocketTimeoutException -> AppException.NetworkException("网络请求超时，请稍后重试")
            is IOException -> AppException.NetworkException("网络异常：${throwable.message}")
            is retrofit2.HttpException -> {
                val code = throwable.code()
                val msg = when (code) {
                    400 -> "请求参数错误"
                    401 -> "未授权，请重新登录"
                    403 -> "拒绝访问"
                    404 -> "请求资源不存在"
                    500 -> "服务器内部错误"
                    502 -> "网关错误"
                    503 -> "服务不可用"
                    else -> "服务器错误($code)"
                }
                AppException.ServerException(code, msg)
            }
            else -> AppException.UnknownException(throwable.message ?: "未知错误")
        }
    }
}
