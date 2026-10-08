package com.newket.infra.jpa.admin.repository

import com.newket.core.auth.admin.AdminUserInfo
import com.newket.core.auth.admin.AdminUserRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class AdminUserRepositoryImpl(
    private val adminUserJpaRepository: AdminUserJpaRepository
) : AdminUserRepository {
    override fun findByUsername(username: String): AdminUserInfo? {
        return adminUserJpaRepository.findByUsername(username)?.let {
            AdminUserInfo(
                username = it.username,
                password = it.password,
                name = it.name,
                role = it.role.name,
                isEnabled = it.isEnabled
            )
        }
    }

    override fun updateLastLoginAt(username: String) {
        val adminUser = adminUserJpaRepository.findByUsername(username) ?: return
        adminUser.lastLoginAt = LocalDateTime.now()
        adminUserJpaRepository.save(adminUser)
    }
}