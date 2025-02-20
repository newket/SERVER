package com.newket.application.artist.dto

class FavoriteArtists {
    data class Request(
        val artistIds: List<Long>
    )
    data class Response(
        val artists: List<Artist>
    )
    data class Artist(
        val name: String,
        val nicknames: String?,
        val artistId: Long
    )
}