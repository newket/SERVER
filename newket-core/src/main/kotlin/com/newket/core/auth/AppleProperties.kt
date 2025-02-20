package com.newket.core.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "apple")
class AppleProperties(
    val clientId: String,
    val keyId: String,
    val teamId: String,
    val privateKey: String
)