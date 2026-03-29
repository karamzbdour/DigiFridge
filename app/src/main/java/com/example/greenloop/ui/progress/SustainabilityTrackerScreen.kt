package com.example.greenloop.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.greenloop.data.model.UpcycleHistory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SustainabilityTrackerScreen(viewModel: ProgressViewModel) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val totalPrice by viewModel.totalPrice.collectAsStateWithLifecycle()
    val timeline by viewModel.spendingTimeline.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Progress & Insights", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Financial Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Total Price",
                        value = "£${String.format(Locale.UK, "%.2f", totalPrice)}",
                        icon = Icons.Default.Payments,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            // Price Timeline Graph
            item {
                Text(
                    text = "Spending Timeline",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                PriceTimelineGraph(timeline)
            }

            item {
                Text(
                    text = "Upcycle History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (history.isEmpty()) {
                item {
                    Text(
                        "No upcycles completed yet. Start your first one from the Recipe Library!",
                        modifier = Modifier.padding(top = 32.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                items(history) { entry ->
                    HistoryItemRow(entry)
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun PriceTimelineGraph(points: List<SpendingTimelinePoint>) {
    if (points.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("No spending data yet", style = MaterialTheme.typography.bodySmall)
            }
        }
        return
    }

    val maxAmount = points.maxOf { it.totalAmount }.toFloat().coerceAtLeast(1f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val width = size.width
                val height = size.height
                val spaceBetween = width / (points.size.coerceAtLeast(2) - 1).coerceAtLeast(1)

                val pathPoints = points.mapIndexed { index, point ->
                    Offset(
                        x = index * spaceBetween,
                        y = height - (point.totalAmount.toFloat() / maxAmount * height)
                    )
                }

                // Draw line
                for (i in 0 until pathPoints.size - 1) {
                    drawLine(
                        color = Color(0xFF4CAF50),
                        start = pathPoints[i],
                        end = pathPoints[i + 1],
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Draw dots
                pathPoints.forEach { center ->
                    drawCircle(color = Color(0xFF4CAF50), radius = 4.dp.toPx(), center = center)
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { point ->
                    Text(
                        text = point.dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(entry: UpcycleHistory) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.recipeTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = sdf.format(Date(entry.completedDate)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
