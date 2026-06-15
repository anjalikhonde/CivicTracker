package com.civictracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.civictracker.app.data.model.Issue

@Composable
fun HomeScreen(
    onReportIssue: () -> Unit,
    onIssueClick: (String) -> Unit,
    onNavigateToOfficerDashboard: () -> Unit,
    onNavigateToPublicScorecard: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(1) } // Default to Feed
    val issues = MockData.sampleIssues

    Scaffold(
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab != 2) {
                FloatingActionButton(
                    onClick = onReportIssue,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Report Issue")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> MapPlaceholder()
                1 -> FeedTab(issues = issues, onIssueClick = onIssueClick)
                2 -> ProfileTab(
                    onNavigateToOfficerDashboard = onNavigateToOfficerDashboard,
                    onNavigateToPublicScorecard = onNavigateToPublicScorecard,
                    onLogout = onLogout
                )
            }
        }
    }
}

enum class HomeTab(val label: String, val icon: ImageVector) {
    MAP("Map", Icons.Default.Map),
    FEED("Feed", Icons.Default.List),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun MapPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Map,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Map View - Coming Soon",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "API Key setup required for Google Maps",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun FeedTab(issues: List<Issue>, onIssueClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        item {
            Text(
                "Recent Issues",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
        items(issues) { issue ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                onClick = { onIssueClick(issue.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = issue.category,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.weight(1f))
                        StatusBadge(issue.status)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(text = issue.title, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = issue.description,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            " ${issue.upvotes}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val containerColor = when (status) {
        "Open" -> MaterialTheme.colorScheme.errorContainer
        "In Progress" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun ProfileTab(
    onNavigateToOfficerDashboard: () -> Unit,
    onNavigateToPublicScorecard: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text("John Doe", style = MaterialTheme.typography.headlineMedium)
        Text("Verified Citizen", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(32.dp))

        OutlinedButton(
            onClick = onNavigateToOfficerDashboard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Officer Dashboard")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onNavigateToPublicScorecard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.BarChart, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Public Scorecard")
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout")
        }
    }
}
