package com.newket.api.controller.artist

import com.newket.application.artist.ArtistService
import com.newket.application.artist.dto.Artist
import com.newket.application.artist.dto.ArtistProfile
import com.newket.application.artist.dto.ArtistRequest
import com.newket.application.artist.dto.FavoriteArtists
import org.springframework.web.bind.annotation.*

@RestController
class ArtistController(
    private val artistService: ArtistService
) {
    // 아티스트 등록 요청 V2
    @PostMapping(ArtistApi.V2.REQUEST)
    fun requestArtistV2(@RequestBody request: ArtistRequest.Request) {
        return artistService.requestArtistV2(request)
    }

    //관심 아티스트 불러오기
    @GetMapping(ArtistApi.V1.FAVORITE)
    fun getFavoriteArtists(): FavoriteArtists.Response {
        return artistService.getFavoriteArtists()
    }

    //관심 아티스트 여부
    @GetMapping(ArtistApi.V1.FAVORITE_DETAIL)
    fun getIsFavoriteArtist(@PathVariable artistId: Long): Boolean {
        return artistService.getFavoriteArtist(artistId)
    }

    //관심 아티스트 추가
    @PutMapping(ArtistApi.V1.FAVORITE_DETAIL)
    fun addFavoriteArtist(@PathVariable artistId: Long) {
        return artistService.addFavoriteArtist(artistId)
    }

    //관심 아티스트 삭제
    @DeleteMapping(ArtistApi.V1.FAVORITE_DETAIL)
    fun deleteFavoriteArtist(@PathVariable artistId: Long) {
        return artistService.deleteFavoriteArtist(artistId)
    }

    // 아티스트 상세정보
    @GetMapping(ArtistApi.V1.DETAIL)
    fun getArtistProfile(@PathVariable artistId: Long): ArtistProfile.Response {
        return artistService.getArtistProfile(artistId)
    }

    // 아티스트 랜덤 추천
    @GetMapping(ArtistApi.V1.RANDOM)
    fun getRandomArtists(): List<Artist> {
        return artistService.getRandomArtists()
    }
}