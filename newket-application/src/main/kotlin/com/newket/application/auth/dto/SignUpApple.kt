package com.newket.application.auth.dto

class SignUpApple {
    data class Request(
        val name: String,
        val email: String,
        val socialId: String
    )
}