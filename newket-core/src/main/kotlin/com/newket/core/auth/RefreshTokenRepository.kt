package com.newket.core.auth

import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository {
    fun existsToken(token: String): Boolean

    fun updateToken(userId: Long, token: String)

    fun save(userId: Long, token: String)

    fun saveOrUpdateToken(userId: Long, token: String)
}