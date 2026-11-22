# DOKUMENTASI FUNCTION 1: ANALISIS KESALAHAN BELAJAR & PEMBOBOTAN SOAL ADAPTIF (RAFIQI)

**Aplikasi:** AKSA (Aplikasi Khusus Stimulasi Anak)  
**Package:** `com.aksa.app`  
**Fitur:** Function 1 — Analisis Kesalahan Belajar & Pembobotan Soal Berdasarkan Target Lemah  
**Penanggung Jawab:** Rafiqi  
**Branch Integrasi:** `integration/level-unlock`  
**Status:** **SELESAI & TERVERIFIKASI (121/121 Unit Test Lolos, APK Debug Berhasil Di-build)**

---

## 1. Ringkasan Eksekutif & Tujuan

Fitur Function 1 (F1) mengimplementasikan sistem analitik pembelajaran adaptif (*adaptive learning engine*) yang memantau performa anak pada setiap target materi (huruf `b`, `d`, `p`, dan `q` pada modul *Huruf Mirip*), mendeteksi kelemahan spesifik anak secara otomatis berdasarkan riwayat sesi terdahulu, dan menyusun kumpulan soal baru yang memprioritaskan materi yang paling membutuhkan latihan tambahan.

F1 bekerja secara sinergis dan terintegrasi penuh dengan **Function 2 (Syakira - Evaluasi Kesiapan Naik Level & Peta Dinamis)**:
- Saat anak kesulitan membedakan huruf tertentu (misalnya `b` dan `d`), F1 meningkatkan proporsi latihan huruf tersebut.
- Melalui latihan terarah, tingkat kesalahan (*error rate*) anak berkurang di bawah ambang batas $40\%$.
- Syarat kelulusan F2 (akurasi rata-rata $\ge 80\%$, bukti jawaban $\ge 3$ per target, dan latihan di $\ge 2$ hari dalam 7 hari) dapat tercapai secara alami, membuka level berikutnya pada Peta Petualangan.

---

## 2. Arsitektur Komponen

```
                  ┌─────────────────────────────┐
                  │    FirestoreSessionRepo     │
                  │ (users/.../profiles/.../    │
                  │   answerLogs & sessions)    │
                  └──────────────┬──────────────┘
                                 │
                                 ▼
                  ┌─────────────────────────────┐
                  │ AnalyzeTargetErrorsUseCase  │
                  │  - Filter COMPLETED sessions│
                  │  - Hitung error rate target │
                  │  - Deteksi & rank weak target│
                  └──────────────┬──────────────┘
                                 │ ErrorAnalyticsReport
                                 ▼
                  ┌─────────────────────────────┐
                  │    HurufMiripViewModel      │◄── AksaUserState
                  │  - Resolusi profileId aktif │    (Active Profile)
                  │  - In-game dynamic diff (1-3)│
                  └──────────────┬──────────────┘
                                 │ weakTargets
                                 ▼
                  ┌─────────────────────────────┐
                  │GenerateHurufMiripQuestions- │
                  │          UseCase            │
                  │  - Aturan alokasi 4/3/2/1   │
                  │  - Tepat 10 soal            │
                  │  - Tiap huruf min. 1x muncul│
                  └──────────────┬──────────────┘
                                 │
                                 ▼
                  ┌─────────────────────────────┐
                  │      HurufMiripScreen       │
                  │  - Badge Tingkat 1/2/3      │
                  │  - Pill "Fokus: b, d"       │
                  │  - Kartu Pilihan & Edukasi  │
                  └─────────────────────────────┘
```

---

## 3. Spesifikasi Algoritma & Aturan Bisnis

### A. Model Analitik Data (`TargetErrorAnalytics.kt`)
1. **`TargetErrorMetric`**:
   - `target`: Karakter target (contoh: `"b"`, `"d"`, `"p"`, `"q"`).
   - `totalAttempts`: Total kemunculan target yang dijawab anak pada sesi-sesi valid terdahulu.
   - `incorrectAttempts`: Jumlah jawaban salah pada target tersebut.
   - `errorRate`: Rasio kesalahan ($\frac{\text{incorrectAttempts}}{\text{totalAttempts}}$).
   - `isWeakTarget`: Ditandai `true` jika:
     $$\text{errorRate} \ge 0.30 \text{ (dengan } \text{incorrectAttempts} \ge 1) \quad \text{ATAU} \quad \text{incorrectAttempts} \ge 2$$
   - Properti pendukung: `correctAttempts` dan `accuracy`.

