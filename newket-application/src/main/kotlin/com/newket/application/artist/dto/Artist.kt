package com.newket.application.artist.dto

data class Artist(
    val artistId: Long,
    val name: String,
    val subName: String?,
    val imageUrl: String?
)