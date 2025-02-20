package com.newket.domain.artist.exception

import com.newket.core.exception.BusinessException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class ArtistException(
    message: String,
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
) : BusinessException(DEFAULT_CODE_PREFIX, errorCode, httpStatusCode, message) {

    class ArtistNotFoundException(message: String = "존재하지 않는 아티스트입니다.") : ArtistException(message, 1, HttpStatus.BAD_REQUEST)

    companion object {
        const val DEFAULT_CODE_PREFIX = "ARTIST"
    }
}