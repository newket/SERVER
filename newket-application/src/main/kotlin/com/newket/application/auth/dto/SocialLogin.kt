package com.newket.application.auth.dto

class SocialLogin {
    data class Request(
        val accessToken: String,
    )

    data class Response (
        val accessToken: String,
        val refreshToken: String
    )
}