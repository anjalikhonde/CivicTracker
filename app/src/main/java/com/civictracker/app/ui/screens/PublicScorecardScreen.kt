package com.civictracker.app.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.civictracker.app.ui.theme.*
import com.civictracker.app.ui.viewmodel.PublicScorecardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicScorecardScreen(
    onBack: () -> Unit,
    viewModel: PublicScorecardViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val overallRate by viewModel.overallResolutionRate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "WARD ANALYTICS", 
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshStats() }) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = AccentGreen)
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
            if (isLoading && stats.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentGreen)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        // Main Metric Card - Command Center Style
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF101418))))
                                .border(1.dp, AccentGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        ) {
                            // Optimized Decorative Data Wave using drawWithCache
                            val waveColor = AccentGreen.copy(alpha = 0.05f)
                            Spacer(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(top = 40.dp)
                                    .drawWithCache {
                                        val path = Path().apply {
                                            moveTo(0f, size.height * 0.8f)
                                            quadraticTo(size.width * 0.3f, size.height * 0.5f, size.width * 0.6f, size.height * 0.9f)
                                            lineTo(size.width, size.height * 0.4f)
                                        }
                                        onDrawBehind {
                                            drawPath(path, color = waveColor, style = Stroke(width = 2.dp.toPx()))
                                        }
                                    }
                            )

                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = AccentGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Text(
                                        "MUNICIPAL RESOLUTION INDEX", 
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        ), 
                                        color = TextPrimary
                                    )
                                }
                                
                                Spacer(Modifier.height(28.dp))
                                
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        "%.1f".format(overallRate), 
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-2).sp
                                        ), 
                                        color = AccentGreen
                                    )
                                    Text(
                                        "%", 
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), 
                                        color = AccentGreen, 
                                        modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("REAL-TIME PERFORMANCE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = TextSecondary)
                                        Text("LIVE DATA", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = AccentGreen)
                                    }
                                }
                                
                                Spacer(Modifier.height(20.dp))
                                
                                // Professional Progress Bar
                                Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(30.dp)).background(DividerGray.copy(alpha = 0.5f))) {
                                    Box(modifier = Modifier.fillMaxWidth(overallRate / 100f).fillMaxHeight().background(
                                        Brush.horizontalGradient(listOf(AccentGreen.copy(alpha = 0.7f), AccentGreen))
                                    ))
                                }
                            }
                        }
                    }

                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            Surface(color = AccentGreen.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(20.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.BarChart, null, tint = AccentGreen, modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "DEPARTMENT BREAKDOWN", 
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                ), 
                                color = TextSecondary
                            )
                        }
                    }

                    if (stats.isEmpty() && !isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("No data available from command center.", color = TextSecondary)
                            }
                        }
                    } else {
                        items(stats) { stat ->
                            DepartmentCard(stat = stat)
                        }
                    }
                    
                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
fun DepartmentCard(stat: DepartmentStat) {
    val total = stat.resolved + stat.overdue
    val resolutionRate = if (total > 0) stat.resolved.toFloat() / total else 0f
    val rateColor = when {
        resolutionRate >= 0.8f -> AccentGreen
        resolutionRate >= 0.5f -> StatusAmber
        else -> UrgencyRed
    }

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, DividerGray.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stat.name.uppercase(), 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp), 
                        color = TextPrimary
                    )
                    Text(
                        "PRIMARY RESPONSIBILITY LOG", 
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), 
                        color = TextSecondary
                    )
                }
                
                // Professional Rate Badge
                Surface(
                    color = rateColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, rateColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        "${(resolutionRate * 100).toInt()}%", 
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), 
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black), 
                        color = rateColor
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ScoreItem(label = "RESOLVED", value = stat.resolved.toString(), color = AccentGreen)
                ScoreItem(label = "AVG TIME", value = "${stat.avgDays}D", color = StatusAmber)
                ScoreItem(label = "OVERDUE", value = stat.overdue.toString(), color = if (stat.overdue > 0) UrgencyRed else DividerGray)
            }

            if (stat.overdue > 0) {
                Spacer(Modifier.height(20.dp))
                Surface(
                    color = UrgencyRed.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, UrgencyRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Warning, null, tint = UrgencyRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "${stat.overdue} INCIDENTS BREACHED RESPONSE WINDOW",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), 
                            color = UrgencyRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreItem(label: String, value: String, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = TextSecondary)
        Text(
            value, 
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
            color = color
        )
    }
}
