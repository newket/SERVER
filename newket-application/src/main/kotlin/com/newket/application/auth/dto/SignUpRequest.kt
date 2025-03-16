package com.newket.application.auth.dto

data class SignUpRequest(
    val name: String,
    val email: String,
    val socialId: String
)