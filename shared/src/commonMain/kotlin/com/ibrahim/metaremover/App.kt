package com.ibrahim.metaremover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ibrahim.metaremover.domain.ImageMetadata
import com.ibrahim.metaremover.domain.model.ImageAnalysis
import com.ibrahim.metaremover.domain.model.MetadataCategory
import com.ibrahim.metaremover.picker.ImagePicker
import com.ibrahim.metaremover.presentation.MainViewModel
import com.ibrahim.metaremover.presentation.ProgressPhase

@Composable
fun App(
    viewModel: MainViewModel,
    updateManager: AppUpdateManager,
    reviewManager: AppReviewManager
) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF64B5F6),
            secondary = Color(0xFF81C784),
            error = Color(0xFFE57373),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onSurface = Color.White,
            surfaceVariant = Color(0xFF2C2C2C)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MetaRemoverScreen(
                viewModel = viewModel,
                updateManager = updateManager,
                reviewManager = reviewManager
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetaRemoverScreen(
    viewModel: MainViewModel,
    updateManager: AppUpdateManager,
    reviewManager: AppReviewManager
) {
    val state by viewModel.state.collectAsState()
    val picker = remember { ImagePicker() }

    // Bottom Sheet Control States
    var showUpdateBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var showFullImage by remember { mutableStateOf(false) }

    val launchPicker = picker.pickImage { bytes ->
        viewModel.onImagePicked(bytes)
    }

    // 1. Check for updates seamlessly when the user opens the application
    LaunchedEffect(Unit) {
        updateManager.checkForUpdates {
            showUpdateBottomSheet = true
        }
    }

    // 2. Trigger the rating system automatically right after a successful metadata save action
    LaunchedEffect(state.isSaving) {
        if (!state.isSaving && state.cleanedBytes != null && state.error == null) {
            reviewManager.launchReviewFlow()
        }
    }
    @Composable
    fun FullImageDialog(
        bytes: ByteArray,
        onDismiss: () -> Unit
    ) {
        val imageBitmap = remember(bytes) {
            bytes.toImageBitmap()
        }

        Dialog(
            onDismissRequest = onDismiss
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Full screen image preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            onDismiss()
                        }
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                        .size(48.dp)
                        .clickable {
                            onDismiss()
                        },
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    Scaffold(
        bottomBar = {
            if (state.originalBytes != null) {
                BottomActions(
                    onPickNew = launchPicker,
                    onWipe = viewModel::onCleanImage,
                    onSave = viewModel::onSaveImage,
                    canWipe = state.cleanedBytes == null,
                    isSaving = state.isSaving
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showFullImage && state.originalBytes != null) {
                FullImageDialog(
                    bytes = state.cleanedBytes ?: state.originalBytes!!,
                    onDismiss = {
                        showFullImage = false
                    }
                )
            }
            if (state.originalBytes == null) {
                Column {
                    EmptyStateView(onPickImage = launchPicker)
                    state.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    StatusIndicator(
                        isCleaned = state.cleanedBytes != null,
                        privacyScore = state.analysis?.privacyScore
                    )

                    state.analysis?.let {
                        PrivacyScoreIndicator(score = it.privacyScore)
                    }

                    ImageContainer(
                        bytes = state.cleanedBytes ?: state.originalBytes!!,
                        metadata = state.cleanedMetadata ?: state.originalMetadata,
                        analysis = state.analysis,
                        onImageClick = {
                            showFullImage = true
                        }
                    )
                    state.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 3. Compose Multiplatform Native-Looking Dark Bottom Sheet for Updates
            if (showUpdateBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showUpdateBottomSheet = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✨ New Version Available!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Update now to access the latest features, better performance, and bug fixes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                showUpdateBottomSheet = false
                                updateManager.startFlexibleUpdate()
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("DOWNLOAD NOW", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            if (state.isProcessing || state.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    ScanProgressOverlay(
                        phase = state.progressPhase ?: ProgressPhase.ANALYZING,
                        message = state.loadingMessage
                    )
                }
            }
        }
    }


}


/**
 * The "Guardian Scan" progress experience: instead of a bare spinner, the user
 * watches their photo being forensically scanned. A bright line sweeps over an
 * image reticle, a radar ring orbits it, focus brackets frame the corners and a
 * glow pulses underneath — with colour, glyph and copy driven by [phase] so
 * analysing / stripping / saving each feel like a distinct act.
 */
@Composable
private fun ScanProgressOverlay(
    phase: ProgressPhase,
    message: String?
) {
    val accent = when (phase) {
        ProgressPhase.ANALYZING -> Color(0xFF64B5F6) // scanning blue
        ProgressPhase.STRIPPING -> Color(0xFFFF7043) // shredding orange
        ProgressPhase.SAVING -> Color(0xFF66BB6A)    // protected green
    }
    val glyph = when (phase) {
        ProgressPhase.ANALYZING -> "🔍"
        ProgressPhase.STRIPPING -> "🧨"
        ProgressPhase.SAVING -> "💾"
    }

    val transition = rememberInfiniteTransition(label = "scan")
    val sweep by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "sweep"
    )
    val ringAngle by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Restart),
        label = "ring"
    )
    val pulse by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val dotPhase by transition.animateFloat(
        initialValue = 0f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "dots"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(168.dp), contentAlignment = Alignment.Center) {
            // The photo being inspected, sitting behind the scanner overlay.
            Text(glyph, fontSize = 46.sp)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val minDim = size.minDimension
                val center = Offset(size.width / 2f, size.height / 2f)

                // Pulsing radial glow underneath everything.
                val glowR = minDim * 0.46f * (0.92f + 0.08f * pulse)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.28f), Color.Transparent),
                        center = center, radius = glowR
                    ),
                    radius = glowR, center = center
                )

                // Rotating radar ring with a bright leading arc + dot.
                val ringR = minDim * 0.46f
                drawCircle(
                    color = accent.copy(alpha = 0.18f),
                    radius = ringR, center = center, style = Stroke(width = 2f)
                )
                rotate(ringAngle, pivot = center) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, accent.copy(alpha = 0.9f)), center
                        ),
                        startAngle = 0f, sweepAngle = 90f, useCenter = false,
                        topLeft = Offset(center.x - ringR, center.y - ringR),
                        size = Size(ringR * 2, ringR * 2),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                    drawCircle(color = accent, radius = 4f, center = Offset(center.x + ringR, center.y))
                }

                // Scanner frame with grid + sweeping scan line (clipped to the frame).
                val frame = minDim * 0.58f
                val left = center.x - frame / 2f
                val top = center.y - frame / 2f
                clipRect(left, top, left + frame, top + frame) {
                    val grid = 4
                    for (i in 1 until grid) {
                        val gx = left + frame * i / grid
                        val gy = top + frame * i / grid
                        drawLine(accent.copy(alpha = 0.10f), Offset(gx, top), Offset(gx, top + frame), 1f)
                        drawLine(accent.copy(alpha = 0.10f), Offset(left, gy), Offset(left + frame, gy), 1f)
                    }
                    val scanY = top + frame * sweep
                    val trail = frame * 0.35f
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.Transparent, accent.copy(alpha = 0.35f)),
                            startY = scanY - trail, endY = scanY
                        ),
                        topLeft = Offset(left, scanY - trail),
                        size = Size(frame, trail)
                    )
                    drawLine(
                        color = accent,
                        start = Offset(left, scanY), end = Offset(left + frame, scanY),
                        strokeWidth = 3f, cap = StrokeCap.Round
                    )
                }

                // Corner focus brackets, like a camera reticle locking on.
                val bl = frame * 0.24f
                val r = left + frame
                val b = top + frame
                val corners = listOf(
                    Triple(Offset(left, top), Offset(left + bl, top), Offset(left, top + bl)),
                    Triple(Offset(r, top), Offset(r - bl, top), Offset(r, top + bl)),
                    Triple(Offset(left, b), Offset(left + bl, b), Offset(left, b - bl)),
                    Triple(Offset(r, b), Offset(r - bl, b), Offset(r, b - bl))
                )
                corners.forEach { (corner, h, v) ->
                    drawLine(accent, corner, h, 3f, cap = StrokeCap.Round)
                    drawLine(accent, corner, v, 3f, cap = StrokeCap.Round)
                }
            }
        }

        message?.let {
            Spacer(modifier = Modifier.height(20.dp))
            val dots = ".".repeat(dotPhase.toInt().coerceIn(0, 3))
            Text(
                text = it.trimEnd('.', ' ') + dots,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PrivacyScoreIndicator(score: Int) {
    val color = when {
        score >= 90 -> Color(0xFF4CAF50) // Green
        score >= 70 -> Color(0xFF8BC34A) // Light Green
        score >= 50 -> Color(0xFFFFEB3B) // Yellow
        score >= 30 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }

    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "PRIVACY SCORE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "$score/100",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = score / 100f,
            modifier = Modifier.width(150.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun StatusIndicator(isCleaned: Boolean, privacyScore: Int?) {
    // A risk is "detected" only when the privacy analysis actually deducted points.
    // A perfect 100/100 score means nothing sensitive was found, so the banner must
    // not claim risks just because the image hasn't been stripped yet.
    val hasRisks = privacyScore != null && privacyScore < 100
    val isSafe = isCleaned || !hasRisks

    val label = when {
        isCleaned -> "✓ ALL METADATA REMOVED"
        hasRisks -> "⚠ PRIVACY RISKS DETECTED"
        else -> "✓ NO PRIVACY RISKS DETECTED"
    }
    val accent = if (isSafe) Color(0xFF4CAF50) else Color(0xFFFFEB3B)
    val textColor = if (isSafe) Color(0xFF81C784) else Color(0xFFFFEB3B)

    Surface(
        color = (if (isSafe) Color(0xFF2E7D32) else Color(0xFFFBC02D)).copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun EmptyStateView(onPickImage: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    RoundedCornerShape(25.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("🖼️", fontSize = 40.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Clean Your Media",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Remove GPS, device info, and tracking data from your photos before sharing them online.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onPickImage,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("SELECT PHOTO", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun ImageContainer(
    bytes: ByteArray,
    metadata: ImageMetadata?,
    analysis: ImageAnalysis?,
    onImageClick: () -> Unit
) {
    val imageBitmap = remember(bytes) {
        bytes.toImageBitmap()
    }

    var isExpanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {

        Image(
            bitmap = imageBitmap,
            contentDescription = "Selected image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 450.dp)
                .background(Color.Black)
                .clickable {
                    onImageClick()
                }
        )


        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isExpanded = !isExpanded
                    }
                    .padding(20.dp),

                verticalAlignment = Alignment.CenterVertically,

                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = if (isExpanded)
                        "CLOSE SECURITY LOGS"
                    else
                        "VIEW SECURITY LOGS",

                    style = MaterialTheme.typography.labelLarge,

                    fontWeight = FontWeight.Black,

                    color = MaterialTheme.colorScheme.primary,

                    letterSpacing = 1.sp
                )


                Text(
                    text = if (isExpanded)
                        "▲"
                    else
                        "▼",

                    color = MaterialTheme.colorScheme.primary,

                    fontSize = 12.sp
                )
            }
        }


        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {

            if (analysis != null) {
                AnalysisInfo(
                    analysis
                )
            } else {
                MetadataInfo(
                    metadata
                )
            }
        }
    }
}

@Composable
fun AnalysisInfo(analysis: ImageAnalysis) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(20.dp)
            .heightIn(max = 450.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MetadataGroup("AI & AUTHENTICITY", Color.Red) {
            DetailRow(
                label = "AI Generated",
                value = if (analysis.ai.isAIGenerated) "YES" else null
            )
            DetailRow(label = "AI Provider", value = analysis.ai.provider?.name)
            DetailRow(label = "AI Model", value = analysis.ai.model)
            DetailRow(
                label = "Confidence",
                value = analysis.ai.confidence?.let { "${(it * 100).toInt()}%" })
            DetailRow(label = "Prompt", value = analysis.ai.prompt)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("LOCATION", Color.Cyan) {
            DetailRow(label = "Latitude", value = analysis.gps.latitude?.toString())
            DetailRow(label = "Longitude", value = analysis.gps.longitude?.toString())
            DetailRow(label = "Altitude", value = analysis.gps.altitude?.toString())
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("CAMERA", Color.Yellow) {
            DetailRow(label = "Make", value = analysis.camera.make)
            DetailRow(label = "Model", value = analysis.camera.model)
            DetailRow(label = "Body Serial", value = analysis.camera.bodySerialNumber)
            DetailRow(label = "Lens Serial", value = analysis.camera.lensSerialNumber)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("TECHNICAL", Color.Green) {
            DetailRow(
                label = "Resolution",
                value = if (analysis.basic.width != null) "${analysis.basic.width} x ${analysis.basic.height}" else null
            )
            DetailRow(label = "File Size", value = analysis.basic.fileSize)
            DetailRow(label = "Format", value = analysis.basic.format)
            DetailRow(label = "ISO", value = analysis.technical.isoSpeedRatings)
            DetailRow(label = "Aperture", value = analysis.technical.fNumber)
        }

        // Full categorized dump of every tag read from the image (MetaRemoverDataType.txt categories)
        val categorized = MetadataCategory.categorize(analysis.rawMetadata)
        if (categorized.isNotEmpty()) {
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Text(
                text = "ALL DETECTED METADATA (${analysis.rawMetadata.size})",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            categorized.forEach { group ->
                MetadataGroup(group.category.title.uppercase(), Color(0xFF90CAF9)) {
                    group.entries.forEach { (label, value) ->
                        DetailRow(label = label, value = value)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomActions(
    onPickNew: () -> Unit,
    onWipe: () -> Unit,
    onSave: () -> Unit,
    canWipe: Boolean,
    isSaving: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPickNew,
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.Gray.copy(alpha = 0.5f)
                )
            ) {
                Text("NEW", fontWeight = FontWeight.Bold, color = Color.White)
            }

            if (canWipe) {
                Button(
                    onClick = onWipe,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        "STRIP ALL DATA",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            } else {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSaving,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = if (isSaving) "SAVING..." else "SAVE TO GALLERY",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}


@Composable
fun MetadataInfo(metadata: ImageMetadata?) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(20.dp)
            .heightIn(max = 450.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MetadataGroup("AI & AUTHENTICITY", Color.Red) {
            DetailRow(
                label = "AI Signature",
                value = if (metadata?.isAIGenerated == true || metadata?.isAIOrSecured == true) "DETECTED" else null
            )
            DetailRow(label = "AI Provider", value = metadata?.aiProvider?.name)
            DetailRow(label = "AI Model", value = metadata?.aiModel)
            DetailRow(
                label = "AI Confidence",
                value = metadata?.aiConfidence?.let { "${(it * 100).toInt()}%" })
            DetailRow(label = "Generation Prompt", value = metadata?.prompt)
            DetailRow(label = "Negative Prompt", value = metadata?.negativePrompt)
            DetailRow(
                label = "C2PA Manifest",
                value = metadata?.c2paSoftwareAgent
                    ?: (if (metadata?.hasC2PA == true) "Present" else null)
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("LOCATION & GPS", Color.Cyan) {
            DetailRow(
                label = "Coordinates",
                value = if (metadata?.latitude != null) "LOCATED" else null
            )
            DetailRow(label = "Latitude", value = metadata?.latitude)
            DetailRow(label = "Longitude", value = metadata?.longitude)
            DetailRow(label = "Altitude", value = metadata?.altitude)
            DetailRow(label = "Processing Method", value = metadata?.gpsProcessingMethod)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("HARDWARE & CAMERA", Color.Yellow) {
            DetailRow(label = "Make", value = metadata?.cameraMake)
            DetailRow(label = "Device Model", value = metadata?.cameraModel)
            DetailRow(label = "Camera Serial", value = metadata?.cameraSerialNumber)
            DetailRow(label = "Lens Serial", value = metadata?.lensSerialNumber)
            DetailRow(label = "Body Serial", value = metadata?.bodySerialNumber)
            DetailRow(label = "Software Used", value = metadata?.software)
            DetailRow(label = "Lens Make", value = metadata?.lensMake)
            DetailRow(label = "Lens Model", value = metadata?.lensModel)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("EDITING HISTORY", Color(0xFFFFA726)) {
            DetailRow(label = "Editing Software", value = metadata?.editingSoftware)
            DetailRow(label = "Photoshop", value = metadata?.photoshopVersion)
            DetailRow(label = "Lightroom", value = metadata?.lightroomVersion)
            metadata?.editingHistory?.forEach { history ->
                DetailRow(label = "History Event", value = history)
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("EXPOSURE DETAILS", Color.Green) {
            DetailRow(label = "F-Number", value = metadata?.fNumber)
            DetailRow(label = "Exposure Time", value = metadata?.exposureTime)
            DetailRow(label = "ISO Speed", value = metadata?.isoSpeedRatings)
            DetailRow(label = "Focal Length", value = metadata?.focalLength)
            DetailRow(label = "Flash Status", value = metadata?.flash)
            DetailRow(label = "White Balance", value = metadata?.whiteBalance)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("RIGHTS & COMMENTS", Color.Blue) {
            DetailRow(label = "Copyright", value = metadata?.copyright)
            DetailRow(label = "Artist / Author", value = metadata?.artist)
            DetailRow(label = "User Comment", value = metadata?.userComment)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("INTEGRITY & COMPRESSION", Color.White) {
            DetailRow(label = "SHA-256", value = metadata?.sha256)
            DetailRow(label = "MD5", value = metadata?.md5)
            DetailRow(label = "JPEG Quality", value = metadata?.jpegQuality?.toString())
            DetailRow(
                label = "Progressive",
                value = metadata?.progressive?.let { if (it) "Yes" else "No" })
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("DATE & TECHNICAL", Color.Gray) {
            DetailRow(label = "Date Taken", value = metadata?.dateTimeOriginal)
            DetailRow(
                label = "Resolution",
                value = if (metadata?.width != null) "${metadata.width} x ${metadata.height}" else null
            )
            DetailRow(label = "File Size", value = metadata?.fileSize)
            DetailRow(label = "Orientation", value = metadata?.orientation)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("PROPRIETARY & HIDDEN", Color.Magenta) {
            DetailRow(
                label = "Hidden Thumbnail",
                value = if (metadata?.hasThumbnail == true) "DETECTED" else null
            )
            DetailRow(label = "MakerNotes", value = metadata?.makerNotes)
            DetailRow(label = "XMP History", value = metadata?.xmpData)
            DetailRow(label = "IPTC Keywords", value = metadata?.iptcKeywords)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("WEB SOURCE & ORIGIN", Color.Cyan) {
            DetailRow(
                label = "From Web/Browser",
                value = if (metadata?.isFromWeb == true) "YES" else null
            )
            DetailRow(label = "Origin Website", value = metadata?.webSource)
        }
    }
}

@Composable
fun MetadataGroup(title: String, color: Color, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun DetailRow(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(
            text = value ?: "CLEAN",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (value != null) Color(0xFFE57373) else Color(0xFF81C784)
        )
    }
}

expect fun ByteArray.toImageBitmap(): ImageBitmap
