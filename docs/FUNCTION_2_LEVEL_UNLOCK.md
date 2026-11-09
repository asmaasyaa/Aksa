# DOKUMENTASI IMPLEMENTASI FUNGSI 2: EVALUASI KESIAPAN NAIK LEVEL & DYNAMIC LEVEL MAP

**Proyek:** AKSA (Aplikasi Terapi & Pembelajaran Disleksia)  
**Package:** `com.aksa.app`  
**Penanggung Jawab:** Syakira (Fungsi 2 — Evaluasi Kesiapan Naik Level)  
**Tech Stack:** Kotlin, Jetpack Compose, StateFlow, Coroutines, Repository Pattern  
**Branch:** `feature/level-unlock`

---

## 1. Pendahuluan & Latar Belakang (Konteks BAB II & BAB III)

Pada sistem pembelajaran adaptif untuk anak dengan hambatan disleksia (seperti AKSA), terdapat perbedaan mendasar antara **Session Completion** (menyelesaikan sesi aktivitas) dan **Level Mastery** (penguasaan materi pembelajaran):
* **COMPLETED:** Anak telah menjawab seluruh soal dalam suatu sesi hingga selesai.
* **MASTERED:** Anak telah mendemonstrasikan akurasi performa belajar yang memadai dan konsisten, serta tidak menunjukkan tingkat kesalahan tinggi pada huruf/suku kata tertentu.

Anak dapat menyelesaikan sebuah sesi latihan namun belum tentu menguasai materi jika tingkat akurasinya masih rendah atau tingkat kesalahannya pada huruf mirip masih tinggi. Fungsi 2 bertanggung jawab untuk mengevaluasi kesiapan naik level secara objektif, deterministik, dan berbasis bukti performa (`AnswerLog` & `SessionResult`).

Empat level petualangan AKSA:
1. **Level 1 — Pulau Alfabet (`pulau_alfabet`)**: Terbuka secara default (awalnya aktif). Prasyarat untuk membuka Level 2.
2. **Level 2 — Dunia Suku Kata (`dunia_suku_kata`)**: Awalnya terkunci. Terbuka setelah menguasai Level 1.
3. **Level 3 — Gunung Kata (`gunung_kata`)**: Awalnya terkunci. Terbuka setelah menguasai Level 2.
4. **Level 4 — Sungai Cerita (`sungai_cerita`)**: Awalnya terkunci. Terbuka setelah menguasai Level 3. Menampilkan status *Completed/Mastered* setelah menguasai evaluasi level 4.

---

## 2. Aturan Baku Evaluasi Kesiapan Naik Level (The Unlock Algorithm)

Level berikutnya **HANYA** akan terbuka jika **seluruh 4 kondisi berikut terpenuhi**:

### Kondisi 1: Ketersediaan Sesi Valid Minimal
Terdapat minimal **3 (tiga) sesi latihan valid** yang telah diselesaikan (`SessionStatus.COMPLETED`) pada modul level yang sedang dievaluasi.
* Jika sesi valid < 3: status evaluasi mengembalikan `INSUFFICIENT_SESSIONS`.

### Kondisi 2: Rata-rata Akurasi Minimal 80% (0.80f)
Rata-rata akurasi dihitung dari **3 sesi valid terakhir** (`completedAt` terbaru):
$$\text{sessionAverageAccuracy} = \frac{\text{session}_1.\text{accuracy} + \text{session}_2.\text{accuracy} + \text{session}_3.\text{accuracy}}{3}$$
* Nilai akurasi berada pada rentang desimal Float `0.0f` hingga `1.0f` (80% = `0.80f`).
* Jika `sessionAverageAccuracy < 0.80f`: status mengembalikan `LOW_ACCURACY`.

### Kondisi 3: Kecukupan Bukti Jawaban per Target (Evidence Sufficiency)
Setiap target pembelajaran yang diujikan (huruf pada modul alfabet, suku kata pada modul suku kata, kata pada modul kata) wajib memiliki minimal **3 (tiga) kali percobaan jawaban** yang tercatat dalam log:
* Jika riwayat `AnswerLog` kosong atau ada target yang memiliki percobaan < 3: status mengembalikan `INSUFFICIENT_EVIDENCE`.
* **Prinsip Kritis:** Ketiadaan `AnswerLog` **TIDAK BOLEH** dianggap sebagai "nol kesalahan" (zero errors). Level tidak akan terbuka tanpa bukti log detail.

