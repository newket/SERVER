package com.newket.application.auth.dto

class WithdrawApple {
    data class Request(
        val authorizationCode: String
    )
}