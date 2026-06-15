package com.civictracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

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
                title = { Text("Issue Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = issue.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(issue.status)
                    
                    Button(onClick = { /* Mock upvote */ }) {
                        Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("${issue.upvotes} Upvotes")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = issue.title, style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = issue.category, 
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = issue.description, 
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Resolution Progress", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Before", style = MaterialTheme.typography.labelMedium)
                        AsyncImage(
                            model = issue.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("After", style = MaterialTheme.typography.labelMedium)
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Pending", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
