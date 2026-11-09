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

data class RecitationQueueItem(
    val id: String,
    val studentId: String,
    val studentName: String,
    val studentCode: String = "",
    val studentGender: String = "ذكر",
    val targetSurah: String = "تسميع المقرر",
    val fromAyah: Int = 1,
    val toAyah: Int = 10,
    val joinedAt: String,
    val queueNumber: Int = 1,
    val isAdmitted: Boolean = false
)

data class ActiveRecitationSession(
    val sessionId: String,
    val groupId: String,
    val studentId: String,
    val studentName: String,
    val studentCode: String,
    val teacherName: String,
    val startedAtTimestamp: Long = System.currentTimeMillis(),
    val elapsedSeconds: Int = 0,
    val surahName: String = "سورة البقرة",
    val isRunning: Boolean = true,
    val liveKitRoomName: String = ""
)

data class HalqahGroup(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val timeSlot: GroupTimeSlot,
    val gender: GenderCategory,
    val teacherName: String,
    val activeReciterName: String? = null,
    val activeReciterCode: String? = null,
    val isLiveNow: Boolean = false,
    val liveStartedAt: String? = null,
    val membersCount: Int = 0,
    val currentSurah: String = "حلقات التسميع وتصحيح الأداء",
    val liveRoomId: String = "room-jazari",
    val telegramLink: String? = null,
    val description: String = "",
    val members: List<GroupMember> = emptyList(),
    val queue: List<RecitationQueueItem> = emptyList(),
    val activeSession: ActiveRecitationSession? = null
)
