package com.example.data.model

enum class GroupTimeSlot(val labelAr: String, val labelEn: String) {
    MORNING("الفترة الصباحية (Fajr - Duha)", "Morning Session"),
    EVENING("الفترة المسائية (Asr - Maghrib)", "Evening Session"),
    NIGHT("الفترة الليلية (Isha - Night)", "Night Session")
}

enum class GenderCategory(val labelAr: String, val labelEn: String) {
    BROTHERS("مقرأة الرجال", "Brothers Halqah"),
    SISTERS("مقرأة الأخوات", "Sisters Halqah"),
    GENERAL("الملتقى العام", "General Forum")
}

data class GroupMember(
    val id: String,
    val name: String,
    val role: String, // المشرف العام / معلم مسند / طالب
    val isOnline: Boolean = true
)

data class HalqahGroup(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val timeSlot: GroupTimeSlot,
    val gender: GenderCategory,
    val teacherName: String,
    val activeReciterName: String? = null,
    val isLiveNow: Boolean = true,
    val membersCount: Int = 18,
    val currentSurah: String = "سورة البقرة (الربع الثالث)",
    val liveRoomId: String = "room-jazari",
    val telegramLink: String? = null, // Manual telegram link set by admin
    val description: String = "",
    val members: List<GroupMember> = emptyList()
)

data class RecitationQueueItem(
    val id: String,
    val studentId: String,
    val studentName: String,
    val targetSurah: String,
    val fromAyah: Int,
    val toAyah: Int,
    val joinedAt: String,
    val estimatedMinutes: Int = 15
)

data class ActiveRecitationSession(
    val sessionId: String,
    val groupId: String,
    val studentId: String,
    val studentName: String,
    val teacherName: String,
    val startedAtTimestamp: Long,
    val currentDurationSeconds: Int = 0,
    val surahName: String = "سورة الكهف",
    val isRunning: Boolean = true
)
