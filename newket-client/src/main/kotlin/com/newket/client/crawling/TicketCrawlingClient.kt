package com.newket.client.crawling

import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.jpa.ticket.constant.TicketProvider
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@Component
class TicketCrawlingClient {
    fun fetchTicketInfo(url: String): CreateTicketRequest {
        return when {
            "yes24" in url -> fetchYes24TicketInfo(url)
            "melon" in url -> fetchMelonTicketInfo(url)
            "ticketlink" in url -> fetchTicketlinkTicketInfo(url)
            else -> fetchNolTicketInfo(url)
        }
    }

    fun fetchTicketRaw(url: String): String {
        return when {
            "yes24" in url -> fetchYes24TicketRaw(url)
            "melon" in url -> fetchMelonTicketRaw(url)
            "ticketlink" in url -> fetchTicketlinkTicketRaw(url)
            else -> fetchNolTicketRaw(url)
        }
    }


    private fun fetchNolTicketInfo(url: String): CreateTicketRequest {
        val headers =
            mapOf("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
        val response = Jsoup.connect(url).headers(headers).get()

        val title = response.select("h1").firstOrNull()?.text()?.takeIf { it.isNotBlank() } ?: ""

        val ticketSaleSchedules = mutableListOf<CreateTicketRequest.TicketSaleSchedule>()

        val ticketSale = response
            .select("ul")
            .firstOrNull { ul ->
                ul.select("li time").isNotEmpty()
            }
            ?.select("li")
            ?: emptyList()

        for ((index, element) in ticketSale.withIndex()) {
            val timeElement = element.selectFirst("time") ?: continue

            val datetime = timeElement.attr("datetime")

            if (datetime.isBlank()) continue

            val dateTime = OffsetDateTime.parse(datetime)

            ticketSaleSchedules.add(
                CreateTicketRequest.TicketSaleSchedule(
                    day = dateTime.toLocalDate(),
                    time = dateTime.toLocalTime(),
                    type = if (index == ticketSale.lastIndex) {
                        "일반예매"
                    } else {
                        "선예매"
                    }
                )
            )
        }

        val imageUrl = response
            .select("img[alt]")
            .firstOrNull {
                it.attr("alt").isNotBlank()
            }
            ?.attr("src")
            ?: ""

        return CreateTicketRequest(
            genre = Genre.CONCERT,
            artists = emptyList(),
            place = null,
            title = title,
            imageUrl = imageUrl,
            ticketEventSchedule = emptyList(),
            ticketSaleUrls = listOf(
                CreateTicketRequest.TicketSaleUrl(
                    ticketProvider = TicketProvider.INTERPARK,
                    url = url,
                    isDirectUrl = false,
                    ticketSaleSchedules = ticketSaleSchedules
                )
            ),
            lineupImage = null,
            price = emptyList()
        )
    }

    private fun fetchNolTicketRaw(url: String): String {
        val headers =
            mapOf("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
        val response = Jsoup.connect(url).headers(headers).get()
        val title = response.select("h1").firstOrNull()?.text()?.takeIf { it.isNotBlank() } ?: ""
        val place = response.select(".lc_2").getOrNull(2)?.text() ?: ""
        val price = response
            .select("section")
            .firstOrNull {
                it.selectFirst("h2")?.text() == "가격"
            }
            ?.select("dl[aria-label='티켓 가격 정보'] > div")
            ?.joinToString("\n") { element ->
                val name = element.selectFirst("dt")?.text() ?: ""
                val amount = element.selectFirst("data")?.text() ?: ""

                "$name: $amount"
            }
            ?.takeIf { it.isNotBlank() }
            ?: ""
        val eventSchedule = (response.select(".flex_1")[1].text() ?: "") +
                (response.select("h3")
                    .firstOrNull { it.text() == "운영 시간" }
                    ?.parent()
                    ?.select("div.textStyle_bodyMultiline\\.14\\.regular")
                    ?.firstOrNull()
                    ?.text()
                    ?: "")

        val info = response
            .select("section")
            .firstOrNull {
                it.selectFirst("h2")?.text() == "상품정보"
            }
            ?.text()
            ?.takeIf { it.isNotBlank() }
            ?: ""

        val casting = response
            .select("section")
            .firstOrNull {
                it.selectFirst("h2")?.text() == "캐스팅"
            }
            ?.text()
            ?.takeIf { it.isNotBlank() }
            ?: ""
        return "공연명: $title 장소: $place 공연 시간: $eventSchedule 가격: $price $info $casting"
    }

    private fun fetchYes24TicketInfo(url: String): CreateTicketRequest {
        val userAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36"

        val document = Jsoup.connect(url).userAgent(userAgent).get()

        val scripts = document.select("script[type=text/javascript]")
        var title = ""
        val titlePattern = Pattern.compile("""_n_p1\s*=\s*"([^"]+)"""")

        for (script in scripts) {
            val matcher = titlePattern.matcher(script.html())
            if (matcher.find()) {
                title = matcher.group(1)?.replace(" 티켓", "")?.replace(" 오픈", "")
                    ?.replace(" 안내", "")?.replace("티켓", "")
                    ?.replace("오픈", "")?.replace("안내", "")!!
                break
            }
        }

        val dateRegex = Regex("""(\d{4}-\d{2}-\d{2})""")
        val timeRegex = Regex("""(오전|오후)?\s*(\d{1,2}):(\d{2})""")

        val ticketSaleDate = document.select("#ddSaleTitle1").text()
        val ticketSaleDay = dateRegex.find(ticketSaleDate)?.value ?: ""
        val ticketSaleTime = timeRegex.find(ticketSaleDate)?.let {
            val hour = it.groupValues[2].toInt()
            val minute = it.groupValues[3]
            if (it.groupValues[1] == "오후" && hour < 12) "${hour + 12}:$minute" else "$hour:$minute"
        } ?: ""

        val preTicketSaleDate = document.select("#ddSaleTitle2").text()
        val preTicketSaleDay = dateRegex.find(preTicketSaleDate)?.value ?: ""
        val preTicketSaleTime = timeRegex.find(preTicketSaleDate)?.let {
            val hour = it.groupValues[2].toInt()
            val minute = it.groupValues[3]
            if (it.groupValues[1] == "오후" && hour < 12) "${hour + 12}:$minute" else "$hour:$minute"
        } ?: ""

        val imageUrl = document.select("img[border=0]").attr("src")

        val place = document.select("div.brd_table").text()

        return CreateTicketRequest(
            genre = Genre.CONCERT,
            artists = emptyList(),
            place = place,
            title = title,
            imageUrl = imageUrl,
            ticketEventSchedule = emptyList(),
            ticketSaleUrls = listOf(
                CreateTicketRequest.TicketSaleUrl(
                    ticketProvider = TicketProvider.YES24,
                    url = url,
                    isDirectUrl = false,
                    ticketSaleSchedules = mutableListOf(
                        CreateTicketRequest.TicketSaleSchedule(
                            day = LocalDate.parse(ticketSaleDay, DateTimeFormatter.ISO_DATE),
                            time = LocalTime.parse(ticketSaleTime, DateTimeFormatter.ofPattern("HH:mm")),
                            type = "일반예매"
                        )
                    ).apply {
                        if (!preTicketSaleDate.isNullOrEmpty()) {
                            add(
                                CreateTicketRequest.TicketSaleSchedule(
                                    day = LocalDate.parse(preTicketSaleDay, DateTimeFormatter.ISO_DATE),
                                    time = LocalTime.parse(preTicketSaleTime, DateTimeFormatter.ofPattern("HH:mm")),
                                    type = "선예매"
                                )
                            )
                        }
                    }
                )
            ),
            lineupImage = null,
            price = emptyList()
        )
    }

    private fun fetchYes24TicketRaw(url: String): String {
        val headers =
            mapOf("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
        val response = Jsoup.connect(url).headers(headers).get()
        val infoSection = response.select("div.brd_table").first()
        return infoSection!!.text().trim().replace("\n", " ")
    }

    private fun fetchMelonTicketInfo(url: String): CreateTicketRequest {
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
            .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
            .timeout(10000)
            .get()

        val title = doc.selectFirst("p.tit_consert")?.text() ?: ""
        val scheduleElements = doc.select("dl.schedule_info dd")

        var preTicketSaleDay = ""
        var preTicketSaleTime = ""
        var ticketSaleDay = ""
        var ticketSaleTime = ""

        val datePattern = Pattern.compile("""(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일.*?(\d{2}:\d{2})""")

        if (scheduleElements.isNotEmpty()) {
            if (scheduleElements.size > 1) {
                val preOpenText = scheduleElements[0].text()
                val preMatcher = datePattern.matcher(preOpenText)
                if (preMatcher.find()) {
                    preTicketSaleDay = "${preMatcher.group(1)}-${preMatcher.group(2).padStart(2, '0')}-${
                        preMatcher.group(3).padStart(2, '0')
                    }"
                    preTicketSaleTime = preMatcher.group(4)
                }

                val openText = scheduleElements[1].text()
                val matcher = datePattern.matcher(openText)
                if (matcher.find()) {
                    ticketSaleDay =
                        "${matcher.group(1)}-${matcher.group(2).padStart(2, '0')}-${matcher.group(3).padStart(2, '0')}"
                    ticketSaleTime = matcher.group(4)
                }
            } else {
                val openText = scheduleElements[0].text()
                val matcher = datePattern.matcher(openText)
                if (matcher.find()) {
                    ticketSaleDay =
                        "${matcher.group(1)}-${matcher.group(2).padStart(2, '0')}-${matcher.group(3).padStart(2, '0')}"
                    ticketSaleTime = matcher.group(4)
                }
            }
        }

        val imageUrl = doc.selectFirst("img[onerror='noImage(this, 130, 180)']")?.attr("src")
            ?.replace("130x184", "1300x1840") ?: ""

        return CreateTicketRequest(
            genre = Genre.CONCERT,
            artists = emptyList(),
            place = null,
            title = title,
            imageUrl = imageUrl,
            ticketEventSchedule = emptyList(),
            ticketSaleUrls = listOf(
                CreateTicketRequest.TicketSaleUrl(
                    ticketProvider = TicketProvider.MELON,
                    url = url,
                    isDirectUrl = false,
                    ticketSaleSchedules = mutableListOf<CreateTicketRequest.TicketSaleSchedule>().apply {
                        if (ticketSaleDay.isNotEmpty()) {
                            add(
                                CreateTicketRequest.TicketSaleSchedule(
                                    day = LocalDate.parse(ticketSaleDay, DateTimeFormatter.ISO_DATE),
                                    time = LocalTime.parse(ticketSaleTime, DateTimeFormatter.ofPattern("HH:mm")),
                                    type = "일반예매"
                                )
                            )
                        }
                        if (preTicketSaleDay.isNotEmpty()) {
                            add(
                                CreateTicketRequest.TicketSaleSchedule(
                                    day = LocalDate.parse(preTicketSaleDay, DateTimeFormatter.ISO_DATE),
                                    time = LocalTime.parse(preTicketSaleTime, DateTimeFormatter.ofPattern("HH:mm")),
                                    type = "선예매"
                                )
                            )
                        }
                    }
                )
            ),
            lineupImage = null,
            price = emptyList()
        )
    }

    private fun fetchMelonTicketRaw(url: String): String {
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
            .header(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"
            )
            .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
            .header("Connection", "keep-alive")
            .timeout(15000)
            .get()

        val spanElements = doc.select("span")
        val infos = buildString {
            for (element in spanElements) {
                append(element.text())
            }
        }

        return infos
    }

    private fun fetchTicketlinkTicketInfo(url: String): CreateTicketRequest {
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .timeout(10_000)
            .get()

        // 제목
        val title = doc.selectFirst("dd.title")?.text()!!
            .replace("[티켓링크 티켓오픈]", "")
            .replace("티켓오픈 안내", "")
            .replace("<b>", "")
            .replace("</b>", "")
            .replace("[단독판매]", "")
            .trim()

        // 이미지 URL
        val imageUrl = doc.selectFirst("dd.thumb img")?.attr("src")?.let {
            if (it.startsWith("//")) "https:$it" else it
        } ?: ""

        // 오픈일시는 임의로 지정
        val ticketSaleDay = LocalDate.parse("2000-01-01", DateTimeFormatter.ISO_DATE)
        val ticketSaleTime = LocalTime.of(0, 0) // 00:00

        return CreateTicketRequest(
            genre = Genre.CONCERT,
            artists = emptyList(),
            place = null,
            title = title,
            imageUrl = imageUrl,
            ticketEventSchedule = emptyList(),
            ticketSaleUrls = listOf(
                CreateTicketRequest.TicketSaleUrl(
                    ticketProvider = TicketProvider.TICKETLINK,
                    url = url,
                    isDirectUrl = false,
                    ticketSaleSchedules = mutableListOf(
                        CreateTicketRequest.TicketSaleSchedule(
                            day = ticketSaleDay,
                            time = ticketSaleTime,
                            type = "일반예매"
                        )
                    )
                )
            ),
            lineupImage = null,
            price = emptyList()
        )
    }

    private fun fetchTicketlinkTicketRaw(url: String): String {
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .timeout(10_000)
            .get()

        val info = doc.selectFirst("dd.list_cont")?.text()!!
        return info
    }
}