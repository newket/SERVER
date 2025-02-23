package com.newket.application.artist.dto

data class ArtistRequest (
    val artistName: String,
    val artistInfo: String? = null,
    val deviceToken: String,
)