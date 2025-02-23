package com.newket.api.controller.user

import com.newket.application.user.UserService
import com.newket.application.user.dto.Help
import com.newket.application.user.dto.NotificationAllow
import com.newket.application.user.dto.UserDeviceToken
import com.newket.application.user.dto.UserInfo
import org.springframework.web.bind.annotation.*

@RestController
class UserController(
    private val userService: UserService
) {
    //유저 정보 가져오기
    @GetMapping(UserApi.V1.BASE_URL)
    fun getUser(): UserInfo.Response {
        return userService.getUserInfo()
    }

    // deviceToken 추가
    @PutMapping(UserApi.V1.DEVICE_TOKEN)
    fun putDeviceToken(@RequestBody request: UserDeviceToken.Request) {
        return userService.putDeviceToken(request)
    }

    // 알림 설정 추가
    @PostMapping(UserApi.V1.NOTIFICATION)
    fun postNotificationAllow(@RequestBody request: NotificationAllow.Request) {
        return userService.postNotificationAllow(request)
    }

    // deviceToken 수신 여부
    @PutMapping(UserApi.V1.NOTIFICATION)
    fun getNotificationAllow(@RequestParam token: String): NotificationAllow.Response {
        return userService.getNotificationAllow(token)
    }

    @PostMapping(UserApi.V1.HELP)
    fun createHelp(@RequestBody request: Help.Request) {
        return userService.createHelp(request)
    }

    // deviceToken 삭제
    @DeleteMapping(UserApi.V1.DEVICE_TOKEN)
    fun deleteDeviceToken(@RequestBody request: UserDeviceToken.Request) {
        return userService.deleteDeviceToken(request)
    }
}