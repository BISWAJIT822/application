package com.goatinsurance.app.ui.vaccination

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goatinsurance.app.ui.components.*
import com.goatinsurance.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationScreen(
    onBack: () -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Completed", "History")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vaccination") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* Record vaccination */ },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Record") },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }

            val items = remember {
                listOf(
                    VacItem("GT-001", "PPR", "2024-02-15", if (selectedTab == 0) "scheduled" else "completed"),
                    VacItem("GT-002", "FMD", "2024-02-20", if (selectedTab == 0) "scheduled" else "completed"),
                    VacItem("GT-003", "ET", "2024-01-30", "overdue"),
                    VacItem("GT-004", "Goat Pox", "2024-03-01", if (selectedTab == 0) "scheduled" else "completed"),
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Vaccines,
                                null,
                                modifier = Modifier.size(40.dp),
                                tint = when (item.status) {
                                    "completed" -> StatusActive
                                    "overdue" -> StatusRejected
                                    else -> StatusPending
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.vaccineType, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("Tag: ${item.goatTag}", style = MaterialTheme.typography.bodySmall)
                                Text("Date: ${item.date}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusChip(status = item.status)
                        }
                    }
                }
            }
        }
    }
}

private data class VacItem(val goatTag: String, val vaccineType: String, val date: String, val status: String)
