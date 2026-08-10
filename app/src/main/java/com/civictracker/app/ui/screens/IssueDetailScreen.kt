package com.civictracker.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.civictracker.app.data.model.Issue
import com.civictracker.app.ui.theme.*
import com.civictracker.app.ui.viewmodel.IssueDetailUiState
import com.civictracker.app.ui.viewmodel.IssueDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    issueId: String,
    onBack: () -> Unit,
    onGetDirections: (String) -> Unit,
    viewModel: IssueDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Entry?") },
            text = { Text("This will permanently remove this issue from the system.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteIssue()
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = UrgencyRed)
                ) { Text("DELETE") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("CANCEL") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CivicLink", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchIssue() }) { 
                        Icon(Icons.Default.Refresh, null, tint = AccentGreen) 
                    }
                    IconButton(onClick = { onGetDirections(issueId) }) { 
                        Icon(Icons.Default.Navigation, null, tint = AccentGreen) 
                    }
                    IconButton(onClick = { showDeleteDialog = true }) { 
                        Icon(Icons.Default.Delete, null, tint = UrgencyRed) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground, titleContentColor = TextPrimary)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is IssueDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentGreen)
                }
                is IssueDetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = UrgencyRed)
                        Button(onClick = { viewModel.fetchIssue() }, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Retry")
                        }
                    }
                }
                is IssueDetailUiState.Success -> {
                    IssueDetailContent(
                        issue = state.issue,
                        onUpvote = { viewModel.upvote() },
                        onGetDirections = { onGetDirections(state.issue.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun IssueDetailContent(
    issue: Issue,
    onUpvote: () -> Unit,
    onGetDirections: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = issue.timestamp?.let { dateFormat.format(Date(it)) } ?: "Unknown Date"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Reference Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = AccentGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                Text(
                    " REF: ${issue.id.take(8).uppercase()} ",
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.weight(1f))
            Surface(
                color = Color(0xFF1C2128), 
                shape = RoundedCornerShape(8.dp), 
                border = BorderStroke(1.dp, DividerGray)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("URGENCY", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 8.sp)
                    Text("75", style = MaterialTheme.typography.titleLarge, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(issue.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
        Text("Reported in WARD • $dateString", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

        Spacer(Modifier.height(24.dp))

        // Progress Phase Bar
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PhaseItem("SUBMITTED", true, isCurrent = issue.status == "Pending")
            PhaseItem("IN PROGRESS", issue.status == "In Progress" || issue.status == "Resolved", isCurrent = issue.status == "In Progress")
            PhaseItem("RESOLVED", issue.status == "Resolved", isCurrent = false)
        }

        Spacer(Modifier.height(24.dp))

        // Assigned Dept Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2128)),
            border = BorderStroke(1.dp, DividerGray)
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Surface(color = DividerGray, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Engineering, null, modifier = Modifier.padding(8.dp), tint = TextSecondary)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Assigned to: ${issue.category ?: "Department"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Team Alpha-9 dispatched to location.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    
                    Spacer(Modifier.height(16.dp))
                    Text("SLA COUNTDOWN", style = MaterialTheme.typography.labelSmall, color = UrgencyRed, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("04:22:16", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Directions Button
        Button(
            onClick = onGetDirections,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Navigation, null, tint = Color.Black, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                "GET DIRECTIONS TO LOCATION",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))

        // Community Validation (Upvote)
        Button(
            onClick = onUpvote,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.PriorityHigh, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                "${issue.upvotes ?: 0} SIGNALS - UPVOTE REPORT",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(32.dp))

        // Evidence Section
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Visibility, null, tint = TextPrimary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Citizen Evidence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("PENDING YOUR REVIEW", color = AccentGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
        
        AsyncImage(
            model = issue.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Text("\"${issue.description ?: "No description provided."}\"", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))

        Spacer(Modifier.height(24.dp))

        // Resolution Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF151B22),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DividerGray)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Is this issue resolved?", fontWeight = FontWeight.Bold)
                Text(
                    "Your verification triggers the final \"Accountability\" phase and releases the performance rating for the department.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                        Text("REJECT")
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {}, 
                        modifier = Modifier.weight(1f), 
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1C7CD))
                    ) {
                        Text("VERIFY RESOLUTION", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun PhaseItem(label: String, completed: Boolean, isCurrent: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (completed) {
                        if (isCurrent) Color(0xFFFF9800).copy(alpha = 0.2f) else AccentGreen.copy(alpha = 0.1f)
                    } else Color.Transparent
                )
                .border(
                    width = 1.dp,
                    color = if (completed) (if (isCurrent) Color(0xFFFF9800) else AccentGreen) else DividerGray,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (completed && !isCurrent) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = AccentGreen)
            } else if (isCurrent) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF9800)))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label, 
            style = MaterialTheme.typography.labelSmall, 
            color = if (completed) TextPrimary else TextSecondary,
            fontSize = 9.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
        )
    }
}
