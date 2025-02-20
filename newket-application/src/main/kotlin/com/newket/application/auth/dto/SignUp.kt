package com.newket.application.auth.dto

class SignUp {
    object V1{
        data class Request(
            val accessToken: String,
            val favoriteArtistIds: List<Long>
        )
    }
    object V2 {
        data class Request(
            val accessToken: String,
        )
    }

    data class Response (
        val accessToken: String,
        val refreshToken: String
    )
}