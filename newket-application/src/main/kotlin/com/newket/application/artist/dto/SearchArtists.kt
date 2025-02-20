package com.newket.application.artist.dto

class SearchArtists {
    data class Response(
        val artists: List<Artist>
    )
    data class Artist(
        val name: String,
        val nicknames: String?,
        val artistId: Long
    )
}