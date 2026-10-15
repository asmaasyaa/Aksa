package com.aksa.app.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ParentWelcome : Screen("parent_welcome")
    data object ChildName : Screen("child_name")
    data object ChildAge : Screen("child_age")
    data object Home : Screen("home")
    data object LevelMap : Screen("level_map")
    data object AlphabetSelection : Screen("alphabet_selection")
    data object PronunciationPractice : Screen("pronunciation_practice")
    data object WritingPractice : Screen("writing_practice")
    data object SyllableIntro : Screen("syllable_intro")
    data object SyllableExample : Screen("syllable_example")
    data object SyllableQuiz : Screen("syllable_quiz")
    data object WordFormation : Screen("word_formation")
    data object ReadingPractice : Screen("reading_practice")
    data object Profile : Screen("profile")
    data object ProgressAchievements : Screen("progress_achievements")
    data object HurufMirip : Screen("huruf_mirip")
}