2. **`ErrorAnalyticsReport`**:
   - `childId`: ID profil anak aktif.
   - `moduleId`: ID modul pembelajaran (`"pulau_alfabet"`).
   - `targetMetrics`: Map target ke metrik performanya.
   - `weakTargets`: Daftar target lemah terurut berdasarkan prioritas.
   - `totalSessionsAnalyzed` & `totalQuestionsAnalyzed`: Jumlah sesi dan soal yang dianalisis.

### B. Use Case Analisis Kesalahan (`AnalyzeTargetErrorsUseCase.kt`)
1. Mengambil riwayat sesi dari `SessionRepository.getCompletedSessions(childId)`:
   - Sesi wajib berstatus `SessionStatus.COMPLETED` dan memiliki `completedAt`.
   - Sesi yang belum selesai atau ditinggalkan (*abandoned*) **secara ketat diabaikan** agar tidak mendistorsi rasio kesalahan.
2. Mengambil seluruh `AnswerLog` yang terkait dengan sesi-sesi selesai tersebut melalui `getAnswersForChild(childId)`.
3. Mengelompokkan log jawaban berdasarkan `targetChar` dan menghitung metrik kesalahan.
4. Mengurutkan daftar `weakTargets` dengan kriteria:
   1. *Error rate* tertinggi terlebih dahulu.
   2. Jika seimbang, jumlah kesalahan (*incorrect attempts*) terbanyak.
   3. Jika masih seimbang, urutan alfabetis.

### C. Pembobotan Soal Adaptif (`GenerateHurufMiripQuestionsUseCase.kt`)
Algoritma pembobotan adaptif mengatur distribusi 10 soal dengan prinsip utama:
1. **Total soal per sesi tetap tepat 10 butir**.
2. **Pilihan jawaban tetap 2 opsi** (huruf target dan huruf pasangannya yang mirip: `b` $\leftrightarrow$ `d`, `p` $\leftrightarrow$ `q`), dengan tata letak pilihan kiri-kanan diacak.
3. **Seluruh 4 huruf (`b`, `d`, `p`, `q`) dijamin muncul minimal 1 kali** agar anak tetap memperoleh eksposur komparatif yang seimbang dan terus menghasilkan bukti jawaban untuk evaluasi F2.

#### Matriks Distribusi Soal:
| Jumlah Target Lemah | Huruf Terlemah (Rank 1) | Target Lemah (Rank 2) | Target Lemah (Rank 3) | Target Lemah (Rank 4) | Huruf Lain / Kuat | Total Soal |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **0 Target Lemah** (Anak baru / akurasi 100%) | - | - | - | - | Masing-masing 2 atau 3 ($3+3+2+2$) | **10** |
| **1 Target Lemah** (Contoh: `['b']`) | **4 soal** | - | - | - | Masing-masing 2 soal ($2+2+2$) | **10** |
| **2 Target Lemah** (Contoh: `['b', 'd']`) | **3 soal** | **3 soal** | - | - | Masing-masing 2 soal ($2+2$) | **10** |
| **3 Target Lemah** (Contoh: `['b', 'd', 'p']`) | **4 soal** | **3 soal** | **2 soal** | - | 1 soal tersisa | **10** |
| **4 Target Lemah** (Semua huruf pernah salah) | **3 soal** | **3 soal** | **2 soal** | **2 soal** | - | **10** |

Urutan kemunculan soal diacak menggunakan instance `Random` yang dapat diinjeksi untuk menjamin determinisme dalam pengujian unit.

### D. Penyesuaian Kesulitan Dinamis In-Game (*Dynamic Difficulty Progression*)
Di samping pembobotan soal sebelum sesi dimulai, `HurufMiripViewModel` memantau performa selama sesi berlangsung:
- **Tingkat 1 (Terbimbing)**: Level default dengan visualisasi kartu petunjuk lengkap (*Smart Hint Card*).
- **Tingkat 2 (Mandiri)**: Tercapai jika anak menjawab **3 kali benar berturut-turut** (`consecutiveCorrect >= 3`).
- **Tingkat 3 (Tantangan)**: Tercapai jika anak kembali menjawab **3 kali benar berturut-turut** saat berada di Tingkat 2 (maksimal Tingkat 3).
- **Penurunan Tingkat**: Jika anak menjawab **2 kali salah berturut-turut** (`consecutiveIncorrect >= 2`), tingkat kesulitan turun 1 level (minimal Tingkat 1) untuk mencegah frustrasi belajar.

---

## 4. Integrasi Antarmuka Pengguna (UI/UX)

