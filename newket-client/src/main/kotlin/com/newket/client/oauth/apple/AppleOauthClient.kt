package com.newket.client.oauth.apple

import com.newket.core.auth.AppleProperties
import com.newket.core.auth.JwtTokenProvider
import com.newket.domain.user.exception.UserException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class AppleOauthClient(
    private val jwtTokenProvider: JwtTokenProvider,
    private val appleProperties: AppleProperties,
) {
    private val webClient = WebClient.create("https://appleid.apple.com")

    fun retrieveUserInfo(authorizationCode: String): Mono<AppleUserInfo> {
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("code", authorizationCode)
        params.add("client_id", appleProperties.clientId)
        params.add("client_secret", jwtTokenProvider.generateAppleClientSecret())
        params.add("grant_type", "authorization_code")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        return webClient
            .post()
            .uri("/auth/token")
            .headers { it.addAll(headers) }
            .bodyValue(params)
            .retrieve()
            .bodyToMono(AppleUserInfo::class.java)
            .onErrorMap { e -> UserException.AppleNotFoundTokenException(e.message.toString()) }
    }

    fun revoke(accessToken: String): Mono<Void> {
        val params = LinkedMultiValueMap<String, String>()
        params.add("client_id", appleProperties.clientId)
        params.add("client_secret", jwtTokenProvider.generateAppleClientSecret())
        params.add("token", accessToken)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        return webClient
            .post()
            .uri("/auth/revoke")
            .headers { it.addAll(headers) }
            .bodyValue(params)
            .retrieve()
            .bodyToMono(String::class.java)
            .then()
    }
}