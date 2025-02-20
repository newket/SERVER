package com.newket.infra.jpa.auth.repository

import com.newket.core.auth.AuthException
import com.newket.core.auth.RefreshTokenRepository
import com.newket.infra.jpa.auth.entity.RefreshToken
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenRepositoryImpl(
    private val refreshTokenJpaRepository: RefreshTokenJpaRepository
) : RefreshTokenRepository {
    override fun existsToken(token: String): Boolean {
        return refreshTokenJpaRepository.existsByToken(token)
    }

    override fun updateToken(userId: Long, token: String) {
        val refreshToken = refreshTokenJpaRepository.findByUserId(userId)
            ?: throw AuthException.RefreshTokenNotFoundException()
        refreshToken.updateToken(token)
        refreshTokenJpaRepository.save(refreshToken)
    }

    override fun save(userId: Long, token: String) {
        val refreshToken = RefreshToken(userId, token)
        refreshTokenJpaRepository.save(refreshToken)
    }

    override fun saveOrUpdateToken(userId: Long, token: String) {
        refreshTokenJpaRepository.findByUserId(userId)?.let {
            it.updateToken(token)
            refreshTokenJpaRepository.save(it)
            it
        } ?: run {
            val newToken = RefreshToken(userId, token)
            refreshTokenJpaRepository.save(newToken) // 새로 저장
        }
    }
}