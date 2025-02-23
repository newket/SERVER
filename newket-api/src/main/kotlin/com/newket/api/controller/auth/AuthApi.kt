package com.newket.api.controller.auth

object AuthApi {

    object V1{
        const val BASE_URL = "/api/v1/auth"
        const val SOCIAL_LOGIN = "$BASE_URL/login/{provider}"
        const val SOCIAL_LOGIN_APPLE = "$BASE_URL/login/APPLE"
        const val WITHDRAW_APPLE = "$BASE_URL/APPLE"
        const val SIGNUP = "$BASE_URL/signup/{provider}"
        const val SIGNUP_APPLE = "$BASE_URL/signup/APPLE"
        const val REISSUE = "$BASE_URL/reissue"
    }
}