### Kondisi 4: Batas Maksimal Error Rate Target <= 40% (0.40f)
Tingkat kesalahan pada setiap target yang telah cukup dievaluasi dihitung dengan rumus:
$$\text{targetErrorRate} = \frac{\text{incorrectAttemptsForTarget}}{\text{totalAttemptsForTarget}}$$
* Jika ada target dengan `targetErrorRate > 0.40f` (40%): status mengembalikan `HIGH_ERROR_RATE`.

### Hasil Evaluasi
Jika Kondisi 1, 2, 3, dan 4 terpenuhi seluruhnya, fungsi mengembalikan `READY_TO_UNLOCK` dan level berikutnya terbuka.

---

## 3. Mengapa Tidak Ada Syarat Hari Belajar (Learning Days)?

Aturan pembukaan level dalam Fungsi 2 **secara sengaja TIDAK mencakup** syarat:
* Jumlah hari belajar berbeda (misalnya harus di 2 hari berbeda).
* Aktivitas dalam 7 hari terakhir.
* Daily streak atau mingguan.

**Alasan Arsitektural & Pedagogis:**
1. **Pemisahan Tanggung Jawab Anggota (Separation of Concerns):** Konsistensi kehadiran, bintang mingguan, kalender aktivitas, dan streak dikelola oleh **Fungsi 4 (Diny)**.
2. **Pedagogi Disleksia:** Penguasaan kognitif/fonologis (Mastery) ditentukan oleh performa akurasi dan pengenalan target, bukan oleh kalender dinding. Pembatasan hari belajar pada evaluasi penguasaan level akan menghambat anak yang memiliki motivasi tinggi untuk menyelesaikan latihan secara intensif.

---

## 4. Definisi Sesi Valid & Filtrasi Data

Sebuah record `SessionResult` dinyatakan **VALID** jika dan hanya jika:
1. `childId == evaluatedChildId`: Milik anak yang sedang dievaluasi.
2. `moduleId == evaluatedModuleId`: Milik modul pembelajaran yang sesuai.
3. `status == SessionStatus.COMPLETED`: Sesi berstatus selesai. Sesi `IN_PROGRESS` atau `ABANDONED` diabaikan.
4. `completedAt != null && completedAt > 0`: Memiliki timestamp selesai yang valid.
5. `totalQuestions > 0`: Jumlah soal lebih dari nol.
6. Konsistensi internal: `correctAnswers >= 0 && incorrectAnswers >= 0 && (correctAnswers + incorrectAnswers == totalQuestions)`.
7. `accuracy in 0.0f..1.0f`: Nilai akurasi valid.
8. Bukan aktivitas mini-game: Aktivitas seperti `tangkap_huruf` (Fungsi 3) dikecualikan dari evaluasi level modul formal.
9. Deduplikasi record: Sesi dengan `sessionId` yang sama dideduplikasi sehingga tidak melipatgandakan hitungan sesi.

Sesi valid diurutkan menurun berdasarkan `completedAt` (`sortedByDescending { it.completedAt }`), dan hanya 3 sesi teratas yang diambil untuk evaluasi rolling window.

---

## 5. Status Evaluasi & Pesan Ramah Anak (Child-Friendly Indonesian)

| Status | Penjelasan | Contoh Pesan / Umpan Balik |
|---|---|---|
| `INSUFFICIENT_SESSIONS` | Sesi valid < 3 | *"Selesaikan 2 sesi latihan lagi, yuk!"* |
| `LOW_ACCURACY` | Rata-rata akurasi 3 sesi terakhir < 80% | *"Ayo latihan lagi supaya hasil akurasimu mencapai 80% atau lebih!"* |
| `INSUFFICIENT_EVIDENCE` | Log detail jawaban kosong atau ada target dengan percobaan < 3 | *"Kita perlu beberapa jawaban lagi untuk memastikan kamu siap."* |
| `HIGH_ERROR_RATE` | Ada target dengan error rate > 40% | *"Yuk, latihan lagi membedakan huruf b, d!"* |
| `READY_TO_UNLOCK` | Seluruh 4 kondisi terpenuhi | *"Hebat! Petualangan berikutnya sudah terbuka!"* |

