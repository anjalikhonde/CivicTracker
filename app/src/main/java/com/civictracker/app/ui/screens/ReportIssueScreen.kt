package com.civictracker.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.civictracker.app.ui.theme.*
import com.civictracker.app.ui.viewmodel.ReportIssueViewModel
import com.civictracker.app.ui.viewmodel.ReportUiState
import com.civictracker.app.util.LocationHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, kotlinx.coroutines.FlowPreview::class)
@Composable
fun ReportIssueScreen(
    viewModel: ReportIssueViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Road") }
    var priority by remember { mutableStateOf("Medium") }
    var expanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Tracks if the user has manually selected a category to stop AI auto-overwriting
    var isManualCategorySelection by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val aiAnalysis by viewModel.aiAnalysis.collectAsState()
    val predictedCategory by viewModel.predictedCategory.collectAsState()
    val isClassifying by viewModel.isClassifying.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val similarIssues by viewModel.similarIssues.collectAsState()
    
    val scope = rememberCoroutineScope()
    var showChatbot by remember { mutableStateOf(false) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Road", "Water", "Waste", "Lighting", "Drainage", "Electricity", "General")
    val priorities = listOf("High", "Medium", "Low")

    // 1. Background Logic: Category suggestion as user types in title OR description
    LaunchedEffect(Unit) {
        snapshotFlow { title to description }
            .debounce(1500)
            .collectLatest { (t, d) ->
                if (t.length > 3 || d.length > 10) {
                    Log.d("MLClassify", "Screen: Triggering classification for: '$t' + '$d'")
                    viewModel.classifyComplaint(t, d)
                }
            }
    }

    // 2. Auto-fill Logic: Apply predicted category ONLY if user hasn't made a manual choice
    LaunchedEffect(predictedCategory) {
        if (!isManualCategorySelection) {
            predictedCategory?.let { pred ->
                val trimmedPred = pred.trim()
                categories.find { it.equals(trimmedPred, ignoreCase = true) }?.let { matched ->
                    category = matched
                }
            }
        }
    }

    // 3. User-Triggered AI logic: Update fields when AI Smart Check completes
    LaunchedEffect(aiAnalysis) {
        aiAnalysis?.let { result ->
            Log.d("SmartCheck", "Screen: Auto-populating fields from AI analysis")
            
            // AI Smart Check is considered a "Manual/Verified" update, so we lock it
            isManualCategorySelection = true

            // Auto-set Category
            categories.find { it.equals(result.category.trim(), ignoreCase = true) }?.let {
                category = it
            }

            // Auto-set Description (Professional Rewrite)
            if (result.professionalDescription.isNotBlank()) {
                description = result.professionalDescription
            }

            // Auto-set Priority
            priorities.find { it.equals(result.priority.trim(), ignoreCase = true) }?.let {
                priority = it
            }

            // Set Title if it's still blank
            if (title.isBlank() && result.identifiedObject != "Unknown") {
                title = "Report: ${result.identifiedObject}"
            }
            
            Toast.makeText(context, "AI has updated category and description", Toast.LENGTH_SHORT).show()
        }
    }

    fun fetchLocation() {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            locationError = "Permission required."
            return
        }
        isFetchingLocation = true
        locationError = null
        scope.launch {
            try {
                val location = LocationHelper.getCurrentLocation(context)
                if (location != null) {
                    viewModel.setLocation(location)
                } else {
                    locationError = "GPS failed. Is location on?"
                }
            } catch (e: Exception) {
                locationError = "Location error"
            } finally {
                isFetchingLocation = false
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) fetchLocation()
        else locationError = "Permission denied"
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            viewModel.clearAiAnalysis()
            // Reset manual flag when new evidence is added to allow AI to re-suggest
            isManualCategorySelection = false
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            capturedBitmap = try {
                if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
            } catch (e: Exception) { null }
            viewModel.clearAiAnalysis()
            isManualCategorySelection = false
        }
    }

    var showImageSourceDialog by remember { mutableStateOf(false) }

    // Auto-fetch location
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) fetchLocation()
        else locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ReportUiState.Success -> onSuccess()
            is ReportUiState.Error -> Toast.makeText(context, (uiState as ReportUiState.Error).message, Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI SMART REPORT", style = MaterialTheme.typography.labelLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    // Assistant button - This is the top-right menu icon
                    IconButton(onClick = { showChatbot = true }) {
                        Icon(Icons.Default.SupportAgent, "Assistant", tint = AccentGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground, titleContentColor = TextPrimary)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            
            // 1. Photo Section
            val dashStroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .clickable { showImageSourceDialog = true }
                    .drawBehind {
                        drawRoundRect(
                            color = DividerGray,
                            style = dashStroke,
                            cornerRadius = CornerRadius(12.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (capturedBitmap != null) {
                    Image(bitmap = capturedBitmap!!.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, null, tint = AccentGreen, modifier = Modifier.size(32.dp))
                        Text("ADD PHOTO EVIDENCE", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. Main Details
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("REPORT TITLE") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors(), shape = RoundedCornerShape(8.dp))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = description, 
                onValueChange = { description = it }, 
                label = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("DETAILED DESCRIPTION")
                        if (isClassifying) {
                            Spacer(Modifier.width(8.dp))
                            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = AccentGreen)
                        }
                    }
                }, 
                modifier = Modifier.fillMaxWidth(), 
                minLines = 3, 
                colors = fieldColors(), 
                shape = RoundedCornerShape(8.dp)
            )

            // 3. AI Smart Check Button
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { 
                    if (description.isBlank() && capturedBitmap == null) {
                        Toast.makeText(context, "Please add a description or photo first", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.analyzeIssue(description, capturedBitmap) 
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAnalyzing,
                colors = ButtonDefaults.buttonColors(containerColor = ActionGreen, contentColor = Color.Black)
            ) {
                if (isAnalyzing) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                else Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("RUN AI SMART CHECK", fontWeight = FontWeight.Bold)
                }
            }

            // AI Analysis Results (Optional Insights Card)
            AnimatedVisibility(visible = aiAnalysis != null) {
                aiAnalysis?.let { result ->
                    Card(modifier = Modifier.padding(top = 12.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceDark), border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.3f))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("AI INSIGHTS", style = MaterialTheme.typography.labelSmall, color = AccentGreen)
                            }
                            Spacer(Modifier.height(8.dp))
                            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val sentimentColor = if (result.sentiment.contains("Urgent", true)) UrgencyRed else AccentGreen
                                SuggestionChip(onClick = {}, label = { Text(result.sentiment.uppercase()) }, colors = SuggestionChipDefaults.suggestionChipColors(labelColor = sentimentColor))
                                SuggestionChip(onClick = {}, label = { Text(result.identifiedObject.uppercase()) })
                                SuggestionChip(onClick = {}, label = { Text(result.priority.uppercase()) })
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(result.imageInsight, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            // Duplicate Detection
            AnimatedVisibility(visible = similarIssues.isNotEmpty()) {
                Card(modifier = Modifier.padding(top = 12.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = UrgencyRed.copy(alpha = 0.1f)), border = BorderStroke(1.dp, UrgencyRed.copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = UrgencyRed, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("SIMILAR ISSUES NEARBY", style = MaterialTheme.typography.labelSmall, color = UrgencyRed)
                        }
                        similarIssues.take(2).forEach { issue ->
                            Text("• ${issue.title}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 4. Dropdowns
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = category, 
                        onValueChange = {}, 
                        readOnly = true, 
                        label = { Text("CATEGORY") }, 
                        trailingIcon = { 
                            if (isClassifying) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            else ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) 
                        }, 
                        modifier = Modifier.menuAnchor().fillMaxWidth(), 
                        colors = fieldColors(), 
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = SurfaceDark) {
                        categories.forEach { cat -> 
                            DropdownMenuItem(
                                text = { Text(cat.uppercase()) }, 
                                onClick = { 
                                    category = cat
                                    isManualCategorySelection = true // USER OVERRIDE: Stop auto-fill from changing this
                                    expanded = false 
                                }
                            ) 
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = priorityExpanded, onExpandedChange = { priorityExpanded = !priorityExpanded }, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(value = priority, onValueChange = {}, readOnly = true, label = { Text("PRIORITY") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = fieldColors(), shape = RoundedCornerShape(8.dp))
                    ExposedDropdownMenu(expanded = priorityExpanded, onDismissRequest = { priorityExpanded = false }, containerColor = SurfaceDark) {
                        priorities.forEach { p -> DropdownMenuItem(text = { Text(p.uppercase()) }, onClick = { priority = p; priorityExpanded = false }) }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            
            // 5. Location Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, if (currentLocation != null) AccentGreen.copy(alpha = 0.3f) else UrgencyRed.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.LocationOn, null, tint = if (currentLocation != null) AccentGreen else UrgencyRed, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(if (currentLocation != null) "LOCATION SECURED" else "LOCATION REQUIRED", style = MaterialTheme.typography.labelSmall, color = if (currentLocation != null) AccentGreen else UrgencyRed)
                            if (currentLocation != null) {
                                Text("${currentLocation!!.latitude.toString().take(7)}, ${currentLocation!!.longitude.toString().take(7)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            } else if (locationError != null) {
                                Text(locationError!!, style = MaterialTheme.typography.bodySmall, color = UrgencyRed)
                            }
                        }
                    }
                    if (currentLocation == null || locationError != null) {
                        Button(
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) fetchLocation()
                                else locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen.copy(alpha = 0.1f), contentColor = AccentGreen)
                        ) {
                            if (isFetchingLocation) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AccentGreen, strokeWidth = 2.dp)
                            else Text(if (locationError != null) "RETRY" else "FETCH", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // 6. Final Submit Button
            Button(
                onClick = { 
                    if (title.isBlank()) {
                        Toast.makeText(context, "Please enter a report title", Toast.LENGTH_SHORT).show()
                    } else if (currentLocation == null) {
                        Toast.makeText(context, "Waiting for location...", Toast.LENGTH_SHORT).show()
                        fetchLocation()
                    } else {
                        viewModel.submitIssue(title, description, category, priority, capturedBitmap) 
                    }
                }, 
                modifier = Modifier.fillMaxWidth().height(56.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.Black),
                enabled = uiState !is ReportUiState.Loading
            ) {
                if (uiState is ReportUiState.Loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                else {
                    Icon(Icons.AutoMirrored.Filled.FactCheck, null)
                    Spacer(Modifier.width(12.dp))
                    Text("SUBMIT OFFICIAL REPORT", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    // AI Assistant Chatbot Dialog
    if (showChatbot) {
        AlertDialog(onDismissRequest = { showChatbot = false }, containerColor = SurfaceDark, title = { Text("AI FILING ASSISTANT", color = AccentGreen) }, text = {
            Column {
                Text("How can I help you today?", color = TextPrimary)
                Spacer(Modifier.height(12.dp))
                AssistantOption("Suggest Category based on Photo") { 
                    showChatbot = false
                    viewModel.analyzeIssue(description, capturedBitmap) 
                }
                AssistantOption("Improve my Description") { 
                    // This option now triggers the Smart Check to get a professional rewrite
                    showChatbot = false
                    viewModel.analyzeIssue(description, capturedBitmap)
                }
            }
        }, confirmButton = { TextButton(onClick = { showChatbot = false }) { Text("CLOSE", color = TextSecondary) } })
    }

    if (showImageSourceDialog) {
        // ... (Image Source Dialog Remains Same)
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            containerColor = SurfaceDark,
            title = { Text("SELECT SOURCE") },
            confirmButton = {
                TextButton(onClick = {
                    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    showImageSourceDialog = false
                }) { Text("CAMERA", color = AccentGreen) }
            },
            dismissButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showImageSourceDialog = false
                }) { Text("GALLERY", color = AccentGreen) }
            }
        )
    }
}

@Composable
fun AssistantOption(text: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, DividerGray)) {
        Text(text, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
    }
}

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FocusedGreen, unfocusedBorderColor = DividerGray,
    focusedLabelColor = FocusedGreen, unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
    focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(modifier: Modifier, horizontalArrangement: Arrangement.Horizontal, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(modifier = modifier, horizontalArrangement = horizontalArrangement) { content() }
}
