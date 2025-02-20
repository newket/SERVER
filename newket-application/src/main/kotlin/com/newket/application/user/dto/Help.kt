package com.newket.application.user.dto

class Help {
    object V1 {
        data class Request(
            val title: String,
            val content: String
        )
    }

    object V2 {
        data class Request(
            val title: String,
            val content: String,
            val email: String?=null
        )
    }
}