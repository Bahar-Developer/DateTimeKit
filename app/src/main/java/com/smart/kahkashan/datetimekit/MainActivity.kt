package com.smart.kahkashan.datetimekit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smart.kahkashan.date_time_kit.CalendarDate
import com.smart.kahkashan.date_time_kit.CalendarSystem
import com.smart.kahkashan.date_time_kit.DatePicker
import com.smart.kahkashan.date_time_kit.SelectionMode
import com.smart.kahkashan.datetimekit.ui.theme.DateTimeKitTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DateTimeKitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Gallery()
                }
            }
        }
    }
}


@Composable
private fun Gallery() {
    data class Preset(val label: String, val locale: String, val calendar: CalendarSystem)
    val presets = listOf(
        Preset("فارسی · شمسی", "fa-IR", CalendarSystem.JALALI),
        Preset("English · Gregorian", "en-US", CalendarSystem.GREGORIAN),
        Preset("العربية · أم القرى", "ar-SA", CalendarSystem.ISLAMIC_UMALQURA),
        Preset("العربية · مدني", "ar-EG", CalendarSystem.ISLAMIC_CIVIL),
        Preset("Deutsch · Gregorian", "de-DE", CalendarSystem.GREGORIAN),
        Preset("Français · Gregorian", "fr-FR", CalendarSystem.GREGORIAN),
        Preset("Español · Gregorian", "es-ES", CalendarSystem.GREGORIAN),
        Preset("हिन्दी · Gregorian", "hi-IN", CalendarSystem.GREGORIAN),
        Preset("日本語 · Gregorian", "ja-JP", CalendarSystem.GREGORIAN),
        Preset("简体中文 · Gregorian", "zh-CN", CalendarSystem.GREGORIAN),
        Preset("فارسی · میلادی", "fa-IR", CalendarSystem.GREGORIAN),
    )
    var preset by remember { mutableStateOf(presets.first()) }
    var mode by remember { mutableStateOf(SelectionMode.RANGE) }
    var chosen by remember { mutableStateOf<List<CalendarDate>>(emptyList()) }

    Scaffold { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("DateTimeKit", style = MaterialTheme.typography.headlineMedium)
            Text("Locale, calendar system and selection mode can be configured independently.")
            presets.forEach { option ->
                OutlinedButton(
                    onClick = { preset = option; chosen = emptyList() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (preset == option) "✓ ${option.label}" else option.label) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectionMode.values().forEach { option ->
                    FilterChip(
                        selected = mode == option,
                        onClick = { mode = option; chosen = emptyList() },
                        label = { Text(option.name) }
                    )
                }
            }
            DatePicker(
                locale = Locale.forLanguageTag(preset.locale),
                calendarSystem = preset.calendar,
                selectionMode = mode,
                onSelectionChanged = { chosen = it }
            )
            Text("Selected dates", style = MaterialTheme.typography.titleMedium)
            Text(
                chosen.ifEmpty { emptyList() }.joinToString("\n") {
                    "${it.year}/${it.month}/${it.day} · ISO ${it.isoDate}"
                }.ifBlank { "No date selected" }
            )
        }
    }
}
