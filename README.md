# AKSA 📚

### Aplikasi Pembelajaran Membaca untuk Anak dengan Disleksia

AKSA adalah aplikasi Android yang membantu anak-anak berlatih membaca Bahasa Indonesia secara bertahap dan interaktif. Dirancang khusus dengan mempertimbangkan tantangan disleksia, AKSA menggunakan pendekatan **Huruf → Suku Kata → Kata** yang dikemas dalam mini-game adaptif, pelacakan progres, dan sistem reward berbasis pencapaian.

## ✨ Fitur Utama

### Autentikasi & Profil
- Login dan registrasi via email/password atau Google Sign-In (Firebase Auth)
- Manajemen multi-profil anak dalam satu akun orang tua
- Data profil dan progres belajar tersimpan di Firestore

### Aktivitas Belajar
- **Pengenalan Huruf** — kuis interaktif per huruf A–Z
- **Huruf Mirip** — latihan membedakan huruf yang sering tertukar (b/d, p/q)
- **Latihan Menulis** — tracing stroke huruf di atas kanvas
- **Latihan Pengucapan** — praktik pelafalan berbasis mikrofon
- **Suku Kata** — pengenalan, contoh, dan kuis penggabungan suku kata
- **Tangkap Huruf** — mini-game menangkap huruf yang jatuh
- **Pembentukan Kata** — menyusun huruf menjadi kata
- **Latihan Membaca** — membaca kalimat dan teks sederhana

### Sistem Adaptif & Progres
- Analisis pola kesalahan per sesi untuk menyesuaikan soal berikutnya
- Pelacakan aktivitas mingguan dan pencapaian milestone
- Sistem bintang per sesi dan badge pencapaian
- Penyimpanan seluruh data progres di Firestore

## 🛠️ Teknologi

| Teknologi | Versi |
|---|---|
| Kotlin | 2.4.0 | 
| Android Gradle Plugin | 9.3.3 | 
| Jetpack Compose BOM | 2026.02.01 | 
| Material 3 | (via BOM) | 
| Navigation Compose | 2.9.8 | 
| Firebase BOM | 33.9.0 | 
| Firebase Auth | (via BOM) | 
| Firebase Firestore | (via BOM) | 
| Credentials API | 1.5.0 | 
| Coroutines | 1.10.1 | 
| Compile SDK | 37 | 
| Min SDK | 24 

## 🚀 Panduan Penggunaan

### Prasyarat

- Android Studio (versi terbaru)
- JDK 11 atau lebih baru
- Android Emulator atau perangkat fisik (API 24+)
- File `google-services.json` dari Firebase Console (tidak disertakan di repo)

### Menjalankan Aplikasi

```bash
git clone https://github.com/asmaasyaa/Aksa.git
cd Aksa
```

Buka folder `Aksa` di Android Studio, tunggu Gradle Sync selesai, lalu tekan **Run ▶**.

> Aplikasi membutuhkan `google-services.json` yang valid untuk fitur autentikasi dan penyimpanan data. Tambahkan file tersebut ke direktori `app/` setelah mengonfigurasi project di [Firebase Console](https://console.firebase.google.com).

### Build APK

```bash
# Linux / macOS
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## 📂 Struktur Proyek

```
app/src/main/java/com/aksa/app/
│
├── AksaApplication.kt          # Inisialisasi aplikasi
├── MainActivity.kt             # Entry point Activity
│
├── audio/                      # Text-to-speech dan audio helper
│
├── data/
│   ├── model/                  # DTO untuk serialisasi Firestore
│   ├── remote/                 # Konstanta path Firestore
│   └── repository/             # Implementasi repository (Firebase + demo)
│
├── domain/
│   ├── model/                  # Domain model (ChildProfile, SessionResult, dll.)
│   ├── repository/             # Interface repository
│   └── usecase/                # Business logic (analisis error, kalkulasi bintang, dll.)
│
├── navigation/                 # NavGraph dan definisi Screen
│
└── ui/
    ├── components/             # Reusable Compose components (AksaButton, AksaNavbar, dll.)
    ├── screens/                # Seluruh screen aplikasi (~24 screens)
    ├── state/                  # State holder global (AksaUserState)
    ├── theme/                  # Warna, tipografi, shape, ikon
    └── viewmodel/              # ViewModel per fitur
```


## 📜 Referensi & Atribusi

- **Referensi Utama:** [AyoBaca](https://github.com/yoshikazuuu/AyoBaca), aplikasi pembelajaran membaca untuk iOS yang menjadi referensi konsep dan alur pembelajaran AKSA.
- **Adaptasi Proyek:** AKSA dikembangkan kembali untuk platform Android menggunakan Kotlin dan Jetpack Compose. Struktur UI, navigasi, komponen, dan logika aplikasi diimplementasikan ulang oleh tim AKSA.

## 👥 Kontributor

AKSA dikembangkan oleh **Kelompok 3** sebagai Proyek Akhir Praktikum mata kuliah **Pengembangan Aplikasi Perangkat Bergerak (PAPB), kelas TIF-A**.

| Kontributor | GitHub |
|---|---|
| Rafiqi Abyan | [@rafeeq-s](https://github.com/rafeeq-s) |
| Dealova Nabila | [@dvsalmah](https://github.com/dvsalmah) |
| Diny Eka | [@dzharafahh](https://github.com/dzharafahh) |
| Syakira Asma | [@asmaasyaa](https://github.com/asmaasyaa) |

---

<p align="center">
  <b>AKSA</b>
  <br>
  <i>Belajar membaca, satu langkah dalam satu petualangan.</i>
  <br>
  📚 ✨
</p>