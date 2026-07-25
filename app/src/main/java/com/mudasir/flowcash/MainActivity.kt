package com.mudasir.flowcash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
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
                    composable(
                        route = "splash",
                        exitTransition = {
                            scaleOut(targetScale = 1.08f, animationSpec = tween(350, easing = FastOutSlowInEasing)) +
                                    fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        SplashScreen(
                            onSplashFinished = {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = "login",
                        enterTransition = {
                            scaleIn(initialScale = 0.92f, animationSpec = tween(350, easing = FastOutSlowInEasing)) +
                                    fadeIn(animationSpec = tween(350))
                        },
                        exitTransition = {
                            slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                                    fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                                    fadeIn(animationSpec = tween(300))
                        }
                    ) {
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

                    composable(
                        route = "signup",
                        enterTransition = {
                            slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                                    fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                                    fadeOut(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                                    fadeOut(animationSpec = tween(300))
                        }
                    ) {
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

                    composable(
                        route = "main",
                        enterTransition = {
                            slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                                    scaleIn(initialScale = 0.95f, animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                                    fadeIn(animationSpec = tween(350))
                        },
                        exitTransition = {
                            scaleOut(targetScale = 0.95f, animationSpec = tween(300)) +
                                    fadeOut(animationSpec = tween(300))
                        }
                    ) {
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