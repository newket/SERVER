package com.newket.client.slack

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "slack")
class SlackProperties(
    val secret: String
)