package com.newket.api.controller.user

object UserApi {

    object V1 {
        const val BASE_URL = "/api/v1/users"
        const val DEVICE_TOKEN = "$BASE_URL/device-token"
        const val NOTIFICATION = "$BASE_URL/notification"
        const val HELP = "$BASE_URL/help"
    }

    object V2 {
        const val BASE_URL = "/api/v2/users"
        const val HELP = "$BASE_URL/help"
    }
}