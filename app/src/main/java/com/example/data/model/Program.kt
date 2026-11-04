package com.example.data.model

data class Program(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val titleAm: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val instructorName: String,
    val instructorSanad: String,
    val durationText: String,
    val totalHalaqat: Int,
    val prerequisiteAr: String,
    val featuresAr: List<String> = emptyList(),
    val studyMethodAr: String = "حلقات مرئية وصوتية تفاعلية وتسميع فردي",
    val badgeTag: String = "إجازة بالسند",
    val isOpenForRegistration: Boolean = true,
    val enrolledCount: Int = 42
)

data class EnrollmentRequest(
    val id: String,
    val studentId: String,
    val studentName: String,
    val studentCode: String,
    val programId: String,
    val programTitle: String,
    val age: Int,
    val gender: String,
    val city: String,
    val contactPhone: String,
    val currentMemorization: String,
    val previousExperience: String,
    val preferredTimeSlot: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val rejectionReason: String? = null,
    val submittedAt: String
)
