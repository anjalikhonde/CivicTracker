package com.civictracker.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.civictracker.app.data.model.Issue
import com.civictracker.app.ui.theme.*
import com.civictracker.app.ui.viewmodel.HomeViewModel
import com.civictracker.app.util.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.location.GeocoderNominatim
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

// ─── HOME SCREEN ────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    routingIssueId: String? = null,
    onReportIssue: () -> Unit,
    onIssueClick: (String) -> Unit,
    onNavigateToOfficerDashboard: () -> Unit,
    onNavigateToPublicScorecard: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val issues by viewModel.issues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(routingIssueId) {
        if (routingIssueId != null) selectedTab = 2
    }

    Scaffold(
        topBar = { if (selectedTab == 0 || selectedTab == 3) CivicTopBar() },
        bottomBar = { CivicBottomNav(selectedTab = selectedTab, onTabSelected = { selectedTab = it }) },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> DashboardTab(
                    issues = issues, 
                    isLoading = isLoading, 
                    onIssueClick = onIssueClick, 
                    onRefresh = { viewModel.refreshIssues() },
                    onReportNavigate = { selectedTab = 1 }
                )
                1 -> ReportIssueScreen(onBack = { selectedTab = 0 }, onSuccess = { selectedTab = 0; viewModel.refreshIssues() })
                2 -> MapTab(issues = issues, onIssueClick = onIssueClick, routingIssueId = routingIssueId)
                3 -> ProfileTab(onNavigateToOfficerDashboard, onNavigateToPublicScorecard, onLogout)
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
                Surface(color = AccentGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp), modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.LocationOn, null, tint = AccentGreen, modifier = Modifier.size(22.dp)) }
                }
                Spacer(Modifier.width(14.dp))
                Text("CivicLink", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-1).sp))
            }
        },
        actions = {
            IconButton(onClick = { }) {
                AsyncImage(model = "https://ui-avatars.com/api/?name=User&background=4ADE80&color=fff", contentDescription = "Profile", modifier = Modifier.size(34.dp).clip(CircleShape).border(1.5.dp, AccentGreen.copy(alpha = 0.5f), CircleShape))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground, titleContentColor = TextPrimary)
    )
}

// ─── BOTTOM NAV ─────────────────────────────────────────────────────────────

@Composable
fun CivicBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color(0xFF0B1015), tonalElevation = 0.dp, modifier = Modifier.border(1.dp, DividerGray.copy(alpha = 0.2f), RoundedCornerShape(0.dp))) {
        val tabs = listOf(Triple("Home", Icons.Outlined.Home, Icons.Filled.Home), Triple("Report", Icons.Outlined.AddCircleOutline, Icons.Filled.AddCircle), Triple("Map", Icons.Outlined.Map, Icons.Filled.Map), Triple("Profile", Icons.Outlined.Person, Icons.Filled.Person))
        tabs.forEachIndexed { index, (label, unselectedIcon, selectedIcon) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(if (selectedTab == index) selectedIcon else unselectedIcon, contentDescription = label, modifier = Modifier.size(26.dp)) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = AccentGreen, selectedTextColor = AccentGreen, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary, indicatorColor = AccentGreen.copy(alpha = 0.08f) )
            )
        }
    }
}

// ─── DASHBOARD TAB ──────────────────────────────────────────────────────────

