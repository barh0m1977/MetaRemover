package com.ibrahim.metaremover

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateAndReviewScreen(
    updateManager: AppUpdateManager,
    reviewManager: AppReviewManager
) {
    var showUpdateSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Check for updates when the screen opens
    LaunchedEffect(Unit) {
        updateManager.checkForUpdates {
            showUpdateSheet = true
        }
    }

    Scaffold {
        if (showUpdateSheet) {
            ModalBottomSheet(
                onDismissRequest = { showUpdateSheet = false },
                sheetState = sheetState
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Update Available!", style = MaterialTheme.typography.headlineSmall)
                    Text("Please download the latest version for the best experience.")

                    Button(
                        onClick = {
                            showUpdateSheet = false
                            updateManager.startFlexibleUpdate()
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text("Download Now")
                    }
                }
            }
        }

        // Example trigger for Rating Flow (e.g., after completing a task)
        Button(onClick = { reviewManager.launchReviewFlow() }) {
            Text("Rate Our App")
        }
    }
}
