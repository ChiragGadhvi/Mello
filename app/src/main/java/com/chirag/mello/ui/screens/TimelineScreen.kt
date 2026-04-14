package com.chirag.mello.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import com.chirag.mello.data.ALL_MOODS
import com.chirag.mello.data.JournalEntry
import com.chirag.mello.ui.theme.*
import com.chirag.mello.ui.util.rememberSpeechRecognizer
import com.chirag.mello.viewmodel.JournalViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ── Data model ────────────────────────────────────────────────────────────────

private data class CalendarDay(
    val dayOfMonth: Int,
    val midnightMillis: Long,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val entries: List<JournalEntry>
)

private fun buildCalendarDays(
    year: Int,
    month: Int,
    entriesByDay: Map<Long, List<JournalEntry>>
): List<CalendarDay> {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
    }
    val todayMillis = today.timeInMillis

    fun midnightOf(cal: Calendar): Long {
        return Calendar.getInstance().apply {
            set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val firstOfMonth = Calendar.getInstance().apply {
        set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0)
    }
    val daysInMonth = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    // Sunday=1 … Saturday=7; we want Sunday as column 0
    val startOffset = firstOfMonth.get(Calendar.DAY_OF_WEEK) - 1

    val days = mutableListOf<CalendarDay>()

    // Leading days from previous month
    if (startOffset > 0) {
        val prevCal = (firstOfMonth.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -startOffset) }
        repeat(startOffset) {
            val millis = midnightOf(prevCal)
            days.add(CalendarDay(prevCal.get(Calendar.DAY_OF_MONTH), millis, false, millis == todayMillis, emptyList()))
            prevCal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    // Current month days
    val cur = (firstOfMonth.clone() as Calendar)
    repeat(daysInMonth) {
        val millis = midnightOf(cur)
        days.add(CalendarDay(cur.get(Calendar.DAY_OF_MONTH), millis, true, millis == todayMillis,
            entriesByDay[millis] ?: emptyList()))
        cur.add(Calendar.DAY_OF_MONTH, 1)
    }

    // Trailing days to fill final row
    val remainder = days.size % 7
    if (remainder != 0) {
        val trail = cur.clone() as Calendar
        repeat(7 - remainder) {
            val millis = midnightOf(trail)
            days.add(CalendarDay(trail.get(Calendar.DAY_OF_MONTH), millis, false, millis == todayMillis, emptyList()))
            trail.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    return days
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(viewModel: JournalViewModel, onBack: () -> Unit) {
    val entries      by viewModel.entries.collectAsState()
    val entriesByDay by viewModel.entriesByDay.collectAsState()

    // Month the calendar is showing (starts at current month)
    var displayedMonth by remember { mutableStateOf(Calendar.getInstance()) }
    val currentMonth   = remember { Calendar.getInstance() }

    // Sheet / dialog state
    var selectedDayEntries by remember { mutableStateOf<List<JournalEntry>>(emptyList()) }
    var selectedDayLabel   by remember { mutableStateOf("") }
    var selectedEntry      by remember { mutableStateOf<JournalEntry?>(null) }
    var showDaySheet       by remember { mutableStateOf(false) }
    var showEntrySheet     by remember { mutableStateOf(false) }
    var showEditDialog     by remember { mutableStateOf(false) }
    var showDeleteConfirm  by remember { mutableStateOf(false) }
    var editText           by remember { mutableStateOf("") }

    val daySheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val entrySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope  = rememberCoroutineScope()

    // Build calendar grid
    val calendarDays = remember(displayedMonth, entriesByDay) {
        buildCalendarDays(
            displayedMonth.get(Calendar.YEAR),
            displayedMonth.get(Calendar.MONTH),
            entriesByDay
        )
    }

    val isCurrentMonth = remember(displayedMonth) {
        displayedMonth.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
        displayedMonth.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Journal",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { padding ->
        if (entries.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Month navigation
                MonthNavigationHeader(
                    displayedMonth = displayedMonth,
                    isCurrentMonth = isCurrentMonth,
                    onPrev = {
                        displayedMonth = (displayedMonth.clone() as Calendar)
                            .apply { add(Calendar.MONTH, -1) }
                    },
                    onNext = {
                        displayedMonth = (displayedMonth.clone() as Calendar)
                            .apply { add(Calendar.MONTH, 1) }
                    }
                )

                Spacer(Modifier.height(12.dp))

                // Day-of-week header
                WeekDayLabels()

                Spacer(Modifier.height(8.dp))

                // Calendar grid
                CalendarGrid(
                    days = calendarDays,
                    onDayClick = { day ->
                        if (day.entries.isNotEmpty()) {
                            selectedDayEntries = day.entries.sortedBy { it.timestamp }
                            selectedDayLabel = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                                .format(Date(day.midnightMillis))
                            showDaySheet = true
                        }
                    }
                )
            }
        }
    }

    // ── Day entries sheet ─────────────────────────────────────────────────────
    if (showDaySheet) {
        ModalBottomSheet(
            onDismissRequest = { showDaySheet = false },
            sheetState = daySheetState,
            containerColor = Surface,
            tonalElevation = 0.dp,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .width(40.dp).height(4.dp)
                        .background(SurfaceVariant, RoundedCornerShape(2.dp))
                )
            }
        ) {
            DayEntriesSheetContent(
                entries = selectedDayEntries,
                dayLabel = selectedDayLabel,
                onEntryClick = { entry ->
                    coroutineScope.launch { daySheetState.hide() }.invokeOnCompletion {
                        showDaySheet = false
                        selectedEntry = entry
                        editText = entry.text
                        showEntrySheet = true
                    }
                }
            )
        }
    }

    // ── Entry detail sheet ────────────────────────────────────────────────────
    if (showEntrySheet) {
        selectedEntry?.let { entry ->
            ModalBottomSheet(
                onDismissRequest = {
                    showEntrySheet = false
                    showDeleteConfirm = false
                },
                sheetState = entrySheetState,
                containerColor = Surface,
                tonalElevation = 0.dp,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 8.dp)
                            .width(40.dp).height(4.dp)
                            .background(SurfaceVariant, RoundedCornerShape(2.dp))
                    )
                }
            ) {
                EntryDetailSheetContent(
                    entry = entry,
                    showDeleteConfirm = showDeleteConfirm,
                    onEditClick = { showEditDialog = true },
                    onDeleteClick = { showDeleteConfirm = true },
                    onDeleteConfirm = {
                        coroutineScope.launch { entrySheetState.hide() }.invokeOnCompletion {
                            showEntrySheet = false
                            showDeleteConfirm = false
                            viewModel.deleteEntry(entry)
                            selectedEntry = null
                        }
                    },
                    onDeleteDismiss = { showDeleteConfirm = false }
                )
            }
        }
    }

    // ── Edit dialog ───────────────────────────────────────────────────────────
    if (showEditDialog) {
        selectedEntry?.let { entry ->
            val context = LocalContext.current
            var hasAudioPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED
                )
            }
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted -> hasAudioPermission = granted }
            val speech = rememberSpeechRecognizer(
                context = context,
                onResult = { result ->
                    val appended = if (editText.isBlank()) result else "$editText $result"
                    editText = appended.take(150)
                }
            )

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = {
                    Text("Edit Journal Entry", fontWeight = FontWeight.Bold, color = TextPrimary)
                },
                text = {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { if (it.length <= 150) editText = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Lavender,
                            cursorColor = Lavender,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (!hasAudioPermission) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else if (speech.isListening) {
                                    speech.stopListening()
                                } else {
                                    speech.startListening()
                                }
                            }) {
                                Icon(
                                    imageVector = if (speech.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Voice input",
                                    tint = if (speech.isListening) Lavender else TextSecondary
                                )
                            }
                        },
                        supportingText = {
                            Text(
                                "${editText.length}/150",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (editText.length >= 140) Peach else TextSecondary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateEntry(entry.copy(text = editText))
                            showEditDialog = false
                            coroutineScope.launch { entrySheetState.hide() }.invokeOnCompletion {
                                showEntrySheet = false
                                selectedEntry = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Lavender, contentColor = Background)
                    ) { Text("Save Changes") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEditDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                    ) { Text("Cancel") }
                },
                containerColor = Surface
            )
        }
    }
}