@Composable
fun DashboardTab(
    issues: List<Issue>, 
    isLoading: Boolean, 
    onIssueClick: (String) -> Unit, 
    onRefresh: () -> Unit,
    onReportNavigate: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SYSTEM STATUS: ONLINE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp, fontSize = 10.sp), color = AccentGreen)
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(AccentGreen))
                    }
                    Text("Welcome back", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp), modifier = Modifier.padding(vertical = 4.dp))
                    Text("Synchronizing real-time infrastructure data and community contributions.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, lineHeight = 22.sp)
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ImpactCard(
                        value = "${issues.count { it.status == "Resolved" }}", 
                        label = "Issues Resolved", 
                        icon = Icons.Default.CheckCircle, 
                        showPulse = false, 
                        isLoading = isLoading,
                        modifier = Modifier.weight(1f)
                    )
                    ImpactCard(
                        value = "${issues.count { it.status != "Resolved" }}", 
                        label = "Total Active Reports", 
                        icon = Icons.Default.Radar, 
                        showPulse = true, 
                        isLoading = isLoading,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nearby Reports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Surface(onClick = onRefresh, color = SurfaceDark, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, DividerGray.copy(alpha = 0.6f))) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentGreen)
                            Spacer(Modifier.width(8.dp))
                            Text("REFRESH", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp), color = TextPrimary)
                        }
                    }
                }
            }
            if (issues.isEmpty() && !isLoading) {
                item { 
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp), 
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) { 
                        Text(
                            "No active reports in your area yet — be the first to report an issue", 
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                        Text(
                            "REPORT ISSUE →",
                            color = StatusAmber,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.clickable { onReportNavigate() }
                        )
                    } 
                }
            } else {
                items(issues, key = { it.id }) { issue -> NearbyReportCard(issue = issue, onClick = { onIssueClick(issue.id) }) }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
        if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentGreen)
    }
}

// ─── IMPACT CARD ────────────────────────────────────────────────────────────

@Composable
fun ImpactCard(value: String, label: String, icon: ImageVector, showPulse: Boolean, isLoading: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, 
        targetValue = 1f, 
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = LinearEasing), repeatMode = RepeatMode.Reverse), 
        label = "alpha"
    )

    Box(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0F1419)))).border(1.dp, Brush.linearGradient(listOf(DividerGray, Color.Transparent)), RoundedCornerShape(20.dp))) {
        // Optimized Decorative Wave - drawWithCache ensures Path is only created once
        val waveColor = AccentGreen.copy(alpha = 0.05f)
        Spacer(
            modifier = Modifier.matchParentSize().padding(top = 40.dp).drawWithCache {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.85f)
                    quadraticTo(size.width * 0.2f, size.height * 0.4f, size.width * 0.5f, size.height * 0.75f)
                    quadraticTo(size.width * 0.8f, size.height * 0.3f, size.width, size.height * 0.6f)
                }
                onDrawBehind {
                    drawPath(path, color = waveColor, style = Stroke(width = 1.5.dp.toPx()))
                }
            }
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = AccentGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(18.dp), tint = FocusedGreen) }
                }
                // Optimized pulsing dot: Using graphicsLayer ensures animation runs on GPU, skipping CPU recomposition
                if (showPulse) Box(modifier = Modifier.size(6.dp).clip(CircleShape).graphicsLayer { this.alpha = alpha }.background(AccentGreen))
            }
            Spacer(Modifier.height(18.dp))
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AccentGreen, strokeWidth = 2.dp)
                Spacer(Modifier.height(12.dp))
            } else {
                Text(value, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, letterSpacing = (-1.5).sp, fontSize = 32.sp), color = TextPrimary)
            }
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.2.sp, fontSize = 9.sp), color = TextSecondary, lineHeight = 14.sp)
        }
    }
}

