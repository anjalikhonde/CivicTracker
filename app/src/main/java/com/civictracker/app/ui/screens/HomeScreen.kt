package com.civictracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.civictracker.app.ui.theme.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// ─── HOME SCREEN ────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onReportIssue: () -> Unit,
    onIssueClick: (String) -> Unit,
    onNavigateToOfficerDashboard: () -> Unit,
    onNavigateToPublicScorecard: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            if (selectedTab != 2) {
                CivicTopBar()
            }
        },
        bottomBar = {
            CivicBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> DashboardTab(onIssueClick = onIssueClick)
                1 -> ReportIssueScreen(onBack = { selectedTab = 0 }, onSuccess = { selectedTab = 0 })
                2 -> MapTab(onIssueClick = onIssueClick)
                3 -> ProfileTab(
                    onNavigateToOfficerDashboard = onNavigateToOfficerDashboard,
                    onNavigateToPublicScorecard = onNavigateToPublicScorecard,
                    onLogout = onLogout
                )
            }
        }
    }
}

// ─── TOP BAR ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivicTopBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "CivicLink",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        },
        actions = {
            IconButton(onClick = { }) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=Alex&background=4ADE80&color=fff",
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, DividerGray, CircleShape)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkBackground,
            titleContentColor = TextPrimary
        )
    )
}

// ─── BOTTOM NAV ─────────────────────────────────────────────────────────────

@Composable
fun CivicBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF0B1015),
        tonalElevation = 0.dp,
        modifier = Modifier.border(
            1.dp,
            DividerGray.copy(alpha = 0.5f),
            RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
        )
    ) {
        val tabs = listOf(
            Triple("Home", Icons.Outlined.Home, Icons.Filled.Home),
            Triple("Report", Icons.Outlined.AddCircleOutline, Icons.Filled.AddCircle),
            Triple("Map", Icons.Outlined.Map, Icons.Filled.Map),
            Triple("Profile", Icons.Outlined.Person, Icons.Filled.Person)
        )

        tabs.forEachIndexed { index, (label, unselectedIcon, selectedIcon) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        if (selectedTab == index) selectedIcon else unselectedIcon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentGreen,
                    selectedTextColor = AccentGreen,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

// ─── DASHBOARD TAB ──────────────────────────────────────────────────────────

@Composable
fun DashboardTab(onIssueClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    "CITIZEN DASHBOARD",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    "Welcome back, Alex",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    "Your contributions are helping maintain ward safety and transparency.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ImpactCard(value = "03", label = "Issues Resolved This Week", modifier = Modifier.weight(1f))
                ImpactCard(value = "12", label = "Active Reports In Ward 4", modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Nearby Reports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { }) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text("Filter", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                }
            }
        }

        items(MockData.sampleIssues) { issue ->
            NearbyReportCard(issue = issue, onClick = { onIssueClick(issue.id) })
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ─── IMPACT CARD ────────────────────────────────────────────────────────────

@Composable
fun ImpactCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = AccentGreen
            )
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, lineHeight = 16.sp)
        }
    }
}

