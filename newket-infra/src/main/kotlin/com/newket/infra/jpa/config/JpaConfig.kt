package com.newket.infra.jpa.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EntityScan(basePackages = ["com.newket.infra.jpa"])
@EnableJpaRepositories(basePackages = ["com.newket.infra.jpa"])
class JpaConfig