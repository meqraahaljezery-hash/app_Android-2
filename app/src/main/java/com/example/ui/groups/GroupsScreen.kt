package com.example.ui.groups

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.GenderCategory
import com.example.data.model.GroupTimeSlot
import com.example.data.model.HalqahGroup
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.ui.theme.TelegramBlue
import com.example.viewmodel.MaqraahViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    currentLanguage: AppLanguage,
    maqraahViewModel: MaqraahViewModel? = null,
    onOpenGroupChat: ((HalqahGroup) -> Unit)? = null
) {
    val context = LocalContext.current
    val vmGroups = maqraahViewModel?.groups?.collectAsState()?.value

    // تم حذف البيانات الوهمية، نعتمد فقط على Firebase
    val groupsList = vmGroups ?: emptyList()

    var activeGroupDialog by remember { mutableStateOf<HalqahGroup?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = Strings.get("tab_groups", currentLanguage), fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(8.dp) // تقليل المسافة لتصبح مثل تليغرام
        ) {
            // رسالة توضيحية في الأعلى
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "الحلقات القرآنية مقسمة حسب الجنس والفترة لضمان الخصوصية وسهولة المشافهة مع المشايخ والمعلمات.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // شاشة فارغة إذا لم توجد مجموعات حقيقية
            if (groupsList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد حلقات متاحة حالياً.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // تصميم تليغرام (قائمة أنيقة)
                items(groupsList) { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeGroupDialog = group },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // أيقونة المجموعة الدائرية
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (group.gender) {
                                            GenderCategory.BROTHERS -> MaterialTheme.colorScheme.primaryContainer
                                            GenderCategory.SISTERS -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> TelegramBlue.copy(alpha = 0.2f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = if (group.gender == GenderCategory.GENERAL) TelegramBlue else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // بيانات المجموعة
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.ENGLISH -> group.nameEn
                                            else -> group.nameAr
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    // شارة البث المباشر
                                    if (group.isLiveNow) {
                                        Surface(
                                            color = Color(0xFFDC2626),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "مباشر 🔴",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "المشرف: ${group.teacherName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // عدد الأعضاء وحالة التليغرام
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${group.membersCount} عضو",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                    
                                    if (!group.telegramLink.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = TelegramBlue)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "تليغرام",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TelegramBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // نافذة التفاصيل عند النقر على المجموعة
    if (activeGroupDialog != null) {
        val grp = activeGroupDialog!!
        AlertDialog(
            onDismissRequest = { activeGroupDialog = null },
            title = { 
                Text(text = grp.nameAr, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "المعلم المشرف: ${grp.teacherName}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "المنهج والتلاوة: ${grp.currentSurah}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "عدد الطلاب المسجلين: ${grp.membersCount} طالب", style = MaterialTheme.typography.bodyMedium)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // زر دخول الحلقة (للدردشة والبث)
                    Button(
                        onClick = {
                            activeGroupDialog = null
                            onOpenGroupChat?.invoke(grp)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("دخول الحلقة والدردشة", fontWeight = FontWeight.Bold)
                    }

                    // زر التليغرام (إن وجد)
                    if (!grp.telegramLink.isNullOrBlank()) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(grp.telegramLink))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("الانضمام لمجموعة تليغرام")
                        }
                    } else {
                        Text(
                            text = "رابط تليغرام الرسمي سيقوم المشرف العام بتفعيله وإضافته يدوياً قريباً.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // زر التنبيه
                    OutlinedButton(
                        onClick = {
                            com.example.util.SmartNotificationManager.notifyHalaqahLive(
                                context,
                                grp.nameAr,
                                grp.teacherName
                            )
                            Toast.makeText(context, "تم إرسال تذكير الحلقة لشريط التنبيهات 🔔", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تنبيهي على لوحة الإشعارات 🔔", style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeGroupDialog = null }) {
                    Text(text = "إغلاق", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}