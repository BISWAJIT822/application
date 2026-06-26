package com.goatinsurance.app.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.goatinsurance.app.ui.components.*
import com.goatinsurance.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToEnrollment: () -> Unit = {},
    onNavigateToGoats: () -> Unit = {},
    onNavigateToVaccination: () -> Unit = {},
    onNavigateToClaims: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAssistant: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Welcome, ${uiState.userName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = uiState.userRole.replace("_", " ").replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(42.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = uiState.userName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(badge = {
                            if (uiState.unreadNotifications > 0) {
                                Badge { Text("${uiState.unreadNotifications}") }
                            }
                        }) {
                            Icon(Icons.Outlined.Notifications, "Notifications")
                        }
                    }
                    IconButton(onClick = onNavigateToAssistant) {
                        Icon(Icons.Outlined.SmartToy, "AI Assistant")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Stats Cards Grid
            SectionHeader(title = "Dashboard Overview")

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    DashboardCard(
                        title = "Total Enrollments",
                        value = "${uiState.stats.totalEnrollments}",
                        icon = Icons.Filled.Assignment,
                        backgroundColor = CardEnrollment,
                        onClick = onNavigateToEnrollment,
                    )
                }
                item {
                    DashboardCard(
                        title = "Premium Collected",
                        value = "₹${uiState.stats.premiumCollected}",
                        icon = Icons.Filled.CurrencyRupee,
                        backgroundColor = CardPremium,
                    )
                }
                item {
                    DashboardCard(
                        title = "Active Claims",
                        value = "${uiState.stats.totalClaims}",
                        icon = Icons.Filled.Description,
                        backgroundColor = CardClaims,
                        onClick = onNavigateToClaims,
                    )
                }
                item {
                    DashboardCard(
                        title = "Vaccination Due",
                        value = "${uiState.stats.vaccinationDue}",
                        icon = Icons.Filled.Vaccines,
                        backgroundColor = CardVaccination,
                        onClick = onNavigateToVaccination,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // More Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DashboardCard(
                    title = "Death Reports",
                    value = "${uiState.stats.deathReports}",
                    icon = Icons.Filled.Warning,
                    backgroundColor = CardDeathReport,
                    modifier = Modifier.weight(1f),
                )
                DashboardCard(
                    title = "Pending Approvals",
                    value = "${uiState.stats.pendingApprovals}",
                    icon = Icons.Filled.PendingActions,
                    backgroundColor = CardPendingApprovals,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions
            SectionHeader(title = "Quick Actions")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                QuickActionButton(
                    icon = Icons.Filled.PersonAdd,
                    label = "New\nEnrollment",
                    onClick = onNavigateToEnrollment,
                )
                QuickActionButton(
                    icon = Icons.Filled.Pets,
                    label = "View\nGoats",
                    onClick = onNavigateToGoats,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                QuickActionButton(
                    icon = Icons.Filled.Vaccines,
                    label = "Record\nVaccine",
                    onClick = onNavigateToVaccination,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                QuickActionButton(
                    icon = Icons.Filled.Assessment,
                    label = "View\nReports",
                    onClick = onNavigateToReports,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Summary Statistics Card
            SectionHeader(title = "Statistics Summary")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatItem("Total Farmers", "${uiState.stats.totalFarmers}")
                        StatItem("Total Goats", "${uiState.stats.totalGoats}")
                        StatItem("Active Policies", "${uiState.stats.activePolicies}")
                    }
                }
            }

            // Admin Quick Access
            if (uiState.userRole in listOf("admin", "coordinator")) {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "Admin Panel", actionLabel = "View All", onAction = onNavigateToAdmin)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onNavigateToAdmin() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Admin Dashboard",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "Manage users, policies, and analytics",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
