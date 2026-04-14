package com.chirag.mello.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.chirag.mello.data.ALL_MOODS
import com.chirag.mello.data.Mood
import com.chirag.mello.ui.theme.*
import com.chirag.mello.viewmodel.JournalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: JournalViewModel) {
    val entries     by viewModel.entries.collectAsState()
    val streak      by viewModel.streak.collectAsState()
    val totalEntries by viewModel.totalEntries.collectAsState()
    val moodCounts  by viewModel.moodCounts.collectAsState()
    val heatmapData by viewModel.heatmapData.collectAsState()

    val mostCommonMood = remember(moodCounts) {
        if (moodCounts.isEmpty()) null
        else ALL_MOODS.find { it.key == moodCounts.maxByOrNull { (_, v) -> v }?.key }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Insights",
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
            EmptyInsightsState(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                StatsHeaderRow(
                    totalEntries = totalEntries,
                    streak = streak,
                    mostCommonMood = mostCommonMood
                )
                MoodDistributionSection(moodCounts = moodCounts)
                CalendarHeatmapSection(heatmapData = heatmapData)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

// ── Stats Row ─────────────────────────────────────────────────────────────────

@Composable
private fun StatsHeaderRow(totalEntries: Int, streak: Int, mostCommonMood: Mood?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(modifier = Modifier.weight(1f), label = "Entries", value = "$totalEntries")
        StatCard(modifier = Modifier.weight(1f), label = "Streak",  value = "$streak 🔥")
        MoodStatCard(modifier = Modifier.weight(1f), mood = mostCommonMood)
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        border = BorderStroke(1.dp, SurfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Lavender,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MoodStatCard(modifier: Modifier = Modifier, mood: Mood?) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        border = BorderStroke(1.dp, SurfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (mood != null) {
                Image(
                    painter = painterResource(mood.drawableRes),
                    contentDescription = mood.label,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = mood.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.titleLarge,
                    color = Lavender
                )
                Text(
                    text = "Top Mood",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

// ── Mood Distribution ──────────────────────────────────────────────────────────

@Composable
private fun MoodDistributionSection(moodCounts: Map<String, Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader("Mood Distribution")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MoodBarChart(moodCounts = moodCounts)
            }
        }
    }
}

@Composable
private fun MoodBarChart(moodCounts: Map<String, Int>) {
    val maxCount = (moodCounts.values.maxOrNull() ?: 0).coerceAtLeast(1)

    ALL_MOODS.forEach { mood ->
        val count = moodCounts[mood.key] ?: 0
        val targetFraction = count.toFloat() / maxCount
        val animatedFraction by animateFloatAsState(
            targetValue = targetFraction,
            animationSpec = tween(700, easing = FastOutSlowInEasing),
            label = "bar_${mood.key}"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = mood.label,
                modifier = Modifier.width(52.dp),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Track
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Surface, RoundedCornerShape(6.dp))
                )
                // Filled bar
                val filledFraction = animatedFraction.coerceAtLeast(if (count > 0) 0.04f else 0f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(filledFraction)
                        .fillMaxHeight()
                        .background(mood.color, RoundedCornerShape(6.dp))
                )
            }
            Text(
                text = "$count",
                modifier = Modifier.width(24.dp),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.End
            )
        }
    }
}

// ── Calendar Heatmap ──────────────────────────────────────────────────────────

private data class HeatmapDay(
    val midnightMillis: Long,
    val isToday: Boolean,
    val moodKey: String?
)

@Composable
private fun CalendarHeatmapSection(heatmapData: Map<Long, String>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader("Last 3 Months")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                CalendarHeatmap(heatmapData = heatmapData)
            }
        }
    }
}

@Composable
private fun CalendarHeatmap(heatmapData: Map<Long, String>) {
    val days  = remember(heatmapData) { buildHeatmapDays(heatmapData) }
    val weeks = remember(days) { days.chunked(7) }

    val dowLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Day-of-week labels — offset by 20dp to clear the month label row
        Column(
            modifier = Modifier.padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            dowLabels.forEach { label ->
                Box(
                    modifier = Modifier.size(width = 14.dp, height = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = TextSecondary.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Scrollable weeks grid (oldest left → newest right)
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            weeks.forEachIndexed { weekIdx, week ->
                // Show month label when month changes (or on the first column)
                val monthLabel = run {
                    val thisMonth = Calendar.getInstance()
                        .apply { timeInMillis = week[0].midnightMillis }
                        .get(Calendar.MONTH)
                    val prevMonth = if (weekIdx == 0) -1 else
                        Calendar.getInstance()
                            .apply { timeInMillis = weeks[weekIdx - 1][0].midnightMillis }
                            .get(Calendar.MONTH)
                    if (weekIdx == 0 || thisMonth != prevMonth)
                        SimpleDateFormat("MMM", Locale.getDefault())
                            .format(Date(week[0].midnightMillis))
                    else ""
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = TextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.height(16.dp).width(28.dp),
                        textAlign = TextAlign.Center
                    )
                    week.forEach { day ->
                        DayCell(day = day)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: HeatmapDay) {
    val mood = remember(day.moodKey) { ALL_MOODS.find { it.key == day.moodKey } }
    val bgColor = if (mood != null) mood.color.copy(alpha = 0.75f)
                  else Surface.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .size(28.dp)
            .background(bgColor, RoundedCornerShape(5.dp))
            .then(
                if (day.isToday)
                    Modifier.border(1.5.dp, Lavender, RoundedCornerShape(5.dp))
                else
                    Modifier
            )
    )
}

private fun buildHeatmapDays(heatmapData: Map<Long, String>): List<HeatmapDay> {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayMillis = today.timeInMillis

    // Go back 90 days, then snap to the preceding Monday
    val startCal = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -90) }
    while (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
        startCal.add(Calendar.DAY_OF_YEAR, -1)
    }

    val days = mutableListOf<HeatmapDay>()
    val cursor = startCal.clone() as Calendar

    // Fill day-by-day until we've passed today AND completed the current week
    while (cursor.timeInMillis <= todayMillis || days.size % 7 != 0) {
        val millis = cursor.timeInMillis
        days.add(
            HeatmapDay(
                midnightMillis = millis,
                isToday = millis == todayMillis,
                moodKey = heatmapData[millis]
            )
        )
        cursor.add(Calendar.DAY_OF_YEAR, 1)
    }
    return days
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TextPrimary
    )
}

@Composable
private fun EmptyInsightsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SurfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text("📊", fontSize = 48.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "No insights yet",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Start journaling to see your mood trends here.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
