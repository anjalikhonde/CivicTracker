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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.civictracker.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    issueId: String,
    onBack: () -> Unit
) {
    val issue = MockData.sampleIssues.find { it.id == issueId } ?: MockData.sampleIssues[0]

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
                    IconButton(onClick = {}) { Icon(Icons.Default.LocationOn, null) }
                    IconButton(onClick = {}) { Icon(Icons.Default.AccountCircle, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground, titleContentColor = TextPrimary)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Reference Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = AccentGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        " REF: NY-77291 ",
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
                        Text(issue.urgency.toString(), style = MaterialTheme.typography.titleLarge, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(issue.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            Text("Reported in ${issue.ward} • ${issue.timeAgo}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

            Spacer(Modifier.height(24.dp))

            // Progress Phase Bar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PhaseItem("Citizen Report", true)
                PhaseItem("Validated", true)
                PhaseItem("Gov Routing", true, isCurrent = true)
                PhaseItem("Resolution", false)
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
                        Text("Assigned to: ${issue.department ?: "Department"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Team Alpha-9 dispatched to location.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        
                        Spacer(Modifier.height(16.dp))
                        Text("SLA COUNTDOWN", style = MaterialTheme.typography.labelSmall, color = UrgencyRed, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(issue.sla ?: "00:00:00", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Verification Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Visibility, null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Citizen Verification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("PENDING YOUR REVIEW", color = AccentGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
            
            // Image Comparison (Before/After)
            AsyncImage(
                model = issue.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Text("\"${issue.description}\"", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))

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
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                            Text("REJECT")
                        }
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

            // Audit Timeline
            Text("Audit Timeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            issue.timeline.forEach { entry ->
                TimelineEntry(entry)
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun PhaseItem(label: String, completed: Boolean, isCurrent: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (completed) (if (isCurrent) Color(0xFFFF9800) else AccentGreen) else DividerGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (completed) Icons.Default.Check else Icons.Default.Circle,
                null,
                modifier = Modifier.size(16.dp),
                tint = if (completed) Color.Black else TextSecondary
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall, 
            color = if (completed) TextPrimary else TextSecondary, 
            textAlign = TextAlign.Center, 
            fontSize = 8.sp
        )
    }
}

@Composable
fun TimelineEntry(entry: AuditEntry) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(AccentGreen))
            Box(modifier = Modifier.width(1.dp).height(50.dp).background(DividerGray))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(entry.date, style = MaterialTheme.typography.labelSmall, color = AccentGreen, fontWeight = FontWeight.Bold)
            Text(entry.title, fontWeight = FontWeight.Bold)
            Text(entry.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
