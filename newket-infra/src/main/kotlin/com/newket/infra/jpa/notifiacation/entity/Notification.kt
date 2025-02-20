package com.newket.infra.jpa.notifiacation.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Entity

@Entity
class Notification(
    val userId: Long,
    val title: String,
    val content: String,
    var isOpened: Boolean
) : BaseDateEntity() {
    fun updateIsOpened() {
        this.isOpened = true
    }
}