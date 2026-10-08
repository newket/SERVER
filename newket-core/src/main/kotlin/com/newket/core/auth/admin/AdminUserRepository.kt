package com.newket.core.auth.admin

import org.springframework.stereotype.Repository

@Repository
interface AdminUserRepository {
    fun findByUsername(username: String): AdminUserInfo?
    fun updateLastLoginAt(username: String)
}

data class AdminUserInfo(
    val username: String,
    val password: String,
    val name: String,
    val role: String,
    val isEnabled: Boolean,
)