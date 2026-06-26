package com.goatinsurance.app.ui.enrollment

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentListScreen(
    onNewEnrollment: () -> Unit = {},
    onEnrollmentDetail: (Int) -> Unit = {},
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enrollments") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewEnrollment,
                icon = { Icon(Icons.Filled.Add, "New") },
                text = { Text("New Enrollment") },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            var searchQuery by remember { mutableStateOf("") }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search enrollments...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )

            // Demo enrollment list
            val enrollments = remember {
                listOf(
                    Triple("ENR-20240115-A1B2C3", "Ramesh Kumar", "completed"),
                    Triple("ENR-20240115-D4E5F6", "Sita Devi", "step_3_photos"),
                    Triple("ENR-20240114-G7H8I9", "Mohan Lal", "completed"),
                    Triple("ENR-20240113-J1K2L3", "Geeta Kumari", "step_5_vaccination"),
                    Triple("ENR-20240112-M4N5O6", "Ravi Singh", "draft"),
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(enrollments) { (number, name, status) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEnrollmentDetail(1) },
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = number,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            StatusChip(status = status)
                        }
                    }
                }
            }
        }
    }
}
