package com.newket.domain.user

import com.newket.infra.jpa.user.repository.UserDeviceRepository
import org.springframework.stereotype.Service

@Service
class UserRemover(
    private val userDeviceRepository: UserDeviceRepository
) {
    fun deleteUserDevice(deviceToken: String) = userDeviceRepository.deleteByToken(deviceToken)

    fun deleteAllUserDevice(userId: Long) = userDeviceRepository.deleteAllByUserId(userId)
}