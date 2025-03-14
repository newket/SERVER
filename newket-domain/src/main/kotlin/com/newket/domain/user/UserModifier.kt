package com.newket.domain.user

import com.newket.infra.jpa.user.entity.User
import com.newket.infra.jpa.user.entity.UserDevice
import com.newket.infra.jpa.user.repository.UserDeviceRepository
import com.newket.infra.jpa.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserModifier(
    private val userDeviceRepository: UserDeviceRepository,
    private val userRepository: UserRepository
) {
    fun updateArtistNotification(userDevice: UserDevice, isAllow: Boolean) {
        userDevice.updateArtistNotification(isAllow)
        userDeviceRepository.save(userDevice)
    }

    fun updateTicketNotification(userDevice: UserDevice, isAllow: Boolean) {
        userDevice.updateTicketNotification(isAllow)
        userDeviceRepository.save(userDevice)
    }

    fun updateSocialIdWithdraw(user: User) {
        user.withdraw()
        userRepository.save(user)
    }
}