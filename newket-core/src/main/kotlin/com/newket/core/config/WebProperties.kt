package com.newket.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cors")
class WebProperties(
    val admin: String,
    val adminPortfolio: String,
)