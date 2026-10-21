package com.smart.kahkashan.date_time_kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

enum class SelectionStyle { FILLED, OUTLINED }

/**
 * Headless State holder for calendar logic, navigation, and date selection.
 * Exposes all state properties and actions so developers can build 100% custom UI components.
 */
@Stable
class DatePickerState internal constructor(
    initialDate: LocalDate,
    val locale: Locale,
    val calendarSystem: CalendarSystem,
    val selectionMode: SelectionMode,
    val selectionStyle: SelectionStyle,
    val minDate: LocalDate?,
    val maxDate: LocalDate?,
    private val onSelectionChangedCallback: (List<CalendarDate>) -> Unit
) {
    val engine: CalendarEngine = IcuCalendarEngine(calendarSystem, locale)

    var page by mutableStateOf(engine.pageFor(initialDate))
        private set

    var selectedStart by mutableStateOf<LocalDate?>(null)
        private set

    var selectedEnd by mutableStateOf<LocalDate?>(null)
        private set

    val isRtl: Boolean
        get() = locale.language in setOf("fa", "ar", "he", "ur", "ps", "sd", "yi")

    val year: Int
        get() = page.year

    val month: Int
        get() = page.month

    val monthName: String
        get() = engine.monthName(page.month, locale)

    val monthLabel: String
        get() = engine.monthLabel(page, locale)

    val daysInMonth: Int
        get() = engine.daysInMonth(page)

    val weekdayOffset: Int
        get() = engine.weekdayOffset(page, locale)

    val cells: List<Int>
        get() = (1..weekdayOffset).map { 0 } + (1..daysInMonth).toList()

    val weekdayLabels: List<String>
        get() = if (calendarSystem == CalendarSystem.JALALI || locale.language == "fa") {
            listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
        } else {
            val symbols = DateFormatSymbols(locale).shortWeekdays
            val cal = Calendar.getInstance(locale)
            val first = cal.firstDayOfWeek
            (0..6).map { offset ->
                val index = ((first - 1 + offset) % 7) + 1
                symbols[index].take(2)
            }
        }

    val selectedDates: List<CalendarDate>
        get() = listOfNotNull(selectedStart, selectedEnd).map { engine.toCalendarDate(it) }

    // --- ACTIONS ---

    fun selectDate(date: LocalDate) {
        if (!isDateAllowed(date)) return
        if (selectionMode == SelectionMode.SINGLE) {
            selectedStart = date
            selectedEnd = null
            onSelectionChangedCallback(listOf(engine.toCalendarDate(date)))
        } else if (selectedStart == null || selectedEnd != null) {
            selectedStart = date
            selectedEnd = null
            onSelectionChangedCallback(listOf(engine.toCalendarDate(date)))
        } else {
            val a = minOf(selectedStart!!, date)
            val b = maxOf(selectedStart!!, date)
            selectedStart = a
            selectedEnd = b
            onSelectionChangedCallback(listOf(engine.toCalendarDate(a), engine.toCalendarDate(b)))
        }
    }

    fun selectDay(day: Int) {
        if (day <= 0 || day > daysInMonth) return
        selectDate(dateAt(day))
    }

    fun nextMonth() {
        page = engine.move(page, 1)
    }

    fun previousMonth() {
        page = engine.move(page, -1)
    }

    fun moveMonth(amount: Int) {
        page = engine.move(page, amount)
    }

    fun setMonth(month: Int) {
        require(month in 1..12) { "Month must be between 1 and 12" }
        page = MonthPage(page.year, month)
    }

    fun setYear(year: Int) {
        page = MonthPage(year, page.month)
    }

    fun setMonthAndYear(month: Int, year: Int) {
        require(month in 1..12) { "Month must be between 1 and 12" }
        page = MonthPage(year, month)
    }

    fun clear() {
        selectedStart = null
        selectedEnd = null
        onSelectionChangedCallback(emptyList())
    }

    fun isDateAllowed(date: LocalDate): Boolean {
        return (minDate == null || !date.isBefore(minDate)) &&
               (maxDate == null || !date.isAfter(maxDate))
    }

    fun isDayAllowed(day: Int): Boolean {
        if (day <= 0 || day > daysInMonth) return false
        return isDateAllowed(dateAt(day))
    }

    fun isDateSelected(date: LocalDate): Boolean {
        return date == selectedStart || date == selectedEnd
    }

    fun isDaySelected(day: Int): Boolean {
        if (day <= 0) return false
        return isDateSelected(dateAt(day))
    }

    fun isDateBetween(date: LocalDate): Boolean {
        val start = selectedStart ?: return false
        val end = selectedEnd ?: return false
        return date.isAfter(minOf(start, end)) && date.isBefore(maxOf(start, end))
    }

    fun isDayBetween(day: Int): Boolean {
        if (day <= 0) return false
        return isDateBetween(dateAt(day))
    }

    fun dateAt(day: Int): LocalDate {
        return engine.dateAt(page, day)
    }

    fun monthNameFor(month: Int): String {
        return engine.monthName(month, locale)
    }

    fun localizeDigits(value: String): String {
        val zero = when (locale.language) {
            "fa" -> '۰'; "ar" -> '٠'; "bn" -> '۰'; "hi" -> '०'; "th" -> '๐'; "my" -> '၀'
            else -> '0'
        }
        return value.map { ch -> if (ch in '0'..'9') (zero.code + ch.code - '0'.code).toChar() else ch }.joinToString("")
    }
}