---

## 6. Model Data dan Use Case Baru

### File yang Dibuat:
1. `domain/model/LevelReadiness.kt`:
   * `enum class LevelReadinessStatus`
   * `data class TargetPerformance`
   * `data class LevelReadinessResult`
2. `domain/model/LevelProgress.kt`:
   * `enum class LevelUnlockReason`
   * `data class LevelItemProgress`
3. `domain/repository/LevelProgressRepository.kt`:
   * Kontrak penyimpanan progres level (`getUnlockedLevels`, `unlockLevel`, `observeUnlockedLevels`).
4. `data/repository/demo/DemoLevelProgressRepository.kt`:
   * Implementasi in-memory repository progres level untuk Demo Mode.
5. `domain/usecase/GetActiveChildIdUseCase.kt`:
   * Helper terpusat untuk me-resolve child ID aktif dari guardian/profil aktif (`child_$guardianUid` atau profil tersimpan), menggantikan hardcode `child_marvin_demo`.
6. `domain/usecase/EvaluateLevelReadinessUseCase.kt`:
   * Use case deterministik murni independen dari UI yang mengimplementasikan 4 kondisi evaluasi di atas.
7. `domain/usecase/GetLevelProgressionUseCase.kt`:
   * Use case rekonsiliasi progres 4 level, menjamin level yang sudah pernah terbuka tidak akan terkunci kembali.
8. `ui/viewmodel/LevelMapViewModel.kt`:
   * ViewModel reaktif yang mengamati perubahan sesi dan progres level secara real-time.
9. `app/src/test/java/com/aksa/app/LevelReadinessEvaluationTest.kt`:
   * 24 unit test komprehensif menguji seluruh skenario positif, batas, dan negatif.

### File Bersama yang Diperbarui secara Terisolasi:
1. `data/repository/AksaRepositoryProvider.kt`: Ditambahkan `levelProgressRepository` & `demoLevelProgressRepository`.
2. `ui/components/AksaLevelNode.kt`: Diubah agar node berstatus `LOCKED` tetap dapat diklik untuk memunculkan penjelasan ramah anak (tidak pasif/mati).
3. `ui/screens/LevelMapScreen.kt`: Menggunakan `LevelMapViewModel` dan menampilkan modal penjelasan edukatif saat level terkunci diklik.
4. `LetterLearningScreen.kt`: Menyimpan `AnswerLog` per butir kuis suara dan menggunakan `activeChildResolver`.
5. `SyllableQuizScreen.kt`: Menyimpan `AnswerLog` per penyusunan suku kata dan menggunakan `activeChildResolver`.
6. `WordFormationScreen.kt`: Menyimpan `AnswerLog` per susunan kata dan menggunakan `activeChildResolver`.
7. `ReadingPracticeScreen.kt`: Menyimpan `AnswerLog` per soal pemahaman membaca dan menggunakan `activeChildResolver`.

---

## 7. Prinsip Preservasi Level Terbuka (Non-Relocking Rule)

Level progression dirancang berurutan:
* Level 1 terbuka secara default.
* Level 2 terbuka jika Level 1 memenuhi kriteria penguasaan (`READY_TO_UNLOCK`).
* Level 3 terbuka jika Level 2 memenuhi kriteria penguasaan.
* Level 4 terbuka jika Level 3 memenuhi kriteria penguasaan.

**Anti-Relocking:**
Jika seorang anak telah berhasil membuka Level 2, lalu kemudian memainkan Level 1 kembali dan mendapatkan skor rendah (misal akurasi 20%), Level 2 **TETAP TERBUKA**. Progres yang telah diperoleh tersimpan pada `LevelProgressRepository` dan tidak akan dikunci kembali oleh fluktuasi skor sesi berikutnya.

---

## 8. Mode Pengembang (Developer Preview Mode)

