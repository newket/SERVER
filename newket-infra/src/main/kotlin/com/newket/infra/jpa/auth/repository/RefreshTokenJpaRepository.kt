package com.newket.infra.jpa.auth.repository

import com.newket.infra.jpa.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenJpaRepository : JpaRepository<RefreshToken, String> {
    fun findByUserId(userId: Long): RefreshToken?

    fun existsByToken(token: String): Boolean
}