# DOKUMENTASI IMPLEMENTASI FUNGSI 2: EVALUASI KESIAPAN NAIK LEVEL & DYNAMIC LEVEL MAP

**Proyek:** AKSA (Aplikasi Terapi & Pembelajaran Disleksia)  
**Package:** `com.aksa.app`  
**Penanggung Jawab:** Syakira (Fungsi 2 — Evaluasi Kesiapan Naik Level)  
**Tech Stack:** Kotlin, Jetpack Compose, StateFlow, Coroutines, Repository Pattern  
**Branch:** `integration/f2-safe-to-dev`

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
| `LOW_ACCURACY` | Rata-rata akurasi 3 sesi terakhir < 80% | *"Ayo latihan lagi agar kamu semakin siap melanjutkan petualangan!"* |
| `INSUFFICIENT_EVIDENCE` | Log detail jawaban kosong, ada sesi tanpa log, atau target memiliki percobaan < 3 | *"Yuk, coba beberapa latihan lagi supaya kemampuanmu bisa dinilai!"* |
| `HIGH_ERROR_RATE` | Ada target dengan error rate > 40% | *"Yuk, latihan lagi membedakan huruf b dan d!"* |
| `READY_TO_UNLOCK` | Seluruh 4 kondisi terpenuhi | *"Hebat! Petualangan berikutnya sudah terbuka!"* |

---

## 6. Model Data, Repository, dan Use Case

### Komponen Inti Fungsi 2:
1. `domain/model/LevelReadiness.kt`:
   * `enum class LevelReadinessStatus` (`INSUFFICIENT_SESSIONS`, `INSUFFICIENT_EVIDENCE`, `LOW_ACCURACY`, `HIGH_ERROR_RATE`, `READY_TO_UNLOCK`) — tidak lagi memiliki `INSUFFICIENT_PRACTICE_DAYS`.
   * `data class TargetPerformance`
   * `data class LevelReadinessResult` (menyimpan `distinctDaysInLast7Days` hanya sebagai statistik informatif, bukan syarat unlock).
2. `domain/model/LevelProgress.kt`:
   * `enum class LevelUnlockReason` (`DEFAULT_UNLOCKED`, `PREVIOUS_LEVEL_MASTERED`, `ALREADY_EARNED`, `LOCKED_PREREQUISITE`)
   * `data class LevelItemProgress`
3. `domain/repository/LevelProgressRepository.kt` & `data/repository/FirestoreLevelProgressRepository.kt`:
   * Kontrak & implementasi Cloud Firestore (`users/{parentUid}/profiles/{profileId}/levelProgress/unlocks`) dengan `FieldValue.arrayUnion(...)` yang bersifat *monotonic* dan *idempotent*.
4. `domain/usecase/GetActiveChildIdUseCase.kt`:
   * Helper terpusat untuk me-resolve `profileId` anak aktif dari guardian/profil aktif yang terautentikasi.
5. `domain/usecase/EvaluateLevelReadinessUseCase.kt`:
   * Use case deterministik murni independen dari UI yang mengimplementasikan 4 kondisi evaluasi (tanpa syarat jumlah hari belajar) menggunakan `getCompletedSessionsResult(childId)` dan `getAnswersForChildResult(childId)` agar tidak tergantung pada *collectionGroup composite index* serta selalu mempropagasi error pembacaan Firestore.
6. `domain/usecase/GetLevelProgressionUseCase.kt`:
   * Use case rekonsiliasi progres 4 level secara berurutan, menjamin level yang sudah pernah terbuka tidak akan terkunci kembali dan kegagalan tulis ke Firestore tidak diabaikan diam-diam.
7. `ui/viewmodel/LevelMapViewModel.kt`:
   * ViewModel reaktif mode produksi yang mereset progres profil sebelumnya saat profil anak berganti, memperbarui peta saat kembali dari latihan, dan menampilkan pesan error jujur jika pembacaan Firestore gagal.
8. `app/src/test/java/com/aksa/app/LevelReadinessEvaluationTest.kt`:
   * 35 unit test komprehensif menguji seluruh skenario positif, batas, negatif, isolasi multi-profil, dan error handling Firestore.

