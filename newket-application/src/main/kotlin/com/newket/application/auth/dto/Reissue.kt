package com.newket.application.auth.dto

class Reissue {
    data class Request(
        val refreshToken: String
    )

    data class Response (
        val accessToken: String,
        val refreshToken: String
    )
}
