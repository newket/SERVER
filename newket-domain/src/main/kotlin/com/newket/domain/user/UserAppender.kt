package com.newket.domain.user

import com.newket.infra.jpa.user.entity.User
import com.newket.infra.jpa.user.entity.UserDevice
import com.newket.infra.jpa.user.repository.UserDeviceRepository
import com.newket.infra.jpa.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserAppender(
    private val userRepository: UserRepository,
    private val userDeviceRepository: UserDeviceRepository
) {
    fun addUser(user: User) {
        userRepository.save(user)
    }

    fun addDeviceToken(userDevice: UserDevice) {
        userDeviceRepository.save(userDevice)
    }
}