### File Bersama yang Diperbarui secara Terisolasi:
1. `domain/repository/SessionRepository.kt` & `data/repository/FirestoreSessionRepository.kt`: Ditambahkan `getCompletedSessionsResult(childId)` dan `getAnswersForChildResult(childId)` yang mengembalikan `Result` (mempropagasi exception pembacaan Firestore tanpa memengaruhi pemanggil fitur F1/F3/F4).
2. `ui/components/AksaLevelNode.kt`: Node berstatus `LOCKED` dapat diklik untuk memunculkan penjelasan ramah anak.
3. `ui/screens/LevelMapScreen.kt`: Menggunakan `LevelMapViewModel` mode produksi, refresh otomatis pada `ON_RESUME`, menampilkan banner error saat Firestore gagal, dan modal penjelasan edukatif tanpa syarat hari belajar.
4. `LetterLearningScreen.kt`, `SyllableQuizScreen.kt`, `WordFormationScreen.kt`, `ReadingPracticeScreen.kt`: Menyimpan `AnswerLog` + `SessionResult` dalam blok `withContext(NonCancellable)` dengan status `isSavingSession` agar navigasi kembali ke Level Map tidak membatalkan penulisan Firestore.

---

## 7. Prinsip Preservasi Level Terbuka (Non-Relocking Rule)

Level progression dirancang berurutan:
* Level 1 terbuka secara default.
* Level 2 terbuka jika Level 1 memenuhi kriteria penguasaan (`READY_TO_UNLOCK`).
* Level 3 terbuka jika Level 2 memenuhi kriteria penguasaan.
* Level 4 terbuka jika Level 3 memenuhi kriteria penguasaan.

**Anti-Relocking:**
Jika seorang anak telah berhasil membuka Level 2, lalu kemudian memainkan Level 1 kembali dan mendapatkan skor rendah (misal akurasi 20%), Level 2 **TETAP TERBUKA**. Progres yang telah diperoleh tersimpan permanen di Firestore (`levelProgress/unlocks`) dan tidak akan dikunci kembali oleh fluktuasi skor sesi berikutnya.

---

## 8. Penjelasan Level Terkunci & Error Handling pada UI Level Map

Ketika anak mengetuk node level yang berstatus `LOCKED`:
1. Navigasi ke dalam level dicegah.
2. Muncul dialog modal ramah anak (`LockedLevelExplanationDialog`) dengan maskot Aksa.
3. Menjelaskan secara transparan alasan aktual sesuai hasil evaluasi (misal `INSUFFICIENT_SESSIONS`, `LOW_ACCURACY`, `INSUFFICIENT_EVIDENCE`, atau `HIGH_ERROR_RATE`).
4. Menampilkan daftar checklist syarat aktual F2 (minimal 3 sesi selesai, akurasi rata-rata >= 80%, error rate materi <= 40% — **tanpa** syarat hari belajar).
5. Menyediakan tombol aksi langsung: *"Ayo Latihan di [Level Prasyarat]"*.
6. Jika pembacaan Firestore gagal (offline / *permission denied*), UI menampilkan pesan error pembacaan yang jujur dan tombol muat ulang — **tidak pernah** mendiagnosis anak kurang berlatih secara keliru.

---

## 9. Contoh Kasus Nyata

### Contoh 1: Lolos Naik Level (Termasuk pada Hari yang Sama)
Anak menyelesaikan 3 sesi di Level 1 (Pulau Alfabet) pada hari yang sama:
* Sesi 1: Akurasi 90% (huruf target: 'b' 5x, salah 1x; 'd' 5x, salah 0x)
* Sesi 2: Akurasi 80% (huruf target: 'b' 5x, salah 1x; 'd' 5x, salah 1x)
* Sesi 3: Akurasi 100% (huruf target: 'b' 5x, salah 0x; 'd' 5x, salah 0x)

