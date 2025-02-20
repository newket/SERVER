package com.newket.infra.jpa.user.entity

import com.newket.infra.jpa.auth.constant.SocialLoginProvider
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
data class SocialInfo(
    var socialId: String,

    @Enumerated(EnumType.STRING)
    val socialLoginProvider: SocialLoginProvider
)