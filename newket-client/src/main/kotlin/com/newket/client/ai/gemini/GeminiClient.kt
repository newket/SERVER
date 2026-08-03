package com.newket.client.ai.gemini

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

    private val url = "https://generativelanguage.googleapis.com/v1beta/interactions"

    fun generateContent(prompt: String): String? {
        val apiKey = geminiProperties.apiKey
        val restTemplate = RestTemplate().apply {
            messageConverters.add(0, StringHttpMessageConverter(StandardCharsets.UTF_8))
        }

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("x-goog-api-key", apiKey)
            set("Api-Revision", "2026-05-20")
        }

        val objectMapper = ObjectMapper()

        val result = callGemini(prompt, "gemini-3.5-flash-lite", restTemplate, headers, objectMapper)
        if (result != null) {
            return result
        }

        return callGemini(prompt, "gemini-3.1-flash-lite", restTemplate, headers, objectMapper)
    }

    private fun callGemini(
        prompt: String,
        modelName: String,
        restTemplate: RestTemplate,
        headers: HttpHeaders,
        objectMapper: ObjectMapper
    ): String? {
        val requestBodyMap = mapOf(
            "model" to modelName,
            "input" to prompt
        )

        return try {
            val requestBodyJson = objectMapper.writeValueAsString(requestBodyMap)
            val entity = HttpEntity(requestBodyJson, headers)

            val response = restTemplate.exchange(url, HttpMethod.POST, entity, String::class.java)
            val root = objectMapper.readTree(response.body)

            root["steps"]?.let { steps ->
                for (step in steps) {
                    if (step["type"]?.asText() == "model_output") {
                        val contentArray = step["content"]
                        if (contentArray != null && contentArray.isArray) {
                            for (content in contentArray) {
                                if (content["type"]?.asText() == "text") {
                                    return content["text"]?.asText()
                                }
                            }
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            println("Gemini API Error [$modelName]: ${e.message}")
            null
        }
    }
}
