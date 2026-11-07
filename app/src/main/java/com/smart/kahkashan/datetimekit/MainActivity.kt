package com.smart.kahkashan.datetimekit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.kahkashan.date_time_kit.CalendarDate
import com.smart.kahkashan.date_time_kit.CalendarSystem
import com.smart.kahkashan.date_time_kit.DatePicker
import com.smart.kahkashan.date_time_kit.DatePickerColors
import com.smart.kahkashan.date_time_kit.DatePickerLayout
import com.smart.kahkashan.date_time_kit.SelectionMode
import com.smart.kahkashan.date_time_kit.SelectionStyle
import com.smart.kahkashan.date_time_kit.rememberDatePickerState
import com.smart.kahkashan.datetimekit.ui.theme.DateTimeKitTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DateTimeKitTheme {
                MainContainerScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContainerScreen() {
    var selectedTab by remember { mutableIntStateOf(1) } // Default to Sample 2

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.background(Color.White)) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                when (selectedTab) {
                                    0 -> "سمپل ۱ (باتم‌شیت تراکنش)"
                                    1 -> "سمپل ۲ (اسکرول افقی هتل)"
                                    2 -> "سمپل ۳ (اسکرول عمودی ماه‌ها)"
                                    else -> "سمپل ۴ (تم دارک و تغییر تقویم)"
                                },
                                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF101828))
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A6CFF),
                        edgePadding = 16.dp
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("باتم‌شیت", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("اسکرول افقی", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("اسکرول عمودی", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text("تم دارک و سیستم", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }
                }
            },
            containerColor = Color.White
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    0 -> Sample1Screen()
                    1 -> Sample2Screen()
                    2 -> Sample3Screen()
                    else -> Sample4Screen()
                }
            }
        }
    }
}

// ============================================================================
// SAMPLE 1: WALLET & TRANSACTIONS WITH BOTTOMSHEET DATEPICKER
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Sample1Screen() {
    val locale = remember { Locale.forLanguageTag("fa-IR") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedDates by remember { mutableStateOf<List<CalendarDate>>(emptyList()) }

    val dateFilterText = remember(selectedDates) {
        if (selectedDates.isEmpty()) {
            "۱۴۰۵/۰۳/۱۸ الی ۱۴۰۵/۰۳/۱۹"
        } else if (selectedDates.size == 1) {
            "${selectedDates[0].year}/${"%02d".format(selectedDates[0].month)}/${"%02d".format(selectedDates[0].day)}"
        } else {
            "${selectedDates[0].year}/${"%02d".format(selectedDates[0].month)}/${"%02d".format(selectedDates[0].day)} الی ${selectedDates[1].year}/${"%02d".format(selectedDates[1].month)}/${"%02d".format(selectedDates[1].day)}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A6CFF))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("موجودی کیف پول", style = TextStyle(color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("مدیریت حساب‌ها", style = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium))
                    }
                }
                Text("۱۰,۰۰۰,۰۰۰ ریال", style = TextStyle(color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1A6CFF))
                ) {
                    Text("برداشت از کیف پول", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("تراکنش ها", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF101828)))
            Surface(
                onClick = { showBottomSheet = true },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF2F4F7)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.DateRange, contentDescription = null, tint = Color(0xFF344054), modifier = Modifier.size(18.dp))
                    Text(text = dateFilterText, style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF344054)))
                }
            }
        }

        Text("دوشنبه ۱۸ خرداد", style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFF667085)))
        TransactionItem(title = "واریز", subtitle = "افزایش اعتبار", amount = "+۱,۰۰۰,۰۰۰ ریال", time = "۱۳:۰۵", isDeposit = true)
        TransactionItem(title = "برداشت", subtitle = "خرید از فروشگاه", amount = "-۲۰۰,۰۰۰ ریال", time = "۱۴:۰۵", isDeposit = false)

        Spacer(Modifier.height(4.dp))
        Text("دوشنبه ۱۹ خرداد", style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFF667085)))
        TransactionItem(title = "واریز", subtitle = "درآمد پروژه", amount = "+۱۰,۰۰۰,۰۰۰ ریال", time = "۱۴:۰۵", isDeposit = true)
        TransactionItem(title = "برداشت", subtitle = "انتقال وجه", amount = "-۱,۴۰۰,۰۰۰ ریال", time = "۱۶:۲۰", isDeposit = false)
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                DatePicker(
                    locale = locale,
                    calendarSystem = CalendarSystem.JALALI,
                    selectionMode = SelectionMode.RANGE,
                    selectionStyle = SelectionStyle.OUTLINED,
                    onConfirm = { dates ->
                        if (dates.isNotEmpty()) selectedDates = dates
                        showBottomSheet = false
                    },
                    onCancel = { showBottomSheet = false }
                )
            }
        }
    }
}