// ─── NEARBY REPORT CARD ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyReportCard(issue: Issue, onClick: () -> Unit) {
    val timeAgo = remember(issue.timestamp) {
        val seconds = (System.currentTimeMillis() - (issue.timestamp ?: 0)) / 1000
        when { seconds < 60 -> "Just now"; seconds < 3600 -> "${seconds / 60}m ago"; seconds < 86400 -> "${seconds / 3600}h ago"; else -> "${seconds / 86400}d ago" }
    }
    val statusColor = if (issue.status == "Resolved") AccentGreen else StatusAmber

    Card(modifier = Modifier.fillMaxWidth().border(1.dp, DividerGray.copy(alpha = 0.4f), RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), onClick = onClick) {
        Column {
            Box {
                AsyncImage(
                    model = issue.imageUrl, 
                    contentDescription = null, 
                    modifier = Modifier.fillMaxWidth().height(210.dp).drawWithCache {
                        val gradient = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)), startY = size.height * 0.7f)
                        onDrawWithContent {
                            drawContent()
                            drawRect(gradient)
                        }
                    }, 
                    contentScale = ContentScale.Crop
                )
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(color = Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(6.dp)) { Text((issue.category ?: "General").uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Black), color = Color.White) }
                    Surface(color = AccentGreen, shape = RoundedCornerShape(6.dp)) { Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.AutoMirrored.Filled.TrendingUp, null, modifier = Modifier.size(12.dp), tint = Color.Black); Spacer(Modifier.width(4.dp)); Text("${issue.upvotes ?: 0}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = Color.Black) } }
                }
                Surface(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp), color = statusColor, shape = RoundedCornerShape(4.dp)) { Text((issue.status ?: "Pending").uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Black), color = Color.Black) }
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) { Text(issue.title.uppercase(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis); Text(timeAgo.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black), color = AccentGreen) }
                Spacer(Modifier.height(8.dp))
                Text(issue.description ?: "", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = DividerGray.copy(alpha = 0.5f), shape = CircleShape, modifier = Modifier.size(26.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.PinDrop, null, tint = AccentGreen, modifier = Modifier.size(14.dp)) } }
                        Spacer(Modifier.width(10.dp)); Text("WARD ${issue.id.take(4).uppercase()}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
                    }
                    Text("LOG FILE →", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp), color = AccentGreen)
                }
            }
        }
    }
}

// ─── MAP TAB ────────────────────────────────────────────────────────────────

