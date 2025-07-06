package com.newket.client.s3

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "s3")
class S3Properties(
    val accessKey: String,
    val secretKey: String,
    val bucket: String,
    val region: String,
)