// ============================================================================
// SAMPLE 2: HORIZONTAL PAGER CALENDAR (HOTEL BOOKING)
// ============================================================================
@Composable
private fun Sample2Screen() {
    val locale = remember { Locale.forLanguageTag("fa-IR") }
    val calendarSystem = CalendarSystem.JALALI
    val datePickerState = rememberDatePickerState(locale = locale, calendarSystem = calendarSystem, selectionMode = SelectionMode.RANGE)

    val initialPagerPage = 500
    val pagerState = rememberPagerState(initialPage = initialPagerPage, pageCount = { 1000 })
    val coroutineScope = rememberCoroutineScope()

    val currentVisibleMonthPage = remember(pagerState.currentPage) {
        val monthOffset = pagerState.currentPage - initialPagerPage
        datePickerState.engine.move(datePickerState.engine.pageFor(LocalDate.now()), monthOffset)
    }
    val currentMonthName = remember(currentVisibleMonthPage) {
        datePickerState.monthNameFor(currentVisibleMonthPage.month)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F5FF))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF1A6CFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Hotel, contentDescription = null, tint = Color.White)
                }
                Column {
                    Text("رزرو اقامتگاه و هتل", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF101828)))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Swipe, contentDescription = null, tint = Color(0xFF1A6CFF), modifier = Modifier.size(14.dp))
                        Text("برای تغییر ماه، روی تقویم سوایپ افقی کنید", style = TextStyle(fontSize = 12.sp, color = Color(0xFF1A6CFF)))
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${datePickerState.localizeDigits(currentMonthName)} ${datePickerState.localizeDigits(currentVisibleMonthPage.year.toString())}",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF101828))
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF101828))
                        }
                        IconButton(onClick = { coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = Color(0xFF101828))
                        }
                    }
                }

                Row(Modifier.fillMaxWidth()) {
                    datePickerState.weekdayLabels.forEach { label ->
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF667085))
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(240.dp)
                ) { pageIndex ->
                    val monthOffset = pageIndex - initialPagerPage
                    val monthPage = datePickerState.engine.move(datePickerState.engine.pageFor(LocalDate.now()), monthOffset)
                    val dayCount = datePickerState.engine.daysInMonth(monthPage)
                    val offset = datePickerState.engine.weekdayOffset(monthPage, locale)
                    val cells = (1..offset).map { 0 } + (1..dayCount).toList()

                    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxSize()) {
                        items(cells) { day ->
                            if (day == 0) {
                                Spacer(Modifier.aspectRatio(1f))
                            } else {
                                val date = datePickerState.engine.dateAt(monthPage, day)
                                val chosen = datePickerState.isDateSelected(date)
                                val between = datePickerState.isDateBetween(date)
                                val dayShape = RoundedCornerShape(10.dp)

                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .aspectRatio(1f)
                                        .then(
                                            when {
                                                chosen -> Modifier.background(Color(0xFF1A6CFF), dayShape)
                                                between -> Modifier.background(Color(0xFFEBF2FF), dayShape)
                                                else -> Modifier
                                            }
                                        )
                                        .clickable { datePickerState.selectDate(date) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = datePickerState.localizeDigits(day.toString()),
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal,
                                            color = if (chosen) Color.White else Color(0xFF101828)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// SAMPLE 3: VERTICAL INFINITE SCROLLING CALENDAR
// ============================================================================
@Composable
private fun Sample3Screen() {
    val locale = remember { Locale.forLanguageTag("fa-IR") }
    val datePickerState = rememberDatePickerState(locale = locale, calendarSystem = CalendarSystem.JALALI, selectionMode = SelectionMode.SINGLE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("تقویم اسکرولی عمودی (Vertical List)", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF101828)))
                Text("لیست بی‌نهایت ماه‌ها برای اسکرول عمودی", style = TextStyle(fontSize = 12.sp, color = Color(0xFF667085)))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(24) { index ->
                val monthPage = datePickerState.engine.move(datePickerState.engine.pageFor(LocalDate.now()), index - 6)
                val monthName = datePickerState.engine.monthName(monthPage.month, locale)
                val dayCount = datePickerState.engine.daysInMonth(monthPage)
                val offset = datePickerState.engine.weekdayOffset(monthPage, locale)
                val cells = (1..offset).map { 0 } + (1..dayCount).toList()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${datePickerState.localizeDigits(monthName)} ${datePickerState.localizeDigits(monthPage.year.toString())}",
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A6CFF))
                        )

                        Row(Modifier.fillMaxWidth()) {
                            datePickerState.weekdayLabels.forEach { label ->
                                Text(
                                    text = label,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFF667085))
                                )
                            }
                        }

                        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(210.dp)) {
                            items(cells) { day ->
                                if (day == 0) {
                                    Spacer(Modifier.aspectRatio(1f))
                                } else {
                                    val date = datePickerState.engine.dateAt(monthPage, day)
                                    val chosen = datePickerState.isDateSelected(date)
                                    val dayShape = RoundedCornerShape(8.dp)

                                    Box(
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .aspectRatio(1f)
                                            .then(
                                                if (chosen) Modifier.background(Color(0xFF1A6CFF), dayShape)
                                                else Modifier.background(Color(0xFFF9FAFB), dayShape)
                                            )
                                            .clickable { datePickerState.selectDate(date) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = datePickerState.localizeDigits(day.toString()),
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal,
                                                color = if (chosen) Color.White else Color(0xFF101828)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// SAMPLE 4: DARK THEME & CALENDAR SYSTEM SWITCHER
// ============================================================================
@Composable
private fun Sample4Screen() {
    var calendarSystem by remember { mutableStateOf(CalendarSystem.JALALI) }
    val locale = remember(calendarSystem) {
        if (calendarSystem == CalendarSystem.JALALI) Locale.forLanguageTag("fa-IR") else Locale.getDefault()
    }
    var selectedDateInfo by remember { mutableStateOf("تاریخی انتخاب نشده است") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("تم دارک و سیستم تقویم", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
                Text("تغییر آنی بین تقویم شمسی و میلادی با استایل تیره اختصاصی", style = TextStyle(fontSize = 12.sp, color = Color(0xFF94A3B8)))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { calendarSystem = CalendarSystem.JALALI },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (calendarSystem == CalendarSystem.JALALI) Color(0xFF38BDF8) else Color(0xFF334155))
                    ) {
                        Text("شمسی", color = if (calendarSystem == CalendarSystem.JALALI) Color(0xFF0F172A) else Color.White)
                    }
                    Button(
                        onClick = { calendarSystem = CalendarSystem.GREGORIAN },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (calendarSystem == CalendarSystem.GREGORIAN) Color(0xFF38BDF8) else Color(0xFF334155))
                    ) {
                        Text("میلادی", color = if (calendarSystem == CalendarSystem.GREGORIAN) Color(0xFF0F172A) else Color.White)
                    }
                }
            }
        }

        DatePicker(
            locale = locale,
            calendarSystem = calendarSystem,
            selectionMode = SelectionMode.SINGLE,
            selectionStyle = SelectionStyle.FILLED,
            colors = DatePickerColors(
                containerColor = Color(0xFF1E293B),
                primaryAccent = Color(0xFF38BDF8),
                titleTextColor = Color.White,
                clearTextColor = Color(0xFFF87171),
                monthYearTextColor = Color.White,
                weekdayTextColor = Color(0xFF94A3B8),
                dayTextColor = Color.White,
                disabledDayTextColor = Color(0xFF475569),
                selectedDayFilledTextColor = Color(0xFF0F172A),
                iconTint = Color(0xFF38BDF8),
                confirmButtonContainerColor = Color(0xFF38BDF8),
                confirmButtonContentColor = Color(0xFF0F172A),
                cancelButtonContainerColor = Color(0xFF334155),
                cancelButtonContentColor = Color.White
            ),
            onConfirm = { dates ->
                if (dates.isNotEmpty()) selectedDateInfo = "${dates[0].year}/${dates[0].month}/${dates[0].day}"
            },
            onCancel = { selectedDateInfo = "لغو شد" }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
        ) {
            Text(
                text = "تاریخ انتخاب شده: $selectedDateInfo",
                modifier = Modifier.padding(16.dp),
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
            )
        }
    }
}

@Composable
private fun TransactionItem(
    title: String,
    subtitle: String,
    amount: String,
    time: String,
    isDeposit: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isDeposit) Color(0xFFECFDF3) else Color(0xFFFEF3F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDeposit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isDeposit) Color(0xFF12B76A) else Color(0xFFF04438),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF101828)))
                    Text(subtitle, style = TextStyle(fontSize = 12.sp, color = Color(0xFF667085)))
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(amount, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF101828)))
                Text(time, style = TextStyle(fontSize = 12.sp, color = Color(0xFF667085)))
            }
        }
    }
}
