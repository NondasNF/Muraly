package br.com.mural.data.photos

import java.time.LocalDate

data class DayPhotoSummary(
    val date: LocalDate,
    val count: Int,
    val latestMediaId: Long
)
