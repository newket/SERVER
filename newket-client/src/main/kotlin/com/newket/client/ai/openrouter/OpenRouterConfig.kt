package com.newket.client.ai.openrouter

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenRouterProperties::class)
class OpenRouterConfig