package com.civictracker.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.civictracker.app.data.model.Issue
import com.civictracker.app.ui.theme.*
import com.civictracker.app.ui.viewmodel.OfficerDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerDashboardScreen(
    viewModel: OfficerDashboardViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onIssueClick: (String) -> Unit
) {
    val issues by viewModel.issues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "OFFICER COMMAND PANEL", 
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchIssues() }) { 
                        Icon(Icons.Default.Sync, null, tint = AccentGreen) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading && issues.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentGreen)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "SYSTEM DISPATCH QUEUE", 
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    ), 
                                    color = AccentGreen
                                )
                                Spacer(Modifier.width(12.dp))
                                PulseIndicator()
                            }
                            Text(
                                "Incident Priority Queue", 
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), 
                                color = TextPrimary
                            )
                            Text(
                                "Automated urgency scoring applied via community signals and response aging.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    
                    items(issues, key = { it.id }) { issue ->
                        val urgency = viewModel.calculateUrgency(issue)
                        OfficerIssueCard(
                            issue = issue, 
                            urgency = urgency,
                            onClick = { onIssueClick(issue.id) },
                            onStatusUpdate = { newStatus -> viewModel.updateStatus(issue.id, newStatus) }
                        )
                    }
                    
                    if (issues.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 64.dp), contentAlignment = Alignment.Center) {
                                Text("NO ACTIVE DEPLOYMENTS DETECTED", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), color = TextSecondary)
                            }
                        }
                    }
                    
                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
fun OfficerIssueCard(
    issue: Issue, 
    urgency: Long,
    onClick: () -> Unit,
    onStatusUpdate: (String) -> Unit
) {
    val urgencyColor = when {
        issue.status == "Resolved" -> DividerGray
        urgency >= 100 -> UrgencyRed
        urgency >= 60 -> StatusAmber
        else -> AccentGreen
    }
    
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, DividerGray.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0F1419))))) {
            Row(modifier = Modifier.padding(20.dp).height(IntrinsicSize.Min)) {
                Box {
                    AsyncImage(
                        model = issue.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, DividerGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Tactical Urgency HUD
                    Surface(
                        color = urgencyColor,
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = if (issue.status == "Resolved") "✓" else urgency.toString(), 
                            color = Color.Black, 
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(Modifier.width(20.dp))
                
                Column(modifier = Modifier.fillMaxHeight().weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = AccentGreen.copy(alpha = 0.15f), 
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, AccentGreen.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    (issue.category ?: "General").uppercase(), 
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), 
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black), 
                                    color = AccentGreen
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "ID: ${issue.id.take(6).uppercase()}", 
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), 
                                color = TextSecondary,
                                fontSize = 9.sp
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            issue.title.uppercase(), 
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp), 
                            color = TextPrimary, 
                            maxLines = 2,
                            lineHeight = 20.sp
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val hoursElapsed = issue.timestamp?.let { (System.currentTimeMillis() - it) / 3_600_000 } ?: 0
                        Icon(
                            Icons.Default.Timer, 
                            null, 
                            modifier = Modifier.size(14.dp), 
                            tint = if (urgency >= 100) UrgencyRed else TextSecondary
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "AGED: ${hoursElapsed}H", 
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), 
                            color = if (urgency >= 100) UrgencyRed else TextSecondary
                        )
                    }
                }
            }
            
            HorizontalDivider(color = DividerGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PinDrop, null, tint = AccentGreen, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "WARD ${issue.id.take(4).uppercase()}", 
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), 
                            color = TextSecondary
                        )
                        Spacer(Modifier.width(20.dp))
                        Icon(Icons.Default.Group, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${issue.upvotes ?: 0} SIGNALS", 
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), 
                            color = TextSecondary
                        )
                    }
                }
                
                Spacer(Modifier.height(18.dp))
                
                // Professional Status Control Rail
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatusChip(
                        label = "PENDING", 
                        isSelected = issue.status == "Pending",
                        color = StatusAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onStatusUpdate("Pending") }
                    )
                    StatusChip(
                        label = "PROGRESS", 
                        isSelected = issue.status == "In Progress",
                        color = StatusAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onStatusUpdate("In Progress") }
                    )
                    StatusChip(
                        label = "RESOLVED", 
                        isSelected = issue.status == "Resolved",
                        color = AccentGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onStatusUpdate("Resolved") }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color else Color.Transparent,
        border = if (isSelected) null else BorderStroke(1.dp, DividerGray.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 0.5.sp),
                color = if (isSelected) Color.Black else TextSecondary
            )
        }
    }
}

@Composable
fun PulseIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(AccentGreen.copy(alpha = alpha))
    )
}
