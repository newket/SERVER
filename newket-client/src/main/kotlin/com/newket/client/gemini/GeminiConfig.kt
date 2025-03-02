package com.newket.client.gemini

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(GeminiProperties::class)
class GeminiConfig
