# AKSA FIGMA SCREEN INVENTORY & ASSET SPECIFICATION

This document catalogs all major screens, interactive states, and visual variations designed in AKSA Phase 2. It serves as the primary visual reference for our UI/UX design team and teaching assistants when importing Android screenshots into the Figma Interactive Prototype.

---

## 1. Global Navigation & Foundation

| Screen/State ID | Screen Name | Purpose | Preview Route / Scenario | Main Interactive Elements | Destination Screen | Recommended Screenshot Filename |
|---|---|---|---|---|---|---|
| `NAV-01` | Splash Screen | Brand introduction & app initialization | Launch app / `Screen.Splash` | Aksa Mascot & Logo, loading animation | Login (`NAV-02`) | `01-splash-screen.png` |
| `NAV-02` | Login Screen | Authentication entry (Demo / Firebase) | `Screen.Login` | Email & Password inputs, "Masuk", "Masuk Mode Demo", "Daftar di sini" | Home (`HOME-01`) or Register (`NAV-03`) | `02-login-screen.png` |
| `NAV-03` | Register Screen | User onboarding entry | `Screen.Register` | Name, Email, Password, Confirmation inputs, "Daftar Akun" | Parent Welcome (`NAV-04`) | `03-register-screen.png` |
| `NAV-04` | Parent Welcome Screen | Parent onboarding & greeting | `Screen.ParentWelcome` | Mascot illustration, "Mulai Sekarang" button | Home (`HOME-01`) | `04-parent-welcome.png` |
| `HOME-01` | Home Screen (Default / 3 Days Active) | Central child dashboard with weekly activity | `Screen.Home` / Dev Preview `3 Days` | Mascot greeting, "Mulai Belajar" button, Weekly Calendar (3 dots active), Bottom Nav | Level Map (`MAP-01`), Profile (`PROF-01`) | `05-home-default.png` |
| `HOME-02` | Home Screen (0 Days Active) | Dashboard with empty weekly progress | Dev Preview: Weekly Activity `0 Days` | Weekly Calendar (0 dots checked, empty state) | Level Map (`MAP-01`) | `06-home-weekly-0-days.png` |
| `HOME-03` | Home Screen (2 Days Active) | Dashboard with partial weekly progress | Dev Preview: Weekly Activity `2 Days` | Weekly Calendar (Sen, Sel checked) | Level Map (`MAP-01`) | `07-home-weekly-2-days.png` |
| `HOME-04` | Home Screen (7 Days Active / Completed) | Dashboard with full week completion | Dev Preview: Weekly Activity `7 Days` | Weekly Calendar (All 7 checked, streak badge active) | Level Map (`MAP-01`) | `08-home-weekly-7-days.png` |

---

## 2. Adventure Map (Level Map)

| Screen/State ID | Screen Name | Purpose | Preview Route / Scenario | Main Interactive Elements | Destination Screen | Recommended Screenshot Filename |
|---|---|---|---|---|---|---|
| `MAP-01` | Level Map (Default Progression) | Adventure path showing 4 learning worlds | `Screen.LevelMap` / Default | Node 1 (Pulau Alfabet - Active), Node 2 (Suku Kata - Active), Nodes 3 & 4 (Locked), "🎮 Tangkap Huruf" pill, "🛠️ Dev Preview" pill | Selected Level Node | `09-level-map-default.png` |
| `MAP-02` | Level Map (All Levels Unlocked) | Visual testing of unlocked island nodes | Dev Preview: Map State `ALL_UNLOCKED` | Nodes 1, 2, 3, 4 fully colored and clickable | Selected Level Node | `10-level-map-all-unlocked.png` |
| `MAP-03` | Level Map (All Levels Completed) | Visual state of full curriculum mastery | Dev Preview: Map State `ALL_COMPLETED` | Golden star badges on all 4 nodes | Selected Level Node | `11-level-map-all-completed.png` |
| `MAP-04` | Level Map (All Locked Except L1) | Initial onboarding map state | Dev Preview: Map State `ALL_LOCKED_EXCEPT_1` | Node 1 (Active), Nodes 2, 3, 4 (Grayed out with lock icons) | Level 1 (`ALF-01`) | `12-level-map-locked.png` |

---

## 3. Level 1 — Pulau Alfabet

