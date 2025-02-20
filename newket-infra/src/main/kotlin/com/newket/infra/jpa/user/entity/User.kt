package com.newket.infra.jpa.user.entity

import com.newket.infra.jpa.config.BaseDateEntity
import com.newket.infra.jpa.user.constant.UserType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity
class User(
    val socialInfo: SocialInfo,
    val name: String,
    nickname: String,
    val email: String,
    @Enumerated(EnumType.STRING)
    val type: UserType
) : BaseDateEntity() {

    var nickname: String = nickname
        private set

    fun withdraw() {
        this.socialInfo.socialId = "NONE"
    }
}