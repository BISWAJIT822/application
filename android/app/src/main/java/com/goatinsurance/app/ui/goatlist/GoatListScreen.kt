package com.goatinsurance.app.ui.goatlist

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goatinsurance.app.ui.components.*
import com.goatinsurance.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoatListScreen(
    onGoatClick: (Int) -> Unit = {},
    onBack: () -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }

    val goats = remember {
        listOf(
            GoatItem(1, "GT-001", "Black Bengal", "Female", "active", "Ramesh Kumar"),
            GoatItem(2, "GT-002", "Jamunapari", "Male", "insured", "Sita Devi"),
            GoatItem(3, "GT-003", "Sirohi", "Female", "active", "Mohan Lal"),
            GoatItem(4, "GT-004", "Beetal", "Male", "claimed", "Geeta Kumari"),
            GoatItem(5, "GT-005", "Barbari", "Female", "insured", "Ravi Singh"),
            GoatItem(6, "GT-006", "Osmanabadi", "Male", "active", "Priya Devi"),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goat Registry") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { /* Sort */ }) { Icon(Icons.Filled.Sort, "Sort") }
                    IconButton(onClick = { /* Filter */ }) { Icon(Icons.Filled.FilterList, "Filter") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by tag, breed...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )

            // Filter chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("all" to "All", "active" to "Active", "insured" to "Insured", "claimed" to "Claimed").forEach { (value, label) ->
                    FilterChip(
                        selected = selectedFilter == value,
                        onClick = { selectedFilter = value },
                        label = { Text(label) },
                    )
                }
            }

            // Goat list
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val filtered = if (selectedFilter == "all") goats
                               else goats.filter { it.status == selectedFilter }

                items(filtered) { goat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGoatClick(goat.id) },
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Pets, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(goat.tagNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusChip(status = goat.status)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${goat.breed} · ${goat.gender}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Owner: ${goat.owner}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoatDetailScreen(
    onBack: () -> Unit = {},
    onVaccinate: (Int) -> Unit = {},
    onFileClaim: (Int) -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goat Details") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Filled.Pets, null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("GT-001", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Black Bengal · Female", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusChip(status = "insured")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow("Age", "18 months")
                    DetailRow("Weight", "25 kg")
                    DetailRow("Color", "Black")
                    DetailRow("Owner", "Ramesh Kumar")
                    DetailRow("Village", "Palna")
                    DetailRow("Insured Value", "₹5,000")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { onVaccinate(1) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Filled.Vaccines, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vaccinate")
                }
                Button(
                    onClick = { onFileClaim(1) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CardClaims),
                ) {
                    Icon(Icons.Filled.Description, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("File Claim")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider()
}

private data class GoatItem(
    val id: Int,
    val tagNumber: String,
    val breed: String,
    val gender: String,
    val status: String,
    val owner: String,
)
