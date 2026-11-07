package br.com.mural.navigation

object Routes {
    const val CALENDAR = "calendar"
    const val DAY_PATTERN = "day/{dateKey}"

    fun day(dateKey: String): String = "day/$dateKey"
}
