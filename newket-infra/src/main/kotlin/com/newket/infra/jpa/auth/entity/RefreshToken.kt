package com.newket.infra.jpa.auth.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class RefreshToken(
    val userId: Long,
    @Column(
        length = 500,
        nullable = false
    )
    var token: String
) : BaseDateEntity() {
    fun updateToken(token: String) {
        this.token = token
    }
}