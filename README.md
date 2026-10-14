# AKSA — Aplikasi Pembelajaran Membaca Interaktif

AKSA adalah aplikasi edukasi membaca interaktif untuk anak usia 10–15 tahun yang mengalami kesulitan membaca (*dyslexia* atau *reading difficulties*). Proyek ini dikembangkan sebagai prototipe UI/UX High-Fidelity (*clickable mockup*) untuk mata kuliah **Pengembangan Aplikasi Perangkat Bergerak (PAPB)**.

---

## 🌟 Fitur Utama & Alur Aplikasi

Aplikasi memiliki 18 layar interaktif lengkap:

1. **Splash Screen**: Layar pembuka dengan maskot animasi AKSA.
2. **Login & Registrasi Akun**: Formulir akun orang tua dengan toggle PIN/Password.
3. **Onboarding & Profil Anak**: Pengaturan nama dan usia anak (10–15 tahun).
4. **Home Dashboard**: Kartu profil anak (Marvin, 12 tahun, LV 3), indikator streak 9 hari, progres belajar, dan navigasi modul.
5. **Level Map (Peta Petualangan)**: Peta petualangan *winding path* interaktif berlatar hijau dengan 4 node level:
   - **Level 1 — Pulau Alfabet**: Pilihan huruf A–Z dengan petunjuk kata visual.
   - **Latihan Pengucapan (Pronunciation)**: Simulasi deteksi suara mikrofon dengan umpan balik visual dan audio speaker.
   - **Latihan Menulis (Writing Practice)**: Kanvas gambar jari interaktif di atas garis panduan huruf dengan tombol *Ulangi* & *Submit*.
   - **Level 2 — Dunia Suku Kata**: Pengenalan suku kata dengan maskot pemandu.
   - **Contoh Suku Kata**: Visualisasi penggabungan suku kata (MA + TA = MATA).
   - **Kuis Suku Kata**: Menyusun suku kata dengan kartu pilihan interaktif.
   - **Level 3 — Gunung Kata**: Menyusun huruf acak (L, A, B, O) menjadi kata bermakna (BOLA).
   - **Level 4 — Sungai Cerita**: Latihan membaca cerita pendek bertingkat dengan penyorot kalimat dan audio.
6. **Profil & Pencapaian**: Ringkasan streak, persentase progres per modul, dan lencana pencapaian (*achievements*).

---

## 🛠️ Teknologi yang Digunakan

- **Bahasa**: Kotlin 2.2.10
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Navigasi**: Navigation Compose
- **Desain & Aset**: Custom Vector Graphics & Canvas Compose (tanpa dependensi eksternal berat)
- **Tipografi**: Poppins Font Family
- **Build System**: Gradle 9.5.0, Android Gradle Plugin 9.3.3
- **Target SDK**: Android 37 (Min SDK 24)

---

## 🚀 Cara Menjalankan Proyek

### Menggunakan Android Studio:
1. *Clone* repositori ini:
   ```bash
   git clone <URL_REPO_GITHUB_ANDA>
   ```
2. Buka Android Studio, pilih **Open** lalu arahkan ke folder proyek.
3. Sinkronkan Gradle (*Sync Project with Gradle Files*).
4. Hubungkan perangkat Android fisik atau jalankan Android Emulator.
5. Klik tombol **Run (▶)**.

### Menggunakan Command Line (Terminal):
```bash
# Build file APK Debug
./gradlew assembleDebug

# Install langsung ke perangkat Android yang terhubung
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
File APK hasil build dapat ditemukan di:
`app/build/outputs/apk/debug/app-debug.apk`
