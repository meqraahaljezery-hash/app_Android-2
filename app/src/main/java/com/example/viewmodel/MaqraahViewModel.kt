package com.example.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.util.LiveKitManager
import com.example.util.SmartNotificationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // 1. حالة التحميل (تمنع فتح الواجهة قبل جلب البيانات)
    private val _isLoadingAuth = MutableStateFlow(true)
    val isLoadingAuth: StateFlow<Boolean> = _isLoadingAuth.asStateFlow()

    // 2. Onboarding State
    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    // 3. Current User
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // الاستماع التلقائي لحالة الدخول عند فتح التطبيق
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // جلب بيانات المستخدم من Firestore في الخلفية
                db.collection("users").document(firebaseUser.uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            try {
                                _currentUser.value = document.toObject(User::class.java)
                            } catch (e: Exception) {
                                Log.e("FIREBASE_ERROR", "Error mapping user: ${e.message}")
                            }
                        }
                        _isLoadingAuth.value = false
                    }
                    .addOnFailureListener { e ->
                        Log.e("FIREBASE_ERROR", "Error fetching user doc: ${e.message}")
                        _isLoadingAuth.value = false
                    }
            } else {
                _currentUser.value = null
                _isLoadingAuth.value = false
            }
        }
    }

    // ... (قوائم البرامج، الباقات، البنوك، الحلقات كما في كودك الأصلي، لم أحذف منها شيئاً) ...
    private val _programs = MutableStateFlow(
        listOf(
            Program(id = "prog_talqin", titleAr = "التلقين وتصحيح الأداء", titleEn = "Talqin & Recitation Correction", titleAm = "የቁርኣን ንባብ እና አነባበብ ማስተካከያ", descriptionAr = "حلقات يومية مباشرة تركز على تصحيح مخارج الحروف، وضبط أزمنة الغنن والمدود، والمشافهة المباشرة مع المشايخ المسندين لتأسيس القراءة السليمة الخالية من اللحن.", descriptionEn = "Daily live halaqat focusing on articulation points, Tajweed precision, and oral transmission with certified scholars.", instructorName = "الشيخ عبد الله محمد النور", instructorSanad = "مسند في القراءات العشر ومتون التجويد", durationText = "3 أشهر (24 حلقة تفاعلية)", totalHalaqat = 24, prerequisiteAr = "الرغبة في تصحيح التلاوة ومعرفة القراءة الأساسية", featuresAr = listOf("تسميع فردي وتصحيح مباشر", "متابعة أسبوعية في المتون", "شهادة إتمام مستوى"), badgeTag = "تأسيسي وإتقان", isOpenForRegistration = true, enrolledCount = 0),
            Program(id = "prog_hifz", titleAr = "الحفظ المكثف وتثبيت المحفوظ", titleEn = "Intensive Memorization & Revision", titleAm = "የተጠናከረ የቁርኣን ሂፍዝ እና ክለሳ", descriptionAr = "منهج مكثف ومدروس لحفظ القرآن الكريم كاملاً مع المراجعة التراكمية اليومية ونظام استظهار صارم يضمن رسوخ الآيات كالفاتحة بإشراف حفاظ متقنين.", descriptionEn = "Intensive curriculum for full Quran memorization with cumulative daily revision under master teachers.", instructorName = "الشيخ الحافظ محمد أمين", instructorSanad = "حفظ متقن بالسند المتصل", durationText = "12 - 18 شهر", totalHalaqat = 180, prerequisiteAr = "الجدية والالتزام بساعات التسميع اليومية", featuresAr = listOf("خطة حفظ يومية محكمة", "مراجعة 5 أجزاء أسبوعياً", "اختبارات إتقان دورية"), badgeTag = "حفظ وإتقان", isOpenForRegistration = true, enrolledCount = 0),
            Program(id = "prog_hafs", titleAr = "الإجازة برواية حفص عن عاصم", titleEn = "Hafs An Asim Ijazah with Sanad", titleAm = "የሃፍስ ከዓስም የሰነድ ኢጃዛ ፕሮግرام", descriptionAr = "عرض الختمة كاملة غيباً من الفاتحة إلى الناس برواية حفص عن عاصم من طريق الشاطبية، مع تطبيق كافة دقائق التجويد للحصول على الإجازة المعتمدة بالسند المتصل إلى رسول الله ﷺ.", descriptionEn = "Full memorized recitation from Al-Fatihah to An-Nas via Shatibiyyah for connected Sanad Ijazah to the Prophet ﷺ.", instructorName = "فضيلة الشيخ د. إبراهيم أحمد الجزري", instructorSanad = "سند عالي متصل إلى الإمام عاصم والإمام ابن الجزري", durationText = "6 - 12 شهر", totalHalaqat = 60, prerequisiteAr = "حفظ القرآن الكريم كاملاً وإتقان متن الجزرية", featuresAr = listOf("سند متصل معتمد", "مجلس ختم رسمي", "وثيقة إجازة مصدقة"), badgeTag = "إجازة بالسند المتصل", isOpenForRegistration = true, enrolledCount = 0),
            Program(id = "prog_qiraat", titleAr = "الإجازة بالقراءات السبع المتواترة", titleEn = "Seven Mutawatir Qira'at Ijazah", titleAm = "የሰባቱ ተከታታይ የቁርኣን ንባቦች ኢጃዛ", descriptionAr = "برنامج الجمع والإفراد للقراءات السبع من طريق الشاطبية (حرز الأماني ووجه التهاني)، تأهيل نوعي لنخبة القراء ونيل أعلى الأسانيد المتصلة بأئمة الإقراء.", descriptionEn = "Advanced recitation of the Seven Qira'at via Ash-Shatibiyyah for verified Quranic recitation masters.", instructorName = "المقرئ الجامع الشيخ حسن علي", instructorSanad = "إجازة في القراءات العشر الكبرى والصغرى", durationText = "24 شهر", totalHalaqat = 120, prerequisiteAr = "إجازة سابقة برواية حفص وإتقان حفظ الشاطبية", featuresAr = listOf("جمع القراءات بطريقة الإفراد والجمع", "تحريرات الشاطبية", "إجازة مسندة كبرى"), badgeTag = "عالي الإسناد (تخصص)", isOpenForRegistration = true, enrolledCount = 0)
        )
    )
    val programs: StateFlow<List<Program>> = _programs.asStateFlow()

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

    private val _bankAccounts = MutableStateFlow(
        listOf(
            BankAccount("cbe", "CBE", "البنك التجاري الإثيوبي (CBE)", "1000285374425"),
            BankAccount("boa", "BOA", "بنك الحبشة (BOA)", "226626646"),
            BankAccount("telebirr", "Telebirr", "تيليبير (Telebirr)", "0990161372"),
            BankAccount("mpesa", "M-Pesa", "إم-بيسا (M-Pesa Safaricom)", "0721009122")
        )
    )
    val bankAccounts: StateFlow<List<BankAccount>> = _bankAccounts.asStateFlow()

    private val _enrollmentRequests = MutableStateFlow<List<EnrollmentRequest>>(emptyList())
    val enrollmentRequests: StateFlow<List<EnrollmentRequest>> = _enrollmentRequests.asStateFlow()

    private val _paymentRequests = MutableStateFlow<List<PaymentRequest>>(emptyList())
    val paymentRequests: StateFlow<List<PaymentRequest>> = _paymentRequests.asStateFlow()

    private val _creditLedger = MutableStateFlow<List<MinuteLedgerEntry>>(emptyList())
    val creditLedger: StateFlow<List<MinuteLedgerEntry>> = _creditLedger.asStateFlow()

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _quranPosts = MutableStateFlow<List<QuranPost>>(emptyList())
    val quranPosts: StateFlow<List<QuranPost>> = _quranPosts.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _groups = MutableStateFlow(
        listOf(
            HalqahGroup(id = "grp_m_morning", nameAr = "حلقات الذكور - الفترة الصباحية (الفجر والضحى)", nameEn = "Brothers Halaqat - Morning Session", timeSlot = GroupTimeSlot.MORNING, gender = GenderCategory.BROTHERS, teacherName = "المشرف العام وهيئة الإقراء", currentSurah = "سورة البقرة وتصحيح التلاوة", membersCount = 18, isLiveNow = true, liveRoomId = "room-m-morning", telegramLink = null, description = "حلقات مخصصة للطلاب الذكور في الفترة الصباحية المباركة من بعد صلاة الفجر إلى الضحى لمدارسة القرآن الكريم والتلقين المباشر.", members = listOf(GroupMember("m1", "الشيخ د. إبراهيم أحمد", "رئيس هيئة الإقراء", true), GroupMember("m2", "المشرف العام (خالد أحمد)", "المشرف العام", true))),
            HalqahGroup(id = "grp_m_evening", nameAr = "حلقات الذكور - الفترة المسائية (العصر والمغرب)", nameEn = "Brothers Halaqat - Evening Session", timeSlot = GroupTimeSlot.EVENING, gender = GenderCategory.BROTHERS, teacherName = "مشايخ الإقراء المعتمدون", currentSurah = "سورة آل عمران والنساء", membersCount = 15, isLiveNow = false, liveRoomId = "room-m-evening", telegramLink = null, description = "حلقات مخصصة للطلاب الذكور في الفترة المسائية بين صلاتي العصر والمغرب للمراجعة وضبط الأداء.", members = listOf(GroupMember("m3", "الشيخ عبد الله محمد النور", "معلم مسند", true), GroupMember("m2", "المشرف العام (خالد أحمد)", "المشرف العام", true))),
            HalqahGroup(id = "grp_m_night", nameAr = "حلقات الذكور - الفترة الليلية (العشاء والسحر)", nameEn = "Brothers Halaqat - Night Session", timeSlot = GroupTimeSlot.NIGHT, gender = GenderCategory.BROTHERS, teacherName = "مشايخ الحفظ والإجازة بالسند", currentSurah = "عرض الختمات والمراجعة المكثفة", membersCount = 22, isLiveNow = true, liveRoomId = "room-m-night", telegramLink = null, description = "حلقات مخصصة للطلاب الذكور في الفترة الليلية بعد صلاة العشاء وفترة السحر لعرض الختمات ونيل الإجازة بالسند.", members = listOf(GroupMember("m4", "الشيخ الحافظ محمد أمين", "معلم إقراء", true), GroupMember("m2", "المشرف العام (خالد أحمد)", "المشرف العام", true))),
            HalqahGroup(id = "grp_f_morning", nameAr = "حلقات الإناث - الفترة الصباحية (الفجر والضحى)", nameEn = "Sisters Halaqat - Morning Session", timeSlot = GroupTimeSlot.MORNING, gender = GenderCategory.SISTERS, teacherName = "المعلمات المسندات الفضليات", currentSurah = "سورة البقرة والتلقين", membersCount = 16, isLiveNow = true, liveRoomId = "room-f-morning", telegramLink = null, description = "حلقات مخصصة للأخوات والطالبات في الفترة الصباحية لتصحيح التلاوة وضبط المخارج والصفات.", members = listOf(GroupMember("f1", "المعلمة أم عبد الله الحافظة", "معلمة مسندة", true), GroupMember("f2", "المشرفة التعليمية", "إشراف مقرأة الأخوات", true))),
            HalqahGroup(id = "grp_f_evening", nameAr = "حلقات الإناث - الفترة المسائية (العصر والمغرب)", nameEn = "Sisters Halaqat - Evening Session", timeSlot = GroupTimeSlot.EVENING, gender = GenderCategory.SISTERS, teacherName = "المعلمات المقرئات", currentSurah = "الحفظ والمراجعة", membersCount = 14, isLiveNow = false, liveRoomId = "room-f-evening", telegramLink = null, description = "حلقات مخصصة للأخوات والطالبات في الفترة المسائية بين صلاتي العصر والمغرب لمتابعة الحفظ وتثبيته.", members = listOf(GroupMember("f3", "المعلمة خديجة مسعود", "معلمة قرآن", true), GroupMember("f2", "المشرفة التعليمية", "إشراف مقرأة الأخوات", true))),
            HalqahGroup(id = "grp_f_night", nameAr = "حلقات الإناث - الفترة الليلية (العشاء والسحر)", nameEn = "Sisters Halaqat - Night Session", timeSlot = GroupTimeSlot.NIGHT, gender = GenderCategory.SISTERS, teacherName = "نخبة المعلمات المسندات", currentSurah = "عرض الختمات والإتقان", membersCount = 19, isLiveNow = true, liveRoomId = "room-f-night", telegramLink = null, description = "حلقات مخصصة للأخوات والطالبات في الفترة الليلية لعرض المتون والختمات القرآنية برواية حفص وغيرها.", members = listOf(GroupMember("f4", "المعلمة عائشة عمر", "معلمة إتقان", true), GroupMember("f2", "المشرفة التعليمية", "إشراف مقرأة الأخوات", true))),
            HalqahGroup(id = "grp_general", nameAr = "الملتقى القرآني العام (كافة الطلاب والمعلمين)", nameEn = "General Quranic Forum", timeSlot = GroupTimeSlot.MORNING, gender = GenderCategory.GENERAL, teacherName = "إدارة المقرأة وهيئة الإقراء", currentSurah = "مذاكرة أحكام التجويد والمتون والفوائد", membersCount = 45, isLiveNow = true, liveRoomId = "room-general", telegramLink = null, description = "الملتقى العام الجامع لكافة منتسبي مقرأة الإمام الجزري من طلاب ومعلمين وإداريين، مخصص للفوائد العلمية ومذاكرة متون التجويد.", members = listOf(GroupMember("m1", "الشيخ د. إبراهيم أحمد", "رئيس هيئة الإقراء", true), GroupMember("m2", "المشرف العام (خالد أحمد)", "المشرف العام", true), GroupMember("f1", "المعلمة أم عبد الله الحافظة", "معلمة مسندة", true)))
        )
    )
    val groups: StateFlow<List<HalqahGroup>> = _groups.asStateFlow()

    private val _students = MutableStateFlow<List<User>>(emptyList())
    val students: StateFlow<List<User>> = _students.asStateFlow()

    private val _conversations = MutableStateFlow(
        listOf(
            Conversation(id = "conv_admin", participantName = "الحساب الرسمي للمقرأة والإشراف", participantRole = "إدارة المقرأة", participantCode = "HQ-001", participantBio = "الحساب المعتمد لمقرأة الإمام الجزري لخدمة الطلاب ومتابعة التسجيل والدعم الفني", lastMessage = "السلام عليكم ورحمة الله، تم تفعيل اشتراكك وجدول حلقات التسميع", lastMessageTime = "10:45 ص", unreadCount = 1, isOfficial = true, enrolledPrograms = listOf("الإدارة العامة", "هيئة الإقراء")),
            Conversation(id = "conv_sheikh_ibrahim", participantName = "الشيخ د. إبراهيم أحمد", participantRole = "مقرئ مجاز - رئيس هيئة الإقراء", participantCode = "TCH-102", participantBio = "مسند في القراءات العشر الكبرى والصغرى ومشرف برنامج إجازة حفص", lastMessage = "السلام عليكم ورحمة الله، موعدنا في حلقة التسميع القادمة بإذن الله تعالى", lastMessageTime = "أمس", unreadCount = 0, isOfficial = false, enrolledPrograms = listOf("الإجازة برواية حفص عن عاصم"))
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

    // ==================== دوال المصادقة ====================

    fun completeOnboarding() {
        _isOnboardingCompleted.value = true
    }

    fun isAdminEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val clean = email.trim().lowercase()
        return clean == "meqraahaljezery@gmail.com" || clean == "khdahd241@gmail.com" || clean == "admin@aljazari.org"
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

        val dummyPassword = "123456"

        auth.signInWithEmailAndPassword(cleanEmail, dummyPassword)
            .addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    forceLoginAndSync(signInTask.result?.user?.uid, cleanEmail, displayName, role, phone, gender, targetProgram, isAdmin)
                } else {
                    auth.createUserWithEmailAndPassword(cleanEmail, dummyPassword)
                        .addOnCompleteListener { createTask ->
                            if (createTask.isSuccessful) {
                                forceLoginAndSync(createTask.result?.user?.uid, cleanEmail, displayName, role, phone, gender, targetProgram, isAdmin)
                            } else {
                                Log.e("FIREBASE_ERROR", "Auth failed: ${createTask.exception?.message}")
                                _isLoadingAuth.value = false 
                            }
                        }
                }
            }
    }

    private fun forceLoginAndSync(
        userId: String?,
        email: String,
        displayName: String,
        role: UserRole,
        phone: String,
        gender: String,
        targetProgram: String,
        isAdmin: Boolean
    ) {
        if (userId == null) {
            _isLoadingAuth.value = false
            return
        }

        val fallbackUser = User(
            id = userId,
            name = displayName,
            email = email,
            role = role,
            studentCode = if (isAdmin) "ADM-001" else "JZR-${(1000..9999).random()}",
            phone = if (phone.isNotBlank()) phone.trim() else (if (isAdmin) "+251 990 161 372" else "+251 912 000 000"),
            gender = gender,
            minuteBalance = 0,
            tajweedLevel = if (isAdmin) "مقرئ مسند بالقراءات العشر" else "طالب مسجل جديد",
            sanadTrack = if (isAdmin) "إشراف هيئة الإقراء والسند" else targetProgram,
            bio = if (isAdmin) "الحساب الإداري الرسمي لمقرأة الإمام الجزري" else "طالب علم في $targetProgram"
        )

        _currentUser.value = fallbackUser
        _isOnboardingCompleted.value = true
        _isLoadingAuth.value = false

        val userDocRef = db.collection("users").document(userId)
        userDocRef.get().addOnCompleteListener { docTask ->
            if (docTask.isSuccessful && docTask.result != null && docTask.result.exists()) {
                try {
                    val realUser = docTask.result.toObject(User::class.java)
                    _currentUser.value = realUser
                } catch (e: Exception) { 
                    Log.e("FIREBASE_ERROR", "Mapping error") 
                }
            } else {
                userDocRef.set(fallbackUser).addOnFailureListener { e ->
                    Log.e("FIREBASE_ERROR", "Set error: ${e.message}")
                }
            }
        }
    }

    fun signInWithGoogle(email: String, name: String) {
        registerOrSignInStudent(email, name)
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    fun updateProfile(name: String, bio: String, phone: String, city: String) {
        val current = _currentUser.value ?: return
        val updatedUser = current.copy(name = name, bio = bio, phone = phone, city = city)
        _currentUser.value = updatedUser
        db.collection("users").document(current.id).set(updatedUser)
    }

    // ... (باقي دوال الدفع، البث، والمحادثات كما هي، لا تحتاج تغيير) ...
}