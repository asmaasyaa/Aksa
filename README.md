# AKSA 📚

### Aplikasi Pembelajaran Membaca Interaktif untuk Android

AKSA adalah aplikasi pembelajaran membaca berbasis Android yang dirancang untuk membantu anak usia **10–15 tahun** berlatih membaca melalui pengalaman belajar yang interaktif, bertahap, dan menyenangkan.

Project ini dikembangkan sebagai **High-Fidelity Interactive Prototype** untuk Proyek Akhir Praktikum **Pengembangan Aplikasi Perangkat Bergerak (PAPB) 2026**.

> **Status Project:** Prototype / Mockup Interaktif  
>
> Versi saat ini difokuskan pada implementasi antarmuka, navigasi, dan simulasi interaksi aplikasi. Beberapa fitur seperti autentikasi, speech recognition, dan penyimpanan data permanen masih berupa simulasi menggunakan local state.

---

## 📖 Tentang AKSA

AKSA menggunakan pendekatan pembelajaran membaca secara bertahap:

**Huruf → Suku Kata → Kata → Kalimat**

Pengalaman belajar dikemas dalam bentuk petualangan melalui beberapa level agar proses belajar terasa lebih menarik dan tidak monoton.

### 🏝️ Level 1 — Pulau Alfabet

Mengenal huruf, melatih pengucapan, dan menulis huruf.

### 🌱 Level 2 — Dunia Suku Kata

Belajar menggabungkan huruf menjadi suku kata.

### ⛰️ Level 3 — Gunung Kata

Menyusun huruf menjadi sebuah kata.

### 🌊 Level 4 — Sungai Cerita

Menerapkan kemampuan membaca melalui kalimat dan cerita sederhana.

AKSA juga memiliki sistem **learning streak**, **progress tracking**, dan **achievement** untuk mendukung pengalaman belajar yang lebih engaging.

---

## ✨ Fitur Utama

### 🔐 Authentication & Onboarding

- Splash Screen
- Login
- Registrasi akun
- Input NISN
- Input tanggal lahir
- Setup nama anak
- Pemilihan usia 10–15 tahun
- Parent onboarding

### 📚 Learning Experience

- Home Dashboard
- Interactive Level Map
- Alphabet Selection A–Z
- Pronunciation Practice
- Writing Practice
- Syllable Learning
- Syllable Quiz
- Word Formation
- Reading Practice

### 🏆 Progress & Gamification

- Learning streak
- Progress per modul
- Level progression
- Achievement badges
- Profil anak

---

## 🗺️ Application Flow

```text
Splash
   ↓
Login / Register
   ↓
Parent Welcome
   ↓
Child Profile Setup
   ↓
Home
   ↓
Level Map
   │
   ├── Pulau Alfabet
   │      ├── Alphabet Selection
   │      ├── Pronunciation Practice
   │      └── Writing Practice
   │
   ├── Dunia Suku Kata
   │      ├── Introduction
   │      ├── Example
   │      └── Syllable Quiz
   │
   ├── Gunung Kata
   │      └── Word Formation
   │
   └── Sungai Cerita
          └── Reading Practice

Home
   └── Profile
          └── Progress & Achievements
```

---

## 📱 Screens

AKSA saat ini memiliki **18 interactive screens**:

1. Splash Screen
2. Login
3. Register
4. Parent Welcome
5. Child Name
6. Child Age
7. Home
8. Level Map
9. Alphabet Selection
10. Pronunciation Practice
11. Writing Practice
12. Syllable Introduction
13. Syllable Example
14. Syllable Quiz
15. Word Formation
16. Reading Practice
17. Profile
18. Progress & Achievements

---

## 🛠️ Tech Stack

| Technology | Penggunaan |
|---|---|
| **Kotlin 2.2.10** | Bahasa pemrograman |
| **Jetpack Compose** | Declarative Android UI |
| **Material 3** | Base UI components |
| **Navigation Compose 2.9.8** | Navigasi antar-screen |
| **Compose Canvas** | Custom drawing dan visual component |
| **Poppins** | Typography |
| **Gradle** | Build system |
| **Android SDK 37** | Compile & Target SDK |
| **Min SDK 24** | Minimum Android SDK |

---

## 🏗️ Project Structure

```text
app/src/main/java/com/aksa/app/
│
├── MainActivity.kt
│
├── navigation/
│   ├── AksaNavGraph.kt
│   └── Screen.kt
│
└── ui/
    │
    ├── components/
    │   ├── AksaAlphabetCard.kt
    │   ├── AksaButton.kt
    │   ├── AksaLevelNode.kt
    │   ├── AksaMascot.kt
    │   ├── AksaProfileCard.kt
    │   ├── AksaProgressBar.kt
    │   ├── AksaSyllableCard.kt
    │   └── AksaTextField.kt
    │
    ├── screens/
    │   ├── SplashScreen.kt
    │   ├── LoginScreen.kt
    │   ├── RegisterScreen.kt
    │   ├── ParentWelcomeScreen.kt
    │   ├── ChildNameScreen.kt
    │   ├── ChildAgeScreen.kt
    │   ├── HomeScreen.kt
    │   ├── LevelMapScreen.kt
    │   ├── AlphabetSelectionScreen.kt
    │   ├── PronunciationScreen.kt
    │   ├── WritingPracticeScreen.kt
    │   ├── SyllableIntroScreen.kt
    │   ├── SyllableExampleScreen.kt
    │   ├── SyllableQuizScreen.kt
    │   ├── WordFormationScreen.kt
    │   ├── ReadingPracticeScreen.kt
    │   ├── ProfileScreen.kt
    │   └── ProgressAchievementsScreen.kt
    │
    └── theme/
        ├── AksaIcons.kt
        ├── Color.kt
        ├── Shape.kt
        ├── Theme.kt
        └── Type.kt
```

