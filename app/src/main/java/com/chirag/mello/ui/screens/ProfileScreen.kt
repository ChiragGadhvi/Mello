package com.chirag.mello.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.chirag.mello.notification.ReminderWorker
import com.chirag.mello.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mello_prefs", Context.MODE_PRIVATE) }

    // — State —
    var hour by remember { mutableStateOf(prefs.getInt("reminder_hour", 21)) }
    var minute by remember { mutableStateOf(prefs.getInt("reminder_minute", 0)) }
    var userName by remember { mutableStateOf(prefs.getString("user_name", "Awesome User") ?: "Awesome User") }
    var dpUriString by remember { mutableStateOf(prefs.getString("dp_uri", null)) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(userName) }

    // Load dp bitmap from URI
    val dpBitmap: ImageBitmap? = remember(dpUriString) {
        dpUriString?.let { uriStr ->
            try {
                val uri = Uri.parse(uriStr)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (e: Exception) { null }
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistable read permission so URI survives app restarts
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val uriStr = it.toString()
            prefs.edit().putString("dp_uri", uriStr).apply()
            dpUriString = uriStr
        }
    }

    val formattedTime = remember(hour, minute) {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm)
    }

    val timePickerDialog = TimePickerDialog(context, { _, h, m ->
        hour = h; minute = m
        prefs.edit().putInt("reminder_hour", h).putInt("reminder_minute", m).apply()
        ReminderWorker.schedule(context)
    }, hour, minute, false)

    // — Edit Name Dialog —
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = Surface,
            title = {
                Text("Edit Name", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    label = { Text("Your name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Lavender,
                        cursorColor = Lavender,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = nameInput.trim().ifBlank { "Awesome User" }
                        userName = trimmed
                        prefs.edit().putString("user_name", trimmed).apply()
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Lavender, contentColor = Background)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditNameDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                ) { Text("Cancel") }
            }
        )
    }

    MelloBackground {
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile & Settings",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // — Profile Card —
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = BorderStroke(1.dp, SurfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar with camera overlay
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        // Avatar circle
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(colors = listOf(Lavender, Peach))),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dpBitmap != null) {
                                Image(
                                    bitmap = dpBitmap,
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "M",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Background
                                )
                            }
                        }

                        // Camera badge
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Lavender),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Change photo",
                                tint = Background,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Name + edit button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        IconButton(
                            onClick = {
                                nameInput = userName
                                showEditNameDialog = true
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit name",
                                tint = Lavender,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        "Journaling everyday ✨",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // — Preferences Section —
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = BorderStroke(1.dp, SurfaceVariant.copy(alpha = 0.5f))
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Lavender.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = Lavender)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Daily Reminder", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                                Text(formattedTime, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }
                        TextButton(
                            onClick = { timePickerDialog.show() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Mint)
                        ) { Text("Change") }
                    }

                    HorizontalDivider(color = SurfaceVariant, thickness = 0.5.dp)

                    Button(
                        onClick = { ReminderWorker.showNotification(context) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant, contentColor = TextPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Peach, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Notification")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    } // end MelloBackground
}
