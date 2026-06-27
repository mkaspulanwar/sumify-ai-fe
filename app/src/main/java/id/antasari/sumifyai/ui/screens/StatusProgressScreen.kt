package id.antasari.sumifyai.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.antasari.sumifyai.ui.components.SumifyTopAppBar
import id.antasari.sumifyai.ui.components.SumifyTopBarContentColor
import id.antasari.sumifyai.ui.theme.BackgroundLight
import id.antasari.sumifyai.ui.theme.BorderLight
import id.antasari.sumifyai.ui.theme.ColorCompleted
import id.antasari.sumifyai.ui.theme.ColorFailed
import id.antasari.sumifyai.ui.theme.PrimaryIndigo
import id.antasari.sumifyai.ui.theme.SurfaceLight
import id.antasari.sumifyai.ui.theme.TextMuted
import id.antasari.sumifyai.ui.theme.TextPrimary
import id.antasari.sumifyai.ui.theme.TextSecondary
import id.antasari.sumifyai.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusProgressScreen(
    meetingId: String,
    viewModel: MainViewModel,
    onNavigateToDetails: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val activeMeeting by viewModel.activeMeeting.collectAsState()

    LaunchedEffect(meetingId) {
        viewModel.startPollingMeetingStatus(meetingId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPolling()
        }
    }

    val displayedMeeting = activeMeeting?.takeIf { it.id == meetingId }
    val currentStatus = displayedMeeting?.status?.lowercase() ?: "queued"
    val isCompleted = currentStatus == "completed"
    val isFailed = currentStatus == "failed"
    val isCancelled = currentStatus == "cancelled"
    val isProcessing = !isCompleted && !isFailed && !isCancelled

    val stages = listOf(
        MeetingStage("queued", "Uploaded & Queued", "Your audio file is successfully uploaded and queued for parsing."),
        MeetingStage("transcribing", "Transcribing Audio", "AI Whisper model is transcribing voice to text segments."),
        MeetingStage("summarizing", "Summarizing Content", "Apilogy LLM is extracting key insights, actions, and decisions."),
        MeetingStage("generating_pdf", "Generating PDF", "Rendering layout from design templates into PDF."),
        MeetingStage("completed", "Summary Ready", "Your PDF document has been successfully created and saved.")
    )

    val currentStageIndex = remember(currentStatus) {
        val index = stages.indexOfFirst { it.statusId == currentStatus }
        if (index != -1) index else 0
    }
    var visualStageIndex by remember(meetingId) { mutableIntStateOf(currentStageIndex) }
    var isTimelineComplete by remember(meetingId) { mutableStateOf(false) }
    var showSummaryReady by remember(meetingId) { mutableStateOf(false) }

    LaunchedEffect(currentStatus, currentStageIndex) {
        if (isCompleted) {
            showSummaryReady = false
            isTimelineComplete = false

            val startIndex = visualStageIndex.coerceIn(0, stages.lastIndex)
            for (index in startIndex until stages.lastIndex) {
                visualStageIndex = index
                delay(240)
            }

            visualStageIndex = stages.lastIndex
            isTimelineComplete = true
            delay(220)
            showSummaryReady = true
        } else {
            visualStageIndex = currentStageIndex
            isTimelineComplete = false
            showSummaryReady = false
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            SumifyTopAppBar(
                title = { Text("Processing Status", color = SumifyTopBarContentColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SumifyTopBarContentColor)
                    }
                },
                actions = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Header Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = displayedMeeting?.title ?: "Meeting Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = displayedMeeting?.description?.ifEmpty { "No description provided." } ?: "Processing...",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stepper Pipeline
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = showSummaryReady,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(180)) togetherWith
                            fadeOut(animationSpec = tween(120))
                    },
                    label = "processing-content"
                ) { ready ->
                    if (ready) {
                        SummaryReadyIndicator()
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            stages.forEachIndexed { index, stage ->
                                val stageState = when {
                                    isTimelineComplete -> StageState.Completed
                                    (isFailed || isCancelled) && index == visualStageIndex -> StageState.Failed
                                    (isFailed || isCancelled) && index > visualStageIndex -> StageState.Pending
                                    index < visualStageIndex -> StageState.Completed
                                    index == visualStageIndex -> StageState.Active
                                    else -> StageState.Pending
                                }

                                StageItem(
                                    stage = stage,
                                    state = stageState,
                                    isLast = index == stages.lastIndex
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 128.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isFailed || isCancelled) {
                    Text(
                        text = if (isCancelled) {
                            "Processing was cancelled. This report remains saved in your summary history."
                        } else {
                            "Summarization failed. Please check your backend connections."
                        },
                        color = if (isCancelled) TextSecondary else ColorFailed,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else if (!showSummaryReady) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryIndigo,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "This might take a minute. You can safely go back; processing continues in background.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (showSummaryReady) {
                    Button(
                        onClick = onNavigateToDetails,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceLight),
                        border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.28f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "View Summary Details",
                            color = PrimaryIndigo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else if (isProcessing) {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceLight),
                        border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.28f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Run in Background",
                            color = PrimaryIndigo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.cancelMeetingProcessing(meetingId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorFailed.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, ColorFailed.copy(alpha = 0.32f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel processing",
                            tint = ColorFailed,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Batalkan",
                            color = ColorFailed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Spacer(modifier = Modifier.height(64.dp))
                }

                if (!isProcessing) {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceLight),
                        border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.28f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (showSummaryReady || isCancelled) "Back to Dashboard" else "Run in Background",
                            color = PrimaryIndigo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryReadyIndicator() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .background(ColorCompleted.copy(alpha = 0.12f), CircleShape)
                .border(BorderStroke(2.dp, ColorCompleted), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Summary completed",
                tint = ColorCompleted,
                modifier = Modifier.size(58.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Summary Ready",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your PDF document has been successfully created and saved.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun StageItem(
    stage: MeetingStage,
    state: StageState,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Step Icon and Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            val circleColor = when (state) {
                StageState.Pending -> BorderLight.copy(alpha = 0.4f)
                StageState.Active -> PrimaryIndigo.copy(alpha = 0.1f)
                StageState.Completed -> ColorCompleted.copy(alpha = 0.1f)
                StageState.Failed -> ColorFailed.copy(alpha = 0.1f)
            }
            val borderColor = when (state) {
                StageState.Pending -> TextMuted
                StageState.Active -> PrimaryIndigo
                StageState.Completed -> ColorCompleted
                StageState.Failed -> ColorFailed
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(circleColor, CircleShape)
                    .border(BorderStroke(1.5.dp, borderColor), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    StageState.Completed -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = ColorCompleted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    StageState.Failed -> {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Failed",
                            tint = ColorFailed,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    StageState.Active -> {
                        // Spinning rotating outer ring simulation
                        val infiniteTransition = rememberInfiniteTransition(label = "spin")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing)
                            ),
                            label = "rotate"
                        )
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .rotate(rotation)
                                .border(BorderStroke(1.5.dp, PrimaryIndigo), CircleShape)
                        )
                    }
                    StageState.Pending -> {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(TextMuted, CircleShape)
                        )
                    }
                }
            }

            // Connection Line
            if (!isLast) {
                val lineColor = when (state) {
                    StageState.Completed -> ColorCompleted
                    else -> BorderLight
                }
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(34.dp)
                        .background(lineColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
        ) {
            val titleColor = when (state) {
                StageState.Pending -> TextMuted
                StageState.Active -> PrimaryIndigo
                StageState.Completed -> TextPrimary
                StageState.Failed -> ColorFailed
            }

            Text(
                text = stage.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            
            Spacer(modifier = Modifier.height(2.dp))

            if (state == StageState.Active || state == StageState.Completed || state == StageState.Failed) {
                Text(
                    text = stage.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

data class MeetingStage(
    val statusId: String,
    val title: String,
    val description: String
)

enum class StageState {
    Pending,
    Active,
    Completed,
    Failed
}
