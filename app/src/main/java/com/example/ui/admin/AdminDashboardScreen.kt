package com.example.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.PaymentStatus
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.i18n.AppLanguage
import com.example.util.ReceiptStorageUtil
import com.example.viewmodel.MaqraahViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    currentLanguage: AppLanguage,
    maqraahViewModel: MaqraahViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by maqraahViewModel.currentUser.collectAsState()
    val paymentRequests by maqraahViewModel.paymentRequests.collectAsState()
    val enrollmentRequests by maqraahViewModel.enrollmentRequests.collectAsState()
    val creditLedger by maqraahViewModel.creditLedger.collectAsState()
    val announcements by maqraahViewModel.announcements.collectAsState()
    val posts by maqraahViewModel.quranPosts.collectAsState()
    val students by maqraahViewModel.students.collectAsState()

    val isAdmin = maqraahViewModel.isAdminEmail(currentUser?.email) || currentUser?.role == UserRole.ADMIN

    // Access Control Guard
    if (!isAdmin) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("صلاحية مقيدة", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "ليس لديك صلاحية الوصول إلى هذه الصفحة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "لوحة التحكم مخصصة فقط للمشرف العام على المقرأة والحسابات الإدارية المعتمدة (meqraahaljezery@gmail.com).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("العودة إلى الصفحة الرئيسية")
                        }
                    }
                }
            }
        }
        return
    }

    var selectedTab by remember { mutableStateOf(0) }
    // 0: ملخص, 1: إدارة الطلاب, 2: خصم الجلسات, 3: طلبات الشحن, 4: طلبات البرامج, 5: نشر إعلانات وحالات

    // Student Management States
    var managingStudent by remember { mutableStateOf<User?>(null) }
    var showAwardDialog by remember { mutableStateOf(false) }
    var awardMinutesInput by remember { mutableStateOf("60") }
    var awardReasonInput by remember { mutableStateOf("مكافأة اجتهاد في الحفظ") }

    var showAlertSendDialog by remember { mutableStateOf(false) }
    var alertMessageInput by remember { mutableStateOf("") }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Session Deduction States
    var deductionGenderFilter by remember { mutableStateOf("الكل") } // الكل / ذكور / إناث
    var deductionSlotFilter by remember { mutableStateOf("الكل") } // الكل / الفجر / الضحى / العصر / المغرب / العشاء
    var deductingStudent by remember { mutableStateOf<User?>(null) }
    var deductionMinutes by remember { mutableStateOf(30) }
    var customDeductionMinutesText by remember { mutableStateOf("30") }
    var recitationSpanInput by remember { mutableStateOf("سورة البقرة من آية 1 إلى 25") }
    var tajweedNotesInput by remember { mutableStateOf("إتقان طيب لأحكام النون الساكنة والمدود") }

    // Announcement and Status Publishing States
    var showNewAnnouncementDialog by remember { mutableStateOf(false) }
    var newAncTitle by remember { mutableStateOf("") }
    var newAncContent by remember { mutableStateOf("") }
    var newAncPinned by remember { mutableStateOf(false) }

    var showNewPostDialog by remember { mutableStateOf(false) }
    var newPostContent by remember { mutableStateOf("") }
    var newPostRole by remember { mutableStateOf("إدارة المقرأة وهيئة الإقراء") }

    // Rejection reason dialog
    var rejectingPaymentId by remember { mutableStateOf<String?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }
    var viewingReceiptUri by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "لوحة تحكم المشرف العام", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        com.example.util.SmartNotificationManager.sendTestNotification(context, "تنبيه إداري من المشرف العام")
                        Toast.makeText(context, "تم إرسال إشعار فوري لشريط إشعارات الجوال 🔔", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "إشعار تجريبي للجوال", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("الملخص") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("إدارة الطلاب (${students.size})") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("خصم الجلسة القرآنية") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("طلبات الشحن (${paymentRequests.count { it.status == PaymentStatus.PENDING }})") })
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("طلبات البرامج (${enrollmentRequests.count { it.status == "PENDING" }})") })
                Tab(selected = selectedTab == 5, onClick = { selectedTab = 5 }, text = { Text("نشر إعلانات وحالات") })
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Summary & Statistics Cards
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "الطلاب المسجلين",
                                    value = "${students.size} طالب وطالبة",
                                    icon = Icons.Default.People
                                )
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "الحلقات النشطة",
                                    value = "6 حلقات",
                                    icon = Icons.Default.Groups
                                )
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "طلبات الشحن المعلقة",
                                    value = "${paymentRequests.count { it.status == PaymentStatus.PENDING }} طلب",
                                    icon = Icons.Default.Payment
                                )
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "طلبات التسجيل",
                                    value = "${enrollmentRequests.count { it.status == "PENDING" }} طلب",
                                    icon = Icons.Default.School
                                )
                            }
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "المشرف العام المفوض: أ. خالد أحمد مصطفى",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "الحساب المعتمد: meqraahaljezery@gmail.com • الهاتف: +251 990 161 372",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "النظام الإداري والمالي متصل بسجل تدقيق مالي غير قابل للتعديل (Ledger) لإثبات الخصومات وعمليات الشحن.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // 1. STUDENT MANAGEMENT DIRECTORY
                        item {
                            Text(
                                text = "قائمة الطلاب المسجلين (انقر على حساب الطالب للإدارة):",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        items(students) { student ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { managingStudent = student },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (student.isSuspended) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = student.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFFDDF2E3),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = student.studentCode,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF105C3B),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Surface(
                                            color = if (student.isSuspended) Color(0xFFEF4444) else Color(0xFF18794E),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (student.isSuspended) "حساب مجمّد" else "حساب نشط",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "الهاتف: ${student.phone} • المدينة: ${student.city} • الجنس: ${student.gender}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "المسار: ${student.sanadTrack}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "الرصيد: ${student.minuteBalance} دقيقة",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // 2. QURAN SESSION DEDUCTION SECTION WITH GENDER & TIME FILTERS
                        item {
                            Text(
                                text = "تسجيل جلسة التسميع وخصم الدقائق التلقائي:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        // Filter by Gender
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "تصفية حسب الجنس:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("الكل", "ذكر", "أنثى").forEach { g ->
                                        FilterChip(
                                            selected = deductionGenderFilter == g,
                                            onClick = { deductionGenderFilter = g },
                                            label = { Text(if (g == "الكل") "الكل (ذكور وإناث)" else if (g == "ذكر") "الذكور فقط" else "الإناث فقط") }
                                        )
                                    }
                                }
                            }
                        }

                        // Filter by Time Slot
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "تصفية حسب فترة الحلقة:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("الكل", "الفجر", "الضحى", "العصر", "المغرب", "العشاء").forEach { slot ->
                                        FilterChip(
                                            selected = deductionSlotFilter == slot,
                                            onClick = { deductionSlotFilter = slot },
                                            label = { Text(slot, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }
                            }
                        }

                        // Filtered Students List
                        val filteredStudents = students.filter { student ->
                            (deductionGenderFilter == "الكل" || student.gender == deductionGenderFilter) &&
                                    (deductionSlotFilter == "الكل" || student.timeSlot == deductionSlotFilter)
                        }

                        item {
                            Text(
                                text = "الطلاب المطابقون للتصفية (${filteredStudents.size}) - اختر طالباً لخصم الجلسة:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(filteredStudents) { student ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        deductingStudent = student
                                        customDeductionMinutesText = "30"
                                        deductionMinutes = 30
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = student.name, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "(${student.studentCode})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Text(text = "الفترة: ${student.timeSlot} • الجنس: ${student.gender}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "الرصيد المتبقي: ${student.minuteBalance} دقيقة", fontWeight = FontWeight.SemiBold, color = if (student.minuteBalance < 20) Color(0xFFEF4444) else Color(0xFF18794E))
                                    }

                                    Button(
                                        onClick = {
                                            deductingStudent = student
                                            customDeductionMinutesText = "30"
                                            deductionMinutes = 30
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("خصم الجلسة")
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // 3. PAYMENT REQUESTS LIST
                        item {
                            Text(text = "مراجعة واعتماد إشعارات التحويل البنكي الواردة:", fontWeight = FontWeight.Bold)
                        }

                        if (paymentRequests.isEmpty()) {
                            item {
                                Text(text = "لا توجد طلبات شحن حالياً.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        items(paymentRequests) { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = req.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Surface(
                                            color = when (req.status) {
                                                PaymentStatus.APPROVED -> Color(0xFFDDF2E3)
                                                PaymentStatus.REJECTED -> Color(0xFFFEE2E2)
                                                else -> MaterialTheme.colorScheme.secondaryContainer
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = when (req.status) {
                                                    PaymentStatus.APPROVED -> "معتمد ومضاف"
                                                    PaymentStatus.REJECTED -> "مرفوض"
                                                    else -> "قيد المراجعة"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = when (req.status) {
                                                    PaymentStatus.APPROVED -> Color(0xFF105C3B)
                                                    PaymentStatus.REJECTED -> Color(0xFFEF4444)
                                                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Text(text = "البنك: ${req.targetBank} • المبلغ: ${req.amountPaidEtb} ETB • الدقائق: +${req.packageMinutes} دقيقة")
                                    Text(text = "المحوّل: ${req.senderFullName} (هاتف: ${req.senderPhoneNumber})")
                                    Text(text = "رقم العملية / المرجع: ${req.transactionRef}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(text = "تاريخ الإرسال: ${req.submittedAt}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    // Receipt Attachment Preview Card
                                    if (req.receiptUri != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewingReceiptUri = req.receiptUri }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AsyncImage(
                                                    model = req.receiptUri,
                                                    contentDescription = "صورة الإيصال",
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .clip(RoundedCornerShape(6.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "صورة إيصال التحويل مرفقة",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = if (req.isDownloaded) "تم حفظ الإيصال في مجلد الطلبات ✓" else "انقر للمعاينة وتكبير الصورة",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (req.isDownloaded) Color(0xFF105C3B) else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Icon(Icons.Default.ZoomIn, contentDescription = "تكبير", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "ملاحظة: لم يتم إرفاق صورة إيصال، يرجى التحقق برقم العملية (${req.transactionRef}).",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Action Buttons: Download Receipt & Delete
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val res = ReceiptStorageUtil.saveReceiptToFolder(
                                                    context = context,
                                                    receiptUriString = req.receiptUri,
                                                    studentName = req.studentName,
                                                    transactionRef = req.transactionRef
                                                )
                                                res.onSuccess { path ->
                                                    maqraahViewModel.markPaymentReceiptDownloaded(req.id)
                                                    Toast.makeText(
                                                        context,
                                                        "تم حفظ الإيصال في مجلد: مقرأة الإمام الجزري/طلبات بنجاح",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }.onFailure { e ->
                                                    Toast.makeText(context, "تعذر حفظ الإيصال: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.primary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (req.isDownloaded) "تم التنزيل ✓ (تكرار)" else "تنزيل الإيصال",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                maqraahViewModel.deletePaymentRequest(req.id)
                                                Toast.makeText(context, "تم حذف الطلب نهائياً", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "حذف الطلب", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }

                                    // Approval and Rejection actions
                                    if (req.status == PaymentStatus.PENDING) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    maqraahViewModel.approvePayment(req.id)
                                                    Toast.makeText(context, "تم اعتماد الشحن وإضافة ${req.packageMinutes} دقيقة للطالب", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "اعتماد وإضافة الدقائق")
                                            }
                                            Button(
                                                onClick = {
                                                    rejectingPaymentId = req.id
                                                    rejectionReasonText = ""
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "رفض الطلب")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // 4. PROGRAM ENROLLMENT REQUESTS
                        item {
                            Text(text = "طلبات التسجيل في البرامج الإقرائية:", fontWeight = FontWeight.Bold)
                        }

                        if (enrollmentRequests.isEmpty()) {
                            item {
                                Text(text = "لا توجد طلبات تسجيل حالياً.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        items(enrollmentRequests) { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = req.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Surface(
                                            color = if (req.status == "APPROVED") Color(0xFFDDF2E3) else MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (req.status == "APPROVED") "مقبول" else "قيد المراجعة",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (req.status == "APPROVED") Color(0xFF105C3B) else MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Text(text = "البرنامج: ${req.programTitle}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(text = "المدينة: ${req.city} • العمر: ${req.age} • المحفوظ: ${req.currentMemorization}")
                                    Text(text = "الفترة المفضلة: ${req.preferredTimeSlot} • الهاتف: ${req.contactPhone}")
                                    Text(text = "الخبرة: ${req.previousExperience}", style = MaterialTheme.typography.bodySmall)

                                    if (req.status == "PENDING") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    maqraahViewModel.approveEnrollment(req.id)
                                                    Toast.makeText(context, "تم قبول طلب الطالب بنجاح", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(text = "قبول الطلب")
                                            }
                                            Button(
                                                onClick = {
                                                    maqraahViewModel.rejectEnrollment(req.id, "المقاعد مكتملة حالياً")
                                                    Toast.makeText(context, "تم رفض الطلب وتدوين السبب", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(text = "رفض")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    5 -> {
                        // 5. ANNOUNCEMENTS AND STATUSES PUBLISHING
                        item {
                            Text(text = "نشر الإعلانات الرسمية والفوائد القرآنية وحالات المقرأة:", fontWeight = FontWeight.Bold)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { showNewAnnouncementDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "نشر إعلان رسمي")
                                }

                                Button(
                                    onClick = { showNewPostDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF105C3B)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "نشر فائدة / حالة")
                                }
                            }
                        }

                        item {
                            Text(text = "الإعلانات الرسمية المنشورة حالياً:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }

                        items(announcements) { anc ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = anc.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        if (anc.isPinned) {
                                            Surface(color = Color(0xFFDDF2E3), shape = RoundedCornerShape(4.dp)) {
                                                Text(text = "مثبت", color = Color(0xFF105C3B), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = anc.content, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "${anc.author} • ${anc.date}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "الفوائد القرآنية وحالات المقرأة المنشورة:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }

                        items(posts) { post ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "${post.authorName} (${post.authorRole})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(text = post.content, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = post.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: Student Management Modal ---
    if (managingStudent != null) {
        val s = managingStudent!!
        AlertDialog(
            onDismissRequest = { managingStudent = null },
            title = {
                Text(text = "إدارة حساب الطالب: ${s.name}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "كود الطالب: ${s.studentCode}", fontWeight = FontWeight.Bold)
                            Text(text = "الهاتف: ${s.phone} • المدينة: ${s.city}")
                            Text(text = "رصيد الدقائق الحالي: ${s.minuteBalance} دقيقة")
                            Text(text = "الحالة: ${if (s.isSuspended) "مجمّد" else "نشط"}", color = if (s.isSuspended) Color(0xFFEF4444) else Color(0xFF18794E), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Action Buttons Grid
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (s.isSuspended) {
                            Button(
                                onClick = {
                                    maqraahViewModel.activateStudent(s.id)
                                    managingStudent = s.copy(isSuspended = false)
                                    Toast.makeText(context, "تم تفعيل حساب الطالب", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تفعيل الحساب", style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            Button(
                                onClick = {
                                    maqraahViewModel.suspendStudent(s.id)
                                    managingStudent = s.copy(isSuspended = true)
                                    Toast.makeText(context, "تم تجميد حساب الطالب", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تجميد الحساب", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Button(
                            onClick = { showAwardDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF105C3B)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("منح دقائق", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showAlertSendDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إرسال تنبيه", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف الحساب", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { managingStudent = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E))
                ) {
                    Text("إغلاق")
                }
            }
        )
    }

    // --- DIALOG: Award Minutes Modal ---
    if (showAwardDialog && managingStudent != null) {
        AlertDialog(
            onDismissRequest = { showAwardDialog = false },
            title = { Text("تكريم ومنح دقائق إضافية") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "الطالب: ${managingStudent!!.name}")
                    OutlinedTextField(
                        value = awardMinutesInput,
                        onValueChange = { awardMinutesInput = it },
                        label = { Text("عدد الدقائق الممنوحة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = awardReasonInput,
                        onValueChange = { awardReasonInput = it },
                        label = { Text("سبب المنح والتكريم") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val m = awardMinutesInput.toIntOrNull() ?: 0
                        if (m > 0) {
                            maqraahViewModel.awardMinutesToStudent(managingStudent!!.id, m, awardReasonInput)
                            showAwardDialog = false
                            managingStudent = managingStudent!!.copy(minuteBalance = managingStudent!!.minuteBalance + m)
                            Toast.makeText(context, "تم منح $m دقيقة للطالب بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E))
                ) {
                    Text("تأكيد المنح")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAwardDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("إلغاء")
                }
            }
        )
    }

    // --- DIALOG: Send Alert Modal ---
    if (showAlertSendDialog && managingStudent != null) {
        AlertDialog(
            onDismissRequest = { showAlertSendDialog = false },
            title = { Text("إرسال تنبيه إداري للطالب") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "إلى: ${managingStudent!!.name}")
                    OutlinedTextField(
                        value = alertMessageInput,
                        onValueChange = { alertMessageInput = it },
                        label = { Text("نص التنبيه أو الملاحظة") },
                        placeholder = { Text("مثال: نرجو الحضور قبل موعد الحلقة بـ 5 دقائق") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (alertMessageInput.isNotBlank()) {
                            maqraahViewModel.sendStudentAlert(managingStudent!!.id, alertMessageInput)
                            showAlertSendDialog = false
                            Toast.makeText(context, "تم إرسال التنبيه إلى إشعارات الطالب", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E))
                ) {
                    Text("إرسال")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAlertSendDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("إلغاء")
                }
            }
        )
    }

    // --- DIALOG: Delete Confirm Modal ---
    if (showDeleteConfirmDialog && managingStudent != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("تأكيد حذف الحساب نهائياً", color = Color(0xFFDC2626)) },
            text = {
                Text(text = "هل أنت متأكد من حذف حساب الطالب (${managingStudent!!.name}) نهائياً من المقرأة؟ لا يمكن التراجع عن هذا الإجراء.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        maqraahViewModel.deleteStudent(managingStudent!!.id)
                        showDeleteConfirmDialog = false
                        managingStudent = null
                        Toast.makeText(context, "تم حذف حساب الطالب", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("تأكيد الحذف")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B776F))
                ) {
                    Text("إلغاء")
                }
            }
        )
    }

    // --- DIALOG: Session Deduction Modal ---
    if (deductingStudent != null) {
        val s = deductingStudent!!
        AlertDialog(
            onDismissRequest = { deductingStudent = null },
            title = {
                Text(text = "تسجيل جلسة تسميع: ${s.name}", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Surface(
                            color = Color(0xFFDDF2E3),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "كود الطالب: ${s.studentCode}", fontWeight = FontWeight.Bold, color = Color(0xFF105C3B))
                                Text(text = "الرصيد المتاح: ${s.minuteBalance} دقيقة", fontWeight = FontWeight.Bold, color = Color(0xFF18794E))
                            }
                        }
                    }

                    item {
                        Text(text = "اختر مدة الجلسة بالدقائق:", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(15, 30, 45, 60).forEach { mins ->
                                FilterChip(
                                    selected = deductionMinutes == mins,
                                    onClick = {
                                        deductionMinutes = mins
                                        customDeductionMinutesText = mins.toString()
                                    },
                                    label = { Text("$mins د") }
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = customDeductionMinutesText,
                            onValueChange = {
                                customDeductionMinutesText = it
                                deductionMinutes = it.toIntOrNull() ?: 0
                            },
                            label = { Text("عدد الدقائق المراد خصمها") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = recitationSpanInput,
                            onValueChange = { recitationSpanInput = it },
                            label = { Text("مقدار التلاوة والتسميع (من سورة/آية إلى سورة/آية)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = tajweedNotesInput,
                            onValueChange = { tajweedNotesInput = it },
                            label = { Text("ملاحظات التجويد والأداء العلمي") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = deductionMinutes
                        if (mins > 0 && s.minuteBalance >= mins) {
                            maqraahViewModel.deductQuranSession(s.id, mins, recitationSpanInput, tajweedNotesInput)
                            deductingStudent = null
                            Toast.makeText(context, "تم تسجيل الجلسة وخصم $mins دقيقة بنجاح", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "تنبيه: رصيد الطالب غير كافٍ لخصم هذه المدة!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E))
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تأكيد الخصم", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { deductingStudent = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إلغاء")
                }
            }
        )
    }

    // --- DIALOG: New Announcement Dialog ---
    if (showNewAnnouncementDialog) {
        AlertDialog(
            onDismissRequest = { showNewAnnouncementDialog = false },
            title = { Text(text = "نشر إعلان رسمي للمقرأة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newAncTitle,
                        onValueChange = { newAncTitle = it },
                        label = { Text("عنوان الإعلان") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAncContent,
                        onValueChange = { newAncContent = it },
                        label = { Text("نص الإعلان") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = newAncPinned, onCheckedChange = { newAncPinned = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "تثبيت الإعلان في أعلى الصفحة الرئيسية")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAncTitle.isNotBlank()) {
                            maqraahViewModel.addAnnouncement(newAncTitle, newAncContent, newAncPinned)
                            com.example.util.SmartNotificationManager.notifyAnnouncement(context, newAncTitle, newAncContent)
                            showNewAnnouncementDialog = false
                            newAncTitle = ""
                            newAncContent = ""
                            Toast.makeText(context, "تم نشر الإعلان وبث الإشعار لشريط التنبيهات بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E))
                ) {
                    Text(text = "نشر الآن")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showNewAnnouncementDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("إلغاء") }
            }
        )
    }

    // --- DIALOG: New Quran Post / Status Dialog ---
    if (showNewPostDialog) {
        AlertDialog(
            onDismissRequest = { showNewPostDialog = false },
            title = { Text(text = "نشر فائدة قرآنية / حالة للمقرأة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPostRole,
                        onValueChange = { newPostRole = it },
                        label = { Text("صفة الناشر") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPostContent,
                        onValueChange = { newPostContent = it },
                        label = { Text("نص الفائدة أو الحالة القرآنية") },
                        placeholder = { Text("مثال: قال الإمام ابن الجزري رحمه الله...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPostContent.isNotBlank()) {
                            maqraahViewModel.publishQuranPost(newPostContent, newPostRole)
                            showNewPostDialog = false
                            newPostContent = ""
                            Toast.makeText(context, "تم نشر الفائدة القرآنية في الرئيسية", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E))
                ) {
                    Text("نشر الفائدة")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showNewPostDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("إلغاء") }
            }
        )
    }

    // --- DIALOG: Reject Payment Dialog ---
    if (rejectingPaymentId != null) {
        AlertDialog(
            onDismissRequest = { rejectingPaymentId = null },
            title = { Text(text = "رفض طلب التحويل") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "يرجى ذكر سبب الرفض لتبليغ الطالب:")
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        label = { Text("سبب الرفض (مثال: رقم العملية غير مطابق)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        maqraahViewModel.rejectPayment(rejectingPaymentId!!, rejectionReasonText.ifBlank { "رقم العملية غير مطابق لكشف الحساب" })
                        rejectingPaymentId = null
                        Toast.makeText(context, "تم رفض الطلب وإشعار الطالب", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(text = "تأكيد الرفض")
                }
            },
            dismissButton = {
                Button(
                    onClick = { rejectingPaymentId = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B776F))
                ) { Text("إلغاء") }
            }
        )
    }

    // --- DIALOG: Fullscreen Receipt Viewer ---
    if (viewingReceiptUri != null) {
        AlertDialog(
            onDismissRequest = { viewingReceiptUri = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "معاينة إيصال التحويل", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewingReceiptUri = null }) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = viewingReceiptUri,
                        contentDescription = "صورة الإيصال بحجم كامل",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewingReceiptUri = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("إغلاق")
                }
            }
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
