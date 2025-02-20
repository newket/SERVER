package com.newket.application.user.dto

import com.newket.infra.jpa.auth.constant.SocialLoginProvider


class UserInfo {
    data class Response(
        val provider: SocialLoginProvider,
        val name: String,
        val email: String
    )
}