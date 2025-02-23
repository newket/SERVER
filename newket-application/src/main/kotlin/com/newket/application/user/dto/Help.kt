package com.newket.application.user.dto

class Help {
    data class Request(
        val title: String,
        val content: String,
        val email: String?=null
    )
}