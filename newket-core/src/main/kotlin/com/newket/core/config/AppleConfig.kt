package com.newket.core.config

import com.newket.core.auth.AppleProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AppleProperties::class)
class AppleConfig