| Screen/State ID | Screen Name | Purpose | Preview Route / Scenario | Main Interactive Elements | Destination Screen | Recommended Screenshot Filename |
|---|---|---|---|---|---|---|
| `ALF-01` | Alphabet Selection Screen | Letter grid & Huruf Mirip entry | `Screen.AlphabetSelection` | Letter Cards (A, B, C, D...), "Huruf Mirip" banner card, Back button | Letter Learning (`ALF-02`) or Huruf Mirip (`ALF-07`) | `13-alphabet-selection.png` |
| `ALF-02` | Letter Intro (Idle / Step 1) | Introduction to letters A, B, C with audio | `Screen.LetterLearning` (Step 1) | Large letter card, word example ("A untuk Apel"), "🔊 Dengarkan Bunyi" button, "Lanjut" button | Next Letter Intro | `14-letter-intro-a.png` |
| `ALF-03` | Letter Intro (Step 2 - B) | Introduction to letter B | `Screen.LetterLearning` (Step 2) | Letter card "B", "B untuk Bola", TTS button, "Lanjut" button | Letter Intro C | `15-letter-intro-b.png` |
| `ALF-04` | Letter Quiz (Question State) | Listen and choose quiz question | `Screen.LetterLearning` (Quiz) | Audio speaker button, question prompt, 3 letter choice tiles (A, B, C) | Answer Selected | `16-letter-quiz-question.png` |
| `ALF-05` | Letter Quiz (Correct Feedback) | Immediate positive reinforcement | Quiz: Tap correct tile | Green highlight, Cheering mascot, "Hebat Sekali! Jawabanmu benar! 🎉", "Pertanyaan Berikutnya" | Next Question | `17-letter-quiz-feedback-correct.png` |
| `ALF-06` | Letter Quiz (Incorrect Feedback) | Gentle non-punitive feedback | Quiz: Tap incorrect tile | Soft amber highlight, encouraging mascot, "Ayo coba lagi bersama Aksa! 💪", "Lanjut Belajar" | Next Question | `18-letter-quiz-feedback-incorrect.png` |
| `ALF-07` | Letter Learning (Session Result) | Celebration modal & session summary | Quiz completed | Trophy icon, Stars rating, Accuracy percentage, "Selesai & Ke Peta", "Ulangi Latihan" | Level Map (`MAP-01`) | `19-letter-learning-result.png` |
| `ALF-08` | Huruf Mirip (Active Question) | Distinguish confusing pairs (b/d/p/q) | `Screen.HurufMirip` (Phase 1 preserved) | Audio prompt, 10-step progress bar, 4 answer tiles, Aksa mascot | Answer Selected | `20-huruf-mirip-question.png` |
| `ALF-09` | Huruf Mirip (Session Result) | Huruf Mirip 10-question completion | Huruf Mirip completed | Final score, Stars, Accuracy, XP gained, "Kembali ke Peta" | Level Map (`MAP-01`) | `21-huruf-mirip-result.png` |

---

## 4. Level 2 — Dunia Suku Kata

| Screen/State ID | Screen Name | Purpose | Preview Route / Scenario | Main Interactive Elements | Destination Screen | Recommended Screenshot Filename |
|---|---|---|---|---|---|---|
| `SUKU-01` | Syllable Intro Screen | Concept introduction of syllable blending | `Screen.SyllableIntro` | Mascot illustration, syllable cards ("BA - JU"), "Mulai Belajar" button | Syllable Example (`SUKU-02`) | `22-syllable-intro.png` |
| `SUKU-02` | Syllable Example Screen | Audio-supported interactive syllable blending | `Screen.SyllableExample` | Cards "MA" + "TA" = "MATA", "🔊 Dengarkan Bunyi" (TTS), "Mulai Kuis Suku Kata" | Syllable Quiz (`SUKU-03`) | `23-syllable-example.png` |
| `SUKU-03` | Syllable Quiz (Interactive Question) | Construct target word from syllables | `Screen.SyllableQuiz` (Q1: MATA) | Target word banner, TTS audio prompt, 2 drop-target slots, 4 syllable choice chips (MA, TA, BU, KU) | Syllable Placed | `24-syllable-quiz-question.png` |
| `SUKU-04` | Syllable Quiz (Correct Feedback) | Correct syllable construction feedback | Tap correct chips | Green banner, "Luar Biasa! 'MA' + 'TA' = 'MATA'! 🌟", "Lanjut Kuis" | Next Question | `25-syllable-quiz-feedback-correct.png` |
| `SUKU-05` | Syllable Quiz (Incorrect Feedback) | Non-punitive incorrect guidance | Tap incorrect chips | Soft peach banner, "Hampir tepat! Ayo perhatikan bunyinya lagi ya!", "Coba Lagi" | Retry / Next Question | `26-syllable-quiz-feedback-incorrect.png` |
| `SUKU-06` | Syllable Quiz (Session Result) | 5-question completion celebration | 5 questions completed | 3 Stars, Accuracy %, "Hebat! Kamu Menguasai Suku Kata!", "Kembali ke Peta", "Main Lagi" | Level Map (`MAP-01`) | `27-syllable-quiz-result.png` |

