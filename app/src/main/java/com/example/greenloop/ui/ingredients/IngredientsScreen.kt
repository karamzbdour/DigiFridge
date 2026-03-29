package com.example.greenloop.ui.ingredients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.greenloop.data.model.Ingredient
import com.example.greenloop.ui.dashboard.DashboardViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(viewModel: DashboardViewModel) {
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Inventory", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (inventoryItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No ingredients in inventory.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(inventoryItems, key = { it.id }) { item ->
                        IngredientItemCard(
                            item = item,
                            onDelete = { viewModel.deleteIngredient(item) },
                            onMarkExpired = { viewModel.markAsExpired(item) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddIngredientDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, category, expiryDays, price ->
                    viewModel.addIngredient(name, category, expiryDays, price)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun IngredientItemCard(
    item: Ingredient,
    onDelete: () -> Unit,
    onMarkExpired: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isExpired) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row {
                    if (!item.isExpired) {
                        TextButton(onClick = onMarkExpired) {
                            Text("Expire", color = Color(0xFFF57C00))
                        }
                    }
                    TextButton(onClick = onDelete) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Price: £${String.format(Locale.UK, "%.2f", item.price ?: 0.0)}")
            }

            if (item.isExpired) {
                Text(
                    "EXPIRED",
                    color = Color.Red,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Cupboard") }
    var expiryDays by remember { mutableStateOf("7") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ingredient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category, 
                    onValueChange = { category = it }, 
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = expiryDays,
                    onValueChange = { expiryDays = it },
                    label = { Text("Expiry (days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (£)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    name,
                    category,
                    expiryDays.toIntOrNull() ?: 7,
                    price.toDoubleOrNull() ?: 0.0
                )
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
