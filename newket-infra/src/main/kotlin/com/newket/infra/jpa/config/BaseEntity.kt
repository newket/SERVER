package com.newket.infra.jpa.config

import com.newket.core.exception.validateProperty
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
open class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
        get() {
            validateProperty((field != 0L))
            return field
        }
}