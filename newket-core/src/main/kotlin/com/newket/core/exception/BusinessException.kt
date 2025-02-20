package com.newket.core.exception

import org.springframework.http.HttpStatusCode

abstract class BusinessException(
    codePrefix: String = DEFAULT_CODE_PREFIX,
    errorCode: Int,
    val httpStatusCode: HttpStatusCode,
    override val message: String = DEFAULT_MESSAGE,
) : RuntimeException(message) {

    val code: String = "$codePrefix-${errorCode}"


    companion object {
        const val DEFAULT_CODE_PREFIX = "UNKNOWN"
        const val DEFAULT_MESSAGE = "예상하지 못한 오류가 발생했습니다."
    }

}