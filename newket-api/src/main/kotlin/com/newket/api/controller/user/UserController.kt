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
){
    //유저 정보 가져오기
    @GetMapping(UserApi.V1.BASE_URL)
    fun getUser() : UserInfo.Response {
        return userService.getUserInfo()
    }

    // deviceToken 추가
    @PutMapping(UserApi.V1.DEVICE_TOKEN)
    fun putDeviceToken(@RequestBody request: UserDeviceToken.Request) {
        return userService.putDeviceToken(request)
    }

    // 알림 설정 추가
    @PutMapping(UserApi.V1.NOTIFICATION)
    fun putNotificationAllow(@RequestParam isAllow: String,
                        @RequestParam target: String,
                        @RequestBody request: UserDeviceToken.Request) {
        return userService.updateNotificationAllow(isAllow, target, request)
    }

    // deviceToken 수신 여부
    @GetMapping(UserApi.V1.NOTIFICATION)
    fun getNotificationAllow(@RequestBody request: UserDeviceToken.Request) : NotificationAllow.Response{
        return userService.getNotificationAllow(request)
    }

    // 문의하기 v1
    @PostMapping(UserApi.V1.HELP)
    fun createHelpV1(@RequestBody request: Help.V1.Request){
        return userService.createHelpV1(request)
    }

    @PostMapping(UserApi.V2.HELP)
    fun createHelpV2(@RequestBody request: Help.V2.Request){
        return userService.createHelpV2(request)
    }

    // deviceToken 삭제
    @DeleteMapping(UserApi.V1.DEVICE_TOKEN)
    fun deleteDeviceToken(@RequestBody request: UserDeviceToken.Request){
        return userService.deleteDeviceToken(request)
    }
}