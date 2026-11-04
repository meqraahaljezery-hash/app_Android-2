import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.data.model.MinutePackage
import com.example.data.model.User
import com.example.i18n.AppLanguage
import com.example.viewmodel.MaqraahViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesScreen(
    currentLanguage: AppLanguage,
    currentUser: User?,
    maqraahViewModel: MaqraahViewModel
) {
    val context = LocalContext.current
    val packages by maqraahViewModel.minutePackages.collectAsState()
    val bankAccounts by maqraahViewModel.bankAccounts.collectAsState()
    val creditLedger by maqraahViewModel.creditLedger.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: الباقات والشحن، 1: سجل حركات الرصيد
    var selectedPackageForRecharge by remember { mutableStateOf<MinutePackage?>(null) }

    // Recharge Form State
    var selectedBankCode by remember { mutableStateOf("CBE") }
    var senderName by remember { mutableStateOf(currentUser?.name ?: "") }
    var senderPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var transactionRef by remember { mutableStateOf("") }
    var attachedReceiptUri by remember { mutableStateOf<Uri?>(null) }
    var rechargeSuccessDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        attachedReceiptUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "باقاتي ورصيد التسميع", fontWeight = FontWeight.Bold) },
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
            // Balance Highlight Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الرصيد المتاح حالياً للتسميع",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${currentUser?.minuteBalance ?: 0}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "دقيقة تسميع معتمدة",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Tabs: [الباقات والشحن] - [سجل حركات الرصيد]
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("باقات الدقائق المتاحة", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("سجل الحركات المالي", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Approved Bank Accounts Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "الحسابات البنكية المعتمدة للمقرأة (اسم المستفيد: Kalid Ahmed Mustefa)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                bankAccounts.forEach { acc ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = acc.bankNameAr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(text = acc.accountNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Account Number", acc.accountNumber))
                                                Toast.makeText(context, "تم نسخ رقم الحساب: ${acc.accountNumber}", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    if (acc != bankAccounts.last()) {
                                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    }
                                }
                            }
                        }
                    }

                    // 7 Packages
                    items(packages) { pkg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPackageForRecharge = pkg
                                    transactionRef = ""
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = pkg.titleAr,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (pkg.isPopular) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "الأكثر طلباً",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${pkg.minutes} دقيقة تسميع مباشرة مع المشايخ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "القيمة: ${pkg.priceEtb} بير إثيوبي (ETB)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = {
                                        selectedPackageForRecharge = pkg
                                        transactionRef = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = "شحن الباقة")
                                }
                            }
                        }
                    }
                }
            } else {
                // Credit Ledger History
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "سجل العمليات والحركات المالية غير القابل للتعديل",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(creditLedger) { entry ->
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
                                    Text(
                                        text = entry.reason,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        color = if (entry.changeAmount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (entry.changeAmount > 0) "+${entry.changeAmount} دقيقة" else "${entry.changeAmount} دقيقة",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (entry.changeAmount > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "الرصيد: ${entry.balanceBefore} ← ${entry.balanceAfter} دقيقة",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = entry.timestamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- RECHARGE REQUEST DIALOG ---
    if (selectedPackageForRecharge != null) {
        val pkg = selectedPackageForRecharge!!
        val currentSelectedBank = bankAccounts.find { it.bankCode == selectedBankCode } ?: bankAccounts.first()

        AlertDialog(
            onDismissRequest = { selectedPackageForRecharge = null },
            title = {
                Text(text = "طلب شحن ${pkg.titleAr}", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            color = Color(0xFFDDF2E3),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "المبلغ المطلوب تحويله: ${pkg.priceEtb} ETB",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF105C3B),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "عدد الدقائق المعتمدة: ${pkg.minutes} دقيقة تسميع",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF18794E)
                                )
                            }
                        }
                    }

                    item {
                        Text(text = "1. اختر البنك أو المحفظة التي حولت إليها:", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            bankAccounts.forEach { b ->
                                FilterChip(
                                    selected = selectedBankCode == b.bankCode,
                                    onClick = { selectedBankCode = b.bankCode },
                                    label = { Text(b.bankCode, fontWeight = FontWeight.SemiBold) }
                                )
                            }
                        }
                    }

                    // Bank Account Details Display
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = currentSelectedBank.bankNameAr,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "رقم الحساب: ${currentSelectedBank.accountNumber}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "اسم المستفيد: Kalid Ahmed Mustefa",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Account Number", currentSelectedBank.accountNumber))
                                            Toast.makeText(context, "تم نسخ رقم الحساب بنجاح", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ رقم الحساب", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(text = "2. أدخل بيانات إشعار التحويل:", fontWeight = FontWeight.Bold)
                    }

                    item {
                        OutlinedTextField(
                            value = senderName,
                            onValueChange = { senderName = it },
                            label = { Text("اسم المرسل / المحوِّل الثلاثي") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = senderPhone,
                            onValueChange = { senderPhone = it },
                            label = { Text("رقم هاتف المحوّل") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = transactionRef,
                            onValueChange = { transactionRef = it },
                            label = { Text("رقم العملية البنكية / Transaction Ref") },
                            placeholder = { Text("مثال: CBE98273645") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    // Receipt Image Picker Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "صورة إيصال التحويل (مرفق رسمي):",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (attachedReceiptUri != null) {
                                        TextButton(onClick = { attachedReceiptUri = null }) {
                                            Text("حذف الصورة", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                if (attachedReceiptUri != null) {
                                    AsyncImage(
                                        model = attachedReceiptUri,
                                        contentDescription = "صورة إيصال التحويل",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(170.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF18794E), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "تم إرفاق صورة الإيصال بنجاح وستصل للمشرف المالي",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF105C3B),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("اختيار صورة الإيصال من المعرض", fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "يرجى تصوير أو إرفاق لقطة شاشة لإيصال التحويل البنكي أو إشعار التيليبير لتسريع الاعتماد",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (transactionRef.isNotBlank()) {
                            maqraahViewModel.submitPaymentRequest(
                                pkg = pkg,
                                targetBank = selectedBankCode,
                                senderName = senderName,
                                senderPhone = senderPhone,
                                transactionRef = transactionRef,
                                receiptUri = attachedReceiptUri?.toString()
                            )
                            selectedPackageForRecharge = null
                            attachedReceiptUri = null
                            rechargeSuccessDialog = true
                        } else {
                            Toast.makeText(context, "يرجى إدخال رقم العملية للتأكيد", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "تأكيد إرسال الطلب", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        selectedPackageForRecharge = null
                        attachedReceiptUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "إلغاء")
                }
            }
        )
    }

    if (rechargeSuccessDialog) {
        AlertDialog(
            onDismissRequest = { rechargeSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF18794E))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "تم إرسال الطلب بنجاح", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(text = "تم استلام إشعار شحن الرصيد بنجاح، ووصلك إشعار فوري في مركز الإشعارات بالرئيسية. سيقوم المشرف المالي باعتماد التحويل وإضافة الدقائق فور التحقق.")
            },
            confirmButton = {
                Button(
                    onClick = { rechargeSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "حسناً")
                }
            }
        )
    }
}
