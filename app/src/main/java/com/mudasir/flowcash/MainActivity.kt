package com.mudasir.flowcash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mudasir.flowcash.data.preferences.ThemeMode
import com.mudasir.flowcash.ui.screens.LoginScreen
import com.mudasir.flowcash.ui.screens.MainContainerScreen
import com.mudasir.flowcash.ui.screens.SignUpScreen
import com.mudasir.flowcash.ui.screens.SplashScreen
import com.mudasir.flowcash.ui.theme.FlowCashTheme
import com.mudasir.flowcash.ui.viewmodel.AuthViewModel
import com.mudasir.flowcash.ui.viewmodel.DashboardViewModel
import com.mudasir.flowcash.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()
            val dashboardViewModel: DashboardViewModel = viewModel()

            val themeMode by settingsViewModel.themeMode.collectAsState()

            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            FlowCashTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(
                            onSplashFinished = {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("login") {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onNavigateToSignUp = {
                                navController.navigate("signup")
                            },
                            onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("signup") {
                        SignUpScreen(
                            authViewModel = authViewModel,
                            onNavigateToLogin = {
                                navController.popBackStack()
                            },
                            onSignUpSuccess = {
                                navController.navigate("main") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("main") {
                        MainContainerScreen(
                            authViewModel = authViewModel,
                            dashboardViewModel = dashboardViewModel,
                            settingsViewModel = settingsViewModel,
                            onLogoutClick = {
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}