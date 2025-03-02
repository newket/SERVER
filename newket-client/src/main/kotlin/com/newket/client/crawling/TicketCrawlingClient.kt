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

        val title = response.select(".section_notice h3").text().split("단독판매")[0]
            .replace("상대우위", "").replace("절대우위", "").replace("좌석우위", "").trim()


        val ticketSaleDate = response.select(".open").text()
        val datePattern = Pattern.compile("(\\d{4})년 (\\d{1,2})월 (\\d{1,2})일")
        val timePattern = Pattern.compile("(오전|낮|오후)?\\s*(\\d{1,2})시")

        val dayFormatted = datePattern.matcher(ticketSaleDate).takeIf { it.find() }?.let {
            "%04d-%02d-%02d".format(it.group(1).toInt(), it.group(2).toInt(), it.group(3).toInt())
        } ?: ""

        val timeFormatted = timePattern.matcher(ticketSaleDate).takeIf { it.find() }?.let {
            val timeOfDay = it.group(1)
            var hour = it.group(2).toInt()
            if (timeOfDay == "오후" && hour != 12) hour += 12
            "%02d:00".format(hour)
        } ?: ""

        val ticketSaleSchedules = mutableListOf<CreateTicketRequest.TicketSaleSchedule>()

        if (dayFormatted.isNotEmpty() && timeFormatted.isNotEmpty()) {
            ticketSaleSchedules.add(
                CreateTicketRequest.TicketSaleSchedule(
                    day = LocalDate.parse(dayFormatted, DateTimeFormatter.ISO_DATE),
                    time = LocalTime.parse(timeFormatted, DateTimeFormatter.ofPattern("HH:mm")),
                    type = "일반예매"
                )
            )
        }


        val preTicketSaleDate = response.select("li")[1].text().trim()
        val preDayFormatted = datePattern.matcher(preTicketSaleDate).takeIf { it.find() }?.let {
            "%04d-%02d-%02d".format(it.group(1).toInt(), it.group(2).toInt(), it.group(3).toInt())
        } ?: ""

        val preTimeFormatted = timePattern.matcher(preTicketSaleDate).takeIf { it.find() }?.let {
            val timeOfDay = it.group(1)
            var hour = it.group(2).toInt()
            if (timeOfDay == "오후" && hour != 12) hour += 12
            if ((timeOfDay == "오전" || timeOfDay == "낮") && hour == 12) hour = 0
            "%02d:00".format(hour)
        } ?: ""

        if (preDayFormatted.isNotEmpty() && preTimeFormatted.isNotEmpty()) {
            ticketSaleSchedules.add(
                CreateTicketRequest.TicketSaleSchedule(
                    day = LocalDate.parse(preDayFormatted, DateTimeFormatter.ISO_DATE),
                    time = LocalTime.parse(preTimeFormatted, DateTimeFormatter.ofPattern("HH:mm")),
                    type = "선예매"
                )
            )
        }

        val imageUrl = response.select(".poster img").attr("src").let {
            if (it.startsWith("http")) it else "https:$it"
        }

        val ticketUrlElement = response.select(".btn_book")
        val ticketUrl = if (ticketUrlElement.isNotEmpty()) ticketUrlElement.attr("href") else url
        val isDirectUrl = ticketUrlElement.isNotEmpty()

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
                    url = ticketUrl,
                    isDirectUrl = isDirectUrl,
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
        val introduceSection = response.select("div.introduce").text()
        val introElement = response.selectFirst(".info1 h4 + .data")
        val artistElement = response.selectFirst(".info2 h4 + .data p")
        val artist = artistElement?.text()?.trim() ?: ""
        return introduceSection + introElement + artist
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

        // 예매 URL 추출
        val link = document.select("a.btn_c.btn_red").attr("href")
        val ticketIdPattern = Pattern.compile("""jsf_RedirectPerfDetail\((\d+)\)""")
        val matcher = ticketIdPattern.matcher(link)

        val (ticketUrl, isDirectUrl) = if (matcher.find()) {
            "http://m.ticket.yes24.com/Perf/Detail/PerfInfo.aspx?IdPerf=${matcher.group(1)}" to true
        } else {
            url to false
        }

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
                    url = ticketUrl,
                    isDirectUrl = isDirectUrl,
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

        val imageUrl = document.select("img[onerror='noImage(this, 130, 180)']").attr("src").replace("130x184","1300x1840")


        // URL 추출
        val detailMatcher = Pattern.compile("""bannerLanding\('TD', '(\d+)'\);""").matcher(document.toString())
        val (ticketUrl, isDirectUrl) = if (detailMatcher.find()) {
            "https://ticket.melon.com/performance/index.htm?prodId=${detailMatcher.group(1)}" to true
        } else {
            url to false
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
                    ticketProvider = TicketProvider.MELON,
                    url = ticketUrl,
                    isDirectUrl = isDirectUrl,
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

            val directUrl = if (page.locator("a.btn.btn_reserve").count() > 0) {
                page.locator("a.btn.btn_reserve").getAttribute("href")
            } else {
                null
            }
            val ticketUrl = directUrl?.let { "https://www.ticketlink.co.kr$it" } ?: url

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
                        url = ticketUrl,
                        isDirectUrl = ticketUrl != url,
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