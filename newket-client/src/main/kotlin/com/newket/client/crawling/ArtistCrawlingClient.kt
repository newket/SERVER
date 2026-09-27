package com.newket.client.crawling

import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class ArtistCrawlingClient {
    fun profileCrawling(query: String): CrawlArtistRequest {
        val url =
            "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=0&ie=utf8&query=${query}&ackey=8xokta1v"

        val userAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/118.0.0.0 Safari/537.36"

        val document = Jsoup.connect(url)
            .userAgent(userAgent)
            .get()

        val name = listOf(
            document.select("._titleText_1h9gi_31").firstOrNull()?.text(),
            document.select(".area_text_title").firstOrNull()?.text()
        ).firstNotNullOfOrNull {
            it?.takeIf { text -> text.isNotBlank() }
        } ?: ""

        val subName = listOf(
            document.select("._text_1h9gi_52.line_1").text(),
            document.select("div.sub_title.first_elss .txt").firstOrNull()?.text()
        ).firstNotNullOfOrNull {
            it?.takeIf { text -> text.isNotBlank() }
        } ?: ""

        val imageUrl = listOf(
            document.select("._thumbBox_1eucg_8 img").firstOrNull()?.attr("src"),
            document.select("a.thumb img._img").firstOrNull()?.attr("src"),
            document.select("ul.img_list._scroller img").firstOrNull()?.attr("src") ?: ""
        ).firstNotNullOfOrNull {
            it?.takeIf { text -> text.isNotBlank() }
        }?.replace(
            Regex("size=\\d+"),
            "size=3000"
        ) ?: ""

        return CrawlArtistRequest(
            name = name,
            subName = subName,
            imageUrl = imageUrl
        )
    }
}