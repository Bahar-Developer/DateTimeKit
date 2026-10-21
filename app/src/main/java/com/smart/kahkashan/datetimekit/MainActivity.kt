package com.smart.kahkashan.datetimekit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.smart.kahkashan.date_time_kit.MonthPage
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
    var selectedTab by remember { mutableIntStateOf(1) } // Default to Sample 2 (Scrollable Calendar)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.background(Color.White)) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                if (selectedTab == 1) "سمپل ۲ (تقویم اسکرولی افقی)" else "سمپل ۱ (باتم شیت تراکنش)",
                                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF101828))
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                    PrimaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A6CFF)
                    ) {
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("سمپل ۲ (اسکرولی)", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                        )
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("سمپل ۱ (باتم‌شیت)", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                        )
                    }
                }
            },
            containerColor = Color.White
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                if (selectedTab == 1) {
                    Sample2Screen()
                } else {
                    Sample1Screen()
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
        // 1. Blue Wallet Balance Card
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
                    Text(
                        "موجودی کیف پول",
                        style = TextStyle(color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable { }
                    ) {
                        Icon(
                            Icons.Outlined.CreditCard,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "مدیریت حساب‌ها",
                            style = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                }

                Text(
                    "۱۰,۰۰۰,۰۰۰ ریال",
                    style = TextStyle(color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                )

                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A6CFF)
                    )
                ) {
                    Text("برداشت از کیف پول", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp))
                }
            }
        }

        // 2. Income Report Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("گزارش درآمد", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF101828)))
                    Text("مشاهده تاریخچه سفرها", style = TextStyle(fontSize = 12.sp, color = Color(0xFF667085)))
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Color(0xFF667085)
                )
            }
        }

        // 3. Transactions Section Header + Date Filter Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "تراکنش ها",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF101828))
            )

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
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = "انتخاب تاریخ",
                        tint = Color(0xFF344054),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = dateFilterText,
                        style = TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF344054)
                        )
                    )
                }
            }
        }

        // 4. Transactions List Grouped by Date
        Text("دوشنبه ۱۸ خرداد", style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFF667085)))

        TransactionItem(title = "واریز", subtitle = "افزایش اعتبار", amount = "+۱,۰۰۰,۰۰۰ ریال", time = "۱۳:۰۵", isDeposit = true)
        TransactionItem(title = "واریز", subtitle = "بازگشت وجه", amount = "+۲۰۰,۰۰۰ ریال", time = "۱۲:۰۵", isDeposit = true)
        TransactionItem(title = "برداشت", subtitle = "برداشت از کیف پول", amount = "-۲۰۰,۰۰۰ ریال", time = "۱۴:۰۵", isDeposit = false)

        Spacer(Modifier.height(4.dp))
        Text("دوشنبه ۱۹ خرداد", style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFF667085)))

        TransactionItem(title = "واریز", subtitle = "درآمد", amount = "+۱۰,۰۰۰,۰۰۰ ریال", time = "۱۴:۰۵", isDeposit = true)
        TransactionItem(title = "برداشت", subtitle = "برداشت از کیف پول", amount = "-۱,۴۰۰,۰۰۰ ریال", time = "۱۴:۰۵", isDeposit = false)
        TransactionItem(title = "واریز", subtitle = "درآمد", amount = "+۱۰,۰۰۰,۰۰۰ ریال", time = "۱۲:۰۵", isDeposit = true)
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
                    colors = DatePickerColors(
                        containerColor = Color.White,
                        primaryAccent = Color(0xFF1A6CFF),
                        confirmButtonContainerColor = Color(0xFF1A6CFF),
                        cancelButtonContainerColor = Color(0xFFF2F4F7)
                    ),
                    onConfirm = { dates ->
                        if (dates.isNotEmpty()) {
                            selectedDates = dates
                        }
                        showBottomSheet = false
                    },
                    onCancel = { showBottomSheet = false }
                )
            }
        }
    }
}

