package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Model representing a student's recitation progress record.
 * Conforms to Firestore type-safe serialization requirements:
 * - Empty/default constructor parameters
 * - DocumentId annotation
 * - Nullable ServerTimestamp fields
 */
@IgnoreExtraProperties
data class RecitationProgress(
    @DocumentId val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val studentCode: String = "",
    val surahNumber: Int = 1,
    val surahName: String = "",
    val startAyah: Int = 1,
    val endAyah: Int = 7,
    val recitationType: String = "حفظ جديد", // حفظ جديد, مراجعة كبرى, مراجعة صغرى, تلاوة وتجويد
    val rating: String = "ممتاز", // ممتاز, جيد جداً, جيد, يحتاج إعادة
    val riwayah: String = "حفص عن عاصم",
    val teacherId: String = "",
    val teacherName: String = "",
    val notes: String = "",
    val sessionDurationMinutes: Int = 15,
    val isPassed: Boolean = true,
    @ServerTimestamp val recordedAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * Model representing course/program progress and curriculum milestone.
 */
@IgnoreExtraProperties
data class CourseProgress(
    @DocumentId val id: String = "",
    val studentId: String = "",
    val programId: String = "",
    val programTitle: String = "",
    val completedHalaqat: Int = 0,
    val totalHalaqat: Int = 30,
    val memorizedJuzCount: Int = 0,
    val currentSurah: String = "",
    val status: String = "نشط", // نشط, مكتمل, معلق
    val sanadEligible: Boolean = false,
    @ServerTimestamp val lastSessionAt: Timestamp? = null,
    @ServerTimestamp val enrolledAt: Timestamp? = null
)
