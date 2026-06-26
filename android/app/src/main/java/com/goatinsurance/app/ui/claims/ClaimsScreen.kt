package com.goatinsurance.app.ui.claims

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goatinsurance.app.ui.components.*
import com.goatinsurance.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimsScreen(
    onClaimClick: (Int) -> Unit = {},
    onBack: () -> Unit = {},
) {
    val claims = remember {
        listOf(
            ClaimItem(1, "CLM-20240115-ABC123", "Ramesh Kumar", "submitted", "₹5,000", "2024-01-10"),
            ClaimItem(2, "CLM-20240114-DEF456", "Sita Devi", "under_review", "₹4,500", "2024-01-08"),
            ClaimItem(3, "CLM-20240113-GHI789", "Mohan Lal", "approved", "₹5,000", "2024-01-05"),
            ClaimItem(4, "CLM-20240112-JKL012", "Geeta Kumari", "settled", "₹4,800", "2024-01-02"),
            ClaimItem(5, "CLM-20240111-MNO345", "Ravi Singh", "rejected", "₹5,000", "2023-12-28"),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Claims") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* File new claim */ },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("File Claim") },
                containerColor = CardClaims,
                contentColor = androidx.compose.ui.graphics.Color.White,
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(claims) { claim ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClaimClick(claim.id) },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(claim.claimNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            StatusChip(status = claim.status)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Farmer: ${claim.farmerName}", style = MaterialTheme.typography.bodyMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Amount: ${claim.amount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            Text("Filed: ${claim.date}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileClaimScreen(onBack: () -> Unit = {}) {
    // Placeholder for file claim form
    Scaffold(
        topBar = { TopAppBar(title = { Text("File New Claim") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } }) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("File a new insurance claim", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimDetailScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Claim Details") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Description, null, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("CLM-20240115-ABC123", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    StatusChip(status = "under_review")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow2("Farmer", "Ramesh Kumar")
                    DetailRow2("Policy", "POL-20240101-XYZ789")
                    DetailRow2("Death Date", "2024-01-10")
                    DetailRow2("Death Cause", "Disease")
                    DetailRow2("Claim Amount", "₹5,000")
                    DetailRow2("AI Score", "0.85 (Legitimate)")
                    DetailRow2("Carcass Verified", "Yes ✓")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Review Actions
            Text("Review Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusActive),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("Approve") }

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("Hold") }

                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRejected),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("Reject") }
            }
        }
    }
}

@Composable
private fun DetailRow2(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider()
}

private data class ClaimItem(val id: Int, val claimNumber: String, val farmerName: String, val status: String, val amount: String, val date: String)
