package com.aksa.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aksa.app.ui.screens.AlphabetSelectionScreen
import com.aksa.app.ui.screens.ChildAgeScreen
import com.aksa.app.ui.screens.ChildNameScreen
import com.aksa.app.ui.screens.HomeScreen
import com.aksa.app.ui.screens.LevelMapScreen
import com.aksa.app.ui.screens.LoginScreen
import com.aksa.app.ui.screens.ParentWelcomeScreen
import com.aksa.app.ui.screens.ProfileScreen
import com.aksa.app.ui.screens.ProgressAchievementsScreen
import com.aksa.app.ui.screens.PronunciationScreen
import com.aksa.app.ui.screens.ReadingPracticeScreen
import com.aksa.app.ui.screens.RegisterScreen
import com.aksa.app.ui.screens.SplashScreen
import com.aksa.app.ui.screens.SyllableExampleScreen
import com.aksa.app.ui.screens.SyllableIntroScreen
import com.aksa.app.ui.screens.SyllableQuizScreen
import com.aksa.app.ui.screens.WordFormationScreen
import com.aksa.app.ui.screens.WritingPracticeScreen

@Composable
fun AksaNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
        }
    ) {
        // SCREEN 1 — SPLASH
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // SCREEN 2 — LOGIN
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToParentWelcome = {
                    navController.navigate(Screen.ParentWelcome.route)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // SCREEN 3 — REGISTER
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToParentWelcome = {
                    navController.navigate(Screen.ParentWelcome.route)
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // SCREEN 4 — PARENT WELCOME
        composable(Screen.ParentWelcome.route) {
            ParentWelcomeScreen(
                onNavigateToChildName = {
                    navController.navigate(Screen.ChildName.route)
                }
            )
        }

        // SCREEN 5 — CHILD NAME
        composable(Screen.ChildName.route) {
            ChildNameScreen(
                onNavigateToChildAge = {
                    navController.navigate(Screen.ChildAge.route)
                }
            )
        }

        // SCREEN 6 — CHILD AGE
        composable(Screen.ChildAge.route) {
            ChildAgeScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // SCREEN 7 — HOME
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLevelMap = {
                    navController.navigate(Screen.LevelMap.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        // SCREEN 8 — LEVEL MAP
        composable(Screen.LevelMap.route) {
            LevelMapScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLevel1 = {
                    navController.navigate(Screen.AlphabetSelection.route)
                },
                onNavigateToLevel2 = {
                    navController.navigate(Screen.SyllableIntro.route)
                },
                onNavigateToLevel3 = {
                    navController.navigate(Screen.WordFormation.route)
                },
                onNavigateToLevel4 = {
                    navController.navigate(Screen.ReadingPractice.route)
                }
            )
        }

        // SCREEN 9 — MODULE 1: ALPHABET SELECTION
        composable(Screen.AlphabetSelection.route) {
            AlphabetSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPronunciation = { _ ->
                    navController.navigate(Screen.PronunciationPractice.route)
                }
            )
        }

        // SCREEN 10 — PRONUNCIATION PRACTICE
        composable(Screen.PronunciationPractice.route) {
            PronunciationScreen(
                letter = "A",
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToWriting = {
                    navController.navigate(Screen.WritingPractice.route)
                }
            )
        }

        // SCREEN 11 — WRITING PRACTICE
        composable(Screen.WritingPractice.route) {
            WritingPracticeScreen(
                letter = "A",
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLevelMap = {
                    if (!navController.popBackStack(Screen.LevelMap.route, inclusive = false)) {
                        navController.navigate(Screen.LevelMap.route)
                    }
                }
            )
        }

        // SCREEN 12 — MODULE 2: SYLLABLE INTRO
        composable(Screen.SyllableIntro.route) {
            SyllableIntroScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToExample = {
                    navController.navigate(Screen.SyllableExample.route)
                }
            )
        }

        // SCREEN 13 — SYLLABLE EXAMPLE
        composable(Screen.SyllableExample.route) {
            SyllableExampleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToQuiz = {
                    navController.navigate(Screen.SyllableQuiz.route)
                }
            )
        }

        // SCREEN 14 — SYLLABLE QUIZ
        composable(Screen.SyllableQuiz.route) {
            SyllableQuizScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLevelMap = {
                    if (!navController.popBackStack(Screen.LevelMap.route, inclusive = false)) {
                        navController.navigate(Screen.LevelMap.route)
                    }
                }
            )
        }

        // SCREEN 15 — MODULE 3: WORD FORMATION
        composable(Screen.WordFormation.route) {
            WordFormationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLevelMap = {
                    if (!navController.popBackStack(Screen.LevelMap.route, inclusive = false)) {
                        navController.navigate(Screen.LevelMap.route)
                    }
                }
            )
        }

        // SCREEN 16 — MODULE 4: READING PRACTICE
        composable(Screen.ReadingPractice.route) {
            ReadingPracticeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLevelMap = {
                    if (!navController.popBackStack(Screen.LevelMap.route, inclusive = false)) {
                        navController.navigate(Screen.LevelMap.route)
                    }
                }
            )
        }

        // SCREEN 17 — PROFILE
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProgress = {
                    navController.navigate(Screen.ProgressAchievements.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // SCREEN 18 — PROGRESS & ACHIEVEMENTS
        composable(Screen.ProgressAchievements.route) {
            ProgressAchievementsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
