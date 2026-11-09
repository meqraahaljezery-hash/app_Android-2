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

    // 4. Official Quran Programs (4 Core Programs)
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
            // هنا الباقة الذهبية: 200 دقيقة، السعر 1000 (يمكنك تغيير 200 إلى 1200 إذا كنت تقصد الدقائق)
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

    // 13. Groups (مفرغة لتكون حقيقية)
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

        val dummyPassword = "123456" // كلمة مرور وهمية لتسهيل الدخول عبر الواجهة الحالية

        auth.signInWithEmailAndPassword(cleanEmail, dummyPassword)
            .addOnFailureListener {
                // إنشاء حساب جديد إذا لم يكن موجوداً
                auth.createUserWithEmailAndPassword(cleanEmail, dummyPassword)
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid ?: UUID.randomUUID().toString()

                    // جلب بيانات المستخدم من قاعدة البيانات
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
                                // ⚠️ هنا الرصيد الافتراضي. يمكنك تغيير 1000 للمدير أو 60 للطالب
                                minuteBalance = if (isAdmin) 1000 else 60, 
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

    fun submitEnrollmentRequest(program: Program, age: Int, gender: String, city: String, phone: String, memorization: String, experience: String, timeSlot: String): Boolean {
        val user = _currentUser.value ?: return false
        val hasConflicting = _enrollmentRequests.value.any { it.studentId == user.id && it.programId == program.id && it.status != "REJECTED" }
        if (hasConflicting) return false

        val newRequest = EnrollmentRequest(id = "enr_${UUID.randomUUID().toString().take(6)}", studentId = user.id, studentName = user.name, studentCode = user.studentCode, programId = program.id, programTitle = program.titleAr, age = age, gender = gender, city = city, contactPhone = phone, currentMemorization = memorization, previousExperience = experience, preferredTimeSlot = timeSlot, status = "PENDING", submittedAt = dateFormat.format(Date()))
        _enrollmentRequests.value = listOf(newRequest) + _enrollmentRequests.value

        val notif = AppNotification(id = "notif_${UUID.randomUUID().toString().take(6)}", title = "تم استلام طلب التسجيل", message = "تم تسجيل طلب التحاقك ببرنامج (${program.titleAr}) بنجاح، وهو قيد المراجعة والاعتماد.", date = dateFormat.format(Date()), type = "ENROLLMENT", isRead = false)
        _notifications.value = listOf(notif) + _notifications.value
        return true
    }

    fun approveEnrollment(requestId: String) {
        val req = _enrollmentRequests.value.find { it.id == requestId }
        _enrollmentRequests.value = _enrollmentRequests.value.map { if (it.id == requestId) it.copy(status = "APPROVED") else it }
        if (req != null) {
            val notif = AppNotification(id = "notif_${UUID.randomUUID().toString().take(6)}", title = "تم قبول طلب التسجيل", message = "مبارك! تم اعتماد طلبك للالتحاق ببرنامج (${req.programTitle}).", date = dateFormat.format(Date()), type = "ENROLLMENT", isRead = false)
            _notifications.value = listOf(notif) + _notifications.value
        }
    }

    fun rejectEnrollment(requestId: String, reason: String) {
        _enrollmentRequests.value = _enrollmentRequests.value.map { if (it.id == requestId) it.copy(status = "REJECTED", rejectionReason = reason) else it }
    }

    fun submitPaymentRequest(pkg: MinutePackage, targetBank: String, senderName: String, senderPhone: String, transactionRef: String, receiptUri: String? = null): Boolean {
        val user = _currentUser.value ?: return false
        val newPayment = PaymentRequest(id = "pay_${UUID.randomUUID().toString().take(6)}", studentId = user.id, studentName = user.name, studentCode = user.studentCode, senderFullName = senderName, transactionRef = transactionRef, senderPhoneNumber = senderPhone, targetBank = targetBank, packageMinutes = pkg.minutes, amountPaidEtb = pkg.priceEtb, status = PaymentStatus.PENDING, submittedAt = dateFormat.format(Date()), receiptUri = receiptUri, isDownloaded = false, isArchived = false)
        _paymentRequests.value = listOf(newPayment) + _paymentRequests.value

        val notif = AppNotification(id = "notif_${UUID.randomUUID().toString().take(6)}", title = "تم إرسال إشعار شحن الرصيد", message = "تم تسجيل إشعار تحويل باقة ${pkg.titleAr} (${pkg.minutes} دقيقة) برقم عملية $transactionRef.", date = dateFormat.format(Date()), type = "RECHARGE", isRead = false)
        _notifications.value = listOf(notif) + _notifications.value
        return true
    }

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

        val ledgerEntry = MinuteLedgerEntry(id = "led_${UUID.randomUUID().toString().take(6)}", studentId = payment.studentId, studentName = payment.studentName, changeAmount = payment.packageMinutes, balanceBefore = before, balanceAfter = after, type = LedgerEntryType.DEPOSIT_PAYMENT, reason = "اعتماد شحن باقة ${payment.packageMinutes} دقيقة", timestamp = dateFormat.format(Date()), actorName = "المشرف العام")
        _creditLedger.value = listOf(ledgerEntry) + _creditLedger.value
    }

    fun rejectPayment(paymentId: String, reason: String) {
        _paymentRequests.value = _paymentRequests.value.map { if (it.id == paymentId) it.copy(status = PaymentStatus.REJECTED, rejectionReason = reason, reviewedByAdmin = "خالد أحمد مصطفى") else it }
    }

    fun markPaymentReceiptDownloaded(paymentId: String) {
        _paymentRequests.value = _paymentRequests.value.map { if (it.id == paymentId) it.copy(isDownloaded = true) else it }
    }

    fun deletePaymentRequest(paymentId: String) {
        _paymentRequests.value = _paymentRequests.value.filter { it.id != paymentId }
    }

    fun activateStudent(studentId: String) {
        _students.value = _students.value.map { if (it.id == studentId) it.copy(isSuspended = false) else it }
    }

    fun suspendStudent(studentId: String) {
        _students.value = _students.value.map { if (it.id == studentId) it.copy(isSuspended = true) else it }
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

        if (_currentUser.value?.id == studentId) {
            val updatedUser = _currentUser.value?.copy(minuteBalance = afterBal)
            _currentUser.value = updatedUser
            if (updatedUser != null) {
                db.collection("users").document(updatedUser.id).set(updatedUser)
            }
        }

        val ledgerEntry = MinuteLedgerEntry(id = "led_${UUID.randomUUID().toString().take(6)}", studentId = studentId, studentName = studentName, changeAmount = minutes, balanceBefore = beforeBal, balanceAfter = afterBal, type = LedgerEntryType.HONOR_AWARD, reason = "تكريم: $reason", timestamp = dateFormat.format(Date()), actorName = "المشرف العام")
        _creditLedger.value = listOf(ledgerEntry) + _creditLedger.value
    }

    fun sendStudentAlert(studentId: String, alertMessage: String) {
        val notif = AppNotification(id = "notif_${UUID.randomUUID().toString().take(6)}", title = "تنبيه إداري", message = alertMessage, date = dateFormat.format(Date()), type = "SYSTEM", isRead = false)
        _notifications.value = listOf(notif) + _notifications.value
    }

    fun deductQuranSession(studentId: String, minutes: Int, recitationSpan: String, tajweedNotes: String): Boolean {
        val student = _students.value.find { it.id == studentId } ?: return false
        if (student.minuteBalance < minutes) return false

        val before = student.minuteBalance
        val after = before - minutes

        _students.value = _students.value.map { if (it.id == studentId) it.copy(minuteBalance = after) else it }
        if (_currentUser.value?.id == studentId) {
            val updatedUser = _currentUser.value?.copy(minuteBalance = after)
            _currentUser.value = updatedUser
            if (updatedUser != null) {
                db.collection("users").document(updatedUser.id).set(updatedUser)
            }
        }

        val ledgerEntry = MinuteLedgerEntry(id = "led_${UUID.randomUUID().toString().take(6)}", studentId = studentId, studentName = student.name, changeAmount = -minutes, balanceBefore = before, balanceAfter = after, type = LedgerEntryType.DEDUCTION_SESSION, reason = "جلسة تسميع: $recitationSpan", timestamp = dateFormat.format(Date()), actorName = "المقرئ")
        _creditLedger.value = listOf(ledgerEntry) + _creditLedger.value

        val notif = AppNotification(id = "notif_${UUID.randomUUID().toString().take(6)}", title = "خصم جلسة تسميع", message = "تم خصم $minutes دقيقة. رصيدك: $after.", date = dateFormat.format(Date()), type = "SESSION", isRead = false)
        _notifications.value = listOf(notif) + _notifications.value
        return true
    }

    fun markAllNotificationsAsRead() { _notifications.value = _notifications.value.map { it.copy(isRead = true) } }
    fun markNotificationAsRead(id: String) { _notifications.value = _notifications.value.map { if (it.id == id) it.copy(isRead = true) else it } }

    fun addAnnouncement(title: String, content: String, isPinned: Boolean) {
        val newAnc = Announcement(id = "anc_${UUID.randomUUID().toString().take(6)}", title = title, content = content, date = dateFormat.format(Date()).substringBefore(" "), isPinned = isPinned, author = "المشرف العام")
        _announcements.value = listOf(newAnc) + _announcements.value
    }

    fun publishQuranPost(content: String, authorRole: String = "إدارة المقرأة") {
        val newPost = QuranPost(id = "post_${UUID.randomUUID().toString().take(6)}", authorName = "مقرأة الإمام الجزري", authorRole = authorRole, content = content, date = "الآن", likesCount = 0, commentsCount = 0, isLikedByMe = false)
        _quranPosts.value = listOf(newPost) + _quranPosts.value
    }

    fun manualAdjustMinutes(studentName: String, amount: Int, reason: String) {
        val user = _currentUser.value ?: return
        val before = user.minuteBalance
        val after = (before + amount).coerceAtLeast(0)
        val updatedUser = user.copy(minuteBalance = after)
        _currentUser.value = updatedUser
        db.collection("users").document(user.id).set(updatedUser)

        val entry = MinuteLedgerEntry(id = "led_${UUID.randomUUID().toString().take(6)}", studentId = user.id, studentName = studentName, changeAmount = amount, balanceBefore = before, balanceAfter = after, type = LedgerEntryType.MANUAL_ADJUSTMENT_ADMIN, reason = reason, timestamp = dateFormat.format(Date()), actorName = "المشرف العام")
        _creditLedger.value = listOf(entry) + _creditLedger.value
    }

    fun toggleLikePost(postId: String) {
        _quranPosts.value = _quranPosts.value.map {
            if (it.id == postId) { val newLiked = !it.isLikedByMe; val newCount = if (newLiked) it.likesCount + 1 else it.likesCount - 1; it.copy(isLikedByMe = newLiked, likesCount = newCount) } else it
        }
    }

    fun sendChatMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        val user = _currentUser.value ?: return
        val newMsg = ChatMessage(id = "msg_${UUID.randomUUID()}", conversationId = conversationId, senderId = user.id, senderName = user.name, senderRole = user.role, content = text, type = MessageType.TEXT, timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), isMine = true)
        val currentList = _messages.value[conversationId] ?: emptyList()
        _messages.value = _messages.value + (conversationId to (currentList + newMsg))
        _conversations.value = _conversations.value.map { if (it.id == conversationId) it.copy(lastMessage = text, lastMessageTime = "الآن") else it }
    }

    fun updateGroupTelegramLink(groupId: String, link: String) {
        _groups.value = _groups.value.map { g -> if (g.id == groupId) g.copy(telegramLink = link.trim().ifBlank { null }) else g }
    }

    fun createGroup(nameAr: String, nameEn: String, gender: GenderCategory, timeSlot: GroupTimeSlot, teacherName: String, currentSurah: String, telegramLink: String?, description: String) {
        val newId = "grp_custom_${System.currentTimeMillis()}"
        val newGroup = HalqahGroup(id = newId, nameAr = nameAr, nameEn = nameEn.ifBlank { nameAr }, timeSlot = timeSlot, gender = gender, teacherName = teacherName.ifBlank { "إدارة المقرأة" }, currentSurah = currentSurah.ifBlank { "القرآن الكريم" }, membersCount = 1, isLiveNow = false, liveRoomId = "room_$newId", telegramLink = telegramLink?.trim()?.ifBlank { null }, description = description, members = listOf(GroupMember("admin", _currentUser.value?.name ?: "المشرف العام", "المشرف العام", true)))
        _groups.value = _groups.value + newGroup
    }

    fun sendGroupChatMessage(groupId: String, text: String) {
        if (text.isBlank()) return
        val user = _currentUser.value ?: return
        val newMsg = ChatMessage(id = "grp_msg_${UUID.randomUUID()}", conversationId = groupId, senderId = user.id, senderName = user.name, senderRole = user.role, content = text.trim(), type = MessageType.TEXT, timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), isMine = true)
        val currentList = _messages.value[groupId] ?: emptyList()
        _messages.value = _messages.value + (groupId to (currentList + newMsg))
    }

    private val activeSessionJobMap = mutableMapOf<String, Job>()

    private fun sendSystemGroupMessage(groupId: String, text: String) {
        val newMsg = ChatMessage(id = "sys_msg_${UUID.randomUUID()}", conversationId = groupId, senderId = "system_jazari", senderName = "إدارة المقرأة (البث المباشر)", senderRole = UserRole.ADMIN, content = text, type = MessageType.TEXT, timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), isMine = false)
        val currentList = _messages.value[groupId] ?: emptyList()
        _messages.value = _messages.value + (groupId to (currentList + newMsg))
    }

    fun startGroupBroadcast(groupId: String, context: Context?) {
        val user = _currentUser.value ?: return
        val isAdmin = isAdminEmail(user.email) || user.role == UserRole.ADMIN
        if (!isAdmin) return

        val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        var targetGroupName = "حلقة التسميع"
        var teacherName = user.name

        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) { targetGroupName = g.nameAr; teacherName = g.teacherName; g.copy(isLiveNow = true, liveStartedAt = nowTime) } else g
        }

        if (context != null) {
            SmartNotificationManager.notifyHalaqahLive(context, targetGroupName, teacherName)
            LiveKitManager.connectToRoom(context = context, roomId = "room_$groupId", participantId = user.id, participantName = user.name, isTeacher = true)
        }
        sendSystemGroupMessage(groupId, "🔴 بدأ المشرف العام البث المباشر لحلقة ($targetGroupName) الآن.")
    }

    fun endGroupBroadcast(groupId: String, context: Context?) {
        val user = _currentUser.value ?: return
        val isAdmin = isAdminEmail(user.email) || user.role == UserRole.ADMIN
        if (!isAdmin) return

        val currentGroup = _groups.value.find { it.id == groupId }
        if (currentGroup?.activeSession != null) { endStudentSessionAndDeduct(groupId, "إنهاء تلقائي مع إغلاق البث", context) }

        activeSessionJobMap[groupId]?.cancel()
        activeSessionJobMap.remove(groupId)
        _groups.value = _groups.value.map { g -> if (g.id == groupId) g.copy(isLiveNow = false, liveStartedAt = null, activeReciterName = null, activeReciterCode = null, activeSession = null) else g }
        LiveKitManager.disconnect()
        sendSystemGroupMessage(groupId, "⏹️ تم إنهاء جلسة البث المباشر للحلقة.")
    }

    fun joinRecitationQueue(groupId: String, targetSurah: String = "تسميع المقرر") {
        val user = _currentUser.value ?: return
        val grp = _groups.value.find { it.id == groupId } ?: return
        if (grp.queue.any { it.studentId == user.id }) return

        val nextNumber = (grp.queue.maxOfOrNull { it.queueNumber } ?: 0) + 1
        val queueItem = RecitationQueueItem(id = "q_${UUID.randomUUID().toString().take(6)}", studentId = user.id, studentName = user.name, studentCode = user.studentCode, studentGender = user.gender, targetSurah = targetSurah.ifBlank { "تسميع المقرر" }, joinedAt = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), queueNumber = nextNumber, isAdmitted = false)
        _groups.value = _groups.value.map { g -> if (g.id == groupId) g.copy(queue = g.queue + queueItem) else g }
        sendSystemGroupMessage(groupId, "📋 حجز الطالب (${user.name}) دوره في طابور التسميع رقم #$nextNumber.")
    }

    fun leaveRecitationQueue(groupId: String) {
        val user = _currentUser.value ?: return
        _groups.value = _groups.value.map { g -> if (g.id == groupId) g.copy(queue = g.queue.filterNot { it.studentId == user.id }) else g }
    }

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

        if (grp.activeSession != null) { endStudentSessionAndDeduct(groupId, "إنهاء تلقائي لبدء طالب جديد", context) }

        val session = ActiveRecitationSession(sessionId = "ses_${UUID.randomUUID().toString().take(6)}", groupId = groupId, studentId = studentId, studentName = studentName, studentCode = studentCode, teacherName = user.name, startedAtTimestamp = System.currentTimeMillis(), elapsedSeconds = 0, surahName = surah, isRunning = true, liveKitRoomName = "room_$groupId")
        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) {
                val updatedQueue = g.queue.map { q -> if (q.studentId == studentId) q.copy(isAdmitted = true) else q }
                g.copy(activeSession = session, activeReciterName = studentName, activeReciterCode = studentCode, queue = updatedQueue)
            } else g
        }

        activeSessionJobMap[groupId]?.cancel()
        activeSessionJobMap[groupId] = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val currentG = _groups.value.find { it.id == groupId }
                val currentS = currentG?.activeSession ?: break
                if (currentS.isRunning) {
                    val updatedS = currentS.copy(elapsedSeconds = currentS.elapsedSeconds + 1)
                    _groups.value = _groups.value.map { g -> if (g.id == groupId) g.copy(activeSession = updatedS) else g }
                }
            }
        }
        LiveKitManager.setStudentParticipant(studentId, studentName)
        sendSystemGroupMessage(groupId, "🎙️ دخل الطالب ($studentName) إلى البث المباشر مع المعلم.")
    }

    fun toggleSessionTimer(groupId: String) {
        _groups.value = _groups.value.map { g -> if (g.id == groupId && g.activeSession != null) { val updated = g.activeSession.copy(isRunning = !g.activeSession.isRunning); g.copy(activeSession = updated) } else g }
    }

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
        val student = _students.value.find { it.id == session.studentId }
        val oldBalance = student?.minuteBalance ?: _currentUser.value?.minuteBalance ?: 60
        val newBalance = maxOf(0, oldBalance - exactDeductedMinutes)

        if (student != null) {
            val updatedStudent = student.copy(minuteBalance = newBalance)
            _students.value = _students.value.map { if (it.id == student.id) updatedStudent else it }
        }
        if (_currentUser.value?.id == session.studentId) {
            val updatedUser = _currentUser.value?.copy(minuteBalance = newBalance)
            _currentUser.value = updatedUser
            if (updatedUser != null) { db.collection("users").document(updatedUser.id).set(updatedUser) }
        }

        val ledger = MinuteLedgerEntry(id = "led_${UUID.randomUUID().toString().take(6)}", studentId = session.studentId, studentName = session.studentName, changeAmount = -exactDeductedMinutes, balanceBefore = oldBalance, balanceAfter = newBalance, type = LedgerEntryType.DEDUCTION_SESSION, reason = "خصم تلقائي: جلسة تسميع مشافهة (${durationSeconds} ثانية)", timestamp = dateFormat.format(Date()), actorName = user.name)
        _creditLedger.value = listOf(ledger) + _creditLedger.value
        _groups.value = _groups.value.map { g -> if (g.id == groupId) g.copy(activeSession = null, activeReciterName = null, activeReciterCode = null, queue = grp.queue.filterNot { it.studentId == session.studentId }) else g }
        LiveKitManager.clearStudentParticipant()
        sendSystemGroupMessage(groupId, "✅ تم إنهاء جلسة التسميع للطالب (${session.studentName}).")
    }
}