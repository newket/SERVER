package com.newket.infra.jpa.admin.repository

import com.newket.infra.jpa.admin.entity.AdminUser
import org.springframework.data.jpa.repository.JpaRepository

interface AdminUserJpaRepository : JpaRepository<AdminUser, Long> {
    fun findByUsername(username: String): AdminUser?
}