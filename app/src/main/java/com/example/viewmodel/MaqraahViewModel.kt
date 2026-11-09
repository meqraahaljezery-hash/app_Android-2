package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.util.LiveKitManager
import com.example.util.SmartNotificationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

class MaqraahViewModel : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    // 1. Onboarding State
    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    // 2. Current User (Null until real sign in)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // 3. Official Quran Programs (4 Core Programs from Spec)
    private val _programs = MutableStateFlow(
        listOf(
            Program(
                id = "prog_talqin",
                titleAr = "التلقين وتصحيح الأداء",
                titleEn = "Talqin & Recitation Correction",
                titleAm = "የቁርኣን ንባብ እና አነባበብ ማስተካከያ",
                descriptionAr = "حلقات يومية مباشرة تركز على تصحيح مخارج الحروف، وضبط أزمنة الغنن والمدود، والمشافهة المباشرة مع المشايخ المسندين لتأسيس القراءة السليمة الخالية من اللحن.",
                descriptionEn = "Daily live halaqat focusing on articulation points, Tajweed precision, and oral transmission with certified scholars.",
                instructorName = "الشيخ عبد الله محمد النور",
                instructorSanad = "مسند في القراءات العشر ومتون التجويد",
                durationText = "3 أشهر (24 حلقة تفاعلية)",
                totalHalaqat = 24,
                prerequisiteAr = "الرغبة في تصحيح التلاوة ومعرفة القراءة الأساسية",
                featuresAr = listOf("تسميع فردي وتصحيح مباشر", "متابعة أسبوعية في المتون", "شهادة إتمام مستوى"),
                badgeTag = "تأسيسي وإتقان",
                isOpenForRegistration = true,
                enrolledCount = 0
            ),
            Program(
                id = "prog_hifz",
                titleAr = "الحفظ المكثف وتثبيت المحفوظ",
                titleEn = "Intensive Memorization & Revision",
                titleAm = "የተጠናከረ የቁርኣን ሂፍዝ እና ክለሳ",
                descriptionAr = "منهج مكثف ومدروس لحفظ القرآن الكريم كاملاً مع المراجعة التراكمية اليومية ونظام استظهار صارم يضمن رسوخ الآيات كالفاتحة بإشراف حفاظ متقنين.",
                descriptionEn = "Intensive curriculum for full Quran memorization with cumulative daily revision under master teachers.",
                instructorName = "الشيخ الحافظ محمد أمين",
                instructorSanad = "حفظ متقن بالسند المتصل",
                durationText = "12 - 18 شهر",
                totalHalaqat = 180,
                prerequisiteAr = "الجدية والالتزام بساعات التسميع اليومية",
                featuresAr = listOf("خطة حفظ يومية محكمة", "مراجعة 5 أجزاء أسبوعياً", "اختبارات إتقان دورية"),
                badgeTag = "حفظ وإتقان",
                isOpenForRegistration = true,
                enrolledCount = 0
            ),
            Program(
                id = "prog_hafs",
                titleAr = "الإجازة برواية حفص عن عاصم",
                titleEn = "Hafs An Asim Ijazah with Sanad",
                titleAm = "የሃፍስ ከዓስም የሰነድ ኢጃዛ ፕሮግرام",
                descriptionAr = "عرض الختمة كاملة غيباً من الفاتحة إلى الناس برواية حفص عن عاصم من طريق الشاطبية، مع تطبيق كافة دقائق التجويد للحصول على الإجازة المعتمدة بالسند المتصل إلى رسول الله ﷺ.",
                descriptionEn = "Full memorized recitation from Al-Fatihah to An-Nas via Shatibiyyah for connected Sanad Ijazah to the Prophet ﷺ.",
                instructorName = "فضيلة الشيخ د. إبراهيم أحمد الجزري",
                instructorSanad = "سند عالي متصل إلى الإمام عاصم والإمام ابن الجزري",
                durationText = "6 - 12 شهر",
                totalHalaqat = 60,
                prerequisiteAr = "حفظ القرآن الكريم كاملاً وإتقان متن الجزرية",
                featuresAr = listOf("سند متصل معتمد", "مجلس ختم رسمي", "وثيقة إجازة مصدقة"),
                badgeTag = "إجازة بالسند المتصل",
                isOpenForRegistration = true,
                enrolledCount = 0
            ),
            Program(
                id = "prog_qiraat",
                titleAr = "الإجازة بالقراءات السبع المتواترة",
                titleEn = "Seven Mutawatir Qira'at Ijazah",
                titleAm = "የሰባቱ ተከታታይ የቁርኣን ንባቦች ኢጃዛ",
                descriptionAr = "برنامج الجمع والإفراد للقراءات السبع من طريق الشاطبية (حرز الأماني ووجه التهاني)، تأهيل نوعي لنخبة القراء ونيل أعلى الأسانيد المتصلة بأئمة الإقراء.",
                descriptionEn = "Advanced recitation of the Seven Qira'at via Ash-Shatibiyyah for verified Quranic recitation masters.",
                instructorName = "المقرئ الجامع الشيخ حسن علي",
                instructorSanad = "إجازة في القراءات العشر الكبرى والصغرى",
                durationText = "24 شهر",
                totalHalaqat = 120,
                prerequisiteAr = "إجازة سابقة برواية حفص وإتقان حفظ الشاطبية",
                featuresAr = listOf("جمع القراءات بطريقة الإفراد والجمع", "تحريرات الشاطبية", "إجازة مسندة كبرى"),
                badgeTag = "عالي الإسناد (تخصص)",
                isOpenForRegistration = true,
                enrolledCount = 0
            )
        )
    )
    val programs: StateFlow<List<Program>> = _programs.asStateFlow()

    // 4. Enrollment Requests
    private val _enrollmentRequests = MutableStateFlow<List<EnrollmentRequest>>(emptyList())
    val enrollmentRequests: StateFlow<List<EnrollmentRequest>> = _enrollmentRequests.asStateFlow()

    // 5. Official Packages (7 Packages from Spec)
    private val _minutePackages = MutableStateFlow(
        listOf(
            MinutePackage("pkg_1", "الفضية العادية", "Silver Package", 100, 500, false, listOf("100 دقيقة تسميع", "صلاحية 30 يوم", "دخول الحلقات العامة")),
            MinutePackage("pkg_2", "الذهبية", "Gold Package", 200, 1000, true, listOf("200 دقيقة تسميع", "صلاحية 60 يوم", "تسميع فردي مباشر")),
            MinutePackage("pkg_3", "المتميزة", "Premium Package", 300, 1470, false, listOf("300 دقيقة تسميع", "صلاحية 90 يوم", "متابعة أسبوعية في الحفظ")),
            MinutePackage("pkg_4", "النخبة", "Elite Package", 400, 1920, false, listOf("400 دقيقة تسميع", "صلاحية 90 يوم", "أولوية في طابور التسميع")),
            MinutePackage("pkg_5", "البداية", "Starter Package", 500, 2350, false, listOf("500 دقيقة تسميع", "صلاحية 120 يوم", "تقارير إتقان دورية")),
            MinutePackage("pkg_6", "التقدم", "Progress Package", 600, 2760, false, listOf("600 دقيقة تسميع", "صلاحية 150 يوم", "إشراف شيخ مسند")),
            MinutePackage("pkg_7", "باقة 1000 دقيقة", "1000 Minutes Master", 1000, 4500, false, listOf("1000 دقيقة تسميع", "صلاحية سنة كاملة", "مسار ختمة كاملة بالسند"))
        )
    )
    val minutePackages: StateFlow<List<MinutePackage>> = _minutePackages.asStateFlow()

    // 6. Bank Accounts
    private val _bankAccounts = MutableStateFlow(
        listOf(
            BankAccount("cbe", "CBE", "البنك التجاري الإثيوبي (CBE)", "1000285374425"),
            BankAccount("boa", "BOA", "بنك الحبشة (BOA)", "226626646"),
            BankAccount("telebirr", "Telebirr", "تيليبير (Telebirr)", "0990161372"),
            BankAccount("mpesa", "M-Pesa", "إم-بيسا (M-Pesa Safaricom)", "0721009122")
        )
    )
    val bankAccounts: StateFlow<List<BankAccount>> = _bankAccounts.asStateFlow()

    // 7. Payment Requests
    private val _paymentRequests = MutableStateFlow<List<PaymentRequest>>(emptyList())
    val paymentRequests: StateFlow<List<PaymentRequest>> = _paymentRequests.asStateFlow()

    // 8. Credit Ledger (Immutable Financial History)
    private val _creditLedger = MutableStateFlow<List<MinuteLedgerEntry>>(emptyList())
    val creditLedger: StateFlow<List<MinuteLedgerEntry>> = _creditLedger.asStateFlow()

    // 9. Announcements (Starts clean for real admin posts)
    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    // 10. Quran Posts (Starts clean for real posts)
    private val _quranPosts = MutableStateFlow<List<QuranPost>>(emptyList())
    val quranPosts: StateFlow<List<QuranPost>> = _quranPosts.asStateFlow()

    // 11. Notifications (Starts clean for real user events)
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // 12. Groups (The 7 Specific Groups: 6 Gender x Time Slot + 1 General Forum)
    private val _groups = MutableStateFlow(
        listOf(
            HalqahGroup(
                id = "grp_m_morning",
                nameAr = "حلقات الذكور - الفترة الصباحية (الفجر والضحى)",
                nameEn = "Brothers Halaqat - Morning Session",
                timeSlot = GroupTimeSlot.MORNING,
                gender = GenderCategory.BROTHERS,
                teacherName = "المشرف العام وهيئة الإقراء",
                currentSurah = "سورة البقرة وتصحيح التلاوة",
                membersCount = 18,
                isLiveNow = true,
                liveRoomId = "room-m-morning",
                telegramLink = null,
                description = "حلقات مخصصة للطلاب الذكور في الفترة الصباحية المباركة من بعد صلاة الفجر إلى الضحى لمدارسة القرآن الكريم والتلقين المباشر.",
                members = listOf(
                    GroupMember("m1", "الشيخ د. إبراهيم أحمد", "رئيس هيئة الإقراء", true),
                    GroupMember("m2", "المشرف العام (خالد أحمد)", "المشرف العام", true)
                )
            ),
            HalqahGroup(
                id = "grp_m_evening",
                nameAr = "حلقات الذكور - الفترة المسائية (العصر والمغرب)",
                nameEn = "Brothers Halaqat - Evening Session",
                timeSlot = GroupTimeSlot.EVENING,
                gender = GenderCategory.BROTHERS,
                teacherName = "مشايخ الإقراء المعتمدون",
                currentSurah = "سورة آل عمران والنساء",
                membersCount = 15,
                isLiveNow = false,
                liveRoomId = "room-m-evening",
                telegramLink = null,
                description = "حلقات مخصصة للطلاب الذكور في الفترة المسائية بين صلاتي العصر والمغرب للمراجعة وضبط الأداء.",
                members = listOf(
                    GroupMember("m3", "الشيخ عبد الله محمد النور", "معلم مسند", true),
                    GroupMember("m2", "المشرف العام (خالد أحمد)", "المشرف العام", true)
                )
            ),
            HalqahGroup(
                id = "grp_m_night",
                nameAr = "حلقات الذكور - الفترة الليلية (العشاء والسحر)",
                nameEn = "Brothers Halaqat - Night Session",
                timeSlot = GroupTimeSlot.NIGHT,
                gender = GenderCategory.BROTHERS,
                teacherName = "مشايخ الحفظ والإجازة بالسند",
                currentSurah = "عرض الختمات والمراجعة المكثفة",
                membersCount = 22,
                isLiveNow = true,
                liveRoomId = "room-m-night",
                telegramLink = null,
                description = "حلقات مخصصة للطلاب الذكور في الفترة الليلية بعد صلاة العشاء وفترة السحر لعرض الختمات ونيل الإجازة بالسند.",
                members = listOf(
                    GroupMember("m4", "الشيخ الحافظ محمد أمين", "معلم إقراء", true),
                    GroupMember("m2", "المشرف العام (خالد أحمد)", "المشرف العام", true)
                )
            ),
            HalqahGroup(
                id = "grp_f_morning",
                nameAr = "حلقات الإناث - الفترة الصباحية (الفجر والضحى)",
                nameEn = "Sisters Halaqat - Morning Session",
                timeSlot = GroupTimeSlot.MORNING,
                gender = GenderCategory.SISTERS,
                teacherName = "المعلمات المسندات الفضليات",
                currentSurah = "سورة البقرة والتلقين",
                membersCount = 16,
                isLiveNow = true,
                liveRoomId = "room-f-morning",
                telegramLink = null,
                description = "حلقات مخصصة للأخوات والطالبات في الفترة الصباحية لتصحيح التلاوة وضبط المخارج والصفات.",
                members = listOf(
                    GroupMember("f1", "المعلمة أم عبد الله الحافظة", "معلمة مسندة", true),
                    GroupMember("f2", "المشرفة التعليمية", "إشراف مقرأة الأخوات", true)
                )
            ),
            HalqahGroup(
                id = "grp_f_evening",
                nameAr = "حلقات الإناث - الفترة المسائية (العصر والمغرب)",
                nameEn = "Sisters Halaqat - Evening Session",
                timeSlot = GroupTimeSlot.EVENING,
                gender = GenderCategory.SISTERS,
                teacherName = "المعلمات المقرئات",
                currentSurah = "الحفظ والمراجعة",
                membersCount = 14,
                isLiveNow = false,
                liveRoomId = "room-f-evening",
                telegramLink = null,
                description = "حلقات مخصصة للأخوات والطالبات في الفترة المسائية بين صلاتي العصر والمغرب لمتابعة الحفظ وتثبيته.",
                members = listOf(
                    GroupMember("f3", "المعلمة خديجة مسعود", "معلمة قرآن", true),
                    GroupMember("f2", "المشرفة التعليمية", "إشراف مقرأة الأخوات", true)
                )
            ),
            HalqahGroup(
                id = "grp_f_night",
                nameAr = "حلقات الإناث - الفترة الليلية (العشاء والسحر)",
                nameEn = "Sisters Halaqat - Night Session",
                timeSlot = GroupTimeSlot.NIGHT,
                gender = GenderCategory.SISTERS,
                teacherName = "نخبة المعلمات المسندات",
                currentSurah = "عرض الختمات والإتقان",
                membersCount = 19,
                isLiveNow = true,
                liveRoomId = "room-f-night",
                telegramLink = null,
                description = "حلقات مخصصة للأخوات والطالبات في الفترة الليلية لعرض المتون والختمات القرآنية برواية حفص وغيرها.",
                members = listOf(
                    GroupMember("f4", "المعلمة عائشة عمر", "معلمة إتقان", true),
                    GroupMember("f2", "المشرفة التعليمية", "إشراف مقرأة الأخوات", true)
                )
            ),
            HalqahGroup(
                id = "grp_general",
                nameAr = "الملتقى القرآني العام (كافة الطلاب والمعلمين)",
                nameEn = "General Quranic Forum",
                timeSlot = GroupTimeSlot.MORNING,
                gender = GenderCategory.GENERAL,
                teacherName = "إدارة المقرأة وهيئة الإقراء",
                currentSurah = "مذاكرة أحكام التجويد والمتون والفوائد",
                membersCount = 45,
                isLiveNow = true,
                liveRoomId = "room-general",
                telegramLink = null,
                description = "الملتقى العام الجامع لكافة منتسبي مقرأة الإمام الجزري من طلاب ومعلمين وإداريين، مخصص للفوائد العلمية ومذاكرة متون التجويد.",
                members = listOf(
                    GroupMember("m1", "الشيخ د. إبراهيم أحمد", "رئيس هيئة الإقراء", true),
                    GroupMember("m2", "المشرف العام (خالد أحمد)", "المشرف العام", true),
                    GroupMember("f1", "المعلمة أم عبد الله الحافظة", "معلمة مسندة", true)
                )
            )
        )
    )
    val groups: StateFlow<List<HalqahGroup>> = _groups.asStateFlow()

    // 13. Managed Students Directory (Starts clean, dynamic real accounts only)
    private val _students = MutableStateFlow<List<User>>(emptyList())
    val students: StateFlow<List<User>> = _students.asStateFlow()

    // 14. Conversations & Messages
    private val _conversations = MutableStateFlow(
        listOf(
            Conversation(
                id = "conv_admin",
                participantName = "الحساب الرسمي للمقرأة والإشراف",
                participantRole = "إدارة المقرأة",
                participantCode = "HQ-001",
                participantBio = "الحساب المعتمد لمقرأة الإمام الجزري لخدمة الطلاب ومتابعة التسجيل والدعم الفني",
                lastMessage = "السلام عليكم ورحمة الله، تم تفعيل اشتراكك وجدول حلقات التسميع",
                lastMessageTime = "10:45 ص",
                unreadCount = 1,
                isOfficial = true,
                enrolledPrograms = listOf("الإدارة العامة", "هيئة الإقراء")
            ),
            Conversation(
                id = "conv_sheikh_ibrahim",
                participantName = "الشيخ د. إبراهيم أحمد",
                participantRole = "مقرئ مجاز - رئيس هيئة الإقراء",
                participantCode = "TCH-102",
                participantBio = "مسند في القراءات العشر الكبرى والصغرى ومشرف برنامج إجازة حفص",
                lastMessage = "السلام عليكم ورحمة الله، موعدنا في حلقة التسميع القادمة بإذن الله تعالى",
                lastMessageTime = "أمس",
                unreadCount = 0,
                isOfficial = false,
                enrolledPrograms = listOf("الإجازة برواية حفص عن عاصم")
            )
        )
    )
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            "conv_admin" to listOf(
                ChatMessage("m1", "conv_admin", "admin", "إدارة المقرأة", UserRole.ADMIN, "أهلاً بك في مقرأة الإمام الجزري للإقراء والإجازة بالسند المتصل.", MessageType.TEXT, "09:30 ص", false),
                ChatMessage("m3", "conv_admin", "admin", "إدارة المقرأة", UserRole.ADMIN, "السلام عليكم ورحمة الله، يمكنك الاستفسار هنا عن البرامج والحلقات واعتماد إشعارات الشحن.", MessageType.TEXT, "10:00 ص", false)
            )
        )
    )
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    // --- ACTIONS ---

    fun completeOnboarding() {
        _isOnboardingCompleted.value = true
    }

    fun isAdminEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val clean = email.trim().lowercase()
        return clean == "meqraahaljezery@gmail.com" ||
                clean == "khdahd241@gmail.com" ||
                clean == "admin@aljazari.org"
    }

    fun registerOrSignInStudent(
        email: String,
        name: String,
        phone: String = "",
        gender: String = "ذكر",
        targetProgram: String = "حلقات التلقين وتصحيح الأداء"
    ) {
        val cleanEmail = email.trim().lowercase()
        val isAdmin = isAdminEmail(cleanEmail)
        val role = if (isAdmin) UserRole.ADMIN else UserRole.STUDENT
        val displayName = when {
            name.isNotBlank() -> name.trim()
            cleanEmail == "meqraahaljezery@gmail.com" -> "مقرأة الإمام الجزري (الإدارة العامة)"
            isAdmin -> "المشرف العام (خالد أحمد مصطفى)"
            else -> "طالب المقرأة"
        }
        val user = User(
            id = if (isAdmin) "admin_jezery_01" else "std_${UUID.randomUUID().toString().take(6)}",
            name = displayName,
            email = email.trim(),
            role = role,
            studentCode = if (isAdmin) "ADM-001" else "JZR-${(1000..9999).random()}",
            phone = if (phone.isNotBlank()) phone.trim() else (if (isAdmin) "+251 990 161 372" else "+251 912 000 000"),
            gender = gender,
            minuteBalance = if (isAdmin) 1000 else 60,
            tajweedLevel = if (isAdmin) "مقرئ مسند بالقراءات العشر" else "طالب مسجل جديد",
            sanadTrack = if (isAdmin) "إشراف هيئة الإقراء والسند" else targetProgram,
            bio = if (isAdmin) "الحساب الإداري الرسمي لمقرأة الإمام الجزري للإقراء والإجازة بالسند المتصل" else "طالب علم في $targetProgram"
        )
        _currentUser.value = user
        _isOnboardingCompleted.value = true

        if (role == UserRole.STUDENT) {
            // إضافة الطالب تلقائياً وبشكل فوري لقائمة المدير
            val existingIndex = _students.value.indexOfFirst { it.email.equals(user.email, ignoreCase = true) }
            if (existingIndex >= 0) {
                val updated = _students.value.toMutableList()
                updated[existingIndex] = user
                _students.value = updated
            } else {
                _students.value = listOf(user) + _students.value
            }

            // إنشاء طلب تسجيل فوري في قائمة طلبات المدير
            val enrollmentReq = EnrollmentRequest(
                id = "enr_${UUID.randomUUID().toString().take(6)}",
                studentId = user.id,
                studentName = user.name,
                studentCode = user.studentCode,
                programId = "prog_auto",
                programTitle = targetProgram,
                age = 22,
                gender = gender,
                city = "مسجل عبر الاستمارة",
                contactPhone = user.phone,
                currentMemorization = "مسجل جديد",
                previousExperience = "تم التسجيل عبر استمارة المقرأة",
                preferredTimeSlot = "حسب توفر الحلقات",
                status = "PENDING",
                submittedAt = dateFormat.format(Date())
            )
            _enrollmentRequests.value = listOf(enrollmentReq) + _enrollmentRequests.value

            // إشعار فوري
            val notif = AppNotification(
                id = "notif_${System.currentTimeMillis()}",
                title = "تم استلام استمارة التسجيل",
                message = "أهلاً بك يا ${user.name} في مقرأة الإمام الجزري، تم إرسال ملفك للإدارة العامة للمراجعة والاعتماد.",
                date = dateFormat.format(Date()),
                type = "REGISTRATION",
                isRead = false
            )
            _notifications.value = listOf(notif) + _notifications.value
        }
    }

    fun signInWithGoogle(email: String, name: String) {
        registerOrSignInStudent(email, name)
    }

    fun signOut() {
        _currentUser.value = null
    }

    fun updateProfile(name: String, bio: String, phone: String, city: String) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(name = name, bio = bio, phone = phone, city = city)
    }

    // Submit Program Enrollment Request
    fun submitEnrollmentRequest(
        program: Program,
        age: Int,
        gender: String,
        city: String,
        phone: String,
        memorization: String,
        experience: String,
        timeSlot: String
    ): Boolean {
        val user = _currentUser.value ?: return false

        // Check if student is already enrolled or pending in a conflicting program
        val hasConflicting = _enrollmentRequests.value.any {
            it.studentId == user.id && it.programId == program.id && it.status != "REJECTED"
        }
        if (hasConflicting) return false

        val newRequest = EnrollmentRequest(
            id = "enr_${UUID.randomUUID().toString().take(6)}",
            studentId = user.id,
            studentName = user.name,
            studentCode = user.studentCode,
            programId = program.id,
            programTitle = program.titleAr,
            age = age,
            gender = gender,
            city = city,
            contactPhone = phone,
            currentMemorization = memorization,
            previousExperience = experience,
            preferredTimeSlot = timeSlot,
            status = "PENDING",
            submittedAt = dateFormat.format(Date())
        )
        _enrollmentRequests.value = listOf(newRequest) + _enrollmentRequests.value

        // Immediate notification for student
        val notif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(6)}",
            title = "تم استلام طلب التسجيل",
            message = "تم تسجيل طلب التحاقك ببرنامج (${program.titleAr}) بنجاح، وهو قيد المراجعة والاعتماد لدى المشرف العام.",
            date = dateFormat.format(Date()),
            type = "ENROLLMENT",
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
        return true
    }

    fun approveEnrollment(requestId: String) {
        val req = _enrollmentRequests.value.find { it.id == requestId }
        _enrollmentRequests.value = _enrollmentRequests.value.map {
            if (it.id == requestId) it.copy(status = "APPROVED") else it
        }
        if (req != null) {
            val notif = AppNotification(
                id = "notif_${UUID.randomUUID().toString().take(6)}",
                title = "تم قبول طلب التسجيل في البرنامج",
                message = "مبارك! تم اعتماد طلبك للالتحاق ببرنامج (${req.programTitle}) بإشراف المقرأة.",
                date = dateFormat.format(Date()),
                type = "ENROLLMENT",
                isRead = false
            )
            _notifications.value = listOf(notif) + _notifications.value
        }
    }

    fun rejectEnrollment(requestId: String, reason: String) {
        _enrollmentRequests.value = _enrollmentRequests.value.map {
            if (it.id == requestId) it.copy(status = "REJECTED", rejectionReason = reason) else it
        }
    }

    // Submit Payment Recharge Request with Receipt Image
    fun submitPaymentRequest(
        pkg: MinutePackage,
        targetBank: String,
        senderName: String,
        senderPhone: String,
        transactionRef: String,
        receiptUri: String? = null
    ): Boolean {
        val user = _currentUser.value ?: return false
        val newPayment = PaymentRequest(
            id = "pay_${UUID.randomUUID().toString().take(6)}",
            studentId = user.id,
            studentName = user.name,
            studentCode = user.studentCode,
            senderFullName = senderName,
            transactionRef = transactionRef,
            senderPhoneNumber = senderPhone,
            targetBank = targetBank,
            packageMinutes = pkg.minutes,
            amountPaidEtb = pkg.priceEtb,
            status = PaymentStatus.PENDING,
            submittedAt = dateFormat.format(Date()),
            receiptUri = receiptUri,
            isDownloaded = false,
            isArchived = false
        )
        _paymentRequests.value = listOf(newPayment) + _paymentRequests.value

        // Immediate notification for student
        val notif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(6)}",
            title = "تم إرسال إشعار شحن الرصيد",
            message = "تم تسجيل إشعار تحويل باقة ${pkg.titleAr} (${pkg.minutes} دقيقة) برقم عملية $transactionRef وصورة الإيصال بنجاح وهو قيد المراجعة.",
            date = dateFormat.format(Date()),
            type = "RECHARGE",
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
        return true
    }

    // Approve Payment Request (Atomically adds minutes to student balance and records in Ledger)
    fun approvePayment(paymentId: String) {
        val payment = _paymentRequests.value.find { it.id == paymentId } ?: return
        if (payment.status == PaymentStatus.APPROVED) return

        // 1. Mark payment approved
        _paymentRequests.value = _paymentRequests.value.map {
            if (it.id == paymentId) it.copy(status = PaymentStatus.APPROVED, reviewedByAdmin = "خالد أحمد مصطفى") else it
        }

        // 2. Add minutes to user if user is currently loaded or matches student
        val user = _currentUser.value
        val before = user?.minuteBalance ?: 0
        val after = before + payment.packageMinutes

        if (user != null && user.id == payment.studentId) {
            _currentUser.value = user.copy(minuteBalance = after)
        }

        // Also update in _students list
        _students.value = _students.value.map {
            if (it.id == payment.studentId) it.copy(minuteBalance = it.minuteBalance + payment.packageMinutes) else it
        }

        // 3. Record in Credit Ledger
        val ledgerEntry = MinuteLedgerEntry(
            id = "led_${UUID.randomUUID().toString().take(6)}",
            studentId = payment.studentId,
            studentName = payment.studentName,
            changeAmount = payment.packageMinutes,
            balanceBefore = before,
            balanceAfter = after,
            type = LedgerEntryType.DEPOSIT_PAYMENT,
            reason = "اعتماد شحن باقة ${payment.packageMinutes} دقيقة - حوالة بنك ${payment.targetBank} رقم ${payment.transactionRef}",
            timestamp = dateFormat.format(Date()),
            actorName = "المشرف العام (خالد أحمد)"
        )
        _creditLedger.value = listOf(ledgerEntry) + _creditLedger.value

        // 4. Send Notification
        val notif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(6)}",
            title = "تم شحن الرصيد بنجاح",
            message = "تم اعتماد تحويلك وإضافة ${payment.packageMinutes} دقيقة إلى رصيدك. رصيدك الحالي: $after دقيقة.",
            date = dateFormat.format(Date()),
            type = "RECHARGE",
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    fun rejectPayment(paymentId: String, reason: String) {
        _paymentRequests.value = _paymentRequests.value.map {
            if (it.id == paymentId) it.copy(status = PaymentStatus.REJECTED, rejectionReason = reason, reviewedByAdmin = "خالد أحمد مصطفى") else it
        }
    }

    fun markPaymentReceiptDownloaded(paymentId: String) {
        _paymentRequests.value = _paymentRequests.value.map {
            if (it.id == paymentId) it.copy(isDownloaded = true) else it
        }
    }

    fun deletePaymentRequest(paymentId: String) {
        _paymentRequests.value = _paymentRequests.value.filter { it.id != paymentId }
    }

    // --- STUDENT DIRECTORY ACTIONS (ADMIN) ---

    fun activateStudent(studentId: String) {
        _students.value = _students.value.map {
            if (it.id == studentId) it.copy(isSuspended = false) else it
        }
        val target = _students.value.find { it.id == studentId }
        val notif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(6)}",
            title = "تم تفعيل الحساب",
            message = "تمت إعادة تفعيل حساب الطالب (${target?.name ?: ""}) في المقرأة بنجاح.",
            date = dateFormat.format(Date()),
            type = "SYSTEM",
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    fun suspendStudent(studentId: String) {
        _students.value = _students.value.map {
            if (it.id == studentId) it.copy(isSuspended = true) else it
        }
        val target = _students.value.find { it.id == studentId }
        val notif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(6)}",
            title = "تنبيه إداري: تجميد الحساب",
            message = "تم تجميد حساب الطالب (${target?.name ?: ""}) مؤقتاً لمراجعة الالتزام بالحلقات.",
            date = dateFormat.format(Date()),
            type = "SYSTEM",
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    fun deleteStudent(studentId: String) {
        _students.value = _students.value.filter { it.id != studentId }
    }

    fun awardMinutesToStudent(studentId: String, minutes: Int, reason: String) {
        var studentName = "الطالب"
        var beforeBal = 0
        var afterBal = 0
        _students.value = _students.value.map {
            if (it.id == studentId) {
                studentName = it.name
                beforeBal = it.minuteBalance
                afterBal = beforeBal + minutes
                it.copy(minuteBalance = afterBal)
            } else it
        }

        // If current user is this student, update too
        if (_currentUser.value?.id == studentId) {
            _currentUser.value = _currentUser.value?.copy(minuteBalance = afterBal)
        }

        val ledgerEntry = MinuteLedgerEntry(
            id = "led_${UUID.randomUUID().toString().take(6)}",
            studentId = studentId,
            studentName = studentName,
            changeAmount = minutes,
            balanceBefore = beforeBal,
            balanceAfter = afterBal,
            type = LedgerEntryType.HONOR_AWARD,
            reason = "تكريم ومنح دقائق إضافية: $reason",
            timestamp = dateFormat.format(Date()),
            actorName = "المشرف العام (خالد أحمد)"
        )
        _creditLedger.value = listOf(ledgerEntry) + _creditLedger.value

        val notif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(6)}",
            title = "تكريم: تمت إضافة $minutes دقيقة لرصيدك",
            message = "منحك المشرف العام $minutes دقيقة إضافية تقديراً لاجتهادك. السبب: $reason. رصيدك الحالي: $afterBal دقيقة.",
            date = dateFormat.format(Date()),
            type = "RECHARGE",
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    fun sendStudentAlert(studentId: String, alertMessage: String) {
        val student = _students.value.find { it.id == studentId }
        val notif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(6)}",
            title = "تنبيه إداري من إدارة المقرأة",
            message = alertMessage,
            date = dateFormat.format(Date()),
            type = "SYSTEM",
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    // --- QURAN SESSION DEDUCTION (خصم الجلسة القرآنية) ---

    fun deductQuranSession(
        studentId: String,
        minutes: Int,
        recitationSpan: String,
        tajweedNotes: String
    ): Boolean {
        val student = _students.value.find { it.id == studentId } ?: return false
        if (student.minuteBalance < minutes) return false

        val before = student.minuteBalance
        val after = before - minutes

        // Update student in list
        _students.value = _students.value.map {
            if (it.id == studentId) it.copy(minuteBalance = after) else it
        }

        // If current user is this student, update too
        if (_currentUser.value?.id == studentId) {
            _currentUser.value = _currentUser.value?.copy(minuteBalance = after)
        }

        // Immutable Ledger Entry
        val ledgerEntry = MinuteLedgerEntry(
            id = "led_${UUID.randomUUID().toString().take(6)}",
            studentId = studentId,
            studentName = student.name,
            changeAmount = -minutes,
            balanceBefore = before,
            balanceAfter = after,
            type = LedgerEntryType.DEDUCTION_SESSION,
            reason = "جلسة تسميع قرآن: $recitationSpan ($minutes دقيقة) - ملاحظات الأداء: ${tajweedNotes.ifBlank { "أداء متقن" }}",
            timestamp = dateFormat.format(Date()),
            actorName = "المقرئ المجاز"
        )
        _creditLedger.value = listOf(ledgerEntry) + _creditLedger.value

        // Immediate Notification
        val notif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(6)}",
            title = "تم تسجيل جلسة تسميع وخصم $minutes دقيقة",
            message = "تم خصم $minutes دقيقة لجلسة تسميع ($recitationSpan). رصيدك المتبقي: $after دقيقة. ملاحظات الشيخ: ${tajweedNotes.ifBlank { "ممتاز وبارك الله فيك" }}.",
            date = dateFormat.format(Date()),
            type = "SESSION",
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
        return true
    }

    // --- NOTIFICATIONS MANAGEMENT ---

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    // --- PUBLISHING ACTIONS (ANNOUNCEMENTS & STATUSES) ---

    fun addAnnouncement(title: String, content: String, isPinned: Boolean) {
        val newAnc = Announcement(
            id = "anc_${UUID.randomUUID().toString().take(6)}",
            title = title,
            content = content,
            date = dateFormat.format(Date()).substringBefore(" "),
            isPinned = isPinned,
            author = "المشرف العام (خالد أحمد مصطفى)"
        )
        _announcements.value = listOf(newAnc) + _announcements.value

        // Also broadcast notification
        val notif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(6)}",
            title = "إعلان رسمي جديد: $title",
            message = content,
            date = dateFormat.format(Date()),
            type = "ANNOUNCEMENT",
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    fun publishQuranPost(content: String, authorRole: String = "إدارة المقرأة وهيئة الإقراء") {
        val newPost = QuranPost(
            id = "post_${UUID.randomUUID().toString().take(6)}",
            authorName = "مقرأة الإمام الجزري",
            authorRole = authorRole,
            content = content,
            date = "الآن",
            likesCount = 0,
            commentsCount = 0,
            isLikedByMe = false
        )
        _quranPosts.value = listOf(newPost) + _quranPosts.value
    }

    fun manualAdjustMinutes(studentName: String, amount: Int, reason: String) {
        val user = _currentUser.value ?: return
        val before = user.minuteBalance
        val after = (before + amount).coerceAtLeast(0)
        _currentUser.value = user.copy(minuteBalance = after)

        val entry = MinuteLedgerEntry(
            id = "led_${UUID.randomUUID().toString().take(6)}",
            studentId = user.id,
            studentName = studentName,
            changeAmount = amount,
            balanceBefore = before,
            balanceAfter = after,
            type = LedgerEntryType.MANUAL_ADJUSTMENT_ADMIN,
            reason = reason,
            timestamp = dateFormat.format(Date()),
            actorName = "المشرف العام"
        )
        _creditLedger.value = listOf(entry) + _creditLedger.value
    }

    fun toggleLikePost(postId: String) {
        _quranPosts.value = _quranPosts.value.map {
            if (it.id == postId) {
                val newLiked = !it.isLikedByMe
                val newCount = if (newLiked) it.likesCount + 1 else it.likesCount - 1
                it.copy(isLikedByMe = newLiked, likesCount = newCount)
            } else it
        }
    }

    fun sendChatMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        val user = _currentUser.value ?: return
        val newMsg = ChatMessage(
            id = "msg_${UUID.randomUUID().toString().take(6)}",
            conversationId = conversationId,
            senderId = user.id,
            senderName = user.name,
            senderRole = user.role,
            content = text,
            type = MessageType.TEXT,
            timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            isMine = true
        )
        val currentList = _messages.value[conversationId] ?: emptyList()
        _messages.value = _messages.value + (conversationId to (currentList + newMsg))

        // Update conversation last message
        _conversations.value = _conversations.value.map {
            if (it.id == conversationId) it.copy(lastMessage = text, lastMessageTime = "الآن") else it
        }
    }

    // --- TELEGRAM-STYLE GROUP CHAT & ADMIN GROUP CREATION ---

    fun updateGroupTelegramLink(groupId: String, link: String) {
        val trimmed = link.trim()
        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) g.copy(telegramLink = trimmed.ifBlank { null }) else g
        }
    }

    fun createGroup(
        nameAr: String,
        nameEn: String,
        gender: GenderCategory,
        timeSlot: GroupTimeSlot,
        teacherName: String,
        currentSurah: String,
        telegramLink: String?,
        description: String
    ) {
        val newId = "grp_custom_${System.currentTimeMillis()}"
        val newGroup = HalqahGroup(
            id = newId,
            nameAr = nameAr,
            nameEn = nameEn.ifBlank { nameAr },
            timeSlot = timeSlot,
            gender = gender,
            teacherName = teacherName.ifBlank { "إدارة المقرأة" },
            currentSurah = currentSurah.ifBlank { "القرآن الكريم" },
            membersCount = 1,
            isLiveNow = false,
            liveRoomId = "room_${newId}",
            telegramLink = telegramLink?.trim()?.ifBlank { null },
            description = description,
            members = listOf(
                GroupMember("admin", _currentUser.value?.name ?: "المشرف العام", "المشرف العام", true)
            )
        )
        _groups.value = _groups.value + newGroup
    }

    fun sendGroupChatMessage(groupId: String, text: String) {
        if (text.isBlank()) return
        val user = _currentUser.value ?: return
        val newMsg = ChatMessage(
            id = "grp_msg_${UUID.randomUUID().toString().take(6)}",
            conversationId = groupId,
            senderId = user.id,
            senderName = user.name,
            senderRole = user.role,
            content = text.trim(),
            type = MessageType.TEXT,
            timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            isMine = true
        )
        val currentList = _messages.value[groupId] ?: emptyList()
        _messages.value = _messages.value + (groupId to (currentList + newMsg))
    }

    // =========================================================================
    // نظام البث المباشر والصوت التفاعلي LiveKit والطابور الذكي (1-on-1 Recitation)
    // =========================================================================

    private val activeSessionJobMap = mutableMapOf<String, Job>()

    private fun sendSystemGroupMessage(groupId: String, text: String) {
        val newMsg = ChatMessage(
            id = "sys_msg_${UUID.randomUUID().toString().take(6)}",
            conversationId = groupId,
            senderId = "system_jazari",
            senderName = "إدارة المقرأة (البث المباشر)",
            senderRole = UserRole.ADMIN,
            content = text,
            type = MessageType.TEXT,
            timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            isMine = false
        )
        val currentList = _messages.value[groupId] ?: emptyList()
        _messages.value = _messages.value + (groupId to (currentList + newMsg))
    }

    // 1. بدء البث المباشر للحلقة (صلاحية المشرف / المعلم)
    fun startGroupBroadcast(groupId: String, context: Context?) {
        val user = _currentUser.value ?: return
        val isAdmin = isAdminEmail(user.email) || user.role == UserRole.ADMIN
        if (!isAdmin) return

        val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        var targetGroupName = "حلقة التسميع"
        var teacherName = user.name

        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) {
                targetGroupName = g.nameAr
                teacherName = g.teacherName
                g.copy(isLiveNow = true, liveStartedAt = nowTime)
            } else g
        }

        if (context != null) {
            SmartNotificationManager.notifyHalaqahLive(context, targetGroupName, teacherName)
            LiveKitManager.connectToRoom(
                context = context,
                roomId = "room_$groupId",
                participantId = user.id,
                participantName = user.name,
                isTeacher = true
            )
        }

        sendSystemGroupMessage(
            groupId,
            "🔴 بدأ المشرف العام / المعلم البث المباشر لحلقة ($targetGroupName) الآن. يمكن للطلاب أخذ دورهم في طابور القراءة المشافهة 🎙️."
        )
    }

    // 2. إنهاء البث المباشر العام للحلقة
    fun endGroupBroadcast(groupId: String, context: Context?) {
        val user = _currentUser.value ?: return
        val isAdmin = isAdminEmail(user.email) || user.role == UserRole.ADMIN
        if (!isAdmin) return

        val currentGroup = _groups.value.find { it.id == groupId }
        if (currentGroup?.activeSession != null) {
            endStudentSessionAndDeduct(groupId, "إنهاء تلقائي مع إغلاق البث", context)
        }

        activeSessionJobMap[groupId]?.cancel()
        activeSessionJobMap.remove(groupId)

        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) {
                g.copy(
                    isLiveNow = false,
                    liveStartedAt = null,
                    activeReciterName = null,
                    activeReciterCode = null,
                    activeSession = null
                )
            } else g
        }

        LiveKitManager.disconnect()
        sendSystemGroupMessage(groupId, "⏹️ تم إنهاء جلسة البث المباشر للحلقة. بارك الله في جميع الطلاب والمعلمين.")
    }

    // 3. أخذ دور في طابور القراءة (للطلاب)
    fun joinRecitationQueue(groupId: String, targetSurah: String = "تسميع المقرر") {
        val user = _currentUser.value ?: return
        val grp = _groups.value.find { it.id == groupId } ?: return

        // منع التكرار إذا كان الطالب في الطابور بالفعل
        if (grp.queue.any { it.studentId == user.id }) return

        val nextNumber = (grp.queue.maxOfOrNull { it.queueNumber } ?: 0) + 1
        val queueItem = RecitationQueueItem(
            id = "q_${UUID.randomUUID().toString().take(6)}",
            studentId = user.id,
            studentName = user.name,
            studentCode = user.studentCode,
            studentGender = user.gender,
            targetSurah = targetSurah.ifBlank { "تسميع المقرر" },
            joinedAt = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            queueNumber = nextNumber,
            isAdmitted = false
        )

        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) {
                g.copy(queue = g.queue + queueItem)
            } else g
        }

        sendSystemGroupMessage(
            groupId,
            "📋 حجز الطالب (${user.name} - ${user.studentCode}) دوره في طابور التسميع رقم #$nextNumber للمقرر: $targetSurah."
        )
    }

    // 4. مغادرة طابور القراءة
    fun leaveRecitationQueue(groupId: String) {
        val user = _currentUser.value ?: return
        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) {
                g.copy(queue = g.queue.filterNot { it.studentId == user.id })
            } else g
        }
    }

    // 5. قبول وإدخال الطالب إلى البث المباشر (صلاحية المعلم / المشرف)
    fun admitStudentToLiveSession(groupId: String, studentId: String, context: Context?) {
        val user = _currentUser.value ?: return
        val isAdmin = isAdminEmail(user.email) || user.role == UserRole.ADMIN
        if (!isAdmin) return

        val grp = _groups.value.find { it.id == groupId } ?: return
        val studentItem = grp.queue.find { it.studentId == studentId }
        val student = _students.value.find { it.id == studentId } ?: _currentUser.value

        val studentName = studentItem?.studentName ?: student?.name ?: "طالب المقرأة"
        val studentCode = studentItem?.studentCode ?: student?.studentCode ?: "JZR-1000"
        val surah = studentItem?.targetSurah ?: "سورة البقرة"

        if (grp.activeSession != null) {
            endStudentSessionAndDeduct(groupId, "إنهاء تلقائي لبدء طالب جديد", context)
        }

        val session = ActiveRecitationSession(
            sessionId = "ses_${UUID.randomUUID().toString().take(6)}",
            groupId = groupId,
            studentId = studentId,
            studentName = studentName,
            studentCode = studentCode,
            teacherName = user.name,
            startedAtTimestamp = System.currentTimeMillis(),
            elapsedSeconds = 0,
            surahName = surah,
            isRunning = true,
            liveKitRoomName = "room_$groupId"
        )

        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) {
                val updatedQueue = g.queue.map { q ->
                    if (q.studentId == studentId) q.copy(isAdmitted = true) else q
                }
                g.copy(
                    activeSession = session,
                    activeReciterName = studentName,
                    activeReciterCode = studentCode,
                    queue = updatedQueue
                )
            } else g
        }

        // تشغيل مؤقت العد التصاعدي الدقيق للجلسة بالثواني
        activeSessionJobMap[groupId]?.cancel()
        activeSessionJobMap[groupId] = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val currentG = _groups.value.find { it.id == groupId }
                val currentS = currentG?.activeSession ?: break
                if (currentS.isRunning) {
                    val updatedS = currentS.copy(elapsedSeconds = currentS.elapsedSeconds + 1)
                    _groups.value = _groups.value.map { g ->
                        if (g.id == groupId) g.copy(activeSession = updatedS) else g
                    }
                }
            }
        }

        LiveKitManager.setStudentParticipant(studentId, studentName)

        if (context != null) {
            SmartNotificationManager.sendNotification(
                context = context,
                notificationId = 777,
                channelId = SmartNotificationManager.CHANNEL_ID_HALAQAT,
                title = "حان دورك في التسميع المباشر! 🎙️",
                message = "أهلاً يا $studentName، أدخلك الشيخ إلى البث الصوتي المباشر الآن لتسميع $surah."
            )
        }

        sendSystemGroupMessage(
            groupId,
            "🎙️ دخل الطالب ($studentName - $studentCode) إلى البث المباشر المشافهة مع المعلم (${user.name}) لتسميع $surah."
        )
    }

    // 6. إيقاف مؤقت أو استئناف عداد جلسة التسميع
    fun toggleSessionTimer(groupId: String) {
        _groups.value = _groups.value.map { g ->
            if (g.id == groupId && g.activeSession != null) {
                val updated = g.activeSession.copy(isRunning = !g.activeSession.isRunning)
                g.copy(activeSession = updated)
            } else g
        }
    }

    // 7. إنهاء جلسة الطالب والخصم التلقائي الدقيق من الرصيد بدون كتابة يدوية
    fun endStudentSessionAndDeduct(groupId: String, notes: String = "", context: Context?) {
        val user = _currentUser.value ?: return
        val isAdmin = isAdminEmail(user.email) || user.role == UserRole.ADMIN
        if (!isAdmin) return

        activeSessionJobMap[groupId]?.cancel()
        activeSessionJobMap.remove(groupId)

        val grp = _groups.value.find { it.id == groupId } ?: return
        val session = grp.activeSession ?: return

        val durationSeconds = session.elapsedSeconds
        val exactDeductedMinutes = maxOf(1, ceil(durationSeconds / 60.0).toInt())

        // تحديث رصيد الطالب تلقائياً في قاعدة البيانات
        val student = _students.value.find { it.id == session.studentId }
        val oldBalance = student?.minuteBalance ?: _currentUser.value?.minuteBalance ?: 60
        val newBalance = maxOf(0, oldBalance - exactDeductedMinutes)

        if (student != null) {
            val updatedStudent = student.copy(minuteBalance = newBalance)
            _students.value = _students.value.map { if (it.id == student.id) updatedStudent else it }
        }

        if (_currentUser.value?.id == session.studentId) {
            _currentUser.value = _currentUser.value?.copy(minuteBalance = newBalance)
        }

        // توثيق العملية في دفتر السجلات والحسابات (Credit Ledger)
        val ledger = MinuteLedgerEntry(
            id = "led_${UUID.randomUUID().toString().take(6)}",
            studentId = session.studentId,
            studentName = session.studentName,
            changeAmount = -exactDeductedMinutes,
            balanceBefore = oldBalance,
            balanceAfter = newBalance,
            type = LedgerEntryType.DEDUCTION_SESSION,
            reason = "خصم تلقائي دقيق: جلسة تسميع مشافهة مباشرة (${durationSeconds} ثانية) لـ ${session.surahName}. ${notes.ifBlank { "تقبل الله" }}",
            timestamp = dateFormat.format(Date()),
            actorName = user.name
        )
        _creditLedger.value = listOf(ledger) + _creditLedger.value

        val updatedQueue = grp.queue.filterNot { it.studentId == session.studentId }

        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) {
                g.copy(
                    activeSession = null,
                    activeReciterName = null,
                    activeReciterCode = null,
                    queue = updatedQueue
                )
            } else g
        }

        LiveKitManager.clearStudentParticipant()

        if (context != null) {
            SmartNotificationManager.sendNotification(
                context = context,
                notificationId = 778,
                channelId = SmartNotificationManager.CHANNEL_ID_ALERTS,
                title = "تم إنهاء جلسة التسميع وخصم الرصيد ⏱️",
                message = "بارك الله فيك يا ${session.studentName}. استغرقت الجلسة ${durationSeconds} ثانية ($exactDeductedMinutes دقيقة). تم الخصم تلقائياً ورصيدك المتبقي: $newBalance دقيقة."
            )
        }

        sendSystemGroupMessage(
            groupId,
            "✅ تم إنهاء جلسة التسميع للطالب (${session.studentName}). مدة القراءة: ${durationSeconds} ثانية ($exactDeductedMinutes دقيقة). تم خصمها بدقة وتوثيقها في السجل."
        )
    }
}
