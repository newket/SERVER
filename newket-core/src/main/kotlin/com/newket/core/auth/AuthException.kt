package com.newket.core.auth

import com.newket.core.exception.BusinessException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class AuthException(
    message: String,
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
) : BusinessException(DEFAULT_CODE_PREFIX, errorCode, httpStatusCode, message) {

    class AuthFailedException(message: String = "인증에 실패했습니다.") : AuthException(message, 1, HttpStatus.BAD_REQUEST)

    class RefreshTokenNotFoundException(message: String = "존재하지 않는 refresh token 입니다.") : AuthException(message, 2, HttpStatus.BAD_REQUEST)

    class ExpiredTokenException(message: String = "만료된 토큰입니다.") : AuthException(message, 3, HttpStatus.UNAUTHORIZED)

    companion object {
        const val DEFAULT_CODE_PREFIX = "AUTH"
    }
}