package com.newket.client.ai.openrouter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets

@Component
class OpenRouterClient(
    private val openRouterProperties: OpenRouterProperties,
    private val objectMapper: ObjectMapper
) {

    private val url = "https://openrouter.ai/api/v1/chat/completions"

    private val restTemplate = RestTemplate().apply {
        messageConverters.add(0, StringHttpMessageConverter(StandardCharsets.UTF_8))
    }

    fun generateContent(prompt: String): String? {

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(openRouterProperties.apiKey)
        }

        val requestBody = mapOf(
            "model" to "openrouter/free",
            "messages" to listOf(
                mapOf(
                    "role" to "user",
                    "content" to prompt
                )
            )
        )

        val entity = HttpEntity(
            objectMapper.writeValueAsString(requestBody),
            headers
        )

        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String::class.java
            )

            parseResponse(response.body)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseResponse(body: String?): String? {
        if (body == null) return null

        val root = objectMapper.readTree(body)

        return root["choices"]
            ?.firstOrNull()
            ?.get("message")
            ?.get("content")
            ?.asText()
    }
}