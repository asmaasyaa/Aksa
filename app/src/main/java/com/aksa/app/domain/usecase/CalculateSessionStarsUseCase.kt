package com.aksa.app.domain.usecase

/**
 * Menghitung perolehan bintang (1 - 3) berdasarkan akurasi sesi belajar
 * Ramah anak disleksia: anak selalu diapresiasi (minimal 1 bintang jika selesai).
 */
class CalculateSessionStarsUseCase {
    operator fun invoke(accuracyPercentage: Float): Int{
        return when {
            accuracyPercentage >= 85f -> 3 //akurasi tinggi, sangat baik
            accuracyPercentage >= 60f -> 2 //akurasi sedang, baik
            else -> 1 // nilai apresiasi usaha penyelesaian
        }
    }
}