Pada `HurufMiripScreen.kt`, sistem adaptif direfleksikan secara visual tanpa membebani kognisi anak:
1. **Badge Tingkat Kesulitan**:
   - Menampilkan status kesulitan aktif (*"Tingkat 1: Terbimbing"*, *"Tingkat 2: Mandiri"*, *"Tingkat 3: Tantangan"*) dengan aksen warna Aksa.
2. **Pill Fokus Adaptif**:
   - Jika anak memiliki target lemah dari sesi sebelumnya, sistem menampilkan badge kecil: *"Fokus: b, d"* sehingga orang tua dan anak mengetahui materi yang sedang diperkuat.
3. **Kartu Petunjuk Pintar Komparatif**:
   - Menampilkan petunjuk visual kontras antara pasangan huruf yang membingungkan (posisi bulatan dan arah garis).

---

## 5. Ringkasan Pengujian Unit & Verifikasi

Pengujian komprehensif dilakukan mencakup logika bisnis, isolasi profil multi-anak, penanganan sesi ditinggalkan, serta stabilitas generator soal.

### Rincian Test Suite F1:
1. **`AnalyzeTargetErrorsUseCaseTest`** (5 skenario):
   - `emptyHistoryReturnsEmptyReport`: Menangani anak baru tanpa riwayat (0 sesi).
   - `blankChildIdReturnsEmptyReport`: Penanganan masukan ID kosong.
   - `singleWeakTargetIdentifiedAndRanked`: Deteksi akurat 1 target lemah dengan error rate $66.7\%$.
   - `multipleWeakTargetsSortedByErrorRate`: Pengurutan beberapa target lemah berdasarkan tingkat kesalahan.
   - `profileIsolationEnsuresIndependentErrorProfiles`: Menjamin profil Anak A dan Anak B terisolasi penuh dan tidak saling mencemari metrik.
   - `abandonedSessionsAreExcludedFromAnalytics`: Memastikan sesi `ABANDONED` diabaikan secara ketat.

2. **`HurufMiripQuestionGeneratorTest`** (9 skenario):
   - `generatesExactly10Questions`: Verifikasi ukuran tetap 10 soal.
   - `balancedTargetDistributionAcrossSeeds`: Verifikasi distribusi standar ($3+3+2+2$).
   - `letterPairAssociationsAreCorrect`: Verifikasi asosiasi pasangan `b-d` dan `p-q`.
   - `deterministicOutputWithSameRandomSeed`: Determinisme pengacakan.
   - `adaptiveOneWeakTargetAllocatesFourQuestions`: Alokasi 4 soal untuk 1 target lemah.
   - `adaptiveTwoWeakTargetsAllocatesThreeQuestionsEach`: Alokasi $3+3+2+2$ untuk 2 target lemah.
   - `adaptiveThreeWeakTargetsAllocatesFourThreeTwoOne`: Alokasi $4+3+2+1$ untuk 3 target lemah.
   - `adaptiveFourWeakTargetsAllocatesThreeThreeTwoTwo`: Alokasi $3+3+2+2$ untuk 4 target lemah.
   - `adaptiveFiltersDuplicatesAndInvalidLetters`: Sanitasi target tidak valid.
   - `everyLetterAppearsAtLeastOnceInAllAdaptiveModes`: Jaminan setiap huruf muncul $\ge 1$ kali di semua mode.

3. **`HurufMiripSessionLifecycleTest`** (6 skenario):
   - Penyelesaian sesi tepat setelah soal ke-10.
   - Sesi ditinggalkan tidak dicatat sebagai selesai.
   - Idempotensi `AnswerLog` (mencegah duplikasi jawaban per soal).
   - Penyelesaian sesi dengan akurasi di bawah $80\%$.
   - `inGameAdaptiveDifficultyAdjustsOnConsecutiveAnswers`: Verifikasi transisi naik/turun tingkat kesulitan (1 $\to$ 2 $\to$ 3 $\to$ 2).
   - `adaptiveQuestionsLoadedWhenWeakTargetsExist`: Verifikasi pemuatan otomatis soal adaptif saat riwayat kesalahan ditemukan.

### Hasil Eksekusi:
```text
Total Test Dijalankan: 121 tests
Gagal: 0 failures
Dilewati: 0 skipped
Tingkat Keberhasilan: 100% SUCCESSFUL
Waktu Eksekusi: 12.147s
```

### Hasil Build APK:
- Perintah: `./gradlew assembleDebug`
- Lokasi: `app/build/outputs/apk/debug/app-debug.apk`
- Ukuran: **26 MB**
- Status: **BUILD SUCCESSFUL**
