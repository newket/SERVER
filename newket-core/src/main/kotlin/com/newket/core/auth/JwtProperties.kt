package com.newket.core.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
class JwtProperties(
    val secret: String,
    val accessTokenExpirationTime: Long,
    val refreshTokenExpirationTime: Long
)