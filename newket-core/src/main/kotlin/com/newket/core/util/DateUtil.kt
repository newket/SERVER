package com.newket.core.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

object DateUtil {
    // D-3
    fun dateToDDay(date: LocalDate): String {
        val today = LocalDate.now()
        val daysBetween = ChronoUnit.DAYS.between(today, date)

        return when {
            daysBetween > 0 -> "D-$daysBetween"
            daysBetween < 0 -> ""
            else -> "D-Day"
        }
    }

    // 2025.01.01 (수) - 2025.01.02 (목)
    fun dateToString(date: List<LocalDate>): String {
        val minDateString = "${date.min().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))}(${
            date.min().dayOfWeek.getDisplayName(
                TextStyle.SHORT, Locale.KOREAN
            )
        })"
        val maxDateString = "${date.max().format(DateTimeFormatter.ofPattern("MM.dd"))}(${
            date.max().dayOfWeek.getDisplayName(
                TextStyle.SHORT, Locale.KOREAN
            )
        })"

        return if (date.min() == date.max()) {
            minDateString
        } else {
            "$minDateString - $maxDateString"
        }
    }

    // 2025.01.01 (수)
    fun dateToString(date: LocalDate): String {
        return "${date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))} (${
            (date.dayOfWeek.getDisplayName(
                TextStyle.SHORT, Locale.KOREAN
            ))
        })"
    }

    // 2025.01.01 (수) 20:00
    fun dateTimeToString(date: List<Pair<LocalDate, LocalTime>>): List<String> {
        return date.map {
            "${it.first.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))} (${
                (it.first.dayOfWeek.getDisplayName(
                    TextStyle.SHORT, Locale.KOREAN
                ))
            }) ${it.second.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        }
    }

    // 2025.01.01 (수) 20:00
    fun dateTimeToString(day: LocalDate, time: LocalTime): String {
        return "${day.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))} (${
            (day.dayOfWeek.getDisplayName(
                TextStyle.SHORT, Locale.KOREAN
            ))
        }) ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}"
    }
}