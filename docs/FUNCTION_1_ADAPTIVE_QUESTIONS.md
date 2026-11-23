# DOKUMENTASI FUNCTION 1: ANALISIS POLA KESALAHAN & PEMBOBOTAN SOAL ADAPTIF (RAFIQI)

**Aplikasi:** AKSA (Aplikasi Khusus Stimulasi Anak)  
**Package:** `com.aksa.app`  
**Fitur:** Function 1 — Analisis Pola Kesalahan & Pembobotan Soal 60:40 Adaptif  
**Penanggung Jawab:** Rafiqi Abyan Rasyadan  
**Branch Integrasi:** `integration/level-unlock`  
**Status:** **TERINTEGRASI DENGAN DEV (PR #10) & TERVERIFIKASI PENUH**

---

## 1. Ringkasan Eksekutif & Tujuan

Fitur Function 1 (F1) mengimplementasikan mesin analitik pembelajaran adaptif (*adaptive learning engine*) yang menganalisis pola kesalahan anak pada materi *Huruf Mirip* (`b`, `d`, `p`, dan `q`) dari 3 sesi terakhir yang berstatus `COMPLETED`. Sistem menentukan maksimal 3 huruf prioritas menggunakan formula pembobotan resmi:

$$\text{priorityScore} = 0.7 \times \text{errorRate} + 0.3 \times \text{normalizedResponseTime}$$

Berdasarkan huruf-huruf prioritas yang terdeteksi, generator soal menyusun 10 butir soal baru dengan rasio adaptif resmi **60:40**:
- **6 butir soal (60%)** dialokasikan untuk huruf-huruf prioritas.
- **4 butir soal (40%)** dialokasikan untuk huruf-huruf non-prioritas.
- Seluruh 4 huruf (`b`, `d`, `p`, `q`) dijamin tetap terwakili agar eksposur komparatif anak terjaga dan bukti evaluasi untuk **Function 2 (Syakira)** tetap terpenuhi.

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
                  │ AnalyzeErrorPatternsUseCase │
                  │  - Ambil 3 sesi COMPLETED   │
                  │  - Syarat min. 3 attempts   │
                  │  - Normalisasi waktu respons│
                  │  - priorityScore = 0.7+0.3  │
                  │  - Statistik pasangan b/d & │
                  │    p/q (needsPractice < 70%)│
                  └──────────────┬──────────────┘
                                 │ ErrorAnalysisResult
                                 │ (priorityLetters: max 3)
                                 ▼
                  ┌─────────────────────────────┐
                  │GenerateHurufMiripQuestions- │
                  │          UseCase            │
                  │  - Aturan 60:40 adaptif     │
                  │  - 6 priority : 4 non-prior │
                  │  - Tepat 10 soal            │
                  │  - Fallback seimbang        │
                  │    (3+3+2+2 = 10)           │
                  └──────────────┬──────────────┘
                                 │
                                 ▼
                  ┌─────────────────────────────┐
                  │    HurufMiripViewModel      │◄── AksaUserState
                  │  - Resolusi profileId aktif │    (Active Profile)
                  │  - In-game dynamic diff     │
                  │  - sessionPairStats dialog  │
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

## 3. Spesifikasi Algoritma Resmi

### A. Analisis Pola Kesalahan (`AnalyzeErrorPatternsUseCase.kt`)
1. **Seleksi Sesi Historis**:
   - Hanya mengambil hingga 3 sesi selesai (`isCompleted == true`) terbaru untuk anak aktif pada aktivitas `huruf_mirip`.
   - Sesi ditinggalkan (*abandoned*), sesi yang sedang berjalan, sesi mini-game (*tangkap_huruf*), dan sesi profil lain disaring dan diabaikan secara ketat.
2. **Ambang Batas Bukti Minimum**:
   - `MIN_ATTEMPTS_THRESHOLD = 3`.
   - Huruf dengan jumlah kemunculan $< 3$ kali tidak dimasukkan dalam pemeringkatan prioritas.
3. **Statistik Pasangan Huruf (*Pair Confusion*)**:
   - Mengevaluasi pasangan `b/d` dan `p/q`.
   - Ditandai `needsPractice = true` apabila akurasi anak pada pasangan tersebut $< 70\%$.
4. **Normalisasi Waktu Respons (Skala $[0.0, 1.0]$)**:
   - Waktu respons tiap jawaban dibatasi maksimal $10.000\text{ ms}$ (`MAX_RESPONSE_TIME_CAP_MS`) untuk mencegah pencilan ekstrem akibat distraksi.
   - Menggunakan normalisasi Min-Max relatif terhadap huruf-huruf yang dievaluasi:
     $$\text{normalizedTime} = \frac{\text{avgTime} - \text{minTime}}{\text{maxTime} - \text{minTime}}$$
   - Jika semua huruf memiliki waktu respons rata-rata identik ($\text{maxTime} == \text{minTime}$):
     $$\text{normalizedTime} = \min\left(1.0, \frac{\text{avgTime}}{10.000}\right)$$
5. **Kalkulasi Skor Prioritas**:
   $$\text{priorityScore} = (0.7 \times \text{errorRate}) + (0.3 \times \text{normalizedTime})$$
   - Diurutkan menurun berdasarkan `priorityScore`, diikuti `errorRate`, lalu abjad huruf.
   - Maksimal **3 huruf teratas** dipilih sebagai `priorityLetters`.

---

### B. Distribusi Soal Adaptif 60:40 (`GenerateHurufMiripQuestionsUseCase.kt`)
Setiap sesi Huruf Mirip memiliki tepat **10 butir soal**:
- **Jika ada huruf prioritas ($1 \dots 3$ huruf)**:
  - `priorityPool = buildBalancedPool(validPriority, totalCount = 6)` (60% soal).
  - `nonPriorityPool = buildBalancedPool(otherLetters, totalCount = 4)` (40% soal).
  - Soal digabungkan ($6 + 4 = 10$) lalu diacak posisinya (*shuffled*).
- **Jika tidak ada huruf prioritas / data belum cukup ($0$ huruf prioritas)**:
  - Menggunakan distribusi seimbang default: $3 + 3 + 2 + 2 = 10$ butir soal.

#### Matriks Alokasi Soal 60:40:
| Huruf Prioritas Terpilih | Alokasi Huruf Prioritas (60%) | Alokasi Huruf Non-Prioritas (40%) | Total |
| :--- | :--- | :--- | :---: |
| **0 Huruf** (Data belum cukup) | - | Distribusi seimbang: $3+3+2+2$ | **10** |
| **1 Huruf** (Contoh: `['b']`) | `b = 6` | $d=2, p=1, q=1$ (total 4) | **10** |
| **2 Huruf** (Contoh: `['b', 'd']`) | $b=3, d=3$ (total 6) | $p=2, q=2$ (total 4) | **10** |
| **3 Huruf** (Contoh: `['b', 'd', 'p']`) | $b=2, d=2, p=2$ (total 6) | `q = 4` (total 4) | **10** |
| **4 Huruf Terpilih** | Top 3 diambil: $b=2, d=2, p=2$ (total 6) | Huruf ke-4 (`q`) masuk non-prioritas: `q = 4` | **10** |

---

## 4. Contoh Perhitungan Numerik (*Worked Example*)

Misalkan seorang anak telah menyelesaikan 3 sesi Huruf Mirip dengan log jawaban sebagai berikut:

| Huruf | Percobaan Total | Salah | Benar | Error Rate | Waktu Respons Rata-rata |
| :---: | :---: | :---: | :---: | :---: | :---: |
| `b` | 3 | 1 | 2 | $1 / 3 = 0.333$ | $2.000\text{ ms}$ |
| `d` | 3 | 3 | 0 | $3 / 3 = 1.000$ | $4.000\text{ ms}$ |
| `p` | 3 | 2 | 1 | $2 / 3 = 0.667$ | $3.000\text{ ms}$ |
| `q` | 3 | 0 | 3 | $0 / 3 = 0.000$ | $1.000\text{ ms}$ |

### Langkah 1: Evaluasi Batas Minimum Percobaan
Seluruh 4 huruf memiliki $\text{totalAttempts} = 3 \ge 3$, sehingga keempatnya memenuhi syarat evaluasi.

### Langkah 2: Normalisasi Waktu Respons
- $\text{minTime} = 1.000\text{ ms}$, $\text{maxTime} = 4.000\text{ ms}$, rentang $= 3.000\text{ ms}$.
- `q`: $(1.000 - 1.000) / 3.000 = 0.000$
- `b`: $(2.000 - 1.000) / 3.000 = 0.333$
- `p`: $(3.000 - 1.000) / 3.000 = 0.667$
- `d`: $(4.000 - 1.000) / 3.000 = 1.000$

### Langkah 3: Kalkulasi Skor Prioritas ($0.7 \times \text{errorRate} + 0.3 \times \text{normalizedTime}$)
- `d`: $(0.7 \times 1.000) + (0.3 \times 1.000) = 0.700 + 0.300 = \mathbf{1.000}$ (Peringkat 1)
- `p`: $(0.7 \times 0.667) + (0.3 \times 0.667) = 0.467 + 0.200 = \mathbf{0.667}$ (Peringkat 2)
- `b`: $(0.7 \times 0.333) + (0.3 \times 0.333) = 0.233 + 0.100 = \mathbf{0.333}$ (Peringkat 3)
- `q`: $(0.7 \times 0.000) + (0.3 \times 0.000) = 0.000 + 0.000 = \mathbf{0.000}$ (Peringkat 4)

### Langkah 4: Pemilihan Top 3 Huruf Prioritas
$\text{priorityLetters} = [\text{"d"}, \text{"p"}, \text{"b"}]$.

### Langkah 5: Pembentukan Kumpulan Soal Sesi Berikutnya (60:40)
- **Pool Prioritas (6 butir)**: Huruf `d`, `p`, `b` masing-masing mendapat $\mathbf{2}$ soal ($2 + 2 + 2 = 6$).
- **Pool Non-Prioritas (4 butir)**: Huruf `q` mendapat $\mathbf{4}$ soal.
- **Total Soal Sesi Baru**: $2 \times d + 2 \times p + 2 \times b + 4 \times q = \mathbf{10\text{ butir soal}}$.

---

## 5. Sinergi dengan Function 2 (Syakira)

Function 1 dan Function 2 membentuk siklus pembelajaran yang tertutup (*closed-loop adaptive pedagogy*):
1. **F1 Mengarahkan Intervensi**: Ketika anak banyak melakukan kesalahan pada huruf tertentu, F1 meningkatkan proporsi latihan huruf tersebut pada sesi berikutnya (60% soal terarah).
2. **Peningkatan Akurasi Target**: Dengan latihan intensif yang terfokus, tingkat kesalahan (*error rate*) anak pada huruf yang sebelumnya lemah turun di bawah ambang batas $40\%$.
3. **F2 Memvalidasi Kesiapan Naik Level**: Evaluator kesiapan level (`EvaluateLevelReadinessUseCase`) memverifikasi bahwa:
   - Rata-rata akurasi 3 sesi terakhir $\ge 80\%$.
   - Seluruh target memiliki bukti $\ge 3$ percobaan dan *error rate* $\le 40\%$.
   - Anak telah berlatih minimal pada 2 hari berbeda dalam 7 hari terakhir.
4. **Peta Petualangan Terbuka**: Setelah kriteria terpenuhi, level baru terbuka secara permanen pada Firestore dan antarmuka Peta Level.
