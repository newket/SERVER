package com.newket.infra.jpa.ticket.constant

enum class TicketProvider (val providerName: String, val imageUrl: String) {
    INTERPARK("인터파크", "https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/INTERPARK.png"),
    YES24("Yes24", "https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/YES24.png"),
    MELON("멜론티켓", "https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/MELON.png"),
    TICKETLINK("티켓링크", "https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/TICKETLINK.png"),
    COUPANGPLAY("쿠팡플레이", "https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/COUPANGPLAY.png"),
    ARTCENTER("예술의전당","https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/ARTCENTER.png"),
    CHARLOTTE("샤롯데씨어터","https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/CHARLOTTE.png"),
    DOSAN("두산아트센터","https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/DOSAN.png"),
    GOOGLE("구글폼","https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/GOOGLE.png"),
    NAVERFORM("네이버폼","https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/NAVERFORM.png"),
    NAVERRESERVATION("네이버예약","https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/NAVERRESERVATION.png"),
    SEJONG("세종문화회관","https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/SEJONG.png"),
    SHINSI("신시컴퍼니","https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/SHINSA.png"),
    SHOWNOTE("쇼노트","https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/SHOWNOTE.png"),
    TMON("티몬","https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/TMON.png"),
    TOPING("TOPING","https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/TOPING.png"),
    WMP("위메프","https://newkets3.s3.ap-northeast-2.amazonaws.com/ticket_provider/WMP.png"),
}