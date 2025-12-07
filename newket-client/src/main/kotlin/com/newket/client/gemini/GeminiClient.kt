package com.newket.client.gemini

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
class GeminiClient(private val geminiProperties: GeminiProperties) {

    private val flashUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    private val liteUrl =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent"

    fun generateContent(prompt: String): String? {
        val apiKey = geminiProperties.apiKey
        val restTemplate = RestTemplate().apply {
            messageConverters.add(0, StringHttpMessageConverter(StandardCharsets.UTF_8))
        }

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        val requestBody = """
            {
              "contents": [{
                "parts": [{"text": "$prompt"}]
              }]
            }
        """.trimIndent()

        val entity = HttpEntity(requestBody, headers)
        val objectMapper = ObjectMapper()

        try {
            val url = "$flashUrl?key=$apiKey"
            val response = restTemplate.exchange(url, HttpMethod.POST, entity, String::class.java)
            val root = objectMapper.readTree(response.body)

            return root["candidates"]?.firstOrNull()
                ?.get("content")?.get("parts")?.firstOrNull()
                ?.get("text")?.asText()
        } catch (_: Exception) {
        }

        return try {
            val url = "$liteUrl?key=$apiKey"
            val response = restTemplate.exchange(url, HttpMethod.POST, entity, String::class.java)
            val root = objectMapper.readTree(response.body)

            root["candidates"]?.firstOrNull()
                ?.get("content")?.get("parts")?.firstOrNull()
                ?.get("text")?.asText()
        } catch (e: Exception) {
            null
        }
    }
}