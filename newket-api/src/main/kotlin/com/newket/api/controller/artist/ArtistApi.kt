package com.newket.api.controller.artist

object ArtistApi {
    object V1 {
        const val BASE_URL = "/api/v1/artists"
        const val REQUEST = "$BASE_URL/request"
        const val DETAIL = "$BASE_URL/{artistId}"
        const val RANDOM = "$BASE_URL/random"
    }
}