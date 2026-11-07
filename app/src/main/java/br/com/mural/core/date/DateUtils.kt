package br.com.mural.core.date

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

object DateUtils {

    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun toDateKey(date: LocalDate): String = date.format(isoFormatter)

    fun parseDateKey(dateKey: String): LocalDate = LocalDate.parse(dateKey, isoFormatter)

    fun getMonthCells(
        month: YearMonth,
        firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    ): List<LocalDate?> {
        val firstDay = month.atDay(1)
        val lead = (firstDay.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
        val daysInMonth = (1..month.lengthOfMonth()).map { month.atDay(it) }
        val cells = List(lead) { null } + daysInMonth
        val totalCells = 42 // Always 6 rows x 7 columns
        return cells + List(totalCells - cells.size) { null }
    }

    fun epochMillisToLocalDate(
        millis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
    }

    fun monthStartToEpochMillis(
        month: YearMonth,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        return month.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun monthEndToEpochMillis(
        month: YearMonth,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        return month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun dayStartToEpochMillis(
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        return date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun dayEndToEpochMillis(
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        return date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }
}
