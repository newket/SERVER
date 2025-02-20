package com.newket.infra.jpa.user.repository

import com.newket.infra.jpa.auth.constant.SocialLoginProvider
import com.newket.infra.jpa.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findBySocialInfoSocialIdAndSocialInfoSocialLoginProvider(socialId: String, socialLoginProvider: SocialLoginProvider): User?
}