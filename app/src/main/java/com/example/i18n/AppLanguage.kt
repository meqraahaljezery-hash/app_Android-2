package com.example.i18n

import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage(val code: String, val displayName: String, val layoutDirection: LayoutDirection) {
    ARABIC("ar", "العربية", LayoutDirection.Rtl),
    ENGLISH("en", "English", LayoutDirection.Ltr),
    AMHARIC("am", "አማርኛ", LayoutDirection.Ltr)
}

object Strings {
    fun get(key: String, lang: AppLanguage): String {
        return translations[key]?.get(lang) ?: translations[key]?.get(AppLanguage.ARABIC) ?: key
    }

    private val translations = mapOf(
        "app_title_ar" to mapOf(
            AppLanguage.ARABIC to "مَقْرَأَةُ الإِمَامِ الجَزَرِيِّ لِلإِقْرَاءِ وَالإِجَازَةِ بِالسَّنَد",
            AppLanguage.ENGLISH to "Imam Al-Jazari Maqraah for Quranic Recitation & Ijazah with Sanad",
            AppLanguage.AMHARIC to "የኢማም አል-ጀዘሪ ቁርኣን ንባብ እና የሰነድ ኢጃዛ ማዕከል"
        ),
        "app_subtitle" to mapOf(
            AppLanguage.ARABIC to "منصة تعليمية عالمية للإتقان والإجازة بالسند المتصل إلى رسول الله ﷺ",
            AppLanguage.ENGLISH to "Global Quranic Academy for Recitation Mastery & Connected Sanad",
            AppLanguage.AMHARIC to "ዓለም አቀፍ የቁርኣን ንባብ ማስተካከያ እና የቀጥታ ሰነድ ማዕከል"
        ),
        "tab_home" to mapOf(
            AppLanguage.ARABIC to "الرئيسية",
            AppLanguage.ENGLISH to "Home",
            AppLanguage.AMHARIC to "ዋና ገጽ"
        ),
        "tab_programs" to mapOf(
            AppLanguage.ARABIC to "البرامج",
            AppLanguage.ENGLISH to "Programs",
            AppLanguage.AMHARIC to "ፕሮግራሞች"
        ),
        "tab_groups" to mapOf(
            AppLanguage.ARABIC to "المجموعات",
            AppLanguage.ENGLISH to "Groups",
            AppLanguage.AMHARIC to "ቡድኖች"
        ),
        "tab_chat" to mapOf(
            AppLanguage.ARABIC to "المستشار الذكي",
            AppLanguage.ENGLISH to "AI Advisor",
            AppLanguage.AMHARIC to "አይ-አማካሪ"
        ),
        "tab_profile" to mapOf(
            AppLanguage.ARABIC to "حسابي",
            AppLanguage.ENGLISH to "Profile",
            AppLanguage.AMHARIC to "መገለጫዬ"
        ),
        "tab_admin" to mapOf(
            AppLanguage.ARABIC to "الإدارة",
            AppLanguage.ENGLISH to "Admin",
            AppLanguage.AMHARIC to "አስተዳደር"
        ),
        "tab_packages" to mapOf(
            AppLanguage.ARABIC to "باقاتي",
            AppLanguage.ENGLISH to "Packages",
            AppLanguage.AMHARIC to "ፓኬጆች"
        ),
        "tab_chats" to mapOf(
            AppLanguage.ARABIC to "المحادثات",
            AppLanguage.ENGLISH to "Chats",
            AppLanguage.AMHARIC to "ውይይቶች"
        ),
        "no_admin_access" to mapOf(
            AppLanguage.ARABIC to "ليس لديك صلاحية الوصول إلى هذه الصفحة",
            AppLanguage.ENGLISH to "You do not have permission to access this page",
            AppLanguage.AMHARIC to "ይህንን ገጽ ለመድረስ ፈቃድ የለዎትም"
        ),
        "start_now" to mapOf(
            AppLanguage.ARABIC to "ابدأ الآن",
            AppLanguage.ENGLISH to "Start Now",
            AppLanguage.AMHARIC to "አሁን ጀምር"
        ),
        "skip" to mapOf(
            AppLanguage.ARABIC to "تخطي",
            AppLanguage.ENGLISH to "Skip",
            AppLanguage.AMHARIC to "ዝለል"
        ),
        "next" to mapOf(
            AppLanguage.ARABIC to "التالي",
            AppLanguage.ENGLISH to "Next",
            AppLanguage.AMHARIC to "ቀጣይ"
        ),
        "previous" to mapOf(
            AppLanguage.ARABIC to "السابق",
            AppLanguage.ENGLISH to "Previous",
            AppLanguage.AMHARIC to "ቀዳሚ"
        ),
        "student_code_label" to mapOf(
            AppLanguage.ARABIC to "كود الطالب",
            AppLanguage.ENGLISH to "Student Code",
            AppLanguage.AMHARIC to "የተማሪ ኮድ"
        ),
        "admin_fast_login" to mapOf(
            AppLanguage.ARABIC to "الدخول كمدير النظام (خالد أحمد مصطفى)",
            AppLanguage.ENGLISH to "Admin Fast-Login (Kalid Ahmed)",
            AppLanguage.AMHARIC to "እንደ አስተዳዳሪ ግባ (ካሊድ አህመድ)"
        ),
        "student_demo_login" to mapOf(
            AppLanguage.ARABIC to "الدخول السريع كطالب",
            AppLanguage.ENGLISH to "Quick Login as Student",
            AppLanguage.AMHARIC to "እንደ ተማሪ ፈጣን መግቢያ"
        ),
        "register_as_student" to mapOf(
            AppLanguage.ARABIC to "التسجيل كطالب جديد",
            AppLanguage.ENGLISH to "Register as Student",
            AppLanguage.AMHARIC to "እንደ ተማሪ ይመዝገቡ"
        ),
        "welcome_back" to mapOf(
            AppLanguage.ARABIC to "مرحباً بك في المقرأة المباركة",
            AppLanguage.ENGLISH to "Welcome to the Blessed Maqraah",
            AppLanguage.AMHARIC to "እንኳን ወደ ተባረከው ማዕከላችን በደህና መጡ"
        ),
        "announcement_board" to mapOf(
            AppLanguage.ARABIC to "لوحة الإعلانات الرسمية",
            AppLanguage.ENGLISH to "Official Announcements",
            AppLanguage.AMHARIC to "ኦፊሴላዊ ማስታወቂያዎች"
        ),
        "quick_links" to mapOf(
            AppLanguage.ARABIC to "روابط سريعة وخدمات المقرأة",
            AppLanguage.ENGLISH to "Quick Links & Services",
            AppLanguage.AMHARIC to "ፈጣን አገናኞች"
        ),
        "buy_minutes" to mapOf(
            AppLanguage.ARABIC to "شراء دقائق التسميع",
            AppLanguage.ENGLISH to "Purchase Recitation Minutes",
            AppLanguage.AMHARIC to "የንባብ ደቂቃዎች ይግዙ"
        ),
        "minute_balance" to mapOf(
            AppLanguage.ARABIC to "رصيد الدقائق المتاح",
            AppLanguage.ENGLISH to "Available Minutes Balance",
            AppLanguage.AMHARIC to "ያለዎት የደቂቃ ሂሳብ"
        ),
        "minutes_unit" to mapOf(
            AppLanguage.ARABIC to "دقيقة",
            AppLanguage.ENGLISH to "min",
            AppLanguage.AMHARIC to "ደቂቃ"
        ),
        "request_enrollment" to mapOf(
            AppLanguage.ARABIC to "طلب تسجيل في البرنامج",
            AppLanguage.ENGLISH to "Request Enrollment",
            AppLanguage.AMHARIC to "የምዝገባ ጥያቄ ያቅርቡ"
        ),
        "join_queue" to mapOf(
            AppLanguage.ARABIC to "الانضمام لطابور القراءة",
            AppLanguage.ENGLISH to "Join Recitation Queue",
            AppLanguage.AMHARIC to "ወደ ንባብ ወረፋ ይቀላቀሉ"
        ),
        "leave_queue" to mapOf(
            AppLanguage.ARABIC to "مغادرة الطابور",
            AppLanguage.ENGLISH to "Leave Queue",
            AppLanguage.AMHARIC to "ከወረፋ ውጣ"
        ),
        "recitation_in_progress" to mapOf(
            AppLanguage.ARABIC to "جلسة التسميع جارية الآن",
            AppLanguage.ENGLISH to "Recitation in Progress",
            AppLanguage.AMHARIC to "የንባብ ክፍለ ጊዜ በሂደት ላይ ነው"
        ),
        "video_call" to mapOf(
            AppLanguage.ARABIC to "اتصال مرئي",
            AppLanguage.ENGLISH to "Video Call",
            AppLanguage.AMHARIC to "ቪዲዮ ጥሪ"
        ),
        "audio_call" to mapOf(
            AppLanguage.ARABIC to "اتصال صوتي",
            AppLanguage.ENGLISH to "Audio Call",
            AppLanguage.AMHARIC to "ድምጽ ጥሪ"
        ),
        "live_stream" to mapOf(
            AppLanguage.ARABIC to "بث مباشر",
            AppLanguage.ENGLISH to "Live Stream",
            AppLanguage.AMHARIC to "ቀጥታ ስርጭት"
        ),
        "start_session" to mapOf(
            AppLanguage.ARABIC to "بدء التسميع",
            AppLanguage.ENGLISH to "Start Recitation",
            AppLanguage.AMHARIC to "ንባብ ጀምር"
        ),
        "end_session" to mapOf(
            AppLanguage.ARABIC to "إنهاء التسميع وخصم الدقائق",
            AppLanguage.ENGLISH to "End Recitation & Deduct",
            AppLanguage.AMHARIC to "ንባብ ጨርስ እና ደቂቃ ቀንስ"
        ),
        "payment_manual_title" to mapOf(
            AppLanguage.ARABIC to "التحويل البنكي وتأكيد الدفع",
            AppLanguage.ENGLISH to "Bank Transfer & Payment Proof",
            AppLanguage.AMHARIC to "የባንክ ክፍያ ማረጋገጫ"
        ),
        "sender_name" to mapOf(
            AppLanguage.ARABIC to "اسم المحوِّل الثلاثي",
            AppLanguage.ENGLISH to "Sender Full Name",
            AppLanguage.AMHARIC to "የላኪው ሙሉ ስም"
        ),
        "transaction_ref" to mapOf(
            AppLanguage.ARABIC to "رقم المعاملة / الإشعار",
            AppLanguage.ENGLISH to "Transaction Reference / ID",
            AppLanguage.AMHARIC to "የትራንዛክሽን ቁጥር"
        ),
        "phone_number" to mapOf(
            AppLanguage.ARABIC to "رقم الهاتف",
            AppLanguage.ENGLISH to "Phone Number",
            AppLanguage.AMHARIC to "ስልክ ቁጥር"
        ),
        "submit_payment" to mapOf(
            AppLanguage.ARABIC to "إرسال طلب شحن الرصيد",
            AppLanguage.ENGLISH to "Submit Payment Request",
            AppLanguage.AMHARIC to "የክፍያ ጥያቄ ላክ"
        ),
        "bank_accounts" to mapOf(
            AppLanguage.ARABIC to "الحسابات البنكية المعتمدة",
            AppLanguage.ENGLISH to "Approved Bank Accounts",
            AppLanguage.AMHARIC to "ተቀባይ የባንክ ሂሳቦች"
        ),
        "admin_dashboard" to mapOf(
            AppLanguage.ARABIC to "لوحة تحكم المشرف العام",
            AppLanguage.ENGLISH to "Super Admin Dashboard",
            AppLanguage.AMHARIC to "የዋና አስተዳዳሪ ዳሽቦርድ"
        ),
        "manage_users" to mapOf(
            AppLanguage.ARABIC to "إدارة المستخدمين والطلاب",
            AppLanguage.ENGLISH to "Manage Users & Students",
            AppLanguage.AMHARIC to "ተጠቃሚዎችን አስተዳድር"
        ),
        "manage_payments" to mapOf(
            AppLanguage.ARABIC to "مراجعة واعتماد الحوالات",
            AppLanguage.ENGLISH to "Payment Approvals",
            AppLanguage.AMHARIC to "ክፍያዎችን አጽድቅ"
        ),
        "manage_minutes" to mapOf(
            AppLanguage.ARABIC to "تعديل رصيد الدقائق اليدوي",
            AppLanguage.ENGLISH to "Manual Minutes Adjustment",
            AppLanguage.AMHARIC to "የደቂቃ ሂሳብ ማስተካከያ"
        ),
        "manage_content" to mapOf(
            AppLanguage.ARABIC to "إدارة نصوص المحتوى والإعلانات",
            AppLanguage.ENGLISH to "Content & Announcements",
            AppLanguage.AMHARIC to "ይዘት እና ማስታወቂያዎች"
        ),
        "send_notification" to mapOf(
            AppLanguage.ARABIC to "إرسال إشعار عام للطلاب",
            AppLanguage.ENGLISH to "Broadcast Notification",
            AppLanguage.AMHARIC to "አጠቃላይ ማስታወቂያ ላክ"
        ),
        "approve" to mapOf(
            AppLanguage.ARABIC to "اعتماد وإضافة الدقائق",
            AppLanguage.ENGLISH to "Approve & Add Minutes",
            AppLanguage.AMHARIC to "አጽድቅ እና ደቂቃ ጨምር"
        ),
        "reject" to mapOf(
            AppLanguage.ARABIC to "رفض الطلب",
            AppLanguage.ENGLISH to "Reject",
            AppLanguage.AMHARIC to "ውድቅ አድርግ"
        ),
        "pending" to mapOf(
            AppLanguage.ARABIC to "قيد المراجعة",
            AppLanguage.ENGLISH to "Pending",
            AppLanguage.AMHARIC to "በግምገማ ላይ"
        ),
        "approved" to mapOf(
            AppLanguage.ARABIC to "معتمد",
            AppLanguage.ENGLISH to "Approved",
            AppLanguage.AMHARIC to "ተቀባይነት አግኝቷል"
        ),
        "rejected" to mapOf(
            AppLanguage.ARABIC to "مرفوض",
            AppLanguage.ENGLISH to "Rejected",
            AppLanguage.AMHARIC to "ተቀባይነት አላገኘም"
        ),
        "sanad_pathway" to mapOf(
            AppLanguage.ARABIC to "المسار الإقرائي والسند",
            AppLanguage.ENGLISH to "Sanad Recitation Pathway",
            AppLanguage.AMHARIC to "የሰነድ መንገድ"
        ),
        "ai_advisor_intro" to mapOf(
            AppLanguage.ARABIC to "السلام عليكم ورحمة الله. أنا المستشار الذكي لمقرأة الإمام الجزري. يمكنني إرشادك لاختيار المسار المناسب، واختبار مستواك في التجويد، والإجابة عن أسانيد القراءات العشر المتواترة.",
            AppLanguage.ENGLISH to "Assalamu Alaikum. I am the AI Advisor for Imam Al-Jazari Maqraah. I can assess your Tajweed level, recommend programs, and explain Sanad pathways.",
            AppLanguage.AMHARIC to "አሰላሙ አለይኩም! እኔ የኢማም አል-ጀዘሪ ማዕከል አይ-አማካሪ ነኝ። የትምህርት ፕሮግራም እንድትመርጡ እና የንባብ ደረጃችሁን ለመገምገም እረዳለሁ።"
        )
    )
}
