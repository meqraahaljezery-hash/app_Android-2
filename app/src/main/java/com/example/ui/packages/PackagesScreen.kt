package com.example.ui.packages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.MinutePackage
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.i18n.AppLanguage
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldContainer
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBorder
import com.example.ui.theme.SurfaceBorderDecor
import com.example.ui.theme.TajawalFontFamily
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

    val isAdmin = currentUser?.role == UserRole.ADMIN || maqraahViewModel.isAdminEmail(currentUser?.email)

    var selectedTab by remember { mutableStateOf(0) }
    var selectedPackageForRecharge by remember { mutableStateOf<MinutePackage?>(null) }
    var packageToEdit by remember { mutableStateOf<MinutePackage?>(null) }

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
                title = {
                    Text(
                        text = "باقاتي ورصيد التسميع",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
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
            // تصميم كرت الرصيد الفاخر بتدرج زمردي وذهبي
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicEmerald)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(IslamicEmerald, IslamicEmeraldDark)
                            )
                        )
                        .padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isAdmin) "لوحة رصيد المشرف العام" else "رصيدك المتاح للتسميع",
                                color = Color.White.copy(alpha = 0.85f),
                                fontFamily = TajawalFontFamily,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${currentUser?.minuteBalance ?: 0}",
                                    color = Color.White,
                                    fontFamily = TajawalFontFamily,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "دقيقة",
                                    color = IslamicGold,
                                    fontFamily = TajawalFontFamily,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = IslamicGold,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }

            // أزرار التبويب (Tabs)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(color = SurfaceBorderDecor) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("الباقات المتاحة", fontFamily = TajawalFontFamily, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("سجل الحركات والرصيد", fontFamily = TajawalFontFamily, fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // كرت الحسابات البنكية المعتمدة
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, SurfaceBorderDecor)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "الحسابات البنكية المعتمدة للمقرأة",
                                        fontFamily = TajawalFontFamily,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                bankAccounts.forEach { acc ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(acc.bankNameAr, fontFamily = TajawalFontFamily, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(acc.accountNumber, fontFamily = TajawalFontFamily, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Account Number", acc.accountNumber))
                                            Toast.makeText(context, "تم نسخ رقم الحساب", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    if (acc != bankAccounts.last()) HorizontalDivider(color = SurfaceBorderDecor.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }

                    // قائمة الباقات المتاحة
                    items(packages) { pkg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isAdmin) {
                                        selectedPackageForRecharge = pkg
                                        transactionRef = ""
                                        attachedReceiptUri = null
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = if (pkg.isPopular) BorderStroke(2.dp, IslamicGold) else BorderStroke(1.dp, SurfaceBorderDecor)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(pkg.titleAr, fontFamily = TajawalFontFamily, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        if (pkg.isPopular) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = IslamicGold,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    "الأكثر طلباً",
                                                    fontFamily = TajawalFontFamily,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${pkg.minutes} دقيقة تسميع مباشرة",
                                        fontFamily = TajawalFontFamily,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "القيمة: ${pkg.priceEtb} بير إثيوبي (ETB)",
                                        fontFamily = TajawalFontFamily,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isAdmin) {
                                    IconButton(onClick = { packageToEdit = pkg }) {
                                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            selectedPackageForRecharge = pkg
                                            transactionRef = ""
                                            attachedReceiptUri = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("شحن الآن", fontFamily = TajawalFontFamily, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // سجل الحركات والرصيد (Credit Ledger)
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (creditLedger.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد حركات مسجلة حالياً في رصيدك",
                                    fontFamily = TajawalFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(creditLedger) { entry ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, SurfaceBorderDecor)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(entry.reason, fontFamily = TajawalFontFamily, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Surface(
                                            color = if (entry.changeAmount > 0) IslamicEmeraldContainer else MaterialTheme.colorScheme.errorContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = if (entry.changeAmount > 0) "+${entry.changeAmount} دقيقة" else "${entry.changeAmount} دقيقة",
                                                fontFamily = TajawalFontFamily,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (entry.changeAmount > 0) IslamicEmeraldDark else MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("الرصيد: ${entry.balanceBefore} ← ${entry.balanceAfter} دقيقة", fontFamily = TajawalFontFamily, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(entry.timestamp, fontFamily = TajawalFontFamily, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: Recharge Package (نافذة شحن الباقة الفاخرة المكتملة) ---
    if (selectedPackageForRecharge != null) {
        val pkg = selectedPackageForRecharge!!
        Dialog(
            onDismissRequest = { selectedPackageForRecharge = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(0.88f)
                    .imePadding()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(IslamicEmerald, IslamicEmeraldDark)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "شحن رصيد التسميع",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${pkg.titleAr} (${pkg.minutes} دقيقة - ${pkg.priceEtb} ETB)",
                                    fontFamily = TajawalFontFamily,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IslamicGold
                                )
                            }
                            IconButton(onClick = { selectedPackageForRecharge = null }) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                            }
                        }
                    }

                    // Body
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "اختر الحساب البنكي المحول إليه:",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )

                        bankAccounts.forEach { acc ->
                            val isSelected = selectedBankCode == acc.bankCode
                            Surface(
                                color = if (isSelected) IslamicEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, if (isSelected) IslamicEmerald else SurfaceBorderDecor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedBankCode = acc.bankCode }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedBankCode = acc.bankCode },
                                        colors = RadioButtonDefaults.colors(selectedColor = IslamicEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(acc.bankNameAr, fontFamily = TajawalFontFamily, fontWeight = FontWeight.Bold)
                                        Text("رقم الحساب: ${acc.accountNumber}", fontFamily = TajawalFontFamily, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = senderName,
                            onValueChange = { senderName = it },
                            label = { Text("اسم صاحب الحساب المحول منه", fontFamily = TajawalFontFamily) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = senderPhone,
                            onValueChange = { senderPhone = it },
                            label = { Text("رقم الهاتف أو الواتساب للتأكيد", fontFamily = TajawalFontFamily) },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = transactionRef,
                            onValueChange = { transactionRef = it },
                            label = { Text("رقم العملية / مرجع التحويل (Transaction ID)", fontFamily = TajawalFontFamily) },
                            leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        // زر إرفاق صورة الإشعار
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, SurfaceBorderDecor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (attachedReceiptUri != null) "تم إرفاق إشعار التحويل بنجاح" else "إرفاق صورة إشعار التحويل البنكي",
                                        fontFamily = TajawalFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (attachedReceiptUri != null) IslamicEmeraldDark else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (attachedReceiptUri != null) "انقر لتغيير الصورة" else "اختياري لسرعة الاعتماد الفوري",
                                        fontFamily = TajawalFontFamily,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (attachedReceiptUri != null) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IslamicEmerald)
                                }
                            }
                        }

                        if (attachedReceiptUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            ) {
                                AsyncImage(
                                    model = attachedReceiptUri,
                                    contentDescription = "صورة الإشعار",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // زر التأكيد والإرسال
                    Box(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                        Button(
                            onClick = {
                                if (transactionRef.isBlank()) {
                                    Toast.makeText(context, "يرجى إدخال رقم العملية أو مرجع التحويل", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                maqraahViewModel.submitPaymentRequest(
                                    pkg = pkg,
                                    targetBank = selectedBankCode,
                                    senderName = senderName.ifBlank { currentUser?.name ?: "طالب" },
                                    senderPhone = senderPhone.ifBlank { currentUser?.phone ?: "" },
                                    transactionRef = transactionRef.trim(),
                                    receiptUri = attachedReceiptUri?.toString()
                                )
                                selectedPackageForRecharge = null
                                rechargeSuccessDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("تأكيد إرسال طلب الشحن", fontFamily = TajawalFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: Recharge Success ---
    if (rechargeSuccessDialog) {
        Dialog(onDismissRequest = { rechargeSuccessDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(IslamicEmerald, IslamicEmeraldDark)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IslamicGold, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "تم إرسال طلب الشحن بنجاح",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "تم استلام بيانات التحويل البنكي ورقم العملية. سيقوم المشرف المالي بمراجعة الإيداع واعتماد الرصيد فورياً في حسابك.\n\nستصلك إشعارات على جوالك فور نزول الدقائق في رصيدك لتتمكن من التسميع مباشرة.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = TajawalFontFamily,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Button(
                            onClick = { rechargeSuccessDialog = false },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("حسناً وبارك الله فيكم", fontFamily = TajawalFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}