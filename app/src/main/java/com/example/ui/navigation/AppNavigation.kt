package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.UserRole
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.chats.ChatsScreen
import com.example.ui.home.HomeScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.packages.PackagesScreen
import com.example.ui.profile.ProfileScreen
import com.example.viewmodel.MaqraahViewModel

@Composable
fun AppNavigation(
    maqraahViewModel: MaqraahViewModel = remember { MaqraahViewModel() }
) {
    val navController = rememberNavController()
    var currentLanguage by remember { mutableStateOf(AppLanguage.ARABIC) }

    val isOnboardingCompleted by maqraahViewModel.isOnboardingCompleted.collectAsState()
    val currentUser by maqraahViewModel.currentUser.collectAsState()

    var showUnauthorizedAdminDialog by remember { mutableStateOf(false) }

    // Enforce Layout Direction (RTL for Arabic, LTR for English and Amharic)
    CompositionLocalProvider(LocalLayoutDirection provides currentLanguage.layoutDirection) {

        // Step 1: Onboarding 4 screens if not completed
        if (!isOnboardingCompleted) {
            OnboardingScreen(
                currentLanguage = currentLanguage,
                onLanguageChanged = { currentLanguage = it },
                onFinish = { maqraahViewModel.completeOnboarding() }
            )
            return@CompositionLocalProvider
        }

        // Step 2: Authentication Screen if user is logged out
        if (currentUser == null) {
            AuthScreen(
                currentLanguage = currentLanguage,
                onSignIn = { email, name ->
                    maqraahViewModel.signInWithGoogle(email, name)
                }
            )
            return@CompositionLocalProvider
        }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

        val showBottomBar = currentRoute in listOf(
            Screen.Profile.route,
            Screen.Packages.route,
            Screen.Home.route,
            Screen.Chats.route,
            Screen.Admin.route
        )

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 10.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. [حسابي] (Profile)
                            BottomNavItem(
                                selected = currentRoute == Screen.Profile.route,
                                icon = Icons.Default.Person,
                                label = Strings.get("tab_profile", currentLanguage),
                                onClick = {
                                    navController.navigate(Screen.Profile.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )

                            // 2. [باقاتي] (Packages)
                            BottomNavItem(
                                selected = currentRoute == Screen.Packages.route,
                                icon = Icons.Default.Payment,
                                label = Strings.get("tab_packages", currentLanguage),
                                onClick = {
                                    navController.navigate(Screen.Packages.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )

                            // 3. [الرئيسية] (Home - CENTER, PROMINENT, LARGER)
                            Box(
                                modifier = Modifier
                                    .offset(y = (-10).dp)
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentRoute == Screen.Home.route)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                    )
                                    .clickable {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "الرئيسية",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // 4. [المحادثات] (Chats)
                            BottomNavItem(
                                selected = currentRoute == Screen.Chats.route,
                                icon = Icons.Default.Chat,
                                label = Strings.get("tab_chats", currentLanguage),
                                onClick = {
                                    navController.navigate(Screen.Chats.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )

                            // 5. [الإدارة] (Admin - Strictly Protected with feedback)
                            BottomNavItem(
                                selected = currentRoute == Screen.Admin.route,
                                icon = Icons.Default.AdminPanelSettings,
                                label = Strings.get("tab_admin", currentLanguage),
                                onClick = {
                                    val isAdmin = currentUser?.role == UserRole.ADMIN ||
                                            maqraahViewModel.isAdminEmail(currentUser?.email)
                                    if (isAdmin) {
                                        navController.navigate(Screen.Admin.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    } else {
                                        showUnauthorizedAdminDialog = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            currentLanguage = currentLanguage,
                            currentUser = currentUser,
                            maqraahViewModel = maqraahViewModel,
                            onLanguageChanged = { currentLanguage = it },
                            onNavigate = { route -> navController.navigate(route) }
                        )
                    }
                    composable(Screen.Packages.route) {
                        PackagesScreen(
                            currentLanguage = currentLanguage,
                            currentUser = currentUser,
                            maqraahViewModel = maqraahViewModel
                        )
                    }
                    composable(Screen.Chats.route) {
                        ChatsScreen(
                            currentLanguage = currentLanguage,
                            maqraahViewModel = maqraahViewModel
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            currentLanguage = currentLanguage,
                            currentUser = currentUser!!,
                            maqraahViewModel = maqraahViewModel,
                            onLanguageChanged = { currentLanguage = it },
                            onNavigateToAdmin = { navController.navigate(Screen.Admin.route) },
                            onSignOut = { maqraahViewModel.signOut() }
                        )
                    }
                    composable(Screen.Admin.route) {
                        AdminDashboardScreen(
                            currentLanguage = currentLanguage,
                            maqraahViewModel = maqraahViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }

        // Unauthorized Admin Dialog
        if (showUnauthorizedAdminDialog) {
            AlertDialog(
                onDismissRequest = { showUnauthorizedAdminDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = { Text(text = "صلاحية مقيدة", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = Strings.get("no_admin_access", currentLanguage),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(onClick = { showUnauthorizedAdminDialog = false }) {
                        Text(text = "حسناً")
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
