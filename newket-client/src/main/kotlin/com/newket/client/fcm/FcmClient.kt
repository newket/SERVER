package com.newket.client.fcm

import com.google.auth.oauth2.GoogleCredentials
import org.springframework.core.io.ClassPathResource
import org.springframework.http.*
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets

@Component
class FcmClient {
    fun sendMessage(message: String): ResponseEntity<*>? {
        val restTemplate = RestTemplate()

        restTemplate.messageConverters
            .add(0, StringHttpMessageConverter(StandardCharsets.UTF_8))

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Authorization"] = "Bearer $accessToken"

        val entity: HttpEntity<*> = HttpEntity(message, headers)

        //가운데 project id
        val API_URL = "https://fcm.googleapis.com/v1/projects/newket-94290/messages:send"
        return try {
            restTemplate.exchange(API_URL, HttpMethod.POST, entity, String::class.java)
        } catch (e:Exception){
            null
        }
    }

    private val accessToken: String
        get() {
            val firebaseConfigPath = "firebase_key.json"

            val googleCredentials: GoogleCredentials = GoogleCredentials
                .fromStream(ClassPathResource(firebaseConfigPath).inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))

            googleCredentials.refreshIfExpired()
            return googleCredentials.accessToken.tokenValue
        }
}