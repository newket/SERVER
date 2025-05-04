package com.newket.infra.jpa.artist.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Entity

@Entity
class Artist(
    var name: String,
    var imageUrl: String?,
    var subName: String?,
    var nickname: String?,
) : BaseDateEntity()