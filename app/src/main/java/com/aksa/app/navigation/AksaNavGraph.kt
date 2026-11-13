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
import com.aksa.app.ui.screens.HurufMiripScreen
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
import com.aksa.app.ui.screens.LetterLearningScreen
import com.aksa.app.ui.screens.TangkapHurufScreen
import com.aksa.app.ui.preview.DeveloperPreviewScreen
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
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateNext = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { hasProfile ->
                    val destination = if (hasProfile) Screen.SelectProfile.route else Screen.CreateProfile.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // Register
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.CreateProfile.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onGoogleSignUpSuccess = { hasProfile ->
                    val destination = if (hasProfile) Screen.SelectProfile.route else Screen.CreateProfile.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Navigation contracts for profile creation and selection (feat/profile-anak)
        composable(Screen.CreateProfile.route) {
            ChildNameScreen(
                onNavigateToChildAge = {
                    navController.navigate(Screen.ChildAge.route)
                }
            )
        }

        composable(Screen.SelectProfile.route) {
            HomeScreen(
                onNavigateToLevelMap = {
                    navController.navigate(Screen.LevelMap.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.ParentWelcome.route) {
            ParentWelcomeScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ChildName.route) {
            ChildNameScreen(
                onNavigateToChildAge = {
                    navController.navigate(Screen.ChildAge.route)
                }
            )
        }

        composable(Screen.ChildAge.route) {
            ChildAgeScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Home
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

        // Level map
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
                },
                onNavigateToTangkapHuruf = {
                    navController.navigate(Screen.TangkapHuruf.route)
                },
                onNavigateToDeveloperPreview = {
                    navController.navigate(Screen.DeveloperPreview.route)
                }
            )
        }

        // Alphabet Selection
        composable(Screen.AlphabetSelection.route) {
            AlphabetSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPronunciation = { _ ->
                    navController.navigate(Screen.PronunciationPractice.route)
                },
                onNavigateToLetterLearning = { _ ->
                    navController.navigate(Screen.LetterLearning.route)
                },
                onNavigateToHurufMirip = {
                    navController.navigate(Screen.HurufMirip.route)
                }
            )
        }

        // INTERACTIVE LETTER LEARNING & QUIZ
        composable(Screen.LetterLearning.route) {
            LetterLearningScreen(
                startLetter = "A",
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLevelMap = {
                    if (!navController.popBackStack(Screen.LevelMap.route, inclusive = false)) {
                        navController.navigate(Screen.LevelMap.route)
                    }
                },
                onNavigateToWriting = {
                    navController.navigate(Screen.WritingPractice.route)
                }
            )
        }

        // PronunciationPractice
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
                },
                onNavigateToHurufMirip = {
                    navController.navigate(Screen.HurufMirip.route)
                }
            )
        }

        // HurufMirip
        composable(Screen.HurufMirip.route) {
            HurufMiripScreen(
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

        // SyllableIntro
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

        // SyllableExample
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

        // SyllableQuiz
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

        // WordFormation
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

        // ReadingPractice
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

        // Profile
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
                },
                onNavigateToDeveloperPreview = {
                    navController.navigate(Screen.DeveloperPreview.route)
                }
            )
        }

        // ProgressAchievements
        composable(Screen.ProgressAchievements.route) {
            ProgressAchievementsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Mini Games
        composable(Screen.TangkapHuruf.route) {
            TangkapHurufScreen(
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

        // DEVELOPER PREVIEW MODE (DEBUG ONLY)
        composable(Screen.DeveloperPreview.route) {
            DeveloperPreviewScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route)
                },
                onNavigateToLevelMap = {
                    navController.navigate(Screen.LevelMap.route)
                },
                onNavigateToAlphabetSelection = {
                    navController.navigate(Screen.AlphabetSelection.route)
                },
                onNavigateToLetterLearning = {
                    navController.navigate(Screen.LetterLearning.route)
                },
                onNavigateToHurufMirip = {
                    navController.navigate(Screen.HurufMirip.route)
                },
                onNavigateToSyllableIntro = {
                    navController.navigate(Screen.SyllableIntro.route)
                },
                onNavigateToSyllableExample = {
                    navController.navigate(Screen.SyllableExample.route)
                },
                onNavigateToSyllableQuiz = {
                    navController.navigate(Screen.SyllableQuiz.route)
                },
                onNavigateToWordFormation = {
                    navController.navigate(Screen.WordFormation.route)
                },
                onNavigateToReadingPractice = {
                    navController.navigate(Screen.ReadingPractice.route)
                },
                onNavigateToTangkapHuruf = {
                    navController.navigate(Screen.TangkapHuruf.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToProgressAchievements = {
                    navController.navigate(Screen.ProgressAchievements.route)
                }
            )
        }
    }
}
