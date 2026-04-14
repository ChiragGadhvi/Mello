package com.chirag.mello.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.chirag.mello.R
import com.chirag.mello.ui.theme.*
import com.chirag.mello.ui.util.rememberSpeechRecognizer
import com.chirag.mello.viewmodel.JournalViewModel
import kotlinx.coroutines.launch

import com.chirag.mello.data.ALL_MOODS
@Composable
fun HomeScreen(viewModel: JournalViewModel, onNavigateToTimeline: () -> Unit, onNavigateToInsights: () -> Unit = {}) {
    var text by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(ALL_MOODS[0].key) }
    var isFocused by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val streak by viewModel.streak.collectAsState()

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
            val appended = if (text.isBlank()) result else "$text $result"
            text = appended.take(150)
        }
    )

    val inputScale by animateFloatAsState(
        targetValue = if (isFocused) 1.01f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "inputScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Premium Header with Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.icon),
                        contentDescription = "Mello Logo",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Mello",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = TextPrimary
                    )
                }
                StreakChip(streak = streak, onClick = onNavigateToInsights)
            }

            // Elegant Greeting
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
                Text(
                    text = "How are you feeling today?",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
            }

            // Input field with glassmorphic modern look
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(inputScale),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = BorderStroke(1.dp, SurfaceVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(if (isFocused) 6.dp else 0.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    TextField(
                        value = text,
                        onValueChange = { if (it.length <= 150) text = it },
                        placeholder = {
                            Text(
                                "Describe your day...",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp)
                            .onFocusChanged { isFocused = it.isFocused },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Lavender,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )

                    // Mic + char counter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 12.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (!hasAudioPermission) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else if (speech.isListening) {
                                    speech.stopListening()
                                } else {
                                    speech.startListening()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (speech.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = if (speech.isListening) "Stop listening" else "Speak",
                                tint = if (speech.isListening) Lavender else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (speech.isListening) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.2f, targetValue = 0.8f,
                                animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                                label = "pulseAlpha"
                            )
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Lavender.copy(alpha = pulseAlpha), CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Listening…",
                                style = MaterialTheme.typography.labelSmall,
                                color = Lavender
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${text.length}/150",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (text.length >= 140) Peach else TextSecondary
                        )
                    }
                }
            }

            // Mood selector with beautiful horizontal scrolling
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Select your mood",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                MoodSelector(selectedMood = selectedMood, onMoodSelected = { selectedMood = it })
            }

            // Premium Save button
            SaveButton(
                enabled = text.isNotBlank(),
                onClick = {
                    viewModel.saveEntry(text, selectedMood)
                    text = ""
                    showSuccess = true
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Success floating feedback
        AnimatedVisibility(
            visible = showSuccess,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { 50 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            LaunchedEffect(showSuccess) {
                kotlinx.coroutines.delay(2000)
                showSuccess = false
            }
            Row(
                modifier = Modifier
                    .background(Surface, RoundedCornerShape(24.dp))
                    .border(1.dp, Lavender.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Lavender, modifier = Modifier.size(20.dp))
                Text("Journal entry saved beautifully ✨", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun MoodSelector(selectedMood: String, onMoodSelected: (String) -> Unit) {
    val initialPage = ALL_MOODS.indexOfFirst { it.key == selectedMood }.takeIf { it >= 0 } ?: 0
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = initialPage,
        pageCount = { ALL_MOODS.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        onMoodSelected(ALL_MOODS[pagerState.currentPage].key)
    }

    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = {
                if (pagerState.currentPage > 0) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            },
            modifier = Modifier.alpha(if (pagerState.currentPage > 0) 1f else 0.3f)
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous", tint = TextPrimary)
        }

        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 72.dp),
            pageSpacing = (-16).dp
        ) { page ->
            val mood = ALL_MOODS[page]
            val isSelected = pagerState.currentPage == page
            
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.25f else 0.85f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "moodScale$page"
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(scale)
                    .clickable {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) SurfaceVariant else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = Lavender.copy(alpha = if (isSelected) 1f else 0f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(mood.drawableRes),
                        contentDescription = mood.label,
                        modifier = Modifier.fillMaxSize().padding(if (isSelected) 8.dp else 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = mood.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) Lavender else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        IconButton(
            onClick = {
                if (pagerState.currentPage < ALL_MOODS.size - 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier.alpha(if (pagerState.currentPage < ALL_MOODS.size - 1) 1f else 0.3f)
        ) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = TextPrimary)
        }
    }
}

@Composable
private fun SaveButton(enabled: Boolean, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "saveScale",
        finishedListener = { pressed = false }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .height(58.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = if (enabled)
                        listOf(Lavender, Mint)
                    else
                        listOf(SurfaceVariant, SurfaceVariant)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(enabled = enabled) {
                pressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Save Entry",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (enabled) Background else TextSecondary
        )
    }
}

@Composable
private fun StreakChip(streak: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .background(Surface, RoundedCornerShape(20.dp))
            .border(1.dp, SurfaceVariant, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("🔥", fontSize = 16.sp)
        Text(
            text = "$streak",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Peach
        )
    }
}
