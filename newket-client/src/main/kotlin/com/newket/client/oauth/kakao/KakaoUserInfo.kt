package com.newket.client.oauth.kakao

data class KakaoUserInfo(
    val id: String,
    val properties: Propreties,
    val kakao_account: KakaoAccount
){
    fun getName(): String {
        return properties.nickname
    }

    fun getEmail(): String {
        return kakao_account.email
    }

    data class Propreties(
        val nickname: String
    )

    data class KakaoAccount(
        val email: String
    )
}

