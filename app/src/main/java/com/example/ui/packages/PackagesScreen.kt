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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MinutePackage
import com.example.data.model.User
import com.example.data.model.UserRole
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

    val photoPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri: Uri? -> attachedReceiptUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "باقاتي ورصيد التسميع", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { /* كود إضافة باقة جديدة */ },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) { Icon(Icons.Default.Add, contentDescription = "إضافة باقة", tint = MaterialTheme.colorScheme.onPrimary) }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background)) {
            
            // تصميم "كرت الرصيد" الفاخر
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp).shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF18794E), Color(0xFF0F5A3A)) // تدرج لوني أخضر ملكي
                        )
                    ).padding(24.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(if (isAdmin) "مرحباً بك يا مشرفنا" else "رصيدك المتاح", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("${currentUser?.minuteBalance ?: 0}", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("دقيقة", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                            }
                        }
                        Box(modifier = Modifier.size(60.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }

            // أزرار التبويب (Tabs)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("الباقات المتاحة", fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("سجل الحركات", fontWeight = FontWeight.Bold) })
            }

            if (selectedTab == 0) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // ... (كود الحسابات البنكية وبطاقات الباقات هنا كما في الرسالة السابقة) ...
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("الحسابات البنكية المعتمدة", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                bankAccounts.forEach { acc ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(acc.bankNameAr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(acc.accountNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Account Number", acc.accountNumber))
                                            Toast.makeText(context, "تم النسخ", Toast.LENGTH_SHORT).show()
                                        }) { Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = MaterialTheme.colorScheme.primary) }
                                    }
                                    if (acc != bankAccounts.last()) Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                }
                            }
                        }
                    }

                    items(packages) { pkg ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { if (!isAdmin) { selectedPackageForRecharge = pkg; transactionRef = "" } },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = if (pkg.isPopular) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(pkg.titleAr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        if (pkg.isPopular) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                                                Text("الأكثر طلباً", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${pkg.minutes} دقيقة تسميع مباشرة", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                    Text("القيمة: ${pkg.priceEtb} بير إثيوبي", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isAdmin) {
                                    IconButton(onClick = { packageToEdit = pkg }) { Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary) }
                                } else {
                                    Button(onClick = { selectedPackageForRecharge = pkg; transactionRef = "" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E)), shape = RoundedCornerShape(10.dp)) {
                                        Text("شحن")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // سجل الحركات (Credit Ledger)
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // ... (كود سجل الحركات كما في السابق) ...
                    items(creditLedger) { entry ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(entry.reason, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Surface(color = if (entry.changeAmount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
                                        Text(if (entry.changeAmount > 0) "+${entry.changeAmount} دقيقة" else "${entry.changeAmount} دقيقة", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (entry.changeAmount > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("الرصيد: ${entry.balanceBefore} ← ${entry.balanceAfter} دقيقة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(entry.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ... (باقي النوافذ المنبثقة للشحن والتعديل هنا، كما في الرسائل السابقة، لضمان عملها) ...
}