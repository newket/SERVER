package com.newket.client.oauth.kakao

import com.newket.domain.user.exception.UserException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class KakaoOauthClient {
    private val webclient = WebClient.create("https://kapi.kakao.com")

    fun retrieveUserInfo(accessToken: String): Mono<KakaoUserInfo> {
        return webclient.get()
            .uri("/v2/user/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(KakaoUserInfo::class.java)
            .onErrorMap { e -> UserException.KakaoUserNotFoundException(e.message.toString()) }
    }
}