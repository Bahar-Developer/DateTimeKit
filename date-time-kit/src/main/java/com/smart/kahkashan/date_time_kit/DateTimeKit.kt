package com.smart.kahkashan.date_time_kit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

/** Optional override for app-specific copy. Missing strings use locale-based defaults. */
data class DatePickerText(
    val title: String? = null,
    val clear: String? = null,
    val today: String? = null,
    val previousMonth: String? = null,
    val nextMonth: String? = null
)

private fun defaultText(key: String, locale: Locale): String {
    val lang = locale.language
    return when (key) {
        "title" -> when (lang) {
            "fa" -> "انتخاب تاریخ"; "ar" -> "اختر التاريخ"; "de" -> "Datum auswählen"
            "fr" -> "Choisir une date"; "es" -> "Elegir fecha"; "hi" -> "तारीख चुनें"
            "ja" -> "日付を選択"; "zh" -> "选择日期"; "ru" -> "Выберите дату"
            else -> "Select date"
        }
        "clear" -> when (lang) {
            "fa" -> "پاک کردن"; "ar" -> "مسح"; "de" -> "Löschen"; "fr" -> "Effacer"
            "es" -> "Borrar"; "hi" -> "साफ़ करें"; "ja" -> "クリア"; "zh" -> "清除"
            "ru" -> "Очистить"; else -> "Clear"
        }
        "today" -> when (lang) {
            "fa" -> "امروز"; "ar" -> "اليوم"; "de" -> "Heute"; "fr" -> "Aujourd’hui"
            "es" -> "Hoy"; "hi" -> "आज"; "ja" -> "今日"; "zh" -> "今天"
            "ru" -> "Сегодня"; else -> "Today"
        }
        "previous" -> when (lang) {
            "fa" -> "ماه قبل"; "ar" -> "الشهر السابق"; "de" -> "Vorheriger Monat"
            "fr" -> "Mois précédent"; "es" -> "Mes anterior"; else -> "Previous month"
        }
        else -> when (lang) {
            "fa" -> "ماه بعد"; "ar" -> "الشهر التالي"; "de" -> "Nächster Monat"
            "fr" -> "Mois suivant"; "es" -> "Mes siguiente"; else -> "Next month"
        }
    }
}

private fun localizeDigits(value: String, locale: Locale): String {
    val zero = when (locale.language) {
        "fa" -> '۰'; "ar" -> '٠'; "bn" -> '০'; "hi" -> '०'; "th" -> '๐'; "my" -> '၀'
        else -> '0'
    }
    return value.map { ch -> if (ch in '0'..'9') (zero.code + ch.code - '0'.code).toChar() else ch }.joinToString("")
}

/**
 * Locale-aware Compose date picker.
 *
 * `locale` controls language, digit shaping and first weekday; `calendarSystem`
 * independently controls calendar arithmetic. `onSelectionChanged` returns dates
 * with both calendar fields and ISO LocalDate values.
 */
@Composable
fun DatePicker(
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    calendarSystem: CalendarSystem = CalendarSystem.GREGORIAN,
    selectionMode: SelectionMode = SelectionMode.SINGLE,
    initialDate: LocalDate = LocalDate.now(),
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    texts: DatePickerText = DatePickerText(),
    onSelectionChanged: (List<CalendarDate>) -> Unit = {}
) {
    require(minDate == null || maxDate == null || !minDate.isAfter(maxDate)) {
        "minDate must be on or before maxDate"
    }
    val engine = remember(locale, calendarSystem) { IcuCalendarEngine(calendarSystem, locale) }
    var page by remember(locale, calendarSystem, initialDate) { mutableStateOf(engine.pageFor(initialDate)) }
    var selectedStart by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEnd by remember { mutableStateOf<LocalDate?>(null) }
    val isRtl = locale.language in setOf("fa", "ar", "he", "ur", "ps", "sd", "yi")
    val weekdayLabels = remember(locale) {
        val symbols = DateFormatSymbols(locale).shortWeekdays
        val cal = Calendar.getInstance(locale)
        val first = cal.firstDayOfWeek // Sunday=1 ... Saturday=7
        (0..6).map { offset ->
            val index = ((first - 1 + offset) % 7) + 1
            symbols[index].take(2)
        }
    }
    val dayCount = engine.daysInMonth(page)
    val offset = engine.weekdayOffset(page, locale)
    val cells = (1..offset).map { 0 } + (1..dayCount).toList()

    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(texts.title ?: defaultText("title", locale), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { page = engine.move(page, -1) }) {
                    Icon(
                        if (isRtl) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = texts.previousMonth ?: defaultText("previous", locale)
                    )
                }
                Text(
                    localizeDigits(engine.monthLabel(page, locale), locale),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { page = engine.move(page, 1) }) {
                    Icon(
                        if (isRtl) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = texts.nextMonth ?: defaultText("next", locale)
                    )
                }
            }
            Row(Modifier.fillMaxWidth()) {
                weekdayLabels.forEach { label ->
                    Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium)
                }
            }
            LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(280.dp)) {
                items(cells) { day ->
                    if (day == 0) {
                        Spacer(Modifier.aspectRatio(1f))
                    } else {
                        val date = engine.dateAt(page, day)
                        val allowed = (minDate == null || !date.isBefore(minDate)) &&
                            (maxDate == null || !date.isAfter(maxDate))
                        val chosen = date == selectedStart || date == selectedEnd
                        val between = selectedStart != null && selectedEnd != null &&
                            date.isAfter(minOf(selectedStart!!, selectedEnd!!)) &&
                            date.isBefore(maxOf(selectedStart!!, selectedEnd!!))
                        Box(
                            modifier = Modifier.padding(2.dp).aspectRatio(1f)
                                .background(
                                    when {
                                        chosen -> accentColor
                                        between -> accentColor.copy(alpha = .18f)
                                        else -> Color.Transparent
                                    }, RoundedCornerShape(50)
                                )
                                .clickable(enabled = allowed) {
                                    if (selectionMode == SelectionMode.SINGLE) {
                                        selectedStart = date
                                        selectedEnd = null
                                        onSelectionChanged(listOf(engine.toCalendarDate(date)))
                                    } else if (selectedStart == null || selectedEnd != null) {
                                        selectedStart = date
                                        selectedEnd = null
                                        onSelectionChanged(listOf(engine.toCalendarDate(date)))
                                    } else {
                                        val a = minOf(selectedStart!!, date)
                                        val b = maxOf(selectedStart!!, date)
                                        selectedStart = a
                                        selectedEnd = b
                                        onSelectionChanged(listOf(engine.toCalendarDate(a), engine.toCalendarDate(b)))
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                localizeDigits(day.toString(), locale),
                                color = when {
                                    !allowed -> MaterialTheme.colorScheme.onSurface.copy(alpha = .3f)
                                    chosen -> MaterialTheme.colorScheme.onPrimary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = {
                    selectedStart = null
                    selectedEnd = null
                    onSelectionChanged(emptyList())
                }) { Text(texts.clear ?: defaultText("clear", locale)) }
                TextButton(onClick = {
                    val today = LocalDate.now()
                    page = engine.pageFor(today)
                }) { Text(texts.today ?: defaultText("today", locale)) }
            }
        }
    }
}
