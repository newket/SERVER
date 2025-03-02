package com.newket.client.gemini

import com.newket.core.exception.BusinessException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class GeminiException(
    message: String,
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
) : BusinessException(DEFAULT_CODE_PREFIX, errorCode, httpStatusCode, message) {

    class ArtistNotFoundException(message: String = "아티스트가 존재하지 않습니다.") :
        GeminiException(message, 1, HttpStatus.BAD_REQUEST)

    class PlaceNotFoundException(message: String = "장소가 존재하지 않습니다.") :
        GeminiException(message, 2, HttpStatus.BAD_REQUEST)

    class EventScheduleNotFoundException(message: String = "공연 날짜 또는 시간이 존재하지 않습니다.") :
        GeminiException(message, 3, HttpStatus.BAD_REQUEST)

    companion object {
        const val DEFAULT_CODE_PREFIX = "GEMINI"
    }
}