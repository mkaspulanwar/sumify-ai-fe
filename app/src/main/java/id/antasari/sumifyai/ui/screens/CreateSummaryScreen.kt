package id.antasari.sumifyai.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import id.antasari.sumifyai.ui.theme.BackgroundLight
import id.antasari.sumifyai.ui.theme.BorderLight
import id.antasari.sumifyai.ui.theme.ColorCompleted
import id.antasari.sumifyai.ui.theme.AccentPurple
import id.antasari.sumifyai.ui.components.SumifyTopAppBar
import id.antasari.sumifyai.ui.components.SumifyTopBarContentColor
import id.antasari.sumifyai.ui.theme.PrimaryIndigo
import id.antasari.sumifyai.ui.theme.SecondaryTeal
import id.antasari.sumifyai.ui.theme.SurfaceLight
import id.antasari.sumifyai.ui.theme.SurfaceLightCard
import id.antasari.sumifyai.ui.theme.TextMuted
import id.antasari.sumifyai.ui.theme.TextPrimary
import id.antasari.sumifyai.ui.theme.TextSecondary
import id.antasari.sumifyai.ui.viewmodel.MainViewModel
import id.antasari.sumifyai.ui.viewmodel.UploadState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateSummaryScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onUploadSuccess: (String) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("id") }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val selectedAudioName by viewModel.selectedAudioName.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordDurationSeconds by viewModel.recordDurationSeconds.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSelectedFile()
            viewModel.cancelActiveRecording()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectAudioFile(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startRecording()
        } else {
            Toast.makeText(context, "Permission for Audio Recording denied", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            SumifyTopAppBar(
                title = { Text("New Summary", color = SumifyTopBarContentColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateBack()
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = SumifyTopBarContentColor)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text("Meeting Details", color = PrimaryIndigo, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(10.dp))

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Meeting Title", color = TextSecondary) },
                placeholder = { Text("e.g. Project Sync meeting", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryIndigo,
                    unfocusedBorderColor = BorderLight,
                    cursorColor = PrimaryIndigo
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)", color = TextSecondary) },
                placeholder = { Text("Write brief notes...", color = TextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryIndigo,
                    unfocusedBorderColor = BorderLight,
                    cursorColor = PrimaryIndigo
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Language Selector
            var languageExpanded by remember { mutableStateOf(false) }
            val languageLabel = when (language) {
                "id" -> "Indonesian (Bahasa Indonesia)"
                "en" -> "English"
                "auto" -> "Auto Detect Language"
                else -> "Indonesian"
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = languageLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Audio Language", color = TextSecondary) },
                    trailingIcon = {
                        IconButton(onClick = { languageExpanded = true }) {
                            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown", tint = PrimaryIndigo)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { languageExpanded = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = BorderLight
                    )
                )

                DropdownMenu(
                    expanded = languageExpanded,
                    onDismissRequest = { languageExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(SurfaceLight)
                ) {
                    DropdownMenuItem(
                        text = { Text("Indonesian (Bahasa Indonesia)", color = TextPrimary) },
                        onClick = {
                            language = "id"
                            languageExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("English", color = TextPrimary) },
                        onClick = {
                            language = "en"
                            languageExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Auto-Detect Language", color = PrimaryIndigo) },
                        onClick = {
                            language = "auto"
                            languageExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Audio Source", color = PrimaryIndigo, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(10.dp))

            // Tab layout
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = BorderLight.copy(alpha = 0.2f),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = PrimaryIndigo
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, BorderLight), RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = {
                        if (!isRecording) selectedTabIndex = 0
                    },
                    text = { Text("Upload File", fontWeight = FontWeight.Bold, color = if (selectedTabIndex == 0) PrimaryIndigo else TextSecondary) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Record Live", fontWeight = FontWeight.Bold, color = if (selectedTabIndex == 1) PrimaryIndigo else TextSecondary) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Contents
            if (selectedTabIndex == 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 184.dp)
                        .clickable(enabled = selectedAudioName == null) { filePickerLauncher.launch("audio/*") }
                        .border(BorderStroke(1.5.dp, PrimaryIndigo.copy(alpha = 0.22f)), shape = RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLightCard),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (selectedAudioName == null) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(PrimaryIndigo.copy(alpha = 0.06f), CircleShape)
                                    .border(BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.24f)), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(PrimaryIndigo.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add audio file",
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Choose or Drop Audio File",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Select a meeting recording from your device",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AudioFormatBadge("MP3")
                                AudioFormatBadge("M4A")
                                AudioFormatBadge("WAV")
                                AudioFormatBadge("AAC")
                                AudioFormatBadge("FLAC")
                                AudioFormatBadge("OGG")
                                AudioFormatBadge("WEBM")
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PrimaryIndigo.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
                                        .border(
                                            BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.14f)),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(ColorCompleted.copy(alpha = 0.12f), CircleShape)
                                            .border(BorderStroke(1.dp, ColorCompleted.copy(alpha = 0.28f)), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "File selected",
                                            tint = ColorCompleted,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Audio file selected",
                                            fontSize = 11.sp,
                                            color = ColorCompleted,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = selectedAudioName ?: "",
                                            fontSize = 14.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = 18.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = { viewModel.clearSelectedFile() },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color.Red.copy(alpha = 0.08f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear selected file",
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { filePickerLauncher.launch("audio/*") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLight),
                                    border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.35f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Change file",
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Change File",
                                        color = PrimaryIndigo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, BorderLight), shape = RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLightCard),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isRecording) {
                            // Pulsing Ring Animation (Polished wave sizing)
                            val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 0.9f,
                                targetValue = 1.1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .scale(scale)
                                    .background(AccentPurple.copy(alpha = 0.12f), CircleShape)
                                    .border(BorderStroke(1.5.dp, AccentPurple), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color.Red, CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Recording Live Audio...",
                                fontSize = 13.sp,
                                color = AccentPurple,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = viewModel.formatDuration(recordDurationSeconds),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = { viewModel.stopRecording() },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Stop & Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = { viewModel.cancelActiveRecording() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    border = BorderStroke(1.dp, BorderLight),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cancel", color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                        } else {
                            if (selectedAudioName != null) {
                                Text(
                                    text = "Recording Captured!",
                                    fontSize = 14.sp,
                                    color = ColorCompleted,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedAudioName ?: "",
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Button(
                                        onClick = {
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                viewModel.startRecording()
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BorderLight),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Re-record", color = PrimaryIndigo, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Button(
                                        onClick = { viewModel.clearSelectedFile() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Clear", color = Color.Red, fontSize = 13.sp)
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(BorderLight.copy(alpha = 0.3f), CircleShape)
                                        .border(BorderStroke(1.5.dp, PrimaryIndigo), CircleShape)
                                        .clickable {
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                viewModel.startRecording()
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(PrimaryIndigo, CircleShape)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Tap to Record Meeting",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Records audio direct from device mic",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            val isUploading = uploadState is UploadState.Uploading
            
            Button(
                onClick = {
                    viewModel.uploadAudio(
                        title = title,
                        description = description,
                        language = language,
                        onUploadSuccess = onUploadSuccess
                    )
                },
                enabled = !isUploading && !isRecording && selectedAudioName != null && title.trim().isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryIndigo,
                    disabledContainerColor = BorderLight.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Uploading Audio...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        text = "Generate AI Summary & PDF",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (title.trim().isNotEmpty() && selectedAudioName != null) Color.White else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun AudioFormatBadge(label: String) {
    Box(
        modifier = Modifier
            .background(SecondaryTeal.copy(alpha = 0.10f), RoundedCornerShape(999.dp))
            .border(BorderStroke(1.dp, SecondaryTeal.copy(alpha = 0.24f)), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = SecondaryTeal,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
