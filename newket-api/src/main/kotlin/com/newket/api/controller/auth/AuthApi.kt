package com.newket.api.controller.auth

object AuthApi {

    object V1{
        const val BASE_URL = "/api/v1/auth"
        const val SOCIAL_LOGIN = "$BASE_URL/login/{provider}"
        const val SOCIAL_LOGIN_KAKAO = "$BASE_URL/login/KAKAO"
        const val SIGNUP = "$BASE_URL/signup/{provider}"
        const val SIGNUP_KAKAO = "$BASE_URL/signup/KAKAO"
        const val REISSUE = "$BASE_URL/reissue"
        const val WITHDRAW_KAKAO = "$BASE_URL/KAKAO"
        const val WITHDRAW_APPLE = "$BASE_URL/APPLE"
    }
}