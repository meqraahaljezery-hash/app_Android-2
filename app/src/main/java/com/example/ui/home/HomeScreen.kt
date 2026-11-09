package com.example.ui.home

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.Program
import com.example.data.model.User
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.NotoSansEthiopicFontFamily
import com.example.util.SmartNotificationManager
import com.example.viewmodel.MaqraahViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentLanguage: AppLanguage,
    currentUser: User?,
    maqraahViewModel: MaqraahViewModel,
    onLanguageChanged: (AppLanguage) -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val programs by maqraahViewModel.programs.collectAsState()
    val announcements by maqraahViewModel.announcements.collectAsState()
    val posts by maqraahViewModel.quranPosts.collectAsState()
    val notifications by maqraahViewModel.notifications.collectAsState()

    var showLanguageMenu by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var selectedProgramForDetails by remember { mutableStateOf<Program?>(null) }
    var selectedProgramForRegistration by remember { mutableStateOf<Program?>(null) }

    // Registration Form State
    var regName by remember { mutableStateOf(currentUser?.name ?: "") } // تم إضافة حقل الاسم
    var regAge by remember { mutableStateOf("24") }
    var regGender by remember { mutableStateOf("ذكر") }
    var regCity by remember { mutableStateOf("أديس أبابا") }
    var regPhone by remember { mutableStateOf(currentUser?.phone ?: "+251 911 234 567") }
    var regMemorization by remember { mutableStateOf("15 جزءاً") }
    var regExperience by remember { mutableStateOf("درست أحكام التجويد الأساسية ومتن تحفة الأطفال") }
    var regSlot by remember { mutableStateOf("الفجر") }
    var registrationSuccessDialog by remember { mutableStateOf(false) }
    var registrationErrorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(IslamicEmerald)
                                .border(1.5.dp, IslamicGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo_al_jezery),
                                contentDescription = "شعار مقرأة الإمام الجزري",
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (currentLanguage == AppLanguage.AMHARIC) "የኢማም አል-ጀዘሪ ማዕከል" else "مقرأة الإمام الجزري",
                                fontFamily = if (currentLanguage == AppLanguage.AMHARIC) NotoSansEthiopicFontFamily else CairoFontFamily,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (currentLanguage == AppLanguage.AMHARIC) "የቁርኣን ንባብ እና የቀጥታ ሰነድ ማዕከል" else "للإقراء والإجازة بالسند المتصل",
                                fontFamily = if (currentLanguage == AppLanguage.AMHARIC) NotoSansEthiopicFontFamily else CairoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    val unreadCount = notifications.count { !it.isRead }
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(containerColor = Color(0xFFEF4444), contentColor = Color.White) {
                                    Text("$unreadCount")
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        IconButton(onClick = { showNotificationsDialog = true }) {
                            Icon(Icons.Default.Notifications, contentDescription = "مركز الإشعارات", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Box {
                        IconButton(onClick = { showLanguageMenu = true }) {
                            Icon(Icons.Default.Language, contentDescription = "Change Language", tint = MaterialTheme.colorScheme.primary)
                        }
                        DropdownMenu(expanded = showLanguageMenu, onDismissRequest = { showLanguageMenu = false }) {
                            DropdownMenuItem(text = { Text("العربية (Arabic)") }, onClick = { onLanguageChanged(AppLanguage.ARABIC); showLanguageMenu = false })
                            DropdownMenuItem(text = { Text("English") }, onClick = { onLanguageChanged(AppLanguage.ENGLISH); showLanguageMenu = false })
                            DropdownMenuItem(text = { Text("አማርኛ (Amharic)") }, onClick = { onLanguageChanged(AppLanguage.AMHARIC); showLanguageMenu = false })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. التصميم الفاخر لبطاقة المستخدم (User Premium Card)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                            )
                        ).padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(32.dp), tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = currentUser?.name ?: "طالب المقرأة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = "${Strings.get("student_code_label", currentLanguage)}: ${currentUser?.studentCode ?: "JZR-1042"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { onNavigate("packages") }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "${currentUser?.minuteBalance ?: 0}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text(text = Strings.get("minutes_unit", currentLanguage), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // 2. صورة الترحيب (Hero Banner)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(170.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_quran_hero),
                            contentDescription = "مصحف شريف في محراب المقرأة",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
                        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                            Text(text = Strings.get("welcome_back", currentLanguage), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "تلقّي القرآن الكريم بالسند المتصل إلى رسول الله ﷺ", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }
            }

            // 3. لوحة الإعلانات
            item {
                val latestAnnouncement = announcements.firstOrNull()
                if (latestAnnouncement != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = Strings.get("announcement_board", currentLanguage), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                if (latestAnnouncement.isPinned) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(4.dp)) {
                                        Text(text = "مثبت", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = latestAnnouncement.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = latestAnnouncement.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 4. البرامج القرآنية
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "البرامج القرآنية المعتمدة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "4 مسارات إقرائية", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            items(programs) { program ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { selectedProgramForRegistration = program; registrationErrorMessage = null },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.ENGLISH -> program.titleEn
                                    AppLanguage.AMHARIC -> program.titleAm
                                    else -> program.titleAr
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                                Text(text = program.badgeTag, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }

                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.ENGLISH -> program.descriptionEn
                                else -> program.descriptionAr
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = "المشرف: ${program.instructorName} (${program.instructorSanad})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Text(text = "مدة المسار: ${program.durationText} • عدد الحلقات: ${program.totalHalaqat} حلقة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "المتطلبات: ${program.prerequisiteAr}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { selectedProgramForRegistration = program; registrationErrorMessage = null },
                            color = Color(0xFFDDF2E3)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HowToReg, contentDescription = null, tint = Color(0xFF105C3B), modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = "التسجيل في هذا البرنامج", fontWeight = FontWeight.Bold, color = Color(0xFF105C3B), style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "انقر لفتح استمارة الالتحاق بمسار ${program.titleAr}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF18794E))
                                    }
                                }
                                Button(
                                    onClick = { selectedProgramForRegistration = program; registrationErrorMessage = null },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "تسجيل الآن", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // 5. منشورات وفوائد
            item {
                Text(text = "منشورات وفوائد التجويد", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            items(posts) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = post.authorName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(text = "${post.authorRole} • ${post.date}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(text = post.content, style = MaterialTheme.typography.bodyMedium)
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { maqraahViewModel.toggleLikePost(post.id) }) {
                                Icon(imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = if (post.isLikedByMe) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${post.likesCount} إعجاب")
                            }
                            Text(text = "${post.commentsCount} تعليقات", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: Program Details ---
    if (selectedProgramForDetails != null) {
        val prog = selectedProgramForDetails!!
        AlertDialog(
            onDismissRequest = { selectedProgramForDetails = null },
            title = { Text(text = prog.titleAr, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Text(text = "الوصف الكامل:", fontWeight = FontWeight.Bold); Text(text = prog.descriptionAr, style = MaterialTheme.typography.bodyMedium) }
                    item { Spacer(modifier = Modifier.height(4.dp)); Text(text = "المشرف والسند:", fontWeight = FontWeight.Bold); Text(text = "${prog.instructorName} (${prog.instructorSanad})", style = MaterialTheme.typography.bodyMedium) }
                    item { Spacer(modifier = Modifier.height(4.dp)); Text(text = "الشروط والمتطلبات:", fontWeight = FontWeight.Bold); Text(text = prog.prerequisiteAr, style = MaterialTheme.typography.bodyMedium) }
                }
            },
            confirmButton = {
                Button(onClick = { val toReg = selectedProgramForDetails; selectedProgramForDetails = null; selectedProgramForRegistration = toReg }) { Text(text = "طلب التسجيل الآن") }
            },
            dismissButton = {
                TextButton(onClick = { selectedProgramForDetails = null }) { Text(text = "إغلاق") }
            }
        )
    }

    // --- DIALOG: Full Registration Form (Premium UI) ---
    if (selectedProgramForRegistration != null) {
        val prog = selectedProgramForRegistration!!
        Dialog(
            onDismissRequest = { selectedProgramForRegistration = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.85f).shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Box(
                        modifier = Modifier.fillMaxWidth().background(
                            Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))),
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ).padding(20.dp)
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { selectedProgramForRegistration = null }) { Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White) }
                                Icon(Icons.Default.School, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "استمارة التسجيل", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = prog.titleAr, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                        }
                    }

                    // Scrollable Form Body
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (registrationErrorMessage != null) {
                            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
                                Text(text = registrationErrorMessage!!, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                            }
                        }
                        
                        // 1. الاسم (تمت إضافته)
                        OutlinedTextField(value = regName, onValueChange = { regName = it }, label = { Text("الاسم الكامل") }, leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)

                        // 2. العمر (تم حذف المكرر)
                        OutlinedTextField(value = regAge, onValueChange = { regAge = it }, label = { Text("العمر") }, leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)

                        // 3. الجنس
                        Text("الجنس", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("ذكر", "أنثى").forEach { option ->
                                val isSelected = regGender == option
                                val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, label = "bg")
                                val borderColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, label = "border")
                                Surface(modifier = Modifier.weight(1f).height(50.dp).clickable { regGender = option }, shape = RoundedCornerShape(12.dp), color = bgColor, border = BorderStroke(1.dp, borderColor)) {
                                    Box(contentAlignment = Alignment.Center) { Text(text = option, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
                                }
                            }
                        }

                        OutlinedTextField(value = regCity, onValueChange = { regCity = it }, label = { Text("المدينة / مكان الإقامة") }, leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
                        OutlinedTextField(value = regPhone, onValueChange = { regPhone = it }, label = { Text("وسيلة التواصل / الواتساب") }, leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
                        OutlinedTextField(value = regMemorization, onValueChange = { regMemorization = it }, label = { Text("مقدار المحفوظ") }, leadingIcon = { Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
                        OutlinedTextField(value = regExperience, onValueChange = { regExperience = it }, label = { Text("الخبرة السابقة") }, leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), maxLines = 2)

                        Text("الفترة المناسبة للتسميع", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("الفجر", "الضحى", "العصر", "المغرب", "العشاء").forEach { option ->
                                val isSelected = regSlot == option
                                val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface, label = "bg")
                                val borderColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant, label = "border")
                                Surface(modifier = Modifier.weight(1f).height(45.dp).clickable { regSlot = option }, shape = RoundedCornerShape(10.dp), color = bgColor, border = BorderStroke(1.dp, borderColor)) {
                                    Box(contentAlignment = Alignment.Center) { Text(text = option, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface) }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Confirm Button
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Button(
                            onClick = {
                                val ageInt = regAge.toIntOrNull() ?: 20
                                val success = maqraahViewModel.submitEnrollmentRequest(prog, ageInt, regGender, regCity, regPhone, regMemorization, regExperience, regSlot)
                                if (success) {
                                    selectedProgramForRegistration = null
                                    registrationSuccessDialog = true
                                } else {
                                    registrationErrorMessage = "تنبيه: أنت مسجل بالفعل أو لديك طلب قيد المراجعة في هذا البرنامج."
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(55.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Text("تأكيد إرسال الطلب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    if (registrationSuccessDialog) {
        AlertDialog(
            onDismissRequest = { registrationSuccessDialog = false },
            title = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF18794E)); Spacer(modifier = Modifier.width(8.dp)); Text(text = "تم إرسال طلبك بنجاح", fontWeight = FontWeight.Bold) } },
            text = { Text(text = "تم استلام طلب التسجيل وحالته الآن (قيد المراجعة). سيقوم المشرف المسؤول بمراجعته والتواصل معك وإرسال إشعار فوري إلى مركز الإشعارات بالاعتماد إن شاء الله.") },
            confirmButton = { Button(onClick = { registrationSuccessDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E)), shape = RoundedCornerShape(8.dp)) { Text(text = "حسناً") } }
        )
    }

    // --- DIALOG: Notification Center ---
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "مركز إشعارات المقرأة", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = { maqraahViewModel.markAllNotificationsAsRead() }) { Text(text = "قراءة الكل", style = MaterialTheme.typography.labelSmall) }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "إشعارات شريط الجوال (Notification Panel)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(text = "تصلك التنبيهات علوياً عند الحلقات والتسجيل", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            FilledTonalButton(onClick = { SmartNotificationManager.sendTestNotification(context); Toast.makeText(context, "تم إرسال إشعار فوري 🔔", Toast.LENGTH_SHORT).show() }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                                Text("تجربة 🔔", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (notifications.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { Text(text = "لا توجد إشعارات مسجلة حالياً", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(notifications) { notif ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { maqraahViewModel.markNotificationAsRead(notif.id) },
                                    colors = CardDefaults.cardColors(containerColor = if (!notif.isRead) Color(0xFFDDF2E3) else MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = notif.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (!notif.isRead) Color(0xFF105C3B) else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                            if (!notif.isRead) { Surface(color = Color(0xFF18794E), shape = CircleShape, modifier = Modifier.size(8.dp)) {} }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = notif.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = notif.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { showNotificationsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E)), shape = RoundedCornerShape(8.dp)) { Text(text = "إغلاق") } }
        )
    }
}