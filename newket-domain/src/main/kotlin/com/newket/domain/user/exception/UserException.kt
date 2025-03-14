package com.newket.domain.user.exception

import com.newket.core.exception.BusinessException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class UserException(
    message: String,
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
) : BusinessException(DEFAULT_CODE_PREFIX, errorCode, httpStatusCode, message) {

    class UserNotFoundException(message: String = "존재하지 않는 유저입니다.") : UserException(message, 1, HttpStatus.BAD_REQUEST)

    class UserNotAdminException(message: String = "ADMIN이 아닙니다") : UserException(message, 2, HttpStatus.BAD_REQUEST)

    class DeviceNotFoundException(message: String = "저장되지 않은 기기토큰입니다.") :
        UserException(message, 3, HttpStatus.BAD_REQUEST)

    class KakaoUserNotFoundException(message: String = "카카오에 존재하지 않는 유저입니다.") :
        UserException(message, 4, HttpStatus.BAD_REQUEST)

    class AppleNotFoundTokenException(message: String) : UserException(message, 5, HttpStatus.BAD_REQUEST)

    companion object {
        const val DEFAULT_CODE_PREFIX = "USER"
    }
}