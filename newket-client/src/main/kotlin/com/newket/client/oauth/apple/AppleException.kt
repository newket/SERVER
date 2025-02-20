package com.newket.client.oauth.apple

import com.newket.core.exception.BusinessException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class AppleException(
    message: String,
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
) : BusinessException(DEFAULT_CODE_PREFIX, errorCode, httpStatusCode, message) {

    class AppleNotFoundTokenException(message: String) : AppleException(message, 1, HttpStatus.BAD_REQUEST)

    class AppleRevokeFailException(message: String = "잘못된 애플의 accessToken 입니다.") : AppleException(message, 2, HttpStatus.BAD_REQUEST)

    companion object {
        const val DEFAULT_CODE_PREFIX = "APPLE"
    }
}