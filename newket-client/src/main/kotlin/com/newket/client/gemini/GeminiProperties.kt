package com.newket.client.gemini

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gemini")
class GeminiProperties (
    val apiKey: String
)