---

## 5. Level 3 — Gunung Kata

| Screen/State ID | Screen Name | Purpose | Preview Route / Scenario | Main Interactive Elements | Destination Screen | Recommended Screenshot Filename |
|---|---|---|---|---|---|---|
| `KATA-01` | Word Formation (Question Idle) | Multi-letter word spelling exercise | `Screen.WordFormation` (Q1: BOLA) | Word illustration (⚽), audio speaker button, 4 empty letter slots, 6 letter choice chips (B, O, L, A, K, U) | Letter Tapped | `28-word-formation-question.png` |
| `KATA-02` | Word Formation (Slots Partially Filled) | Visual state during construction | Tap 2 letters | Filled slots [ B ] [ O ] [ _ ] [ _ ], remaining available letter chips | Letter Tapped | `29-word-formation-partially-filled.png` |
| `KATA-03` | Word Formation (Correct Feedback) | Positive validation upon word completion | All correct letters placed | Green highlight on slots, Mascot cheering, "Hebat Sekali! Kata 'BOLA' tersusun benar! 🎉", "Kata Berikutnya" | Next Word | `30-word-formation-feedback-correct.png` |
| `KATA-04` | Word Formation (Incorrect Feedback) | Encouraging prompt when word is misordered | Incorrect letters placed | Amber banner, "Belum tepat, ayo susun hurufnya lagi! 💪", "Reset Huruf" button | Retry Slot Placement | `31-word-formation-feedback-incorrect.png` |
| `KATA-05` | Word Formation (Session Result) | 5-word completion summary | 5 words completed | Trophy, Accuracy, Questions answered (5/5), "Kembali ke Peta", "Latihan Lagi" | Level Map (`MAP-01`) | `32-word-formation-result.png` |

---

## 6. Level 4 — Sungai Cerita

| Screen/State ID | Screen Name | Purpose | Preview Route / Scenario | Main Interactive Elements | Destination Screen | Recommended Screenshot Filename |
|---|---|---|---|---|---|---|
| `CERITA-01` | Story Reading (Panel 1) | Illustrated short paragraph with audio narration | `Screen.ReadingPractice` (Panel 1) | Story illustration (🐇), Short text ("Kancil yang Cerdik"), "🔊 Bacakan Cerita" (TTS), "Halaman Berikutnya" | Story Panel 2 | `33-story-reading-panel-1.png` |
| `CERITA-02` | Story Reading (Panel 2) | Middle story panel progression | `Screen.ReadingPractice` (Panel 2) | Story illustration (🌳), Short text, audio button, "Halaman Berikutnya" | Story Panel 3 | `34-story-reading-panel-2.png` |
| `CERITA-03` | Story Reading (Panel 3) | Final story panel before comprehension | `Screen.ReadingPractice` (Panel 3) | Story illustration (🍎), Short text, "Mulai Kuis Pemahaman Cerita" button | Comprehension Quiz (`CERITA-04`) | `35-story-reading-panel-3.png` |
| `CERITA-04` | Story Comprehension Quiz (Question) | Reading comprehension assessment | Tap "Mulai Kuis" | Question text ("Siapa yang berjalan di hutan?"), 3 answer option cards | Answer Selected | `36-story-comprehension-question.png` |
| `CERITA-05` | Story Comprehension (Correct Feedback) | Immediate positive feedback | Tap correct option | Green border, "Benar Sekali! Kamu memahami ceritanya! 🌟", "Pertanyaan Berikutnya" | Next Question | `37-story-comprehension-feedback-correct.png` |
| `CERITA-06` | Story Comprehension (Incorrect Feedback) | Friendly retry feedback | Tap incorrect option | Amber border, "Ayo ingat-ingat lagi isi ceritanya ya!", "Lanjut" | Next Question | `38-story-comprehension-feedback-incorrect.png` |
| `CERITA-07` | Story Journey (Session Result) | Level 4 reading completion celebration | 3 questions completed | Certificate of Reading icon, Stars, Accuracy, "Kembali ke Petualangan", "Baca Cerita Lain" | Level Map (`MAP-01`) | `39-story-reading-result.png` |