// ============================================================================
// SAMPLE 2: SWIPEABLE HORIZONTAL PAGER CALENDAR FOR HOTEL / TRIP BOOKING
// ============================================================================
@Composable
private fun Sample2Screen() {
    val locale = remember { Locale.forLanguageTag("fa-IR") }
    val calendarSystem = CalendarSystem.JALALI

    val datePickerState = rememberDatePickerState(
        locale = locale,
        calendarSystem = calendarSystem,
        selectionMode = SelectionMode.RANGE,
        selectionStyle = SelectionStyle.OUTLINED
    )

    val initialPagerPage = 500
    val pagerState = rememberPagerState(initialPage = initialPagerPage, pageCount = { 1000 })
    val coroutineScope = rememberCoroutineScope()

    // Calculate current visible month name for the header based on pager state
    val currentVisibleMonthPage = remember(pagerState.currentPage) {
        val monthOffset = pagerState.currentPage - initialPagerPage
        val now = LocalDate.now()
        val basePage = datePickerState.engine.pageFor(now)
        datePickerState.engine.move(basePage, monthOffset)
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
        // Banner Card
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
                        Text("با سواپ/اسکرول افقی روی تقویم ماه را تغییر دهید", style = TextStyle(fontSize = 12.sp, color = Color(0xFF1A6CFF)))
                    }
                }
            }
        }

        // SWIPEABLE CALENDAR CONTAINER
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
                // Header Row with Month/Year Indicator + Scroll Buttons
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
                        IconButton(onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "ماه قبل", tint = Color(0xFF101828))
                        }
                        IconButton(onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "ماه بعد", tint = Color(0xFF101828))
                        }
                    }
                }

                // Weekday Labels Header
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

                // HORIZONTAL PAGER FOR CALENDAR GRID PAGES
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(240.dp)
                ) { pageIndex ->
                    val monthOffset = pageIndex - initialPagerPage
                    val basePage = datePickerState.engine.pageFor(LocalDate.now())
                    val monthPage = datePickerState.engine.move(basePage, monthOffset)
                    val dayCount = datePickerState.engine.daysInMonth(monthPage)
                    val offset = datePickerState.engine.weekdayOffset(monthPage, locale)
                    val cells = (1..offset).map { 0 } + (1..dayCount).toList()

                    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxSize()) {
                        items(cells) { day ->
                            if (day == 0) {
                                Spacer(Modifier.aspectRatio(1f))
                            } else {
                                val date = datePickerState.engine.dateAt(monthPage, day)
                                val allowed = datePickerState.isDateAllowed(date)
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
                                        .clickable(enabled = allowed) {
                                            datePickerState.selectDate(date)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = datePickerState.localizeDigits(day.toString()),
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                !allowed -> Color(0xFFD0D5DD)
                                                chosen -> Color.White
                                                else -> Color(0xFF101828)
                                            }
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // BOOKING SUMMARY CARD
        val start = datePickerState.selectedStart
        val end = datePickerState.selectedEnd

        val nights = remember(start, end) {
            if (start != null && end != null) {
                ChronoUnit.DAYS.between(start, end).toInt()
            } else {
                0
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
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
                    Column {
                        Text("تاریخ ورود", style = TextStyle(fontSize = 12.sp, color = Color(0xFF667085)))
                        Text(
                            if (start != null) {
                                val calDate = datePickerState.engine.toCalendarDate(start)
                                "${calDate.year}/${"%02d".format(calDate.month)}/${"%02d".format(calDate.day)}"
                            } else "انتخاب کنید",
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF101828))
                        )
                    }

                    if (nights > 0) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFEBF2FF)
                        ) {
                            Text(
                                "$nights شب اقامت",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A6CFF))
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("تاریخ خروج", style = TextStyle(fontSize = 12.sp, color = Color(0xFF667085)))
                        Text(
                            if (end != null) {
                                val calDate = datePickerState.engine.toCalendarDate(end)
                                "${calDate.year}/${"%02d".format(calDate.month)}/${"%02d".format(calDate.day)}"
                            } else "انتخاب کنید",
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF101828))
                        )
                    }
                }

                Button(
                    onClick = {},
                    enabled = start != null && end != null,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A6CFF))
                ) {
                    Text("تایید و ادامه رزرو", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White))
                }
            }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDeposit) Color(0xFFECFDF3) else Color(0xFFFEF3F2)),
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

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    amount,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF101828)
                    )
                )
                Text(time, style = TextStyle(fontSize = 12.sp, color = Color(0xFF667085)))
            }
        }
    }
}
