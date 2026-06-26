package com.goatinsurance.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.goatinsurance.app.ui.auth.LoginScreen
import com.goatinsurance.app.ui.auth.OtpScreen
import com.goatinsurance.app.ui.dashboard.DashboardScreen
import com.goatinsurance.app.ui.enrollment.EnrollmentScreen
import com.goatinsurance.app.ui.enrollment.EnrollmentListScreen
import com.goatinsurance.app.ui.goatlist.GoatListScreen
import com.goatinsurance.app.ui.goatlist.GoatDetailScreen
import com.goatinsurance.app.ui.vaccination.VaccinationScreen
import com.goatinsurance.app.ui.claims.ClaimsScreen
import com.goatinsurance.app.ui.claims.FileClaimScreen
import com.goatinsurance.app.ui.claims.ClaimDetailScreen
import com.goatinsurance.app.ui.reports.ReportsScreen
import com.goatinsurance.app.ui.profile.ProfileScreen
import com.goatinsurance.app.ui.admin.AdminDashboardScreen
import com.goatinsurance.app.ui.assistant.AssistantScreen

data class BottomNavItemData(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItemData("dashboard", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItemData("enrollment_list", "Enroll", Icons.Filled.AddCircle, Icons.Outlined.AddCircle),
    BottomNavItemData("goat_list", "Goats", Icons.Filled.Pets, Icons.Outlined.Pets),
    BottomNavItemData("claims_list", "Claims", Icons.Filled.Description, Icons.Outlined.Description),
    BottomNavItemData("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person),
)

@Composable
fun GoatInsuranceNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    tonalElevation = NavigationBarDefaults.Elevation,
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            alwaysShowLabel = true,
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)) },
        ) {
            // ── Auth ──
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToOtp = { phone ->
                        navController.navigate(Screen.OtpVerification.createRoute(phone))
                    }
                )
            }

            composable(
                route = Screen.OtpVerification.route,
                arguments = listOf(navArgument("phone") { type = NavType.StringType })
            ) { backStackEntry ->
                val phone = backStackEntry.arguments?.getString("phone") ?: ""
                OtpScreen(
                    phone = phone,
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Dashboard ──
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToEnrollment = { navController.navigate(Screen.NewEnrollment.route) },
                    onNavigateToGoats = { navController.navigate(Screen.GoatList.route) },
                    onNavigateToVaccination = { navController.navigate(Screen.VaccinationList.route) },
                    onNavigateToClaims = { navController.navigate(Screen.ClaimsList.route) },
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToAssistant = { navController.navigate(Screen.Assistant.route) },
                    onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) },
                )
            }

            // ── Enrollment ──
            composable(Screen.EnrollmentList.route) {
                EnrollmentListScreen(
                    onNewEnrollment = { navController.navigate(Screen.NewEnrollment.route) },
                    onEnrollmentDetail = { id ->
                        navController.navigate(Screen.EnrollmentDetail.createRoute(id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.NewEnrollment.route) {
                EnrollmentScreen(
                    onComplete = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Goats ──
            composable(Screen.GoatList.route) {
                GoatListScreen(
                    onGoatClick = { id ->
                        navController.navigate(Screen.GoatDetail.createRoute(id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.GoatDetail.route,
                arguments = listOf(navArgument("goatId") { type = NavType.IntType })
            ) {
                GoatDetailScreen(
                    onBack = { navController.popBackStack() },
                    onVaccinate = { goatId ->
                        navController.navigate(Screen.RecordVaccination.createRoute(goatId))
                    },
                    onFileClaim = { policyId ->
                        navController.navigate(Screen.FileClaim.createRoute(policyId))
                    }
                )
            }

            // ── Vaccination ──
            composable(Screen.VaccinationList.route) {
                VaccinationScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Claims ──
            composable(Screen.ClaimsList.route) {
                ClaimsScreen(
                    onClaimClick = { id ->
                        navController.navigate(Screen.ClaimDetail.createRoute(id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ClaimDetail.route,
                arguments = listOf(navArgument("claimId") { type = NavType.IntType })
            ) {
                ClaimDetailScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Reports ──
            composable(Screen.Reports.route) {
                ReportsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Profile ──
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Admin ──
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // ── AI Assistant ──
            composable(Screen.Assistant.route) {
                AssistantScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
