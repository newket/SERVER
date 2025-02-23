package com.newket.application.user

import com.newket.application.user.dto.Help
import com.newket.application.user.dto.NotificationAllow
import com.newket.application.user.dto.UserDeviceToken
import com.newket.application.user.dto.UserInfo
import com.newket.client.slack.SlackClient
import com.newket.core.auth.getCurrentUserId
import com.newket.domain.user.exception.UserException
import com.newket.domain.user.service.UserAppender
import com.newket.domain.user.service.UserModifier
import com.newket.domain.user.service.UserReader
import com.newket.domain.user.service.UserRemover
import com.newket.infra.jpa.user.entity.UserDevice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class UserService(
    private val userReader: UserReader,
    private val userAppender: UserAppender,
    private val userModifier: UserModifier,
    private val slackClient: SlackClient,
    private val userRemover: UserRemover
) {
    fun getUserInfo(): UserInfo.Response {
        val userId = getCurrentUserId()
        val user = userReader.findById(userId)
        return UserInfo.Response(
            provider = user.socialInfo.socialLoginProvider,
            name = user.nickname,
            email = user.email,
        )
    }

    @Transactional
    fun putDeviceToken(request: UserDeviceToken.Request) {
        val userId = getCurrentUserId()
        val deviceToken = userReader.findUserDeviceByTokenOrNull(request.token)
        // 기기가 기록상 존재 하지 않거나 존재하는 데 유저아이디가 다른경우(탈퇴))
        if ((deviceToken == null) || (deviceToken.userId != userId)) {
            userAppender.addDeviceToken(
                UserDevice(
                    userId = userId,
                    token = request.token,
                    artistNotification = true,
                    ticketNotification = true
                )
            )
        }
    }

    @Transactional
    fun postNotificationAllow(request: NotificationAllow.Request) {
        val userDevice =
            userReader.findUserDeviceByTokenOrNull(request.token) ?: throw UserException.DeviceNotFoundException()
        when (request.target) {
            "artist" -> when (request.isAllow) {
                "on" -> userModifier.updateArtistNotification(userDevice, true)
                "off" -> userModifier.updateArtistNotification(userDevice, false)
            }

            "ticket" -> when (request.isAllow) {
                "on" -> userModifier.updateTicketNotification(userDevice, true)
                "off" -> userModifier.updateTicketNotification(userDevice, false)
            }
        }
    }

    fun getNotificationAllow(token: String): NotificationAllow.Response {
        val userDevice =
            userReader.findUserDeviceByTokenOrNull(token) ?: throw UserException.DeviceNotFoundException()
        return NotificationAllow.Response(
            artistNotification = userDevice.artistNotification,
            ticketNotification = userDevice.ticketNotification
        )
    }

    fun createHelp(request: Help.Request) {
        val userId = getCurrentUserId()
        slackClient.sendSlackMessage(
            "userId: $userId\ntitle: ${request.title}\ncontent: ${request.content}\nemail: ${request.email}",
            "help"
        )
    }

    @Transactional
    fun deleteDeviceToken(request: UserDeviceToken.Request) {
        userRemover.deleteUserDevice(request.token)
    }
}