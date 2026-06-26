package id.antasari.sumifyai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.antasari.sumifyai.ui.components.SumifyTopAppBar
import id.antasari.sumifyai.ui.components.SumifyTopBarContentColor
import id.antasari.sumifyai.ui.theme.BackgroundLight
import id.antasari.sumifyai.ui.theme.TextMuted
import id.antasari.sumifyai.ui.theme.TextPrimary
import id.antasari.sumifyai.ui.theme.TextSecondary
import id.antasari.sumifyai.ui.viewmodel.MainViewModel

@Composable
fun FavoriteSummariesScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String, String) -> Unit
) {
    val meetings by viewModel.history.collectAsState()
    val favoriteMeetings = remember(meetings) {
        meetings.filter { it.isFavorite }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        topBar = {
            SumifyTopAppBar(
                title = {
                    Text(
                        text = "Saved Summaries",
                        color = SumifyTopBarContentColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SumifyTopBarContentColor
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(18.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Important Reports",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (favoriteMeetings.isEmpty()) {
                            "No saved summaries yet"
                        } else {
                            "${favoriteMeetings.size} saved report${if (favoriteMeetings.size == 1) "" else "s"}"
                        },
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            if (favoriteMeetings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "No saved summaries",
                                tint = Color(0xFFE4A100),
                                modifier = Modifier.height(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No saved summaries",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap Save on a report summary to show it here",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            } else {
                items(favoriteMeetings, key = { it.id }) { meeting ->
                    MeetingHistoryItem(
                        meeting = meeting,
                        onClick = { onNavigateToDetails(meeting.id, meeting.status) },
                        onToggleFavorite = { viewModel.toggleMeetingFavorite(meeting.id) },
                        onDelete = { viewModel.deleteMeeting(meeting.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
