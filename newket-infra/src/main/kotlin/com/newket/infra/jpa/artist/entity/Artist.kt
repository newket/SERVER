package com.newket.infra.jpa.artist.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Entity

@Entity
class Artist(
    val name: String,
    val imageUrl: String?,
    val subName: String?,
    val nickname: String?,
) : BaseDateEntity()