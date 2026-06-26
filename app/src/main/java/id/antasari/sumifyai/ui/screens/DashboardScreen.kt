package id.antasari.sumifyai.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.antasari.sumifyai.data.model.MeetingLocal
import id.antasari.sumifyai.ui.components.BrandLogo
import id.antasari.sumifyai.ui.components.SumifyTopAppBar
import id.antasari.sumifyai.ui.components.SumifyTopBarContentColor
import id.antasari.sumifyai.ui.theme.BackgroundLight
import id.antasari.sumifyai.ui.theme.BorderLight
import id.antasari.sumifyai.ui.theme.ColorCompleted
import id.antasari.sumifyai.ui.theme.ColorFailed
import id.antasari.sumifyai.ui.theme.ColorGeneratingPdf
import id.antasari.sumifyai.ui.theme.ColorQueued
import id.antasari.sumifyai.ui.theme.ColorSummarizing
import id.antasari.sumifyai.ui.theme.ColorTranscribing
import id.antasari.sumifyai.ui.theme.PrimaryIndigo
import id.antasari.sumifyai.ui.theme.SecondaryTeal
import id.antasari.sumifyai.ui.theme.SurfaceLight
import id.antasari.sumifyai.ui.theme.SurfaceLightCard
import id.antasari.sumifyai.ui.theme.TextMuted
import id.antasari.sumifyai.ui.theme.TextPrimary
import id.antasari.sumifyai.ui.theme.TextSecondary
import id.antasari.sumifyai.ui.viewmodel.MainViewModel
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetails: (String, String) -> Unit
) {
    val meetings by viewModel.history.collectAsState()
    val isDemoMode by viewModel.isDemoMode.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    val filteredMeetings = remember(meetings, searchQuery) {
        if (searchQuery.trim().isEmpty()) meetings else meetings.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        topBar = {
            SumifyTopAppBar(
                title = {
                    BrandLogo(
                        heightDp = 32,
                        widthDp = 108,
                        modifier = Modifier.offset(x = 3.dp)
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SumifyTopBarContentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = PrimaryIndigo,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Audio Summary",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    viewModel.loadMeetingsHistory()
                    delay(500)
                    isRefreshing = false
                }
            },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Audio Summaries",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Your recent reports",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        if (isDemoMode) {
                            Box(
                                modifier = Modifier
                                    .background(SecondaryTeal.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                    .border(BorderStroke(0.5.dp, SecondaryTeal.copy(alpha = 0.4f)), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Mode Demo",
                                    color = SecondaryTeal,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp)
                            .border(BorderStroke(1.dp, BorderLight), shape = RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLightCard),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = meetings.size.toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo
                                )
                                Text(
                                    text = "Total Audios",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(BorderLight)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val completedCount = meetings.count { it.status.equals("completed", ignoreCase = true) }
                                Text(
                                    text = completedCount.toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorCompleted
                                )
                                Text(
                                    text = "Completed PDFs",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(BorderLight)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val favoriteCount = meetings.count { it.isFavorite }
                                Text(
                                    text = favoriteCount.toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE4A100)
                                )
                                Text(
                                    text = "Important",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search summaries...", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "SearchIcon",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceLight,
                            unfocusedContainerColor = SurfaceLight,
                            focusedBorderColor = PrimaryIndigo,
                            unfocusedBorderColor = BorderLight,
                            cursorColor = PrimaryIndigo
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Report Summary",
                                fontSize = 16.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (filteredMeetings.isEmpty()) {
                                    "No reports to show"
                                } else {
                                    "${filteredMeetings.size} report${if (filteredMeetings.size == 1) "" else "s"} available"
                                },
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                if (filteredMeetings.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "No items",
                                    tint = TextMuted,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No search results found" else "No summaries generated yet",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "Try searching a different keyword" else "Tap the '+' button below to start your first summary",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredMeetings, key = { it.id }) { meeting ->
                        MeetingHistoryItem(
                            meeting = meeting,
                            onClick = { onNavigateToDetails(meeting.id, meeting.status) },
                            onToggleFavorite = { viewModel.toggleMeetingFavorite(meeting.id) },
                            onDelete = { viewModel.deleteMeeting(meeting.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingHistoryItem(
    meeting: MeetingLocal,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(meeting.createdAt) {
        val date = Date(meeting.createdAt)
        DateFormat.format("dd MMM yyyy, HH:mm", date).toString()
    }

    val (statusText, statusColor) = remember(meeting.status) {
        when (meeting.status.lowercase()) {
            "queued" -> "Queued" to ColorQueued
            "transcribing" -> "Transcribing" to ColorTranscribing
            "summarizing" -> "Summarizing" to ColorSummarizing
            "generating_pdf" -> "Generating PDF" to ColorGeneratingPdf
            "completed" -> "Completed" to ColorCompleted
            "failed" -> "Failed" to ColorFailed
            else -> meeting.status to ColorQueued
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (meeting.isFavorite) Color(0xFFFFD56A) else BorderLight
                ),
                shape = RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceLightCard),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (meeting.isFavorite) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(PrimaryIndigo.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = meeting.title.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "S",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = meeting.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = dateString,
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (meeting.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = meeting.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                            .border(BorderStroke(0.5.dp, statusColor.copy(alpha = 0.3f)), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .background(BorderLight.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .border(BorderStroke(0.5.dp, BorderLight), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = meeting.language.uppercase(Locale.getDefault()),
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            color = if (meeting.isFavorite) Color(0xFFFFF4CC) else BorderLight.copy(alpha = 0.35f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = if (meeting.isFavorite) "Remove from important" else "Mark as important",
                        tint = if (meeting.isFavorite) Color(0xFFE4A100) else TextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

            IconButton(
                onClick = onDelete,
                    modifier = Modifier
                        .size(34.dp)
                        .background(ColorFailed.copy(alpha = 0.08f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = ColorFailed,
                        modifier = Modifier.size(16.dp)
                )
            }
            }
        }
    }
}
