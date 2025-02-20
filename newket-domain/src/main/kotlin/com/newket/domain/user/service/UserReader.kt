package com.newket.domain.user.service

import com.newket.domain.user.exception.UserException
import com.newket.infra.jpa.auth.constant.SocialLoginProvider
import com.newket.infra.jpa.user.entity.User
import com.newket.infra.jpa.user.entity.UserDevice
import com.newket.infra.jpa.user.repository.UserDeviceRepository
import com.newket.infra.jpa.user.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserReader(
    val userRepository: UserRepository,
    private val userDeviceRepository: UserDeviceRepository
) {
    fun findBySocialIdAndProviderOrNull(socialId: String, socialLoginProvider: SocialLoginProvider): User? {
        return userRepository.findBySocialInfoSocialIdAndSocialInfoSocialLoginProvider(socialId, socialLoginProvider)
    }

    fun findById(userId: Long): User {
        return userRepository.findByIdOrNull(userId) ?: throw UserException.UserNotFoundException()
    }

    fun findUserDeviceByTokenOrNull(token: String): UserDevice? {
        return userDeviceRepository.findByTokenOrderByIdDesc(token).firstOrNull()
    }

    fun findUserDeviceByUserId(userId: Long): List<UserDevice> {
        return userDeviceRepository.findByUserId(userId)
    }
}