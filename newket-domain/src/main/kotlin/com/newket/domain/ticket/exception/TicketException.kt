package com.newket.domain.ticket.exception

import com.newket.core.exception.BusinessException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class TicketException(
    message: String,
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
) : BusinessException(DEFAULT_CODE_PREFIX, errorCode, httpStatusCode, message) {

    class TicketNotFoundException(message: String = "존재하지 않는 공연입니다.") :
        TicketException(message, 1, HttpStatus.BAD_REQUEST)

    companion object {
        const val DEFAULT_CODE_PREFIX = "TICKET"
    }
}