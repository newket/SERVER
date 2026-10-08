package com.newket.core.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class AdminException(
    message: String,
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
) : BusinessException(DEFAULT_CODE_PREFIX, errorCode, httpStatusCode, message) {

    class AdminUserNotFoundException(message: String = "아이디 또는 비밀번호가 올바르지 않습니다.") : AdminException(message, 1, HttpStatus.UNAUTHORIZED)

    companion object {
        const val DEFAULT_CODE_PREFIX = "ADMIN"
    }
}