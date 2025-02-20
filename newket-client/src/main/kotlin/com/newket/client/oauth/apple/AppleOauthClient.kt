package com.newket.client.oauth.apple

import com.newket.core.auth.AppleProperties
import com.newket.core.auth.JwtTokenProvider
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException


@Component
class AppleOauthClient(
    private val jwtTokenProvider: JwtTokenProvider,
    private val appleProperties: AppleProperties
) {

    fun retrieveUserInfo(authorizationCode: String): AppleUserInfo {
        val restTemplate = RestTemplateBuilder().build()
        val authUrl = "https://appleid.apple.com/auth/token"

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("code", authorizationCode)
        params.add("client_id", appleProperties.clientId)
        params.add("client_secret", jwtTokenProvider.generateAppleClientSecret())
        params.add("grant_type", "authorization_code")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        val httpEntity: HttpEntity<MultiValueMap<String, String>> = HttpEntity(params, headers)

        try {
            val response = restTemplate.postForEntity(
                authUrl, httpEntity,
                AppleUserInfo::class.java
            )
            return response.body!!
        } catch (e: HttpClientErrorException) {
            throw AppleException.AppleNotFoundTokenException(e.message.toString())
        }
    }

    fun revoke(accessToken: String) {
        val restTemplate = RestTemplateBuilder().build()
        val revokeUrl = "https://appleid.apple.com/auth/revoke"

        val params = LinkedMultiValueMap<String, String>()
        params.add("client_id", appleProperties.clientId)
        params.add("client_secret", jwtTokenProvider.generateAppleClientSecret())
        params.add("token", accessToken)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        val httpEntity = HttpEntity<MultiValueMap<String, String>>(params, headers)

        restTemplate.postForEntity(revokeUrl, httpEntity, String::class.java)
    }
}