@Composable
fun MapTab(issues: List<Issue>, onIssueClick: (String) -> Unit, routingIssueId: String? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapView = remember { MapView(context) }
    var roadOverlay by remember { mutableStateOf<Polyline?>(null) }
    var userLocationMarker by remember { mutableStateOf<Marker?>(null) }
    
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted -> if (isGranted) { scope.launch { val location = LocationHelper.getCurrentLocation(context); if (location != null) mapView.controller.animateTo(GeoPoint(location.latitude, location.longitude)) } } }

    LaunchedEffect(routingIssueId, issues) {
        if (routingIssueId != null && issues.isNotEmpty()) {
            val destinationIssue = issues.find { it.id == routingIssueId }
            if (destinationIssue != null) {
                val userLoc = LocationHelper.getCurrentLocation(context)
                if (userLoc != null) {
                    withContext(Dispatchers.IO) {
                        val roadManager = OSRMRoadManager(context, context.packageName)
                        val waypoints = arrayListOf(GeoPoint(userLoc.latitude, userLoc.longitude), GeoPoint(destinationIssue.latitude ?: 0.0, destinationIssue.longitude ?: 0.0))
                        val road = roadManager.getRoad(waypoints)
                        if (road.mStatus == org.osmdroid.bonuspack.routing.Road.STATUS_OK) { roadOverlay = RoadManager.buildRoadOverlay(road) }
                        else { withContext(Dispatchers.Main) { val straightLine = Polyline().apply { setPoints(waypoints); outlinePaint.color = android.graphics.Color.BLUE; outlinePaint.strokeWidth = 5f }; roadOverlay = straightLine } }
                    }
                    mapView.controller.animateTo(GeoPoint(destinationIssue.latitude ?: 0.0, destinationIssue.longitude ?: 0.0))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location = LocationHelper.getCurrentLocation(context)
            if (location != null) {
                mapView.controller.setCenter(GeoPoint(location.latitude, location.longitude))
                val marker = Marker(mapView)
                marker.position = GeoPoint(location.latitude, location.longitude); marker.title = "Your Location"; marker.icon?.setTint(android.graphics.Color.BLUE); userLocationMarker = marker
            } else { mapView.controller.setCenter(GeoPoint(19.0760, 72.8777)) }
        } else { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION); mapView.controller.setCenter(GeoPoint(19.0760, 72.8777)) }
    }

    val filter = remember {
        val matrix = android.graphics.ColorMatrix(); matrix.setSaturation(0f)
        val inverseMatrix = android.graphics.ColorMatrix(floatArrayOf(-1.0f, 0f, 0f, 0f, 255f, 0f, -1.0f, 0f, 0f, 255f, 0f, 0f, -1.0f, 0f, 255f, 0f, 0f, 0f, 1.0f, 0f))
        matrix.postConcat(inverseMatrix); android.graphics.ColorMatrixColorFilter(matrix)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView.apply { setTileSource(TileSourceFactory.MAPNIK); setMultiTouchControls(true); controller.setZoom(14.0); overlayManager.tilesOverlay.setColorFilter(filter) } },
            modifier = Modifier.fillMaxSize(),
            update = { mv ->
                // Optimized Marker handling: only clear and redraw if the list actually changes
                mv.overlays.clear()
                roadOverlay?.let { mv.overlays.add(it) }
                userLocationMarker?.let { mv.overlays.add(it) }
                issues.forEach { issue ->
                    val marker = Marker(mv)
                    marker.position = GeoPoint(issue.latitude ?: 0.0, issue.longitude ?: 0.0); marker.title = issue.title
                    marker.icon?.setTint(if (issue.status == "Resolved") android.graphics.Color.GREEN else if (issue.status == "In Progress") android.graphics.Color.rgb(255, 183, 77) else android.graphics.Color.YELLOW)
                    marker.setOnMarkerClickListener { _, _ -> onIssueClick(issue.id); true }
                    mv.overlays.add(marker)
                }
                mv.invalidate()
            }
        )
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter).border(1.dp, DividerGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), color = DarkBackground.copy(alpha = 0.96f)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = AccentGreen, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(14.dp))
                var typingState by remember { mutableStateOf("") }
                BasicTextField(value = typingState, onValueChange = { typingState = it }, modifier = Modifier.weight(1f), textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium), cursorBrush = SolidColor(AccentGreen), singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = {
                    scope.launch {
                        val geocoder = GeocoderNominatim(context.packageName)
                        val results = withContext(Dispatchers.IO) { try { geocoder.getFromLocationName(typingState, 1) } catch (e: Exception) { null } }
                        if (results != null && results.isNotEmpty()) { val loc = results[0]; mapView.controller.animateTo(GeoPoint(loc.latitude, loc.longitude)); mapView.controller.setZoom(16.0) }
                        else { Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show() }
                    }
                }), decorationBox = { innerTextField -> if (typingState.isEmpty()) { Text("Search location...", color = TextSecondary, fontSize = 16.sp) }; innerTextField() })
                IconButton(onClick = { }) { Icon(Icons.Default.Search, null, tint = TextSecondary) }
            }
        }
        if (issues.isNotEmpty()) {
            Column(modifier = Modifier.align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f)))).padding(bottom = 16.dp)) {
                Surface(color = AccentGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp), modifier = Modifier.padding(start = 16.dp, bottom = 12.dp).border(1.dp, AccentGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp))) { Text(" RECENT REPORTS ", style = MaterialTheme.typography.labelSmall, color = AccentGreen, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), letterSpacing = 1.5.sp) }
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Top Issues Nearby", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Black) }
                Spacer(Modifier.height(16.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) { items(issues) { issue -> MapIssueCard(issue, onIssueClick) } }
            }
        }
        Column(modifier = Modifier.align(Alignment.TopEnd).padding(top = 96.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MapControlButton(icon = Icons.Default.Add, onClick = { mapView.controller.zoomIn() })
            MapControlButton(icon = Icons.Default.Remove, onClick = { mapView.controller.zoomOut() })
            MapControlButton(icon = Icons.Default.Layers, onClick = { })
        }
    }
}

