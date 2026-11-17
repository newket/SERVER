package com.newket.core.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleException(
        exception: BusinessException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {

        request.setAttribute("request.exception", exception)

        return ResponseEntity(
            ErrorResponse(exception.code, exception.message),
            exception.httpStatusCode
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        exception: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {

        request.setAttribute("request.exception", exception)

        val internalServerException = DefaultException.InternalServerException(
            exception.message ?: DefaultException.InternalServerException.DEFAULT_MESSAGE
        )

        return ResponseEntity(
            ErrorResponse(internalServerException.code, internalServerException.message),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}