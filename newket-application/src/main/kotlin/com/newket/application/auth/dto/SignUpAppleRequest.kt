package com.newket.application.auth.dto

data class SignUpAppleRequest(
    val name: String,
    val email: String,
    val socialId: String
)