@Composable
fun MapControlButton(icon: ImageVector, onClick: () -> Unit = {}) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = DarkBackground.copy(alpha = 0.9f), modifier = Modifier.size(46.dp).border(1.dp, DividerGray.copy(alpha = 0.6f), RoundedCornerShape(12.dp))) { Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(22.dp)) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapIssueCard(issue: Issue, onIssueClick: (String) -> Unit) {
    Card(modifier = Modifier.width(310.dp).border(1.dp, DividerGray.copy(alpha = 0.4f), RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), onClick = { onIssueClick(issue.id) }) {
        Box(modifier = Modifier.background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0F1419)))).padding(20.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Surface(color = StatusAmber.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(46.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Radar, null, tint = StatusAmber, modifier = Modifier.size(24.dp)) } }
                    Column(horizontalAlignment = Alignment.End) {
                        val statusColor = if (issue.status == "Resolved") AccentGreen else StatusAmber
                        Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) { Text(" " + (issue.status ?: "Pending").uppercase() + " ", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = statusColor, fontWeight = FontWeight.Black) }
                        Text("${issue.upvotes ?: 0} SIGNALS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 0.5.sp), color = TextSecondary, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.height(18.dp)); Text(issue.title.uppercase(), style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 0.5.sp, fontWeight = FontWeight.Black), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp)); Text(issue.description ?: "", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, maxLines = 2, lineHeight = 20.sp)
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AccessTime, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Text(" WARD ${issue.id.take(4).uppercase()}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = TextSecondary)
                    }
                    Button(onClick = { }, modifier = Modifier.height(38.dp), contentPadding = PaddingValues(horizontal = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = DividerGray.copy(alpha = 0.6f)), shape = RoundedCornerShape(10.dp)) { Text("VIEW DETAILS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)) }
                }
            }
        }
    }
}

@Composable
fun ProfileTab(onNavigateToOfficerDashboard: () -> Unit, onNavigateToPublicScorecard: () -> Unit, onLogout: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Spacer(Modifier.height(16.dp)) }
        item {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0F1419)))).border(1.dp, DividerGray.copy(alpha = 0.5f), RoundedCornerShape(24.dp))) {
                Row(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) { Surface(modifier = Modifier.size(90.dp), color = AccentGreen.copy(alpha = 0.1f), shape = CircleShape) {}; AsyncImage(model = "https://ui-avatars.com/api/?name=User&background=4ADE80&color=fff&size=100", contentDescription = "Profile", modifier = Modifier.size(80.dp).clip(CircleShape).border(2.dp, AccentGreen, CircleShape)) }
                    Spacer(Modifier.width(20.dp)); Column { Text("Citizen User", style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp), fontWeight = FontWeight.Black); Surface(color = AccentGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp), modifier = Modifier.padding(top = 6.dp).border(1.dp, AccentGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp))) { Text(" ACTIVE MEMBER ", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.sp), color = AccentGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) } }
                }
            }
        }
        item { Text("QUICK ACTIONS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp), color = TextSecondary, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
        item { ProfileMenuCard(icon = Icons.Default.Dashboard, title = "Officer Dashboard", subtitle = "View government action panel", onClick = onNavigateToOfficerDashboard) }
        item { ProfileMenuCard(icon = Icons.Default.BarChart, title = "Public Scorecard", subtitle = "Ward performance & statistics", onClick = onNavigateToPublicScorecard) }
        item { ProfileMenuCard(icon = Icons.AutoMirrored.Filled.Logout, title = "Sign Out", subtitle = "Log out of your account", onClick = onLogout, isDestructive = true) }
        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
fun ProfileMenuCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, isDestructive: Boolean = false) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().border(1.dp, DividerGray.copy(alpha = 0.4f), RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) { Box(modifier = Modifier.background(Brush.linearGradient(listOf(SurfaceDark.copy(alpha = 0.8f), SurfaceDark))).padding(18.dp)) { Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { val tint = if (isDestructive) UrgencyRed else AccentGreen; Surface(color = tint.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(46.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp)) } }; Spacer(Modifier.width(18.dp)); Column(modifier = Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = if (isDestructive) UrgencyRed else TextPrimary); Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary) }; Icon(Icons.Default.ChevronRight, null, tint = DividerGray) } } }
}
