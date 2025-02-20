package com.newket.application.artist.dto

class ArtistRequest {
    data class Request(
        val artistName: String,
        val artistInfo: String? = null,
        val deviceToken: String
    )
}