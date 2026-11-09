package com.example.viewmodel

import android.content.Context
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
    
    // متغيرات Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // 1. حالة التحميل (لمنع ظهور شاشة الدخول لثانية)
    private val _isLoadingAuth = MutableStateFlow(true)
    val isLoadingAuth: StateFlow<Boolean> = _isLoadingAuth.asStateFlow()

    // 2. Onboarding State
    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    // 3. Current User (Null until real sign in)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // الاستماع لحالة تسجيل الدخول في Firebase وجلب بيانات المستخدم تلقائياً
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                db.collection("users").document(firebaseUser.uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            _currentUser.value = document.toObject(User::class.java)
                        }
                        _isLoadingAuth.value = false
                    }
                    .addOnFailureListener {
                        _isLoadingAuth.value = false
                    }
            } else {
                _currentUser.value = null
                _isLoadingAuth.value = false
            }
        }
    }

    // 4. Official Quran Programs
    private val _programs = MutableStateFlow(
        listOf(
            Program(id = "prog_talqin", titleAr = "التلقين وتصحيح الأداء", titleEn = "Talqin & Recitation Correction", titleAm = "የቁርኣን ንባብ እና አነባበብ ማስተካከያ", descriptionAr = "حلقات يومية مباشرة تركز على تصحيح مخارج الحروف، وضبط أزمنة الغنن والمدود، والمشافهة المباشرة مع المشايخ المسندين لتأسيس القراءة السليمة الخالية من اللحن.", descriptionEn = "Daily live halaqat focusing on articulation points, Tajweed precision, and oral transmission with certified scholars.", instructorName = "الشيخ عبد الله محمد النور", instructorSanad = "مسند في القراءات العشر ومتون التجويد", durationText = "3 أشهر (24 حلقة تفاعلية)", totalHalaqat = 24, prerequisiteAr = "الرغبة في تصحيح التلاوة ومعرفة القراءة الأساسية", featuresAr = listOf("تسميع فردي وتصحيح مباشر", "متابعة أسبوعية في المتون", "شهادة إتمام مستوى"), badgeTag = "تأسيسي وإتقان", isOpenForRegistration = true, enrolledCount = 0),
            Program(id = "prog_hifz", titleAr = "الحفظ المكثف وتثبيت المحفوظ", titleEn = "Intensive Memorization & Revision", titleAm = "የተጠናከረ የቁርኣን ሂፍዝ እና ክለሳ", descriptionAr = "منهج مكثف ومدروس لحفظ القرآن الكريم كاملاً مع المراجعة التراكمية اليومية ونظام استظهار صارم يضمن رسوخ الآيات كالفاتحة بإشراف حفاظ متقنين.", descriptionEn = "Intensive curriculum for full Quran memorization with cumulative daily revision under master teachers.", instructorName = "الشيخ الحافظ محمد أمين", instructorSanad = "حفظ متقن بالسند المتصل", durationText = "12 - 18 شهر", totalHalaqat = 180, prerequisiteAr = "الجدية والالتزام بساعات التسميع اليومية", featuresAr = listOf("خطة حفظ يومية محكمة", "مراجعة 5 أجزاء أسبوعياً", "اختبارات إتقان دورية"), badgeTag = "حفظ وإتقان", isOpenForRegistration = true, enrolledCount = 0),
            Program(id = "prog_hafs", titleAr = "الإجازة برواية حفص عن عاصم", titleEn = "Hafs An Asim Ijazah with Sanad", titleAm = "የሃፍስ ከዓስም የሰነድ ኢጃዛ ፕሮግرام", descriptionAr = "عرض الختمة كاملة غيباً من الفاتحة إلى الناس برواية حفص عن عاصم من طريق الشاطبية، مع تطبيق كافة دقائق التجويد للحصول على الإجازة المعتمدة بالسند المتصل إلى رسول الله ﷺ.", descriptionEn = "Full memorized recitation from Al-Fatihah to An-Nas via Shatibiyyah for connected Sanad Ijazah to the Prophet ﷺ.", instructorName = "فضيلة الشيخ د. إبراهيم أحمد الجزري", instructorSanad = "سند عالي متصل إلى الإمام عاصم والإمام ابن الجزري", durationText = "6 - 12 شهر", totalHalaqat = 60, prerequisiteAr = "حفظ القرآن الكريم كاملاً وإتقان متن الجزرية", featuresAr = listOf("سند متصل معتمد", "مجلس ختم رسمي", "وثيقة إجازة مصدقة"), badgeTag = "إجازة بالسند المتصل", isOpenForRegistration = true, enrolledCount = 0),
            Program(id = "prog_qiraat", titleAr = "الإجازة بالقراءات السبع المتواترة", titleEn = "Seven Mutawatir Qira'at Ijazah", titleAm = "የሰባቱ ተከታታይ የቁርኣን ንባቦች ኢጃዛ", descriptionAr = "برنامج الجمع والإفراد للقراءات السبع من طريق الشاطبية (حرز الأماني ووجه التهاني)، تأهيل نوعي لنخبة القراء ونيل أعلى الأسانيد المتصلة بأئمة الإقراء.", descriptionEn = "Advanced recitation of the Seven Qira'at via Ash-Shatibiyyah for verified Quranic recitation masters.", instructorName = "المقرئ الجامع الشيخ حسن علي", instructorSanad = "إجازة في القراءات العشر الكبرى والصغرى", durationText = "24 شهر", totalHalaqat = 120, prerequisiteAr = "إجازة سابقة برواية حفص وإتقان حفظ الشاطبية", featuresAr = listOf("جمع القراءات بطريقة الإفراد والجمع", "تحريرات الشاطبية", "إجازة مسندة كبرى"), badgeTag = "عالي الإسناد (تخصص)", isOpenForRegistration = true, enrolledCount = 0)
        )
    )
    val programs: StateFlow<List<Program>> = _programs.asStateFlow()

    // 5. Enrollment Requests
    private val _enrollmentRequests = MutableStateFlow<List<EnrollmentRequest>>(emptyList())
    val enrollmentRequests: StateFlow<List<EnrollmentRequest>> = _enrollmentRequests.asStateFlow()

    // 6. Official Packages
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

    // 7. Bank Accounts
    private val _bankAccounts = MutableStateFlow(
        listOf(
            BankAccount("cbe", "CBE", "البنك التجاري الإثيوبي (CBE)", "1000285374425"),
            BankAccount("boa", "BOA", "بنك الحبشة (BOA)", "226626646"),
            BankAccount("telebirr", "Telebirr", "تيليبير (Telebirr)", "0990161372"),
            BankAccount("mpesa", "M-Pesa", "إم-بيسا (M-Pesa Safaricom)", "0721009122")
        )
    )
    val bankAccounts: StateFlow<List<BankAccount>> = _bankAccounts.asStateFlow()

    // 8. Payment Requests
    private val _paymentRequests = MutableStateFlow<List<PaymentRequest>>(emptyList())
    val paymentRequests: StateFlow<List<PaymentRequest>> = _paymentRequests.asStateFlow()

    // 9. Credit Ledger
    private val _creditLedger = MutableStateFlow<List<MinuteLedgerEntry>>(emptyList())
    val creditLedger: StateFlow<List<MinuteLedgerEntry>> = _creditLedger.asStateFlow()

    // 10. Announcements
    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    // 11. Quran Posts
    private val _quranPosts = MutableStateFlow<List<QuranPost>>(emptyList())
    val quranPosts: StateFlow<List<QuranPost>> = _quranPosts.asStateFlow()

    // 12. Notifications
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // 13. Groups (تم إبقاء القائمة الفارغة هنا لأننا سنعتمد على Firebase لاحقاً، لكن يمكنك ترك القائمة القديمة إن أردت)
    private val _groups = MutableStateFlow<List<HalqahGroup>>(emptyList())
    val groups: StateFlow<List<HalqahGroup>> = _groups.asStateFlow()

    // 14. Managed Students Directory
    private val _students = MutableStateFlow<List<User>>(emptyList())
    val students: StateFlow<List<User>> = _students.asStateFlow()

    // 15. Conversations & Messages
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    // --- ACTIONS ---

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

        // كلمة مرور افتراضية (لأن الواجهة الحالية لا تطلب كلمة مرور)
        val dummyPassword = "123456"

        auth.signInWithEmailAndPassword(cleanEmail, dummyPassword)
            .addOnFailureListener {
                // إذا لم يكن الحساب موجوداً، نقوم بإنشائه
                auth.createUserWithEmailAndPassword(cleanEmail, dummyPassword)
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid ?: UUID.randomUUID().toString()

                    // جلب بيانات المستخدم إذا كان موجوداً مسبقاً في قاعدة البيانات، أو إنشاء جديد
                    db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            _currentUser.value = doc.toObject(User::class.java)
                            _isOnboardingCompleted.value = true
                            _isLoadingAuth.value = false
                        } else {
                            val user = User(
                                id = userId,
                                name = displayName,
                                email = cleanEmail,
                                role = role,
                                studentCode = if (isAdmin) "ADM-001" else "JZR-${(1000..9999).random()}",
                                phone = if (phone.isNotBlank()) phone.trim() else (if (isAdmin) "+251 990 161 372" else "+251 912 000 000"),
                                gender = gender,
                                minuteBalance = if (isAdmin) 1000 else 0, // الرصيد يبدأ بصفر للطالب
                                tajweedLevel = if (isAdmin) "مقرئ مسند بالقراءات العشر" else "طالب مسجل جديد",
                                sanadTrack = if (isAdmin) "إشراف هيئة الإقراء والسند" else targetProgram,
                                bio = if (isAdmin) "الحساب الإداري الرسمي لمقرأة الإمام الجزري للإقراء والإجازة بالسند المتصل" else "طالب علم في $targetProgram"
                            )
                            db.collection("users").document(userId).set(user).addOnSuccessListener {
                                _currentUser.value = user
                                _isOnboardingCompleted.value = true
                                _isLoadingAuth.value = false
                            }
                        }
                    }
                }
            }
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

    // --- العمليات المالية (Payment & Ledger) ---
    fun approvePayment(paymentId: String) {
        val payment = _paymentRequests.value.find { it.id == paymentId } ?: return
        if (payment.status == PaymentStatus.APPROVED) return

        _paymentRequests.value = _paymentRequests.value.map {
            if (it.id == paymentId) it.copy(status = PaymentStatus.APPROVED, reviewedByAdmin = "خالد أحمد مصطفى") else it
        }

        val user = _currentUser.value
        val before = user?.minuteBalance ?: 0
        val after = before + payment.packageMinutes

        if (user != null && user.id == payment.studentId) {
            val updatedUser = user.copy(minuteBalance = after)
            _currentUser.value = updatedUser
            db.collection("users").document(user.id).set(updatedUser)
        }

        val ledgerEntry = MinuteLedgerEntry(
            id = "led_${UUID.randomUUID().toString().take(6)}", studentId = payment.studentId, studentName = payment.studentName,
            changeAmount = payment.packageMinutes, balanceBefore = before, balanceAfter = after,
            type = LedgerEntryType.DEPOSIT_PAYMENT, reason = "اعتماد شحن باقة ${payment.packageMinutes} دقيقة",
            timestamp = dateFormat.format(Date()), actorName = "المشرف العام"
        )
        _creditLedger.value = listOf(ledgerEntry) + _creditLedger.value
    }

    // (باقي الدوال مثل البث والمحادثات سأضعها هنا بشكل مختصر لضمان عمل الملف)
    fun sendChatMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        val user = _currentUser.value ?: return
        val newMsg = ChatMessage(id = "msg_${UUID.randomUUID()}", conversationId = conversationId, senderId = user.id, senderName = user.name, senderRole = user.role, content = text, type = MessageType.TEXT, timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), isMine = true)
        val currentList = _messages.value[conversationId] ?: emptyList()
        _messages.value = _messages.value + (conversationId to (currentList + newMsg))
    }

    fun sendGroupChatMessage(groupId: String, text: String) {
        if (text.isBlank()) return
        val user = _currentUser.value ?: return
        val newMsg = ChatMessage(id = "grp_msg_${UUID.randomUUID()}", conversationId = groupId, senderId = user.id, senderName = user.name, senderRole = user.role, content = text.trim(), type = MessageType.TEXT, timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), isMine = true)
        val currentList = _messages.value[groupId] ?: emptyList()
        _messages.value = _messages.value + (groupId to (currentList + newMsg))
    }

    // لضمان عمل التطبيق دون أخطاء في الأجزاء الأخرى التي لم أذكرها، يرجى إبقاء باقي الدوال القديمة (LiveKit، الطابور، إلخ) كما هي في ملفك الأصلي.
    // لقد قمت فقط باستبدال نظام المصادقة والرصيد.
}