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
    fun generateContent(prompt: String): String? {
        val apiKey = geminiProperties.apiKey
        val restTemplate = RestTemplate()

        restTemplate.messageConverters.add(0, StringHttpMessageConverter(StandardCharsets.UTF_8))

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val requestBody = """
            {
              "contents": [{
                "parts": [{"text": "$prompt"}]
              }]
            }
        """.trimIndent()

        val entity = HttpEntity(requestBody, headers)

        val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

        val objectMapper = ObjectMapper()

        val root = objectMapper.readTree(restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String::class.java).body)
        return root["candidates"]?.firstOrNull()
            ?.get("content")?.get("parts")?.firstOrNull()
            ?.get("text")?.asText()

    }
}