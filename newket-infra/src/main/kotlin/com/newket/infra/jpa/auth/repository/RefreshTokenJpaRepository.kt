package com.newket.infra.jpa.auth.repository

import com.newket.infra.jpa.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RefreshTokenJpaRepository : JpaRepository<RefreshToken, String> {
    fun findByUserId(userId: Long): RefreshToken?

    fun existsByToken(token: String): Boolean

    @Modifying
    @Query(
        """
        UPDATE RefreshToken r
        SET r.token = :newToken
        WHERE r.userId = :userId
          AND r.token = :oldToken
    """
    )
    fun rotateToken(
        @Param("userId") userId: Long,
        @Param("oldToken") oldToken: String,
        @Param("newToken") newToken: String
    ): Int
}