/**
 * Remembers a headless [DatePickerState] instance across recompositions.
 */
@Composable
fun rememberDatePickerState(
    initialDate: LocalDate = LocalDate.now(),
    locale: Locale = Locale.getDefault(),
    calendarSystem: CalendarSystem = CalendarSystem.GREGORIAN,
    selectionMode: SelectionMode = SelectionMode.SINGLE,
    selectionStyle: SelectionStyle = SelectionStyle.OUTLINED,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    onSelectionChanged: (List<CalendarDate>) -> Unit = {}
): DatePickerState {
    return remember(locale, calendarSystem, selectionMode, selectionStyle, minDate, maxDate) {
        DatePickerState(
            initialDate = initialDate,
            locale = locale,
            calendarSystem = calendarSystem,
            selectionMode = selectionMode,
            selectionStyle = selectionStyle,
            minDate = minDate,
            maxDate = maxDate,
            onSelectionChangedCallback = onSelectionChanged
        )
    }
}

/**
 * Colors configuration for [DatePicker]. Allows complete design system customization.
 */
data class DatePickerColors(
    val containerColor: Color = Color.White,
    val primaryAccent: Color = Color(0xFF1A6CFF),
    val titleTextColor: Color = Color(0xFF1D2939),
    val clearTextColor: Color = Color(0xFFFF3B30),
    val monthYearTextColor: Color = Color(0xFF1D2939),
    val weekdayTextColor: Color = Color(0xFF667085),
    val dayTextColor: Color = Color(0xFF1D2939),
    val disabledDayTextColor: Color = Color(0xFFD0D5DD),
    val selectedDayBackgroundColor: Color = Color(0xFFEBF2FF),
    val selectedDayBorderColor: Color = Color(0xFF1A6CFF),
    val selectedDayFilledTextColor: Color = Color.White,
    val rangeBetweenBackgroundColor: Color = Color(0xFFEBF2FF),
    val confirmButtonContainerColor: Color = Color(0xFF1A6CFF),
    val confirmButtonContentColor: Color = Color.White,
    val cancelButtonContainerColor: Color = Color(0xFFF2F4F7),
    val cancelButtonContentColor: Color = Color(0xFF344054),
    val iconTint: Color = Color(0xFF1D2939)
)

/**
 * Typography & TextStyles configuration for [DatePicker].
 */
data class DatePickerTypography(
    val titleStyle: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    val clearStyle: TextStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp),
    val monthYearStyle: TextStyle = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    val weekdayStyle: TextStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    val dayStyle: TextStyle = TextStyle(fontSize = 14.sp),
    val buttonStyle: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)
)

