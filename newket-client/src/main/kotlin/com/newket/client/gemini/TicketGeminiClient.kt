package com.newket.client.gemini

import com.fasterxml.jackson.databind.ObjectMapper
import com.newket.client.crawling.CreateTicketRequest
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Component
class TicketGeminiClient(private val geminiClient: GeminiClient) {
    fun getArtists(info: String, artistList: String): List<CreateTicketRequest.Artist> {
        try {
            val prompt =
                """윗글에서 출연하는 아티스트를 아래글을  찾아보고 artistId랑 name을 정리해서 알려줘
                출연하는 아티스트 순서대로 작성하면 돼
                공연 정보를 보고 출연하는 아티스트를 찾는거야 정리하는 방법은 json으로 정리해줘
                이건 예시인데 이것처럼 artistId와 name에 알맞게 리스트 형식으로 그대로 json 형태만 출력해봐
                내용중 첫번째 숫자가 artistId이고 두번째가 name이야
                그래서 artistId는 69 name은 세븐틴 이런식으로 json으로 리스트 형태로 출력하는거지
                지어내면 안되고 내용만 보고 판단해야 해
                주어진 리스트중에 못찾은 아티스트가 있으면 null이라고 해야해
                json 외에 아무런 설명도 말도 하지말고 오로지 json 값만 출력해 json 이라는 말도 하지마
                ${info.replace("\"", "\\\"").replace("{", "").replace("}", "").replace("[", "").replace("]", "")}
                $artistList
                """.trimIndent()
            val json = geminiClient.generateContent(prompt)?.replace("`", "")?.replace("json", "")
            val objectMapper = ObjectMapper()
            val node = objectMapper.readTree(json)

            return node.map {
                CreateTicketRequest.Artist(
                    artistId = it["artistId"].asText().toLong(),
                    name = it["name"].asText().toString()
                )
            }
        } catch (exception: Exception) {
            throw GeminiException.ArtistNotFoundException()
        }

    }

    fun getPlace(info: String, placeList: String): String {
        try {
            val prompt =
                """윗글에서 장소 찾아보는 거야 그 장소를 찾아서 아래 장소 리스트 장소 텍스트 그대로 작성하면 돼
                지어내면 안되고 내용만 보고 리스트에서 비슷한 장소를 찾아서 판단해야 해 무조건 리스트안에 있는 텍스트 그대로 써야해
                주어진 리스트중에 없으면 null이라고 해야해
                장소 외에 아무런 설명도 말도 하지말고 오로지 장소 값만 출력해
                ${info.replace("\"", "\\\"").replace("{", "").replace("}", "").replace("[", "").replace("]", "")}
                $placeList
                """.trimIndent()
            val json = geminiClient.generateContent(prompt)?.replace("`", "")?.replace("json", "")

            return json.toString().trim()
        } catch (exception: Exception) {
            throw GeminiException.PlaceNotFoundException()
        }
    }

    fun getTicketEventSchedules(info: String): List<CreateTicketRequest.TicketEventSchedule> {
        try {
            val prompt =
                """${info.replace("\"", "\\\"").replace("{", "").replace("}", "").replace("[", "").replace("]", "")}
                여기서 공연 날짜를 day와 time에 알맞게 
                day는 2024-01-01 처럼 하고 time은 13:00 처럼해서 리스트 형태로 json 형태만 출력해봐
                지어내면 안되고 내용만 보고 판단해야 해
                json 외에 아무런 설명도 말도 하지말고 오로지 json 값만 출력해 json 이라는 말도 하지마
                """.trimIndent()

            val json = geminiClient.generateContent(prompt)?.replace("`", "")?.replace("json", "")
            val objectMapper = ObjectMapper()
            val node = objectMapper.readTree(json)
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            return node.map {
                CreateTicketRequest.TicketEventSchedule(
                    day = LocalDate.parse(it["day"].asText(), dateFormatter),
                    time = LocalTime.parse(it["time"].asText(), timeFormatter)
                )
            }
        } catch (exception: Exception) {
            throw GeminiException.EventScheduleNotFoundException()
        }
    }

    fun getPrices(info: String): List<CreateTicketRequest.Price> {
        try {
            val prompt =
                """${info.replace("\"", "\\\"").replace("{", "").replace("}", "").replace("[", "").replace("]", "")}
                여기서 공연 가격을 type과 price에 알맞게 
                type은 R석 같은거고 price는 143,000원 처럼해서 원이랑 , 꼭 쓰고 리스트 형태로 json 형태만 출력해봐
                지어내면 안되고 내용만 보고 판단해야 해
                내용에 가격이 없으면 아무값도 반환하지마
                json 외에 아무런 설명도 말도 하지말고 오로지 json 값만 출력해 json 이라는 말도 하지마
                """.trimIndent()

            val json = geminiClient.generateContent(prompt)?.replace("`", "")?.replace("json", "")
            val objectMapper = ObjectMapper()
            val node = objectMapper.readTree(json)

            return node.map {
                CreateTicketRequest.Price(
                    type = it["type"].asText(),
                    price = it["price"].asText()
                )
            }
        } catch (exception: Exception) {
            return emptyList()
        }
    }
}