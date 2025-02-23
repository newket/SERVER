package com.newket.application.artist.dto.common

data class ArtistDto(
    val artistId: Long,
    val name: String,
    val subName: String?,
    val imageUrl: String?
)