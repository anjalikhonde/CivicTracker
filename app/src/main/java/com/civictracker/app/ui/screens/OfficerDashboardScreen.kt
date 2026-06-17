package com.civictracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.civictracker.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerDashboardScreen(
    onBack: () -> Unit,
    onIssueClick: (String) -> Unit
) {
    val issues = MockData.sampleIssues.sortedByDescending { it.urgency }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Officer Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text("ACTION REQUIRED", style = MaterialTheme.typography.labelSmall, color = AccentGreen, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("Issues by Urgency", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
            items(issues) { issue ->
                OfficerIssueCard(issue = issue, onClick = { onIssueClick(issue.id) })
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerIssueCard(issue: EnhancedIssue, onClick: () -> Unit) {
    val urgencyColor = when {
        issue.urgency >= 80 -> UrgencyRed
        issue.urgency >= 50 -> Color(0xFFFF9800)
        else -> AccentGreen
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(modifier = Modifier.padding(12.dp).height(IntrinsicSize.Min)) {
            AsyncImage(
                model = issue.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxHeight().weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Surface(color = AccentGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text(issue.category, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = AccentGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(issue.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 2)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, modifier = Modifier.size(12.dp), tint = UrgencyRed)
                        Text(" SLA: ${issue.sla ?: "24h left"}", style = MaterialTheme.typography.labelSmall, color = UrgencyRed)
                    }
                    Surface(color = urgencyColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp), modifier = Modifier.border(1.dp, urgencyColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))) {
                        Text("URG: ${issue.urgency}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = urgencyColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        HorizontalDivider(color = DividerGray, thickness = 0.5.dp)
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ThumbUp, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Text(" ${issue.upvotes} upvotes", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.LocationOn, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Text(" ${issue.ward}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Button(onClick = {}, modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentGreen), shape = RoundedCornerShape(6.dp)) {
                Text("ASSIGN", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}