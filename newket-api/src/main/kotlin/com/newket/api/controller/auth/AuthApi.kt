package com.newket.api.controller.auth

object AuthApi {

    object V1{
        const val BASE_URL = "/api/v1/auth"
        const val SOCIAL_LOGIN_KAKAO = "$BASE_URL/login/KAKAO"
        const val SOCIAL_LOGIN_APPLE = "$BASE_URL/login/APPLE"
        const val SOCIAL_LOGIN_NAVER = "$BASE_URL/login/NAVER"
        const val SIGNUP_KAKAO = "$BASE_URL/signup/KAKAO"
        const val SIGNUP_APPLE = "$BASE_URL/signup/APPLE"
        const val SIGNUP_NAVER = "$BASE_URL/signup/NAVER"
        const val REISSUE = "$BASE_URL/reissue"
        const val WITHDRAW_KAKAO = "$BASE_URL/KAKAO"
        const val WITHDRAW_APPLE = "$BASE_URL/APPLE"
    }
}