Developer Preview Mode (diperkenalkan pada Phase 2 untuk Figma screenshot dan presentasi UI) **tetap dipertahankan 100%**:
* `ALL_UNLOCKED`: Seluruh level berstatus `ACTIVE` dan dapat diakses.
* `ALL_COMPLETED`: Seluruh level berstatus `COMPLETED` dan dapat diakses.
* `ALL_LOCKED_EXCEPT_LEVEL_1`: Level 1 aktif, Level 2-4 terkunci.
* `DEFAULT_PROGRESSION`: Level 1 & 2 aktif, Level 3 & 4 terkunci.

**Aturan Keamanan Preview:**
1. Override preview hanya aktif jika `com.aksa.app.BuildConfig.DEBUG == true` **DAN** `DeveloperPreviewManager.isPreviewActive == true`.
2. Pada mode rilis (`release`), override developer preview diblokir secara mutlak (`BuildConfig.DEBUG == false`), sehingga akses level selalu mengikuti evaluasi performa asli.
3. Mode preview hanya memengaruhi presentasi UI; tidak ada data palsu yang disimpan ke repository permanen. Menonaktifkan preview akan langsung mengembalikan tampilan ke data performa nyata.

---

## 9. Penjelasan Level Terkunci pada UI Level Map

Ketika anak mengetuk node level yang berstatus `LOCKED`:
1. Navigasi ke dalam level dicegah.
2. Muncul dialog modal ramah anak (`LockedLevelModal`) dengan maskot Aksa.
3. Menjelaskan secara transparan mengapa level terkunci (misalnya: *"Selesaikan 2 sesi latihan lagi di Pulau Alfabet, yuk!"*).
4. Menampilkan daftar checklist syarat (minimal 3 sesi, akurasi >= 80%, error rate <= 40%).
5. Menyediakan tombol aksi langsung: *"Ayo Latihan di [Level Prasyarat]"* yang langsung mengarahkan anak ke level prasyarat untuk berlatih.

---

## 10. Contoh Kasus Nyata

### Contoh 1: Lolos Naik Level
Anak menyelesaikan 3 sesi di Level 1 (Pulau Alfabet):
* Sesi 1: Akurasi 90% (huruf target: 'b' 5x, salah 1x; 'd' 5x, salah 0x)
* Sesi 2: Akurasi 80% (huruf target: 'b' 5x, salah 1x; 'd' 5x, salah 1x)
* Sesi 3: Akurasi 100% (huruf target: 'b' 5x, salah 0x; 'd' 5x, salah 0x)

**Evaluasi:**
* Jumlah sesi valid: 3 (>= 3) -> **Lolos**
* Rata-rata akurasi: $(90\% + 80\% + 100\%) / 3 = 90\%$ (>= 80%) -> **Lolos**
* Bukti target:
  * Huruf 'b': 15 percobaan (>= 3), salah 2 -> Error rate = $2/15 = 13.3\%$ (<= 40%) -> **Lolos**
  * Huruf 'd': 15 percobaan (>= 3), salah 1 -> Error rate = $1/15 = 6.7\%$ (<= 40%) -> **Lolos**
* **Hasil:** `READY_TO_UNLOCK`. Level 2 (Dunia Suku Kata) terbuka!

### Contoh 2: Gagal Karena Tingkat Kesalahan Huruf Tertentu
Anak menyelesaikan 3 sesi di Level 1:
* Rata-rata akurasi sesi: 82% (>= 80%)
* Huruf 'b': 12 percobaan, salah 1 -> Error rate = 8.3%
* Huruf 'd': 6 percobaan, salah 3 -> Error rate = $3/6 = 50\%$ (> 40%)
* **Hasil:** `HIGH_ERROR_RATE`. Level 2 tetap terkunci dengan pesan: *"Yuk, latihan lagi membedakan huruf d!"*

---

## 11. Cakupan 24 Unit Test Mandatori

