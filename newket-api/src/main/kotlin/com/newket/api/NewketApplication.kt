package com.newket.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@ConfigurationPropertiesScan
@EnableMongoRepositories(basePackages = ["com.newket.infra"])
@SpringBootApplication(
    scanBasePackages = [
        "com.newket.api",
        "com.newket.application",
        "com.newket.client",
        "com.newket.domain",
        "com.newket.core",
        "com.newket.infra",
        "com.newket.scheduler"
    ]
)
class NewketApplication

fun main(args: Array<String>) {
    runApplication<NewketApplication>(*args)
}