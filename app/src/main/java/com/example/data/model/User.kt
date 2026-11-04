package com.example.data.model

enum class UserRole {
    STUDENT,
    TEACHER,
    SUPERVISOR,
    ADMIN
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val studentCode: String = "JZR-1042",
    val avatarUrl: String = "",
    val phone: String = "+251 912 345 678",
    val bio: String = "طالب علم شغوف بإتقان التلاوة ونيل الإجازة بالسند المتصل",
    val gender: String = "ذكر",
    val city: String = "أديس أبابا",
    val minuteBalance: Int = 180, // الرصيد المتاح بالدقائق
    val tajweedLevel: String = "متقن للمخارج والصفات",
    val sanadTrack: String = "رواية حفص عن عاصم من طريق الشاطبية",
    val timeSlot: String = "الفجر",
    val isSuspended: Boolean = false,
    val joinedAt: String = "2026-10-15"
)