---

## 🚧 Prototype Status

Versi AKSA saat ini difokuskan pada **UI/UX, navigation, dan interactive prototype**.

| Fitur | Status |
|---|---|
| UI/UX | ✅ Implemented |
| Navigation | ✅ Implemented |
| Interactive Local State | ✅ Implemented |
| Writing Canvas | ✅ Prototype |
| Syllable Interaction | ✅ Prototype |
| Word Formation | ✅ Prototype |
| Reading Practice | ✅ Prototype |
| Progress & Achievement UI | ✅ Prototype |
| Pronunciation Detection | 🟡 Simulated |
| Authentication | 🟡 Mock |
| Persistent Local Database | ⏳ Planned |
| Backend / API | ⏳ Planned |

Pada tahap pengembangan selanjutnya, prototype dapat dikembangkan menjadi aplikasi yang lebih fungsional dengan menambahkan **ViewModel, persistent storage, authentication, speech recognition, dan data progress pengguna**.

---

## 🚀 Menjalankan Project

### Requirements

Pastikan perangkat telah memiliki:

- Android Studio
- Android SDK
- JDK yang kompatibel
- Android Emulator atau perangkat Android
- Minimum Android API 24

### 1. Clone Repository

```bash
git clone https://github.com/asmaasyaa/Aksa.git
cd Aksa
```

### 2. Buka Project

Buka **Android Studio**, lalu pilih:

```text
Open → pilih folder Aksa
```

Tunggu hingga proses **Gradle Sync** selesai.

### 3. Jalankan Aplikasi

Hubungkan perangkat Android atau gunakan Android Emulator, kemudian tekan tombol:

```text
Run ▶
```

---

## 🔨 Build APK

### Linux / macOS

```bash
./gradlew assembleDebug
```

### Windows

```powershell
gradlew.bat assembleDebug
```

APK hasil build dapat ditemukan pada:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🎨 Design System

AKSA menggunakan visual language yang playful dan child-friendly.

### Color Palette

- 🟠 **Orange** — Primary color
- 🟢 **Lime Green** — Level map & environment
- 🔵 **Light Blue** — Learning activity
- 🟡 **Yellow** — Accent & interactive elements
- ⚪ **White** — Card & content surface

### Visual Characteristics

- Rounded UI components
- Large touch targets
- Flat-vector illustration style
- Custom AKSA mascot
- Playful learning environment
- Poppins typography
- Minimal visual complexity
- Consistent color coding pada setiap learning module

---

## 🧸 AKSA Mascot

AKSA menggunakan karakter maskot sebagai pendamping anak selama proses pembelajaran.

Maskot digunakan dalam berbagai kondisi seperti:

- Menyambut pengguna
- Memberikan instruksi
- Menemani latihan
- Memberikan feedback
- Merayakan keberhasilan pengguna

Penggunaan maskot bertujuan membuat proses pembelajaran terasa lebih ramah, interaktif, dan menyenangkan.

---

## 💡 Reference & Inspiration

Konsep AKSA dikembangkan dengan mengadaptasi gagasan aplikasi pembelajaran membaca **AyoBaca**, kemudian direimplementasikan untuk platform Android menggunakan **Kotlin dan Jetpack Compose**.

Original reference:

- **AyoBaca**
- GitHub: https://github.com/yoshikazuuu/AyoBaca

AKSA dikembangkan sebagai **Android reimplementation** untuk kebutuhan Project PAPB.

Source code Swift dari AyoBaca tidak diterjemahkan secara langsung. Struktur UI, navigasi, reusable component, dan implementasi Jetpack Compose pada AKSA dikembangkan kembali untuk platform Android.

---

## 🔮 Future Development

Beberapa pengembangan yang dapat dilakukan pada tahap selanjutnya:

- Implementasi authentication
- Penyimpanan profil pengguna
- Penyimpanan progress belajar
- Room Database
- DataStore
- Android Speech Recognition
- Audio pronunciation
- Persistent learning progress
- Sistem unlock level
- Dynamic learning content
- Improved handwriting interaction
- Achievement system
- Parent progress monitoring

---

## 📌 Project Information

| Informasi | Detail |
|---|---|
| **Project Name** | AKSA |
| **Platform** | Android |
| **Course** | Pengembangan Aplikasi Perangkat Bergerak |
| **Project Type** | Proyek Akhir Praktikum PAPB 2026 |
| **Current Stage** | High-Fidelity Interactive Prototype |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |

---

## 👥 Contributors

Project AKSA dikembangkan sebagai bagian dari Proyek Akhir Praktikum **Pengembangan Aplikasi Perangkat Bergerak (PAPB) 2026**.

> Informasi anggota kelompok dapat ditambahkan pada bagian ini.

---

<p align="center">
  <b>AKSA</b>
  <br>
  <i>Belajar membaca, satu langkah dalam satu petualangan.</i>
  <br><br>
  📚 ✨
</p>