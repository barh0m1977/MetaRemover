package com.ibrahim.metaremover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibrahim.metaremover.domain.ImageMetadata
import com.ibrahim.metaremover.picker.ImagePicker
import com.ibrahim.metaremover.presentation.MainViewModel

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
            if (state.originalBytes == null) {
                EmptyStateView(onPickImage = launchPicker)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    StatusIndicator(isCleaned = state.cleanedBytes != null)

                    ImageContainer(
                        bytes = state.cleanedBytes ?: state.originalBytes!!,
                        metadata = state.cleanedMetadata ?: state.originalMetadata
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        state.loadingMessage?.let {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = it,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun StatusIndicator(isCleaned: Boolean) {
    Surface(
        color = if (isCleaned) Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFFFBC02D).copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isCleaned) Color(0xFF4CAF50) else Color(0xFFFFEB3B))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isCleaned) "✓ ALL METADATA REMOVED" else "⚠ PRIVACY RISKS DETECTED",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isCleaned) Color(0xFF81C784) else Color(0xFFFFEB3B)
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
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(25.dp)),
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
fun ImageContainer(bytes: ByteArray, metadata: ImageMetadata?) {
    val imageBitmap = remember(bytes) { bytes.toImageBitmap() }
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 450.dp)
                .background(Color.Black)
        )

        // Modern Expandable Header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .clickable { isExpanded = !isExpanded }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isExpanded) "CLOSE SECURITY LOGS" else "VIEW SECURITY LOGS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isExpanded) "▲" else "▼",
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
            MetadataInfo(metadata)
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
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
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
                    Text("STRIP ALL DATA", fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = Color.White)
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
            .heightIn(max = 350.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MetadataGroup("AI & AUTHENTICITY", Color.Red) {
            DetailRow(label = "AI Signature", value = if (metadata?.isAIOrSecured == true) "DETECTED" else null)
            DetailRow(label = "C2PA Manifest", value = metadata?.c2paSoftwareAgent)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("LOCATION & GPS", Color.Cyan) {
            DetailRow(label = "Coordinates", value = if (metadata?.latitude != null) "LOCATED" else null)
            DetailRow(label = "Latitude", value = metadata?.latitude)
            DetailRow(label = "Longitude", value = metadata?.longitude)
            DetailRow(label = "Altitude", value = metadata?.altitude)
            DetailRow(label = "Processing Method", value = metadata?.gpsProcessingMethod)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("HARDWARE & CAMERA", Color.Yellow) {
            DetailRow(label = "Make", value = metadata?.cameraMake)
            DetailRow(label = "Device Model", value = metadata?.cameraModel)
            DetailRow(label = "Software Used", value = metadata?.software)
            DetailRow(label = "Lens Make", value = metadata?.lensMake)
            DetailRow(label = "Lens Model", value = metadata?.lensModel)
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

        MetadataGroup("DATE & TECHNICAL", Color.Gray) {
            DetailRow(label = "Date Taken", value = metadata?.dateTimeOriginal)
            DetailRow(label = "Resolution", value = if (metadata?.width != null) "${metadata.width} x ${metadata.height}" else null)
            DetailRow(label = "File Size", value = metadata?.fileSize)
            DetailRow(label = "Orientation", value = metadata?.orientation)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("PROPRIETARY & HIDDEN", Color.Magenta) {
            DetailRow(label = "Hidden Thumbnail", value = if (metadata?.hasThumbnail == true) "DETECTED" else null)
            DetailRow(label = "MakerNotes", value = metadata?.makerNotes)
            DetailRow(label = "XMP History", value = metadata?.xmpData)
            DetailRow(label = "IPTC Keywords", value = metadata?.iptcKeywords)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        MetadataGroup("WEB SOURCE & ORIGIN", Color.Cyan) {
            DetailRow(label = "From Web/Browser", value = if (metadata?.isFromWeb == true) "YES" else null)
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
