package com.newket.core.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(BusinessException::class)
    fun handleException(exception: BusinessException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(exception.code, exception.message), exception.httpStatusCode)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<ErrorResponse> {
        val internalServerException = DefaultException.InternalServerException(
            exception.message ?: DefaultException.InternalServerException.DEFAULT_MESSAGE
        )
        return ResponseEntity(
            ErrorResponse(internalServerException.code, internalServerException.message),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}