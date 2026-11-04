package com.example.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.viewmodel.MaqraahViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentLanguage: AppLanguage,
    currentUser: User,
    maqraahViewModel: MaqraahViewModel,
    onLanguageChanged: (AppLanguage) -> Unit,
    onNavigateToAdmin: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var isEditingBio by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(currentUser.name) }
    var editedBio by remember { mutableStateOf(currentUser.bio) }
    var editedPhone by remember { mutableStateOf(currentUser.phone) }
    var editedCity by remember { mutableStateOf(currentUser.city) }

    var showLanguageMenu by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = Strings.get("tab_profile", currentLanguage), fontWeight = FontWeight.Bold) },
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
            // 1. User Header Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = currentUser.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = currentUser.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "كود الطالب: ${currentUser.studentCode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = if (currentUser.role == UserRole.ADMIN) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (currentUser.role == UserRole.ADMIN) "مشرف عام (Admin)" else "طالب مسجل",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentUser.role == UserRole.ADMIN) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentUser.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = { isEditingBio = !isEditingBio },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isEditingBio) "إلغاء التعديل" else "تعديل البيانات والنبذة")
                        }
                    }
                }
            }

            // Edit Profile Form if open
            if (isEditingBio) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "تعديل بيانات الملف الشخصي", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                label = { Text("الاسم") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editedBio,
                                onValueChange = { editedBio = it },
                                label = { Text("النبذة التعريفية") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editedPhone,
                                onValueChange = { editedPhone = it },
                                label = { Text("رقم الهاتف") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editedCity,
                                onValueChange = { editedCity = it },
                                label = { Text("المدينة") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    maqraahViewModel.updateProfile(editedName, editedBio, editedPhone, editedCity)
                                    isEditingBio = false
                                    Toast.makeText(context, "تم حفظ التعديلات بنجاح", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = "حفظ التغييرات")
                            }
                        }
                    }
                }
            }

            // 2. Enrolled Programs Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "البرامج والمسارات المسجل بها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = currentUser.sanadTrack, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(text = "المستوى: ${currentUser.tajweedLevel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "نشط ومعتمد",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Settings & Options
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "إعدادات الحساب والتطبيق", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        // Language Selection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLanguageMenu = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "لغة التطبيق / Language", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(text = currentLanguage.displayName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Notifications Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "إشعارات الجوال والتنبيهات", style = MaterialTheme.typography.bodyMedium)
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it }
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Privacy & Security
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPrivacyDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "الخصوصية وسياسة الاستخدام", style = MaterialTheme.typography.bodyMedium)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Help & Support
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showHelpDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "المساعدة والدعم الفني", style = MaterialTheme.typography.bodyMedium)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }



            // 5. Admin Dashboard Entry (If Admin)
            if (currentUser.role == UserRole.ADMIN || maqraahViewModel.isAdminEmail(currentUser.email)) {
                item {
                    Button(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = Strings.get("admin_dashboard", currentLanguage), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 6. Sign Out Button
            item {
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "تسجيل الخروج / Sign Out", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Language Dialog
    if (showLanguageMenu) {
        AlertDialog(
            onDismissRequest = { showLanguageMenu = false },
            title = { Text(text = "اختيار لغة التطبيق") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.values().forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageChanged(lang)
                                    showLanguageMenu = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = lang == currentLanguage, onClick = {
                                onLanguageChanged(lang)
                                showLanguageMenu = false
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lang.displayName, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageMenu = false }) { Text("إغلاق") }
            }
        )
    }

    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(text = "المساعدة والدعم الفني") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "للتواصل مع إدارة المقرأة وهيئة الإشراف:")
                    Text(text = "• البريد الإلكتروني: meqraahaljezery@gmail.com")
                    Text(text = "• الهاتف / تيليجرام: +251 990 161 372")
                    Text(text = "• المشرف العام: أ. خالد أحمد مصطفى")
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) { Text("تم") }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(text = "الخصوصية وسياسة الاستخدام") },
            text = {
                Text(text = "تلتزم مقرأة الإمام الجزري بحفظ خصوصية بيانات الطلاب والمعلمين وحصانة سجلات التسميع والأرصدة المالية، وفصل الحلقات بما يحفظ الحشمة الشرعية.")
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) { Text("إغلاق") }
            }
        )
    }
}
