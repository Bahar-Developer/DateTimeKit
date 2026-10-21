package com.smart.kahkashan.date_time_kit

import com.ibm.icu.util.Calendar
import com.ibm.icu.util.GregorianCalendar
import com.ibm.icu.util.PersianCalendar
import com.ibm.icu.text.SimpleDateFormat
import com.ibm.icu.util.IslamicCalendar
import com.ibm.icu.util.ULocale
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

/** Supported calendar systems. ICU supplies calendar arithmetic and leap-year rules. */
enum class CalendarSystem { GREGORIAN, JALALI, ISLAMIC_CIVIL, ISLAMIC_UMALQURA }

enum class SelectionMode { SINGLE, RANGE }

data class CalendarDate(
    val year: Int,
    val month: Int, // 1-based
    val day: Int,
    val calendarSystem: CalendarSystem,
    /** Stable Gregorian/ISO date for storage and interop. */
    val isoDate: LocalDate
) {
    override fun toString(): String = "$year-%02d-%02d".format(Locale.ROOT, month, day)
}

data class MonthPage(val year: Int, val month: Int)

interface CalendarEngine {
    fun pageFor(date: LocalDate): MonthPage
    fun dateAt(page: MonthPage, day: Int): LocalDate
    fun daysInMonth(page: MonthPage): Int
    fun monthLabel(page: MonthPage, locale: Locale): String
    fun monthName(month: Int, locale: Locale): String
    fun weekdayOffset(page: MonthPage, locale: Locale): Int
    fun toCalendarDate(date: LocalDate): CalendarDate
    fun move(page: MonthPage, amount: Int): MonthPage
}

private fun icuCalendar(system: CalendarSystem, locale: Locale, date: LocalDate? = null): Calendar {
    val uLocale = ULocale.forLocale(locale)
    val calendar = when (system) {
        CalendarSystem.GREGORIAN -> GregorianCalendar(uLocale)
        CalendarSystem.JALALI -> PersianCalendar(uLocale)
        CalendarSystem.ISLAMIC_CIVIL -> IslamicCalendar(uLocale).apply {
            calculationType = IslamicCalendar.CalculationType.ISLAMIC_CIVIL
        }
        CalendarSystem.ISLAMIC_UMALQURA -> IslamicCalendar(uLocale).apply {
            calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
        }
    }
    calendar.timeZone = com.ibm.icu.util.TimeZone.getTimeZone("UTC")
    calendar.isLenient = false
    if (date != null) {
        calendar.time = Date.from(date.atStartOfDay(ZoneId.of("UTC")).toInstant())
    }
    return calendar
}

internal class IcuCalendarEngine(
    private val system: CalendarSystem,
    private val locale: Locale
) : CalendarEngine {
    override fun pageFor(date: LocalDate): MonthPage {
        val c = icuCalendar(system, locale, date)
        return MonthPage(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
    }

    override fun dateAt(page: MonthPage, day: Int): LocalDate {
        val c = icuCalendar(system, locale)
        c.clear()
        c.set(Calendar.EXTENDED_YEAR, page.year)
        c.set(Calendar.MONTH, page.month - 1)
        c.set(Calendar.DAY_OF_MONTH, day)
        c.set(Calendar.HOUR_OF_DAY, 12)
        return c.time.toInstant().atZone(ZoneId.of("UTC")).toLocalDate()
    }

    override fun daysInMonth(page: MonthPage): Int {
        val c = icuCalendar(system, locale)
        c.clear()
        c.set(Calendar.EXTENDED_YEAR, page.year)
        c.set(Calendar.MONTH, page.month - 1)
        c.set(Calendar.DAY_OF_MONTH, 1)
        return c.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    override fun monthLabel(page: MonthPage, locale: Locale): String {
        val c = icuCalendar(system, locale)
        c.clear()
        c.set(Calendar.EXTENDED_YEAR, page.year)
        c.set(Calendar.MONTH, page.month - 1)
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 12)
        val sdf = SimpleDateFormat("MMMM yyyy", ULocale.forLocale(locale))
        sdf.calendar = c
        return sdf.format(c.time)
    }

    override fun monthName(month: Int, locale: Locale): String {
        val c = icuCalendar(system, locale)
        c.clear()
        c.set(Calendar.MONTH, month - 1)
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 12)
        val sdf = SimpleDateFormat("MMMM", ULocale.forLocale(locale))
        sdf.calendar = c
        return sdf.format(c.time)
    }

    override fun weekdayOffset(page: MonthPage, locale: Locale): Int {
        val first = dateAt(page, 1)
        val firstDay = icuCalendar(system, locale).firstDayOfWeek // ICU Sunday=1 ... Saturday=7
        val iso = first.dayOfWeek.value // Monday=1 ... Sunday=7
        val icuDow = if (iso == 7) 1 else iso + 1
        return (icuDow - firstDay + 7) % 7
    }

    override fun toCalendarDate(date: LocalDate): CalendarDate {
        val c = icuCalendar(system, locale, date)
        return CalendarDate(
            year = c.get(Calendar.YEAR),
            month = c.get(Calendar.MONTH) + 1,
            day = c.get(Calendar.DAY_OF_MONTH),
            calendarSystem = system,
            isoDate = date
        )
    }

    override fun move(page: MonthPage, amount: Int): MonthPage {
        val c = icuCalendar(system, locale)
        c.clear()
        c.set(Calendar.EXTENDED_YEAR, page.year)
        c.set(Calendar.MONTH, page.month - 1)
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.add(Calendar.MONTH, amount)
        return MonthPage(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
    }
}