// ── Calendar composables ──────────────────────────────────────────────────────

@Composable
private fun MonthNavigationHeader(
    displayedMonth: Calendar,
    isCurrentMonth: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val label = remember(displayedMonth) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(displayedMonth.time)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month", tint = Lavender)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        IconButton(onClick = onNext, enabled = !isCurrentMonth) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Next month",
                tint = if (isCurrentMonth) TextSecondary.copy(alpha = 0.3f) else Lavender
            )
        }
    }
}

@Composable
private fun WeekDayLabels() {
    val labels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarGrid(days: List<CalendarDay>, onDayClick: (CalendarDay) -> Unit) {
    val weeks = days.chunked(7)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                week.forEach { day ->
                    DayCell(
                        day = day,
                        modifier = Modifier.weight(1f),
                        onClick = { onDayClick(day) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: CalendarDay, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val dominantMood = remember(day.entries) {
        if (day.entries.isEmpty()) null
        else ALL_MOODS.find {
            it.key == day.entries.groupBy { e -> e.mood }.maxByOrNull { (_, v) -> v.size }?.key
        }
    }

    val bgColor = when {
        !day.isCurrentMonth          -> androidx.compose.ui.graphics.Color.Transparent
        dominantMood != null         -> dominantMood.color.copy(alpha = 0.18f)
        else                         -> Surface.copy(alpha = 0.6f)
    }
    val numberColor = when {
        !day.isCurrentMonth -> TextSecondary.copy(alpha = 0.25f)
        day.isToday         -> Lavender
        else                -> TextPrimary
    }

    Box(
        modifier = modifier
            .aspectRatio(0.72f)
            .background(bgColor, RoundedCornerShape(12.dp))
            .then(
                if (day.isToday)
                    Modifier.border(1.5.dp, Lavender, RoundedCornerShape(12.dp))
                else Modifier
            )
            .then(
                if (day.isCurrentMonth && day.entries.isNotEmpty())
                    Modifier.clickable { onClick() }
                else Modifier
            )
    ) {
        // Day number — top start
        Text(
            text = "${day.dayOfMonth}",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = numberColor,
            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
        )

        // Mood image — centered
        if (dominantMood != null && day.isCurrentMonth) {
            Image(
                painter = painterResource(dominantMood.drawableRes),
                contentDescription = dominantMood.label,
                modifier = Modifier
                    .fillMaxSize(0.72f)
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )
        }
    }
}

// ── Day entries bottom sheet ──────────────────────────────────────────────────

@Composable
private fun DayEntriesSheetContent(
    entries: List<JournalEntry>,
    dayLabel: String,
    onEntryClick: (JournalEntry) -> Unit
) {
    val dominantMood = remember(entries) {
        if (entries.isEmpty()) null
        else ALL_MOODS.find {
            it.key == entries.groupBy { e -> e.mood }.maxByOrNull { (_, v) -> v.size }?.key
        }
    }
    val entryWord = if (entries.size == 1) "entry" else "entries"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (dominantMood != null) {
                Image(
                    painter = painterResource(dominantMood.drawableRes),
                    contentDescription = dominantMood.label,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Column {
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "${entries.size} $entryWord",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = SurfaceVariant,
            thickness = 0.5.dp
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries, key = { it.id }) { entry ->
                EntryListRow(entry = entry, onClick = { onEntryClick(entry) })
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun EntryListRow(entry: JournalEntry, onClick: () -> Unit) {
    val mood = remember(entry.mood) { ALL_MOODS.find { it.key == entry.mood } }
    val timeStr = remember(entry.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(entry.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time
            Text(
                text = timeStr,
                modifier = Modifier.width(52.dp),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            // Mood image
            if (mood != null) {
                Image(
                    painter = painterResource(mood.drawableRes),
                    contentDescription = mood.label,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Text preview
            Column(modifier = Modifier.weight(1f)) {
                if (mood != null) {
                    Text(
                        text = mood.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Lavender
                    )
                }
                Text(
                    text = entry.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── Entry detail sheet content (preserved from original) ─────────────────────

@Composable
private fun EntryDetailSheetContent(
    entry: JournalEntry,
    showDeleteConfirm: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit
) {
    val mood = remember(entry.mood) { ALL_MOODS.find { it.key == entry.mood } }
    val dateStr = remember(entry.timestamp) {
        SimpleDateFormat("EEEE, MMMM d · h:mm a", Locale.getDefault())
            .format(Date(entry.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (mood != null) {
            Image(
                painter = painterResource(mood.drawableRes),
                contentDescription = mood.label,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = mood.label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Lavender
            )
        }

        HorizontalDivider(color = SurfaceVariant, thickness = 0.5.dp)

        Text(
            text = entry.text,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null,
                modifier = Modifier.size(14.dp), tint = TextSecondary)
            Text(text = dateStr, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Edit button
            Box(
                modifier = Modifier
                    .weight(1f).height(50.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Mint, Lavender)),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Edit, "Edit", tint = Background, modifier = Modifier.size(18.dp))
                    Text("Edit", color = Background,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            // Delete button
            Box(
                modifier = Modifier
                    .weight(1f).height(50.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Peach.copy(alpha = 0.3f), Peach.copy(alpha = 0.15f))),
                        RoundedCornerShape(20.dp)
                    )
                    .border(1.dp, Peach.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { onDeleteClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = Peach, modifier = Modifier.size(18.dp))
                    Text("Delete", color = Peach,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Inline delete confirmation
        AnimatedVisibility(visible = showDeleteConfirm) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                border = BorderStroke(1.dp, Peach.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Delete this entry?",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary)
                    Text("This cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onDeleteDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = onDeleteConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = Peach, contentColor = Background)) {
                            Text("Delete", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape)
                .background(SurfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) { Text("🌙", fontSize = 48.sp) }
        Spacer(Modifier.height(24.dp))
        Text("Your journal is empty",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("Write your first entry to see it here.",
            style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}
