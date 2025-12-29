package com.newket.application.user

import com.newket.application.user.dto.Help
import com.newket.application.user.dto.NotificationAllow
import com.newket.application.user.dto.UserDeviceToken
import com.newket.application.user.dto.UserInfo
import com.newket.client.slack.SlackClient
import com.newket.core.auth.getCurrentUserId
import com.newket.domain.user.UserAppender
import com.newket.domain.user.UserModifier
import com.newket.domain.user.UserReader
import com.newket.domain.user.UserRemover
import com.newket.domain.user.exception.UserException
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
        val token = request.token

        if (userReader.findUserDeviceByTokenAndUserId(token, userId)==null) {
            userAppender.addDeviceToken(
                UserDevice(
                    userId = userId,
                    token = token,
                    artistNotification = true,
                    ticketNotification = true
                )
            )
        }
    }

    @Transactional
    fun postNotificationAllow(request: NotificationAllow.Request) {
        val userId = getCurrentUserId()
        val userDevice =
            userReader.findUserDeviceByTokenAndUserId(request.token, userId) ?: throw UserException.DeviceNotFoundException()
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
        val userId = getCurrentUserId()
        val userDevice =
            userReader.findUserDeviceByTokenAndUserId(token, userId) ?: throw UserException.DeviceNotFoundException()
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