package com.newket.application.notification_request.dto

import com.newket.application.artist.dto.common.ArtistDto

data class ArtistNotificationResponse(
    val artists: List<ArtistDto>
)