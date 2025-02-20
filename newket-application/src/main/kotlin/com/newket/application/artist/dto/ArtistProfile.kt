package com.newket.application.artist.dto

import com.newket.application.ticket.dto.OnSale
import com.newket.application.ticket.dto.SearchResult

class ArtistProfile {
    data class Response(
        val info: Artist,
        val members: List<Artist>,
        val groups: List<Artist>,
        val openingNotice: SearchResult.OpeningNotice.Response,
        val onSale: OnSale.Response,
        val afterSale: OnSale.Response
    )

    data class Artist(
        val artistId: Long,
        val name: String,
        val subName: String?,
        val imageUrl: String?
    )
}