// ─── NEARBY REPORT CARD ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyReportCard(issue: EnhancedIssue, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        onClick = onClick
    ) {
        Column {
            Box {
                AsyncImage(
                    model = issue.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    color = UrgencyRed.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PriorityHigh, null, modifier = Modifier.size(10.dp), tint = Color.White)
                        Text(
                            " URGENCY: ${issue.urgency}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        issue.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(issue.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    issue.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(16.dp))

                // Progress bar
                val phases = listOf("REPORTED", "VALIDATED", "ASSIGNED", "RESOLVED")
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        phases.forEachIndexed { index, label ->
                            val isActive = index < issue.phase
                            val isCurrent = index == issue.phase - 1
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = if (isActive || isCurrent) AccentGreen else TextSecondary,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(4.dp)
                            .clip(CircleShape).background(DividerGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(issue.phase.toFloat() / 4f)
                                .fillMaxHeight()
                                .background(AccentGreen)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${issue.ward} • ${issue.timeAgo}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    // FIX: Use Surface with border modifier instead of border() function
                    Surface(
                        color = PhaseGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.border(1.dp, PhaseGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            "PHASE ${issue.phase}: ${issue.status}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ─── MAP TAB ────────────────────────────────────────────────────────────────

@Composable
fun MapTab(onIssueClick: (String) -> Unit) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    // FIX: Use android.graphics.ColorMatrix directly (not Compose ColorMatrix)
    val filter = remember {
        val matrix = android.graphics.ColorMatrix()
        matrix.setSaturation(0f)
        val inverseMatrix = android.graphics.ColorMatrix(floatArrayOf(
            -1.0f, 0f, 0f, 0f, 255f,
            0f, -1.0f, 0f, 0f, 255f,
            0f, 0f, -1.0f, 0f, 255f,
            0f, 0f, 0f, 1.0f, 0f
        ))
        matrix.postConcat(inverseMatrix)
        android.graphics.ColorMatrixColorFilter(matrix)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                mapView.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(14.0)
                    controller.setCenter(GeoPoint(19.0760, 72.8777))
                    overlayManager.tilesOverlay.setColorFilter(filter)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mv ->
                mv.overlays.clear()
                MockData.sampleIssues.forEach { issue ->
                    val marker = Marker(mv)
                    marker.position = GeoPoint(issue.latitude, issue.longitude)
                    marker.title = issue.title
                    // FIX: Safely set tint only if icon is not null
                    marker.icon?.setTint(
                        when (issue.status) {
                            "REPORTED" -> android.graphics.Color.RED
                            "ASSIGNED" -> android.graphics.Color.YELLOW
                            "RESOLVED" -> android.graphics.Color.GREEN
                            else -> android.graphics.Color.CYAN
                        }
                    )
                    mv.overlays.add(marker)
                }
                mv.invalidate()
            }
        )

        // Search Bar Overlay
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
                .border(1.dp, DividerGray, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0B1015).copy(alpha = 0.9f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("CivicLink", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Search, null, tint = TextSecondary)
            }
        }

        // Bottom overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                .padding(bottom = 16.dp)
        ) {
            Text(
                "CRITICAL REPORTS",
                style = MaterialTheme.typography.labelSmall,
                color = AccentGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Top Issues Nearby", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text("VIEW ALL", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            }
            Spacer(Modifier.height(16.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(MockData.sampleIssues) { issue ->
                    MapIssueCard(issue, onIssueClick)
                }
            }
        }

        // FIX: MapControlButton defined here
        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MapControlButton(icon = Icons.Default.Add, onClick = { mapView.controller.zoomIn() })
            MapControlButton(icon = Icons.Default.Remove, onClick = { mapView.controller.zoomOut() })
            MapControlButton(icon = Icons.Default.Layers, onClick = { })
        }

        FloatingActionButton(
            onClick = {},
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 140.dp, end = 16.dp),
            containerColor = AccentGreen,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, null, tint = Color.Black)
        }
    }
}

// ─── MAP CONTROL BUTTON (was missing!) ──────────────────────────────────────

@Composable
fun MapControlButton(icon: ImageVector, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0B1015).copy(alpha = 0.9f),
        modifier = Modifier
            .size(40.dp)
            .border(1.dp, DividerGray, RoundedCornerShape(8.dp))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
        }
    }
}

// ─── MAP ISSUE CARD ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapIssueCard(issue: EnhancedIssue, onIssueClick: (String) -> Unit) {
    Card(
        modifier = Modifier.width(300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        onClick = { onIssueClick(issue.id) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(color = Color(0xFFFF9800).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Home, null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("PHASE 2: VALIDATION", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold)
                    Text("${issue.urgency} URGENCY", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(issue.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(issue.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2, lineHeight = 18.sp)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccessTime, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Text(" 14m ago • Ward 07", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Button(
                    onClick = {},
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DividerGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("UPVOTE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── PROFILE TAB (was missing!) ─────────────────────────────────────────────

@Composable
fun ProfileTab(
    onNavigateToOfficerDashboard: () -> Unit,
    onNavigateToPublicScorecard: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            // Profile Header
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https://ui-avatars.com/api/?name=Alex&background=4ADE80&color=fff&size=80",
                        contentDescription = "Profile",
                        modifier = Modifier.size(64.dp).clip(CircleShape).border(2.dp, AccentGreen, CircleShape)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Alex Johnson", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Ward 4 Citizen", style = MaterialTheme.typography.bodyMedium, color = AccentGreen)
                        Text("Member since Jan 2024", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        }

        item {
            // Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("8", "Reports Filed", Modifier.weight(1f))
                StatCard("23", "Upvotes Given", Modifier.weight(1f))
                StatCard("5", "Resolved", Modifier.weight(1f))
            }
        }

        item {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            ProfileMenuCard(
                icon = Icons.Default.Dashboard,
                title = "Officer Dashboard",
                subtitle = "View government action panel",
                onClick = onNavigateToOfficerDashboard
            )
        }

        item {
            ProfileMenuCard(
                icon = Icons.Default.BarChart,
                title = "Public Scorecard",
                subtitle = "Ward performance & statistics",
                onClick = onNavigateToPublicScorecard
            )
        }

        item {
            ProfileMenuCard(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Manage alert preferences",
                onClick = {}
            )
        }

        item {
            ProfileMenuCard(
                icon = Icons.Default.Settings,
                title = "Settings",
                subtitle = "App preferences",
                onClick = {}
            )
        }

        item {
            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UrgencyRed.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, null, tint = UrgencyRed)
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", color = UrgencyRed, fontWeight = FontWeight.Bold)
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AccentGreen)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = AccentGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
        }
    }
}