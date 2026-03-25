package com.chirag.mello.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.chirag.mello.data.JournalEntry
import com.chirag.mello.ui.theme.*
import com.chirag.mello.viewmodel.JournalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(viewModel: JournalViewModel, onBack: () -> Unit) {
    val entries by viewModel.entries.collectAsState()

    // Wrapper removed, background relies on nav graph
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
                ) {
                    itemsIndexed(entries, key = { _, e -> e.id }) { index, entry ->
                        AnimatedEntryCard(
                            entry = entry,
                            index = index,
                            onUpdate = { viewModel.updateEntry(it) },
                            onDelete = { viewModel.deleteEntry(it) }
                        )
                    }
                }
            }
        }
}

@Composable
private fun AnimatedEntryCard(entry: JournalEntry, index: Int, onUpdate: (JournalEntry) -> Unit, onDelete: (JournalEntry) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 50L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(
            animationSpec = tween(400, easing = LinearOutSlowInEasing),
            initialOffsetY = { it / 4 }
        )
    ) {
        EntryCard(entry = entry, onUpdate = onUpdate, onDelete = onDelete)
    }
}

@Composable
private fun EntryCard(entry: JournalEntry, onUpdate: (JournalEntry) -> Unit, onDelete: (JournalEntry) -> Unit) {
    val dateStr = remember(entry.timestamp) {
        SimpleDateFormat("EEE, MMM d · h:mm a", Locale.getDefault())
            .format(Date(entry.timestamp))
    }

    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(entry.text) }

    if (isEditing) {
        AlertDialog(
            onDismissRequest = { isEditing = false },
            title = { Text("Edit Journal Entry", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    onUpdate(entry.copy(text = editText))
                    isEditing = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = Lavender, contentColor = Background)) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditing = false }, colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
                    Text("Cancel")
                }
            },
            containerColor = Surface
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, SurfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Circular Mood Bubble with Gradient
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Lavender.copy(alpha=0.15f), Mint.copy(alpha=0.15f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = entry.moodEmoji, fontSize = 24.sp)
                }
                
                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = entry.text,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info, 
                            contentDescription = null, 
                            modifier = Modifier.size(14.dp), 
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SurfaceVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Professional side-by-side action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { isEditing = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = Mint)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { onDelete(entry) },
                    colors = ButtonDefaults.textButtonColors(contentColor = Peach)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete")
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
