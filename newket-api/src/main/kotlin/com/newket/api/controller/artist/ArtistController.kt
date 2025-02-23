package com.newket.api.controller.artist

import com.newket.application.artist.ArtistService
import com.newket.application.artist.dto.common.ArtistDto
import com.newket.application.artist.dto.ArtistProfileResponse
import com.newket.application.artist.dto.ArtistRequest
import org.springframework.web.bind.annotation.*

@RestController
class ArtistController(
    private val artistService: ArtistService
) {
    // 아티스트 등록 요청
    @PostMapping(ArtistApi.V1.REQUEST)
    fun requestArtist(@RequestBody request: ArtistRequest) {
        return artistService.requestArtist(request)
    }

    // 아티스트 프로필
    @GetMapping(ArtistApi.V1.DETAIL)
    fun getArtistProfile(@PathVariable artistId: Long): ArtistProfileResponse {
        return artistService.getArtistProfile(artistId)
    }

    // 아티스트 랜덤 추천
    @GetMapping(ArtistApi.V1.RANDOM)
    fun getRandomArtists(): List<ArtistDto> {
        return artistService.getRandomArtists()
    }
}