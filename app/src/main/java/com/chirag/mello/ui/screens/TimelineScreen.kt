package com.chirag.mello.ui.screens

import androidx.compose.ui.res.painterResource
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
import com.chirag.mello.data.ALL_MOODS
import com.chirag.mello.data.JournalEntry
import com.chirag.mello.ui.theme.*
import com.chirag.mello.ui.util.rememberSpeechRecognizer
import com.chirag.mello.viewmodel.JournalViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(viewModel: JournalViewModel, onBack: () -> Unit) {
    val entries by viewModel.entries.collectAsState()

    var selectedEntry     by remember { mutableStateOf<JournalEntry?>(null) }
    var showBottomSheet   by remember { mutableStateOf(false) }
    var showEditDialog    by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var editText          by remember { mutableStateOf("") }
    val sheetState        = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope    = rememberCoroutineScope()

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
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
            ) {
                itemsIndexed(entries, key = { _, e -> e.id }) { index, entry ->
                    AnimatedMoodGridCard(
                        entry = entry,
                        index = index,
                        onClick = {
                            selectedEntry = entry
                            editText = entry.text
                            showBottomSheet = true
                        }
                    )
                }
            }
        }
    }

    // Bottom sheet — entry detail
    if (showBottomSheet) {
        selectedEntry?.let { entry ->
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    showDeleteConfirm = false
                },
                sheetState = sheetState,
                containerColor = Surface,
                tonalElevation = 0.dp,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 8.dp)
                            .width(40.dp)
                            .height(4.dp)
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
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            showBottomSheet = false
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

    // Edit dialog
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
                    Text(
                        "Edit Journal Entry",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
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
                            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                showBottomSheet = false
                                selectedEntry = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Lavender,
                            contentColor = Background
                        )
                    ) {
                        Text("Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEditDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Surface
            )
        }
    }
}

@Composable
private fun AnimatedMoodGridCard(entry: JournalEntry, index: Int, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(minOf(index, 10) * 50L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(
            animationSpec = tween(400, easing = LinearOutSlowInEasing),
            initialOffsetY = { it / 4 }
        )
    ) {
        MoodGridCard(entry = entry, onClick = onClick)
    }
}

@Composable
private fun MoodGridCard(entry: JournalEntry, onClick: () -> Unit) {
    val mood = remember(entry.mood) { ALL_MOODS.find { it.key == entry.mood } }
    val shortDate = remember(entry.timestamp) {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(entry.timestamp))
    }
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, SurfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(6.dp), contentAlignment = Alignment.Center) {
            if (mood != null) {
                Image(
                    painter = painterResource(mood.drawableRes),
                    contentDescription = mood.label,
                    modifier = Modifier
                        .fillMaxSize(0.75f)
                        .padding(bottom = 14.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = shortDate,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

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
        // Mood image + label
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

        // Journal text
        Text(
            text = entry.text,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth()
        )

        // Date row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = TextSecondary
            )
            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Edit + Delete buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Edit button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .background(
                        brush = Brush.horizontalGradient(listOf(Mint, Lavender)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Background,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Edit",
                        color = Background,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Delete button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Peach.copy(alpha = 0.3f), Peach.copy(alpha = 0.15f))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(1.dp, Peach.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { onDeleteClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Peach,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Delete",
                        color = Peach,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Delete this entry?",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        "This cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDeleteDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onDeleteConfirm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Peach,
                                contentColor = Background
                            )
                        ) {
                            Text("Delete", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
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
            Text("🌙", fontSize = 48.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Your journal is empty",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Write your first entry to see it here.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
