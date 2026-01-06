package com.newket.infra.jpa.user.repository

import com.newket.infra.jpa.user.entity.UserDevice
import org.springframework.data.jpa.repository.JpaRepository

interface UserDeviceRepository : JpaRepository<UserDevice, Long> {
    fun findByTokenAndUserId(token: String, userId: Long): UserDevice?

    fun findByUserId(userId: Long): List<UserDevice>

    fun deleteByToken(token: String)

    fun deleteAllByUserId(userId: Long)
}