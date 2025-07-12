package com.newket.application.admin.dto

import java.time.LocalDate
import java.time.LocalTime

data class AddTicketSaleScheduleRequest(
    val day: LocalDate,
    val time: LocalTime,
    val type: String,
)