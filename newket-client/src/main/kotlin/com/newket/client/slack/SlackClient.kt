package com.newket.client.slack

import com.slack.api.Slack
import com.slack.api.methods.SlackApiException
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class SlackClient(
    slackProperties: SlackProperties,
) {
    var slackToken = slackProperties.secret

    fun sendSlackMessage(message: String?, channel: String) {
        var channelAddress = ""

        if (channel == "artist_request") channelAddress = "#아티스트-요청"
        if (channel == "help") channelAddress = "#문의하기"

        try {
            val methodsClient = Slack.getInstance().methods(slackToken)

            val request = ChatPostMessageRequest.builder()
                .channel(channelAddress)
                .text(message)
                .build()
            methodsClient.chatPostMessage(request)
        } catch (e: SlackApiException) {
            print(e.message)
        } catch (e: IOException) {
            print(e.message)
        }
    }
}