**Evaluasi:**
* Jumlah sesi valid: 3 (>= 3) -> **Lolos**
* Rata-rata akurasi: $(90\% + 80\% + 100\%) / 3 = 90\%$ (>= 80%) -> **Lolos**
* Bukti target:
  * Huruf 'b': 15 percobaan (>= 3), salah 2 -> Error rate = $2/15 = 13.3\%$ (<= 40%) -> **Lolos**
  * Huruf 'd': 15 percobaan (>= 3), salah 1 -> Error rate = $1/15 = 6.7\%$ (<= 40%) -> **Lolos**
* Syarat hari berbeda: **Tidak disyaratkan**
* **Hasil:** `READY_TO_UNLOCK`. Level 2 (Dunia Suku Kata) langsung terbuka!

### Contoh 2: Gagal Karena Tingkat Kesalahan Huruf Tertentu
Anak menyelesaikan 3 sesi di Level 1:
* Rata-rata akurasi sesi: 82% (>= 80%)
* Huruf 'b': 12 percobaan, salah 1 -> Error rate = 8.3%
* Huruf 'd': 6 percobaan, salah 3 -> Error rate = $3/6 = 50\%$ (> 40%)
* **Hasil:** `HIGH_ERROR_RATE`. Level 2 tetap terkunci dengan pesan: *"Yuk, latihan lagi membedakan huruf d!"*

---

## 10. Cakupan 35 Unit Test Fungsi 2 (`LevelReadinessEvaluationTest.kt`)

Seluruh kasus uji mandatori F2 telah diverifikasi pada `app/src/test/java/com/aksa/app/LevelReadinessEvaluationTest.kt`:
* `test1`–`test3`: Default Level 1 terbuka, Level 2 terkunci untuk anak baru, dan < 3 sesi mengembalikan `INSUFFICIENT_SESSIONS`.
* `test4`–`test10`, `test30`: 3 sesi valid, batas akurasi tepat 80% / < 80%, batas error rate tepat 40% / > 40%, bukti target < 3 percobaan, serta ketiadaan `AnswerLog` (penuh maupun parsial) mengembalikan `INSUFFICIENT_EVIDENCE`.
* `test11`–`test18`, `test23`, `test29`: Sesi `ABANDONED`/`IN_PROGRESS` diabaikan, mini-game diabaikan, modul berbeda terisolasi, akun anak terisolasi, anti-relocking monotonic, ketergantungan berurutan Level 2 → 3 → 4, serta deduplikasi sesi.
* `test19`, `test22`: Progres murni berbasis data nyata tanpa override preview dan pembaruan peta reaktif saat histori sesi bertambah.
* `test24`, `test24b`–`test24f`: **3 sesi valid pada hari yang sama berhasil membuka level berikutnya** (`READY_TO_UNLOCK`), 3 sesi pada hari berbeda maupun > 7 hari tetap berhasil membuka level, serta tidak ada status `INSUFFICIENT_PRACTICE_DAYS`.
* `test25`–`test28`: Isolasi multi-profil di bawah satu wali, pergantian profil aktif, persistensi monotonic `LevelProgressRepository`, dan resolusi `GetActiveChildIdUseCase`.
* `test31`–`test32`: Propagasi error pembacaan Firestore tanpa diagnosis mastery menyesatkan, pembersihan progres profil lama saat profil anak diganti di `LevelMapViewModel`, dan verifikasi pesan dialog terkunci tanpa teks 2 hari.

---

## 11. Persistensi Cloud Firestore & Catatan Deployment

Progres pembukaan level kini disimpan permanen di Cloud Firestore melalui `FirestoreLevelProgressRepository` pada dokumen `users/{parentUid}/profiles/{profileId}/levelProgress/unlocks`. Data bertahan saat aplikasi ditutup, dibuka kembali, atau saat wali mengganti profil anak.
**Prasyarat Operasional Firebase:** Pastikan `firestore.rules` (khususnya blok aturan `match /levelProgress/{progressId}`) dan `firestore.indexes.json` sudah di-deploy ke project Firebase konsol agar operasi baca/tulis real-device berjalan tanpa `PERMISSION_DENIED`.

