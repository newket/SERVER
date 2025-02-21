package com.newket.api.controller.artist

object ArtistApi {
    object V1 {
        const val BASE_URL = "/api/v1/artists"
        const val DETAIL = "$BASE_URL/{artistId}"
        const val FAVORITE = "$BASE_URL/favorite"
        const val FAVORITE_DETAIL = "$FAVORITE/{artistId}"
        const val RANDOM = "$BASE_URL/random"
    }

    object V2 {
        const val BASE_URL = "/api/v2/artists"
        const val REQUEST = "$BASE_URL/request"
    }
}