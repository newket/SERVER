package com.newket.client.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.newket.client.ai.openrouter.OpenRouterClient
import com.newket.client.crawling.CreateMusicalRequest
import com.newket.client.crawling.CreateTicketRequest
import com.newket.domain.artist.ArtistReader
import com.newket.infra.jpa.ticket.constant.Genre
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Component
class TicketAiClient(
    private val aiClient: OpenRouterClient,
    private val artistReader: ArtistReader
) {
    fun extractInfo(info: String, artistList: String, placeList: String): CreateTicketRequest {
        try {
            val prompt =
                """
당신은 공연 티켓 공지에서 정보를 추출하는 정보 추출기입니다.

아래 공연 공지와 아티스트 목록, 장소 목록을 참고하여 공연 정보를 추출하세요.

## 규칙

1. 반드시 제공된 내용만을 근거로 판단하세요.
2. 내용을 추측하거나 지어내지 마세요.
3. 결과는 JSON 하나만 출력하세요.
4. ```json, 설명, 주석, 마크다운 등을 절대 출력하지 마세요.
5. 값이 없는 경우에는 빈 배열([]) 또는 null을 사용하세요.

------------------------------------

## artists

공연에 출연하는 아티스트를 찾아주세요.

아래 아티스트 목록에서 공연에 출연하는 아티스트를 찾아
artistId와 name을 그대로 사용하세요.

- 반드시 제공된 artistList 안에서만 선택하세요.
- 순서는 공연 공지에 등장하는 순서를 따르세요.
- 중복은 제거하세요.

출력 형식

[
  {
    "artistId": 1,
    "name": "홍길동"
  }
]

------------------------------------

## place

공연 장소를 찾아주세요.

- 반드시 placeList 안에서 가장 알맞은 장소를 선택하여 그대로 작성하세요.

------------------------------------

## ticketEventSchedule

해당 링크의 공연 정보를 기반으로 공연 일시를 day와 time의 리스트 형태로 json 형태로 출력해줘.

[판단 원칙]
텍스트에 명시된 내용만 근거로 판단한다. 추론하거나 지어내지 않는다.

[대상 날짜 범위 결정]
- 공연 제목에 "n차" 표기가 있으면, 본문에서 동일한 n차 티켓오픈에 딸린 "오픈 회차" 날짜 범위만 대상으로 한다. 다른 차수의 오픈 회차는 무시한다.
- "오픈 회차" 날짜 범위가 별도로 명시되어 있으면 (예: 오픈 회차: 2026.9.5~2026.9.20), 그 범위만 대상으로 한다.
- "오픈 회차"가 없으면 "공연 기간" 전체를 대상으로 한다.

[요일별 시간 매핑]
- 텍스트의 요일별 시간표(예: 화,목 5시 / 수,토,공휴일 2시,7시 / 금 3시,8시 / 일 3시 (월 공연없음))를 해석한다.
- 대상 날짜 범위 안의 모든 날짜에 대해, 해당 요일에 지정된 시간대를 각각 하나의 항목으로 만든다.
- 하루에 시간대가 2개 이상이면 각각 별도 객체로 분리한다.
- "공연없음"으로 명시된 요일은 제외한다.
- 텍스트에 별도로 공휴일이라 명시된 날짜가 없다면 "공휴일" 시간대는 적용하지 않는다.
- 시간은 24시간 "HH:MM" 형식으로 변환한다 (예: 오후 7시 → "19:00").

예시

[
  {
    "day": "2026-08-20",
    "time": "19:30"
  }
]

------------------------------------

## price

공연 가격을 추출하세요.

규칙

- type은 R석, VIP석 등 그대로 작성
- price는 "143,000원" 형식 그대로 작성
- 가격 정보가 없으면 빈 배열 반환

예시

[
  {
    "type": "R석",
    "price": "143,000원"
  }
]

------------------------------------

최종 출력은 아래 JSON 형식 하나만 출력하세요.

{
  "artists": [
    {
      "artistId": 0,
      "name": ""
    }
  ],
  "place": "",
  "ticketEventSchedule": [
    {
      "day": "2026-01-01",
      "time": "19:00"
    }
  ],
  "price": [
    {
      "type": "",
      "price": ""
    }
  ]
}

=========================
공연 공지
=========================

$info

=========================
artistList
=========================

$artistList

=========================
placeList
=========================

$placeList

반드시 JSON 하나만 출력하세요.
                """.trimIndent()

            val json =
                aiClient.generateContent(prompt, "openrouter/free")?.replace("`", "")?.replace("json", "")
            val objectMapper = ObjectMapper()
            val node = objectMapper.readTree(json)

            val artists = node["artists"]?.map {
                val artist = artistReader.findById(it["artistId"].asLong())

                CreateTicketRequest.Artist(
                    artistId = artist.id,
                    name = "**${artist.name}** ${artist.subName ?: ""} ${artist.nickname ?: ""}"
                )
            } ?: emptyList()

            val prices = node["price"]?.map {
                CreateTicketRequest.Price(
                    type = it["type"].asText(),
                    price = it["price"].asText()
                )
            } ?: emptyList()

            val schedules = node["ticketEventSchedule"]?.map {
                CreateTicketRequest.TicketEventSchedule(
                    day = LocalDate.parse(it["day"].asText()),
                    time = LocalTime.parse(it["time"].asText())
                )
            } ?: emptyList()

            return CreateTicketRequest(
                genre = Genre.CONCERT,
                artists = artists,
                place = node["place"]?.asText(),
                title = "",
                imageUrl = "",
                ticketEventSchedule = schedules,
                ticketSaleUrls = emptyList(),
                lineupImage = null,
                price = prices
            )
        } catch (exception: Exception) {
            return CreateTicketRequest(
                genre = Genre.CONCERT,
                artists = emptyList(),
                place = "",
                title = "",
                imageUrl = "",
                ticketEventSchedule = emptyList(),
                ticketSaleUrls = emptyList(),
                lineupImage = null,
                price = emptyList()
            )
        }
    }

    fun extractMusicalInfo(info: String, artistList: String, placeList: String): CreateMusicalRequest {
        try {
            val prompt =
                """
당신은 공연 티켓 공지에서 정보를 추출하는 정보 추출기입니다.

아래 공연 공지와 아티스트 목록, 장소 목록을 참고하여 공연 정보를 추출하세요.

## 규칙

1. 반드시 제공된 내용만을 근거로 판단하세요.
2. 내용을 추측하거나 지어내지 마세요.
3. 결과는 JSON 하나만 출력하세요.
4. ```json, 설명, 주석, 마크다운 등을 절대 출력하지 마세요.
5. 값이 없는 경우에는 빈 배열([]) 또는 null을 사용하세요.

------------------------------------

## artists

공연에 출연하는 아티스트와 역할를 찾아주세요.

아래 아티스트 목록에서 공연에 출연하는 아티스트를 찾아
artistId와 name을 그대로 사용하세요.

- 반드시 제공된 artistList 안에서만 선택하세요.
- 순서는 공연 공지에 등장하는 순서를 따르세요.
- 중복은 제거하세요.

출력 형식

[
  {
    "artistId": 1,
    "name": "홍길동",
    "role": "엘리자벳"
  }
]

------------------------------------

## place

공연 장소를 찾아주세요.

- 반드시 placeList 안에서 가장 알맞은 장소를 선택하여 그대로 작성하세요.

------------------------------------

## ticketEventSchedule

공연 날짜를 추출하세요.

규칙

- day는 yyyy-MM-dd 형식
- time은 HH:mm 형식
- 해당하는 공연 일정을 전부 추출하세요.
- 티켓 오픈 일정은 절대 포함하지 마세요.
- 공연기간과 요일별 공연 시간이 함께 제공된 경우, 반드시 '해당 대상 공연 기간 전체'를 기준으로 각 날짜의 요일을 계산하여 해당하는 모든 공연 회차를 생성하세요.
- 단, 공지 내용이 n차(또는 마지막) 티켓오픈 공지인 경우에는 '해당 티켓오픈 대상 공연 일정'만 추출하세요.

예시

[
  {
    "day": "2026-08-20",
    "time": "19:30"
  }
]

------------------------------------

## price

공연 가격을 추출하세요.

규칙

- type은 R석, VIP석 등 그대로 작성
- price는 "143,000원" 형식 그대로 작성
- 가격 정보가 없으면 빈 배열 반환

예시

[
  {
    "type": "R석",
    "price": "143,000원"
  }
]

------------------------------------

최종 출력은 아래 JSON 형식 하나만 출력하세요.

{
  "artists": [
    {
      "artistId": 0,
      "name": "",
      "role": ""
    }
  ],
  "place": "",
  "ticketEventSchedule": [
    {
      "day": "2026-01-01",
      "time": "19:00"
    }
  ],
  "price": [
    {
      "type": "",
      "price": ""
    }
  ]
}

=========================
공연 공지
=========================

${info}

=========================
artistList
=========================

${artistList}

=========================
placeList
=========================

${placeList}

반드시 JSON 하나만 출력하세요.
                ${info.replace("\"", "\\\"").replace("{", "").replace("}", "").replace("[", "").replace("]", "")}
                """.trimIndent()

            val json =
                aiClient.generateContent(prompt, "openrouter/free")?.replace("`", "")?.replace("json", "")
            val objectMapper = ObjectMapper()
            val node = objectMapper.readTree(json)

            val artists = node["artists"]?.map {
                val artist = artistReader.findById(it["artistId"].asLong())

                CreateMusicalRequest.Artist(
                    artistId = artist.id,
                    name = "**${artist.name}** ${artist.subName ?: ""} ${artist.nickname ?: ""}",
                    role= it["role"].asText()
                )
            } ?: emptyList()

            val prices = node["price"]?.map {
                CreateTicketRequest.Price(
                    type = it["type"].asText(),
                    price = it["price"].asText()
                )
            } ?: emptyList()

            val schedules = node["ticketEventSchedule"]?.map {
                CreateTicketRequest.TicketEventSchedule(
                    day = LocalDate.parse(it["day"].asText()),
                    time = LocalTime.parse(it["time"].asText())
                )
            } ?: emptyList()

            return CreateMusicalRequest(
                genre = Genre.CONCERT,
                artists = artists,
                place = node["place"]?.asText(),
                title = "",
                imageUrl = "",
                ticketEventSchedule = schedules,
                ticketSaleUrls = emptyList(),
                lineupImage = null,
                price = prices
            )
        } catch (exception: Exception) {
            return CreateMusicalRequest(
                genre = Genre.CONCERT,
                artists = emptyList(),
                place = "",
                title = "",
                imageUrl = "",
                ticketEventSchedule = emptyList(),
                ticketSaleUrls = emptyList(),
                lineupImage = null,
                price = emptyList()
            )
        }
    }

    fun getArtists(info: String, artistList: String): List<CreateTicketRequest.Artist> {
        try {
            val prompt =
                """공연 정보를 보고 출연하는 아티스트를 아래글을 찾아보고 artistId랑 name을 정리해서 출연하는 아티스트 순서대로 알려줘
                artistId와 name에 알맞게 리스트 형식으로 그대로 json 형태만 출력해봐
                내용중 첫번째 숫자가 artistId이고 두번째가 name이야
                지어내면 안되고 내용만 보고 판단해야 해
                json 외에 아무런 설명도 말도 하지말고 오로지 json 값만 출력해
                ${info.replace("\"", "\\\"").replace("{", "").replace("}", "").replace("[", "").replace("]", "")}
                $artistList
                """.trimIndent()
            val json = aiClient.generateContent(prompt, "openrouter/free")?.replace("`", "")?.replace("json", "")
            val objectMapper = ObjectMapper()
            val node = objectMapper.readTree(json)

            return node.map {
                val artist = artistReader.findById(it["artistId"].asText().toLong())
                CreateTicketRequest.Artist(
                    artistId = artist.id,
                    name = "**${artist.name}** ${artist.subName ?: ""} ${artist.nickname ?: ""}",
                )
            }.distinctBy { it.artistId }
        } catch (exception: Exception) {
            return emptyList()
        }
    }

    fun getMusicalArtists(info: String, artistList: String): List<CreateMusicalRequest.Artist> {
        try {
            val prompt =
                """공연 정보를 보고 출연하는 아티스트를 아래글을 찾아보고 artistId, name, role을 정리해서 출연하는 아티스트 순서대로 알려줘
                artistId, name, role에 알맞게 리스트 형식으로 그대로 json 형태만 출력해봐
                추가로 주어진 리스트 내용 중 첫번째 숫자가 artistId이고 두번째가 name이야
                1. 절대로 지어내면 안되고 내용만 보고 판단해야 해. 
                2. 출연하지 않는 아티스트는 출력하지 마.
                3. 출연하지만 리스트에 없는 아티스트는 출력하지 마.
                4. json 외에 아무런 설명도 말도 하지말고 오로지 json 값만 출력해
                ${info.replace("\"", "\\\"").replace("{", "").replace("}", "").replace("[", "").replace("]", "")}
                $artistList
                """.trimIndent()
            val json = aiClient.generateContent(prompt, "openrouter/free")?.replace("`", "")?.replace("json", "")
            val objectMapper = ObjectMapper()
            val node = objectMapper.readTree(json)

            return node.map {
                val artist = artistReader.findById(it["artistId"].asText().toLong())
                CreateMusicalRequest.Artist(
                    artistId = artist.id,
                    name = "**${artist.name}** ${artist.subName ?: ""} ${artist.nickname ?: ""}",
                    role = it["role"].asText()
                )
            }.distinctBy { it.artistId }
        } catch (exception: Exception) {
            return emptyList()
        }
    }

    fun getPlace(info: String, placeList: String): String {
        try {
            val prompt =
                """아래 글에서 장소 찾아보는 거야 그 장소를 찾아서 아래 장소 리스트 장소 텍스트 그대로 작성하면 돼
                지어내면 안되고 내용만 보고 장소 리스트 안에서 비슷한 장소를 찾아서 판단해야 해 무조건 리스트안에 있는 텍스트 그대로 써야해
                주어진 리스트중에 없으면 장소 그대로 출력해
                장소 외에 아무런 설명도 말도 하지말고 오로지 장소 값만 출력해
                ${info.replace("\"", "\\\"").replace("{", "").replace("}", "").replace("[", "").replace("]", "")}
                $placeList
                """.trimIndent()
            val json =
                aiClient.generateContent(prompt, "openrouter/free")?.replace("`", "")?.replace("json", "")
            return json.toString().trim()
        } catch (exception: Exception) {
            return ""
        }
    }

    fun getTicketEventSchedules(info: String): List<CreateTicketRequest.TicketEventSchedule> {
        try {
            val prompt =
                """
                여기서 공연 날짜를 day와 time에 알맞게 
                day는 2024-01-01 처럼 하고 time은 13:00 처럼해서 리스트 형태로 json 형태만 출력해봐
                지어내면 안되고 내용만 보고 판단해야 해
                n차 티켓오픈에 대한 내용이 있으면 오로지 n차 티켓오픈의 공연 날짜에 해당하는 날만 정리해서 출력해
                json 외에 아무런 설명도 말도 하지말고 오로지 json 값만 출력해
                ${info.replace("\"", "\\\"").replace("{", "").replace("}", "").replace("[", "").replace("]", "")}
                """.trimIndent()

            val json = aiClient.generateContent(prompt, "openrouter/free")?.replace("`", "")?.replace("json", "")
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
            return emptyList()
        }
    }

    fun getPrices(info: String): List<CreateTicketRequest.Price> {
        try {
            val prompt =
                """
                여기서 공연 가격을 type과 price에 알맞게 
                type은 R석 같은거고 price는 143,000원 처럼해서 원이랑 , 꼭 쓰고 리스트 형태로 json 형태만 출력해봐
                지어내면 안되고 내용만 보고 판단해야 해
                내용에 가격이 없으면 아무값도 반환하지마
                json 외에 아무런 설명도 말도 하지말고 오로지 json 값만 출력해 json 이라는 말도 하지마
                ${info.replace("\"", "\\\"").replace("{", "").replace("}", "").replace("[", "").replace("]", "")}
                """.trimIndent()

            val json =
                aiClient.generateContent(prompt, "openrouter/free")?.replace("`", "")?.replace("json", "")
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