/**
 * Shapes configuration for [DatePicker].
 */
data class DatePickerShapes(
    val cardShape: Shape = RoundedCornerShape(20.dp),
    val dayShape: Shape = RoundedCornerShape(10.dp),
    val buttonShape: Shape = RoundedCornerShape(12.dp),
    val headerBadgeShape: Shape = RoundedCornerShape(8.dp)
)

/** Optional override for app-specific copy. Missing strings use locale-based defaults. */
data class DatePickerText(
    val title: String? = null,
    val clear: String? = null,
    val today: String? = null,
    val previousMonth: String? = null,
    val nextMonth: String? = null,
    val confirm: String? = null,
    val cancel: String? = null
)

private fun defaultText(key: String, locale: Locale): String {
    val lang = locale.language
    return when (key) {
        "title" -> when (lang) {
            "fa" -> "تاریخ را انتخاب کنید"; "ar" -> "اختر التاريخ"; "de" -> "Datum auswählen"
            "fr" -> "Choisir une date"; "es" -> "Elegir fecha"; "hi" -> "تاریب انتخاب کنید"
            "ja" -> "日付を選択"; "zh" -> "选择日期"; "ru" -> "Выберите дату"
            else -> "Select date"
        }
        "clear" -> when (lang) {
            "fa" -> "پاک کردن"; "ar" -> "مسح"; "de" -> "Löschen"; "fr" -> "Effacer"
            "es" -> "Borrar"; "hi" -> "साफ़ करें"; "ja" -> "クリア"; "zh" -> "清除"
            "ru" -> "Очистить"; else -> "Clear"
        }
        "confirm" -> when (lang) {
            "fa" -> "تایید"; "ar" -> "تأكيد"; "de" -> "Bestätigen"; "fr" -> "Confirmer"
            "es" -> "Confirmar"; else -> "Confirm"
        }
        "cancel" -> when (lang) {
            "fa" -> "انصراف"; "ar" -> "إلغاء"; "de" -> "Abbrechen"; "fr" -> "Annuler"
            "es" -> "Cancelar"; else -> "Cancel"
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

/**
 * Scope for composing completely dynamic DatePicker layouts DSL.
 * Exposes state, options, and building block Composables for custom layout composition.
 */
class DatePickerScope(
    val state: DatePickerState,
    val colors: DatePickerColors,
    val typography: DatePickerTypography,
    val shapes: DatePickerShapes,
    val texts: DatePickerText,
    val onConfirm: (List<CalendarDate>) -> Unit,
    val onCancel: () -> Unit
) {
    @Composable
    fun Header(
        modifier: Modifier = Modifier,
        title: String? = texts.title,
        clearText: String? = texts.clear
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = null,
                    tint = colors.iconTint,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title ?: defaultText("title", state.locale),
                    style = typography.titleStyle,
                    color = colors.titleTextColor
                )
            }

            Row(
                modifier = Modifier
                    .clip(shapes.headerBadgeShape)
                    .clickable { state.clear() }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = clearText ?: defaultText("clear", state.locale),
                    style = typography.clearStyle,
                    color = colors.clearTextColor
                )
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = colors.clearTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    @Composable
    fun Navigation(modifier: Modifier = Modifier) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { state.previousMonth() }) {
                Icon(
                    imageVector = if (state.isRtl) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowLeft,
                    contentDescription = texts.previousMonth ?: defaultText("previous", state.locale),
                    tint = colors.iconTint
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                var showMonthMenu by remember { mutableStateOf(false) }
                Box {
                    Row(
                        modifier = Modifier
                            .clip(shapes.headerBadgeShape)
                            .clickable { showMonthMenu = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = state.localizeDigits(state.monthName),
                            style = typography.monthYearStyle,
                            color = colors.monthYearTextColor
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.monthYearTextColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMonthMenu,
                        onDismissRequest = { showMonthMenu = false }
                    ) {
                        (1..12).forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = state.localizeDigits(state.monthNameFor(m)),
                                        style = typography.monthYearStyle,
                                        color = colors.monthYearTextColor
                                    )
                                },
                                onClick = {
                                    state.setMonth(m)
                                    showMonthMenu = false
                                }
                            )
                        }
                    }
                }

                var showYearMenu by remember { mutableStateOf(false) }
                Box {
                    Row(
                        modifier = Modifier
                            .clip(shapes.headerBadgeShape)
                            .clickable { showYearMenu = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = state.localizeDigits(state.year.toString()),
                            style = typography.monthYearStyle,
                            color = colors.monthYearTextColor
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.monthYearTextColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showYearMenu,
                        onDismissRequest = { showYearMenu = false }
                    ) {
                        val currentYear = state.year
                        (currentYear - 20..currentYear + 20).forEach { y ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = state.localizeDigits(y.toString()),
                                        style = typography.monthYearStyle,
                                        color = colors.monthYearTextColor
                                    )
                                },
                                onClick = {
                                    state.setYear(y)
                                    showYearMenu = false
                                }
                            )
                        }
                    }
                }
            }

            IconButton(onClick = { state.nextMonth() }) {
                Icon(
                    imageVector = if (state.isRtl) Icons.Default.KeyboardArrowLeft else Icons.Default.KeyboardArrowRight,
                    contentDescription = texts.nextMonth ?: defaultText("next", state.locale),
                    tint = colors.iconTint
                )
            }
        }
    }

    @Composable
    fun Weekdays(modifier: Modifier = Modifier) {
        Row(modifier.fillMaxWidth()) {
            state.weekdayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = typography.weekdayStyle,
                    color = colors.weekdayTextColor
                )
            }
        }
    }

    @Composable
    fun DayGrid(
        modifier: Modifier = Modifier,
        height: Dp = 240.dp,
        dayContent: (@Composable (day: Int) -> Unit)? = null
    ) {
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = modifier.height(height)) {
            items(state.cells) { day ->
                if (day == 0) {
                    Spacer(Modifier.aspectRatio(1f))
                } else if (dayContent != null) {
                    dayContent(day)
                } else {
                    val allowed = state.isDayAllowed(day)
                    val chosen = state.isDaySelected(day)
                    val between = state.isDayBetween(day)

                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .aspectRatio(1f)
                            .then(
                                when {
                                    chosen && state.selectionStyle == SelectionStyle.OUTLINED ->
                                        Modifier
                                            .background(colors.selectedDayBackgroundColor, shapes.dayShape)
                                            .border(1.5.dp, colors.selectedDayBorderColor, shapes.dayShape)
                                    chosen && state.selectionStyle == SelectionStyle.FILLED ->
                                        Modifier.background(colors.primaryAccent, shapes.dayShape)
                                    between ->
                                        Modifier.background(colors.rangeBetweenBackgroundColor, shapes.dayShape)
                                    else -> Modifier
                                }
                            )
                            .clickable(enabled = allowed) {
                                state.selectDay(day)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.localizeDigits(day.toString()),
                            style = typography.dayStyle.copy(
                                fontWeight = if (chosen) FontWeight.Bold else typography.dayStyle.fontWeight
                            ),
                            color = when {
                                !allowed -> colors.disabledDayTextColor
                                chosen && state.selectionStyle == SelectionStyle.FILLED -> colors.selectedDayFilledTextColor
                                else -> colors.dayTextColor
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun Footer(modifier: Modifier = Modifier) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onConfirm(state.selectedDates) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = shapes.buttonShape,
                colors = ButtonDefaults.buttonColors(containerColor = colors.confirmButtonContainerColor)
            ) {
                Text(
                    text = texts.confirm ?: defaultText("confirm", state.locale),
                    style = typography.buttonStyle,
                    color = colors.confirmButtonContentColor
                )
            }

            Button(
                onClick = { onCancel() },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = shapes.buttonShape,
                colors = ButtonDefaults.buttonColors(containerColor = colors.cancelButtonContainerColor)
            ) {
                Text(
                    text = texts.cancel ?: defaultText("cancel", state.locale),
                    style = typography.buttonStyle,
                    color = colors.cancelButtonContentColor
                )
            }
        }
    }
}

/**
 * Fully dynamic DatePicker component using [DatePickerScope] DSL content block.
 */
@Composable
fun DatePickerLayout(
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    calendarSystem: CalendarSystem = CalendarSystem.GREGORIAN,
    selectionMode: SelectionMode = SelectionMode.SINGLE,
    selectionStyle: SelectionStyle = SelectionStyle.OUTLINED,
    initialDate: LocalDate = LocalDate.now(),
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    onSelectionChanged: (List<CalendarDate>) -> Unit = {},
    state: DatePickerState = rememberDatePickerState(
        initialDate = initialDate,
        locale = locale,
        calendarSystem = calendarSystem,
        selectionMode = selectionMode,
        selectionStyle = selectionStyle,
        minDate = minDate,
        maxDate = maxDate,
        onSelectionChanged = onSelectionChanged
    ),
    colors: DatePickerColors = DatePickerColors(),
    typography: DatePickerTypography = DatePickerTypography(),
    shapes: DatePickerShapes = DatePickerShapes(),
    texts: DatePickerText = DatePickerText(),
    onConfirm: (List<CalendarDate>) -> Unit = {},
    onCancel: () -> Unit = {},
    content: @Composable DatePickerScope.() -> Unit
) {
    val layoutDirection = if (state.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val scope = remember(state, colors, typography, shapes, texts, onConfirm, onCancel) {
        DatePickerScope(state, colors, typography, shapes, texts, onConfirm, onCancel)
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = shapes.cardShape,
            colors = CardDefaults.cardColors(containerColor = colors.containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                scope.content()
            }
        }
    }
}

/**
 * Locale-aware Compose date picker component with pre-built UI and slots overrides.
 */
@Composable
fun DatePicker(
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    calendarSystem: CalendarSystem = CalendarSystem.GREGORIAN,
    selectionMode: SelectionMode = SelectionMode.SINGLE,
    selectionStyle: SelectionStyle = SelectionStyle.OUTLINED,
    initialDate: LocalDate = LocalDate.now(),
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    onSelectionChanged: (List<CalendarDate>) -> Unit = {},
    state: DatePickerState = rememberDatePickerState(
        initialDate = initialDate,
        locale = locale,
        calendarSystem = calendarSystem,
        selectionMode = selectionMode,
        selectionStyle = selectionStyle,
        minDate = minDate,
        maxDate = maxDate,
        onSelectionChanged = onSelectionChanged
    ),
    colors: DatePickerColors = DatePickerColors(),
    typography: DatePickerTypography = DatePickerTypography(),
    shapes: DatePickerShapes = DatePickerShapes(),
    texts: DatePickerText = DatePickerText(),
    onConfirm: (List<CalendarDate>) -> Unit = {},
    onCancel: () -> Unit = {},
    headerContent: (@Composable (DatePickerState) -> Unit)? = null,
    navigationContent: (@Composable (DatePickerState) -> Unit)? = null,
    dayContent: (@Composable (day: Int, state: DatePickerState) -> Unit)? = null,
    footerContent: (@Composable (DatePickerState) -> Unit)? = null
) {
    DatePickerLayout(
        modifier = modifier,
        state = state,
        colors = colors,
        typography = typography,
        shapes = shapes,
        texts = texts,
        onConfirm = onConfirm,
        onCancel = onCancel
    ) {
        if (headerContent != null) {
            headerContent(state)
        } else {
            Header()
        }

        if (navigationContent != null) {
            navigationContent(state)
        } else {
            Navigation()
        }

        Weekdays()

        DayGrid(
            dayContent = if (dayContent != null) { day -> dayContent(day, state) } else null
        )

        if (footerContent != null) {
            footerContent(state)
        } else {
            Footer()
        }
    }
}
