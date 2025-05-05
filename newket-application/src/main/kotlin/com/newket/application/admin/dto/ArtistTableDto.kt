package com.newket.application.admin.dto

data class ArtistTableDto(
    val artistId: Long,
    val name: String,
    val subName: String?,
    val nickname: String?,
    val imageUrl: String?
)