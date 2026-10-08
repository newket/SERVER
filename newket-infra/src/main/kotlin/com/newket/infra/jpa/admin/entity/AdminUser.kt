package com.newket.infra.jpa.admin.entity

import com.newket.infra.jpa.admin.constant.AdminRole
import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDateTime

@Entity
class AdminUser(
    @Column(unique = true)
    val username: String,
    val password: String,
    val name: String,
    @Enumerated(EnumType.STRING)
    val role: AdminRole,
    val isEnabled: Boolean,
    var lastLoginAt: LocalDateTime? = null
) : BaseDateEntity() {
}