---

## 7. Mini-Game: Tangkap Huruf

| Screen/State ID | Screen Name | Purpose | Preview Route / Scenario | Main Interactive Elements | Destination Screen | Recommended Screenshot Filename |
|---|---|---|---|---|---|---|
| `GAME-01` | Tangkap Huruf (Instructions) | Non-punitive game introduction | `Screen.TangkapHuruf` | Game mascot illustration, target explanation ("Tangkap huruf yang disebutkan!"), "Mulai Main!" button | Playing Screen (`GAME-02`) | `40-minigame-instructions.png` |
| `GAME-02` | Tangkap Huruf (Playing - Round 1) | Interactive falling letters in 3 lanes | Stage: Playing / Level 1 | Target prompt banner ("Tangkap Huruf: [ M ]"), 3 falling letter bubble lanes, live round progress (1/15), Score & Difficulty badge | Letter Tapped | `41-minigame-playing.png` |
| `GAME-03` | Tangkap Huruf (Correct Catch) | Visual pop animation on correct catch | Tap target letter bubble | Sparkling bubble effect, Score counter animation (+100), Target audio feedback | Next Round | `42-minigame-correct-catch.png` |
| `GAME-04` | Tangkap Huruf (Adaptive Level Up) | Visual indicator of difficulty advancement | 3 consecutive correct | "Level 2: Sedang" badge unlocked, lane speed slightly adapted | Continued Play | `43-minigame-adaptive-level-up.png` |
| `GAME-05` | Tangkap Huruf (Session Results) | 15-round completion celebration | 15 rounds completed | High Score card, Accuracy percentage, Difficulty achieved ("Tingkat 2"), "Main Lagi", "Kembali ke Peta" | Level Map (`MAP-01`) | `44-minigame-results.png` |

---

## 8. Profile, Badges & Developer Preview

| Screen/State ID | Screen Name | Purpose | Preview Route / Scenario | Main Interactive Elements | Destination Screen | Recommended Screenshot Filename |
|---|---|---|---|---|---|---|
| `PROF-01` | Profile Screen (Standard) | User account & settings overview | `Screen.Profile` | Child avatar, Name ("Budi"), Age, "Lihat Pencapaian & Lencana", "Mode Developer Preview" (Debug only), Logout | Progress (`BADGE-01`) or Dev Preview (`DEV-01`) | `45-profile-screen.png` |
| `BADGE-01` | Progress & Achievements (Partial Badges) | Milestones & rewards display | `Screen.ProgressAchievements` | Active days stats, 3 earned badges (color), 2 locked badges (grayscale with lock icons), Back button | Profile (`PROF-01`) | `46-progress-achievements-badges.png` |
| `BADGE-02` | Progress & Achievements (All Badges Earned) | Full mastery rewards visual state | Dev Preview: Weekly Activity `7 Days` | All 5 badges brightly colored with golden stars | Profile (`PROF-01`) | `47-progress-achievements-all-earned.png` |
| `DEV-01` | Developer Preview Hub | Central debug control panel for screenshots | Profile → "🛠️ Mode Developer Preview" | Screen launcher pills (13 destinations), Map status switchers, Visual state toggles, Weekly activity slider (0/2/3/7 days), Reset button | Selected Preview Screen | `48-developer-preview-hub.png` |

---

## 9. Visual Tokens & Color Reference for Figma

| Token Name | Hex Code | Usage in AKSA UI |
|---|---|---|
| `AksaOrange` | `#FF8A00` | Primary brand color, primary action buttons, active tab indicators |
| `AksaPeachLight` | `#FFF3E0` | Top header backgrounds, subtle container tints |
| `AksaSoftPeach` | `#FFF8F0` | Screen backgrounds (Home, Profile, Learning) |
| `AksaLime` | `#84CC16` | Adventure map background, correct feedback accents, mastery stars |
| `AksaLimeDark` | `#65A30D` | Map borders, active milestone node borders |
| `AksaBlue` | `#0284C7` | Educational header pills, audio speaker icon highlights, syllable chips |
| `AksaYellow` | `#FBBF24` | Stars, trophy badges, achievement cards |
| `AksaDarkBrown` | `#3E2723` | High-contrast readable typography for children |
| `AksaGrayBorder` | `#E2E8F0` | Unlocked slot borders, disabled state frames |
| `PillShape` | `RoundedCornerShape(50)` | Standard AKSA rounded pill buttons, badges, and choice chips |
