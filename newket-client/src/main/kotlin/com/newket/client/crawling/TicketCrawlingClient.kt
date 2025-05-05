package com.newket.client.crawling

import com.microsoft.playwright.Playwright
import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.jpa.ticket.constant.TicketProvider
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@Component
class TicketCrawlingClient {
    fun fetchTicketInfo(url: String): CreateTicketRequest {
        return when {
            "interpark" in url -> fetchInterparkTicketInfo(url)
            "yes24" in url -> fetchYes24TicketInfo(url)
            "melon" in url -> fetchMelonTicketInfo(url)
            "ticketlink" in url -> fetchTicketlinkTicketInfo(url)
            else -> fetchInterparkTicketInfo(url)
        }
    }

    fun fetchTicketRaw(url: String): String {
        return when {
            "interpark" in url -> fetchInterparkTicketRaw(url)
            "yes24" in url -> fetchYes24TicketRaw(url)
            "melon" in url -> fetchMelonTicketRaw(url)
            "ticketlink" in url -> fetchTicketlinkTicketRaw(url)
            else -> fetchInterparkTicketRaw(url)
        }
    }


    private fun fetchInterparkTicketInfo(url: String): CreateTicketRequest {
        val headers =
            mapOf("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
        val response = Jsoup.connect(url).headers(headers).get()

        val title =
            response.selectFirst("li.DetailSummary_title__jqNL3.DetailSummary_solo__cGKlp")!!.text().replace("상대우위", "")
                .replace("절대우위", "").replace("좌석우위", "").trim()

        val ticketSale = response.select("span.DetailBooking_scheduleDate__4WvwQ")
        val ticketSaleSchedules = mutableListOf<CreateTicketRequest.TicketSaleSchedule>()

        for ((index, element) in ticketSale.withIndex()) {
            val text = element.text()
            val parts = text.split(" ")

            val dayFormatted = LocalDate.now().year.toString() + "-" +
                    parts[0].substring(0, 2) + "-" + parts[0].substring(3, 5)
            val timeFormatted = parts[1]

            ticketSaleSchedules.add(
                CreateTicketRequest.TicketSaleSchedule(
                    day = LocalDate.parse(dayFormatted, DateTimeFormatter.ISO_DATE),
                    time = LocalTime.parse(timeFormatted, DateTimeFormatter.ofPattern("HH:mm")),
                    type = if (index == ticketSale.lastIndex) "일반예매" else "선예매"
                )
            )
        }

        val imageUrl = response.select("img[alt=summaryBanner]").attr("src").let {
            if (it.startsWith("http")) it else "https:$it"
        }

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

    private fun fetchInterparkTicketRaw(url: String): String {
        val headers =
            mapOf("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
        val response = Jsoup.connect(url).headers(headers).get()
        val title = response.selectFirst("li.DetailSummary_title__jqNL3.DetailSummary_solo__cGKlp")?.text();
        val summary = response.select("dl.DetailSummary_infoData__aCnzJ.DataList_dataList__zZBw_").text()
        val introduceSection = response.select("div.DetailInfo_contents__grsx5").text()
        val introElement = response.selectFirst(".info1 h4 + .data")
        val artistElement = response.selectFirst(".info2 h4 + .data p")
        val artist = artistElement?.text()?.trim() ?: ""
        return title + summary + introduceSection + introElement + artist
    }

    private fun fetchYes24TicketInfo(url: String): CreateTicketRequest {
        val userAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36"

        val document = Jsoup.connect(url).userAgent(userAgent).get()

        // 공연 제목 추출
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

        // 이미지 URL 추출
        val imageUrl = document.select("img[border=0]").attr("src")

        // 공연 장소 추출
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
        val userAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36"
        val document = Jsoup.connect(url).userAgent(userAgent).get()

        val title = document.select("p.tit_consert").text()

        val ticketSaleDate = document.select("dl.schedule_info")
        var preTicketSaleDay = ""
        var preTicketSaleTime = ""
        var ticketSaleDay = ""
        var ticketSaleTime = ""

        val datePattern = Pattern.compile("""(\d{4})년 (\d{1,2})월 (\d{1,2})일.*?(\d{2}:\d{2})""")

        val scheduleList = ticketSaleDate.select("dd")
        if (scheduleList.size > 1) {
            val preOpen = scheduleList[0].text().split(":")[1] + ":00"
            val preMatcher = datePattern.matcher(preOpen)
            if (preMatcher.find()) {
                preTicketSaleDay = "${preMatcher.group(1)}-${preMatcher.group(2).padStart(2, '0')}-${
                    preMatcher.group(3).padStart(2, '0')
                }"
                preTicketSaleTime = preMatcher.group(4)
            }
            val open = scheduleList[1].text().split(":")[1] + ":00"
            val matcher = datePattern.matcher(open)
            if (matcher.find()) {
                ticketSaleDay =
                    "${matcher.group(1)}-${matcher.group(2).padStart(2, '0')}-${matcher.group(3).padStart(2, '0')}"
                ticketSaleTime = matcher.group(4)
            }
        } else {
            val open = scheduleList[0].text().split(":")[1] + ":00"
            val matcher = datePattern.matcher(open)
            if (matcher.find()) {
                ticketSaleDay =
                    "${matcher.group(1)}-${matcher.group(2).padStart(2, '0')}-${matcher.group(3).padStart(2, '0')}"
                ticketSaleTime = matcher.group(4)
            }
        }

        val imageUrl =
            document.select("img[onerror='noImage(this, 130, 180)']").attr("src").replace("130x184", "1300x1840")

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
                    ticketSaleSchedules = mutableListOf(
                        CreateTicketRequest.TicketSaleSchedule(
                            day = LocalDate.parse(ticketSaleDay, DateTimeFormatter.ISO_DATE),
                            time = LocalTime.parse(ticketSaleTime, DateTimeFormatter.ofPattern("HH:mm")),
                            type = "일반예매"
                        )
                    ).apply {
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
        val userAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36"
        val document = Jsoup.connect(url).userAgent(userAgent).get()
        val infoElements = document.select("span")
        val infos = buildString {
            for (element in infoElements) {
                append(element.text())
            }
        }
        return infos
    }

    private fun fetchTicketlinkTicketInfo(url: String): CreateTicketRequest {
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch()
            val page = browser.newPage()

            page.navigate(url)

            val title = page.locator("meta[property='og:title']").nth(1).getAttribute("content")
                ?.replace("[티켓링크 티켓오픈] ", "")
                ?.replace("티켓오픈 안내", "")
                ?.replace("<b>", "")
                ?.replace("</b>", "")
                ?.replace("[단독판매]", "")
                ?.trim() ?: "알 수 없음"

            val dateTimeText = page.locator("#ticketOpenDatetime").textContent()?.trim() ?: ""
            val (ticketSaleDay, ticketSaleTime) = dateTimeText.split(" ").let {
                Pair(it.getOrElse(0) { "" }.replace('.', '-').replace('.', '-').split('(')[0], it.getOrElse(1) { "" })
            }

            val imageUrl = "https:${page.locator("dd.thumb img").getAttribute("src")}"

            browser.close()
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
                                day = LocalDate.parse(ticketSaleDay, DateTimeFormatter.ISO_DATE),
                                time = LocalTime.parse(ticketSaleTime, DateTimeFormatter.ofPattern("HH:mm")),
                                type = "일반예매"
                            )
                        )
                    )
                ),
                lineupImage = null,
                price = emptyList()
            )
        }
    }

    private fun fetchTicketlinkTicketRaw(url: String): String {
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch()
            val page = browser.newPage()

            page.navigate(url)
            val info = page.evaluate(
                """
            () => {
                const metaTags = document.querySelectorAll('meta[property="og:description"]');
                return Array.from(metaTags).map(tag => tag.content);
            }
            """.trimIndent()
            ) as List<String>

            browser.close()
            return info.joinToString("\n")
        }
    }
}