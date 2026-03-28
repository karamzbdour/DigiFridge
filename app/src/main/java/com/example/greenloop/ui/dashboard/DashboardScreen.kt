package com.example.greenloop.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.greenloop.data.model.Ingredient
import java.text.SimpleDateFormat
import java.util.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    var showCamera by remember { mutableStateOf(false) }
    
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    if (showCamera) {
        if (cameraPermissionState.status.isGranted) {
            CameraView(
                onImageCaptured = { bitmap ->
                    viewModel.scanImage(bitmap)
                    showCamera = false
                },
                isScanning = isScanning,
                onClose = { showCamera = false }
            )
        } else {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Fridge & Cupboard", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                LargeFloatingActionButton(
                    onClick = { showCamera = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Scan Receipt")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Inventory",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (inventoryItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Your fridge is empty! Scan a receipt to stock up.")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(inventoryItems) { item ->
                            IngredientCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientCard(item: Ingredient) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateString = sdf.format(Date(item.expiryDate))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Category: ${item.category}",
                    style = MaterialTheme.typography.bodySmall
                )
                item.quantity?.let {
                    Text(
                        text = "Quantity: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            val daysRemaining = ((item.expiryDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
            val badgeColor = when {
                daysRemaining < 0 -> Color.Gray // Expired
                daysRemaining <= 1 -> Color.Red // Critical
                daysRemaining <= 3 -> Color(0xFFFFA000) // Warning (Orange)
                else -> MaterialTheme.colorScheme.primary // Safe
            }
            
            val statusText = when {
                daysRemaining < 0 -> "Expired"
                daysRemaining == 0 -> "Expires Today"
                daysRemaining == 1 -> "Expires Tomorrow"
                else -> "$daysRemaining days left"
            }
            
            Surface(
                color = badgeColor,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}
