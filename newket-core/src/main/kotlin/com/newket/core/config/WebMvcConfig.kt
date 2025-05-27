package com.newket.core.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig : WebMvcConfigurer {
    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        configurer.setDefaultTimeout(3 * 60 * 1000)
    }
}
