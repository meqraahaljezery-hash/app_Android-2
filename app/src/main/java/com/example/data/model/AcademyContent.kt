package com.example.data.model

data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val isPinned: Boolean = false,
    val author: String = "إدارة مقرأة الإمام الجزري"
)

data class QuranPost(
    val id: String,
    val authorName: String,
    val authorRole: String = "طالب مجاز",
    val content: String,
    val date: String,
    val likesCount: Int = 12,
    val commentsCount: Int = 3,
    val isLikedByMe: Boolean = false
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val date: String,
    val type: String = "INFO", // ENROLLMENT, RECHARGE, SYSTEM, EXAM
    val isRead: Boolean = false
)

data class AcademySettings(
    val nameAr: String = "مَقْرَأَةُ الإِمَامِ الجَزَرِيِّ لِلإِقْرَاءِ وَالإِجَازَةِ بِالسَّنَد",
    val nameEn: String = "Imam Al-Jazari Center for Quran Recitation & Ijazah with Sanad",
    val contactPhone: String = "+251 990 161 372",
    val contactEmail: String = "meqraahaljezery@gmail.com",
    val supervisorEmail: String = "khdahd241@gmail.com",
    val welcomeHeroText: String = "قال الإمام ابن الجزري رحمه الله: والأخذ بالتجويد حتم لازمُ .. من لم يصحح القرآن آثمُ"
)