Seluruh 24 kasus uji yang disyaratkan telah diimplementasikan dalam `app/src/test/java/com/aksa/app/LevelReadinessEvaluationTest.kt`:
1. `test1_level1_unlockedByDefault`: Level 1 terbuka secara default untuk setiap anak.
2. `test2_level2_lockedForNewChild`: Level 2 terkunci untuk profil anak baru.
3. `test3_fewerThanThreeSessions_doNotUnlock`: 1 atau 2 sesi tidak membuka level (`INSUFFICIENT_SESSIONS`).
4. `test4_threeValidSessions_sufficientSessionCount`: 3 sesi valid memenuhi syarat kuantitas sesi.
5. `test5_exactly80PercentAverageAccuracy_passes`: Rata-rata akurasi tepat 80% (0.80f) lolos evaluasi.
6. `test6_averageAccuracyBelow80Percent_fails`: Rata-rata akurasi di bawah 80% gagal (`LOW_ACCURACY`).
7. `test7_exactly40PercentTargetErrorRate_passes`: Error rate target tepat 40% (0.40f) lolos evaluasi.
8. `test8_targetErrorRateAbove40Percent_fails`: Error rate target di atas 40% gagal (`HIGH_ERROR_RATE`).
9. `test9_fewerThanThreeAttemptsForTarget_returnsInsufficientEvidence`: Target dengan < 3 percobaan mengembalikan `INSUFFICIENT_EVIDENCE`.
10. `test10_missingAnswerLog_doesNotCountAsZeroErrors`: Sesi tanpa log jawaban detail tidak dianggap nol kesalahan (`INSUFFICIENT_EVIDENCE`).
11. `test11_abandonedSessions_doNotCount`: Sesi `ABANDONED` diabaikan dari hitungan sesi valid.
12. `test12_inProgressSessions_doNotCount`: Sesi `IN_PROGRESS` diabaikan dari hitungan sesi valid.
13. `test13_sessionsFromDifferentModules_doNotMix`: Sesi dari modul lain tidak bercampur dalam evaluasi level tertentu.
14. `test14_miniGameSessions_doNotUnlockLearningLevels`: Sesi mini-game (Tangkap Huruf) tidak dihitung dalam penguasaan level modul.
15. `test15_differentChildAccounts_doNotShareProgress`: Progres belajar antar akun anak terisolasi secara ketat.
16. `test16_priorUnlockedLevels_remainUnlockedAfterLaterLowScores`: Level yang sudah terbuka tetap terbuka meski skor berikutnya rendah.
17. `test17_level3_dependsOnLevel2Mastery`: Level 3 hanya terbuka setelah Level 2 berstatus `READY_TO_UNLOCK`.
18. `test18_level4_dependsOnLevel3Mastery`: Level 4 hanya terbuka setelah Level 3 berstatus `READY_TO_UNLOCK`.
19. `test19_normalMode_ignoresDeveloperPreviewOverrides`: Mode normal mengabaikan konfigurasi preview manager.
20. `test20_debugPreview_canDisplayAllUnlockedWithoutModifyingRealData`: Preview mode dapat menampilkan semua level tanpa mengubah repository asli.
21. `test21_releaseMode_doesNotAllowPreviewBypass`: Build release tidak mengizinkan bypass preview.
22. `test22_sessionHistoryUpdates_refreshEffectiveMapState`: Penambahan sesi baru secara reaktif memperbarui peta tanpa restart aplikasi.
23. `test23_duplicateSessions_doNotIncreaseSessionCount`: Record sesi duplikat dideduplikasi dan tidak menambah hitungan sesi.
24. `test24_noRequirementChecks_numberOfDistinctLearningDays`: Tidak ada validasi yang menghitung jumlah hari belajar yang berbeda.

---

## 12. Keterbatasan Demo Mode

Pada implementasi Demo Mode saat ini (`DemoLevelProgressRepository` & `DemoSessionRepository`), penyimpanan menggunakan `MutableStateFlow` in-memory. Jika aplikasi ditutup paksa (*kill process*), data sesi dan progres level pada memori volatil akan kembali ke kondisi awal (Level 1 aktif, Level 2-4 terkunci). Untuk persistensi permanen lintas sesi proses, modul dapat dengan mudah dialihkan ke Firestore (`FirestorePaths`) sesuai arsitektur `AksaRepositoryProvider`.
