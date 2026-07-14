package com.newket.client.ai.openrouter

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "openrouter")
class OpenRouterProperties (
    val apiKey: String
)