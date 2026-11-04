package com.example.ui.groups

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    currentLanguage: AppLanguage
) {
    val context = LocalContext.current

    val groupsList = listOf(
        HalqahGroup("grp_m_morning", "حلقات الذكور - الفترة الصباحية (الفجر والضحى)", "Brothers Halaqat - Morning Session", GroupTimeSlot.MORNING, GenderCategory.BROTHERS, "المشرف العام وهيئة الإقراء", null, true, 18, "سورة البقرة وتصحيح التلاوة", "room-m-morning"),
        HalqahGroup("grp_m_evening", "حلقات الذكور - الفترة المسائية (العصر والمغرب)", "Brothers Halaqat - Evening Session", GroupTimeSlot.EVENING, GenderCategory.BROTHERS, "مشايخ الإقراء المعتمدون", null, false, 15, "سورة آل عمران والنساء", "room-m-evening"),
        HalqahGroup("grp_m_night", "حلقات الذكور - الفترة الليلية (العشاء والسحر)", "Brothers Halaqat - Night Session", GroupTimeSlot.NIGHT, GenderCategory.BROTHERS, "مشايخ الحفظ والإجازة بالسند", null, true, 22, "عرض الختمات والمراجعة المكثفة", "room-m-night"),
        HalqahGroup("grp_f_morning", "حلقات الإناث - الفترة الصباحية (الفجر والضحى)", "Sisters Halaqat - Morning Session", GroupTimeSlot.MORNING, GenderCategory.SISTERS, "المعلمات المسندات الفضليات", null, true, 16, "سورة البقرة والتلقين", "room-f-morning"),
        HalqahGroup("grp_f_evening", "حلقات الإناث - الفترة المسائية (العصر والمغرب)", "Sisters Halaqat - Evening Session", GroupTimeSlot.EVENING, GenderCategory.SISTERS, "المعلمات المقرئات", null, false, 14, "الحفظ والمراجعة", "room-f-evening"),
        HalqahGroup("grp_f_night", "حلقات الإناث - الفترة الليلية (العشاء والسحر)", "Sisters Halaqat - Night Session", GroupTimeSlot.NIGHT, GenderCategory.SISTERS, "نخبة المعلمات المسندات", null, true, 19, "عرض الختمات والإتقان", "room-f-night"),
        HalqahGroup("grp_general", "الملتقى القرآني العام (كافة الطلاب والمعلمين)", "General Quranic Forum", GroupTimeSlot.MORNING, GenderCategory.GENERAL, "إدارة المقرأة وهيئة الإقراء", null, true, 45, "مذاكرة أحكام التجويد والمتون والفوائد", "room-general")
    )

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "الحلقات القرآنية الـ 7 مقسمة حسب الجنس والفترة لضمان الخصوصية وسهولة المشافهة مع المشايخ والمعلمات.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            items(groupsList) { group ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            if (group.isLiveNow) {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.LiveTv, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                        Text(text = Strings.get("live_stream", currentLanguage), color = Color.White)
                                    }
                                }
                            }
                        }

                        Text(
                            text = "المشرف: ${group.teacherName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "المنهج والتلاوة: ${group.currentSurah}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "عدد الطلاب: ${group.membersCount}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(
                                onClick = { activeGroupDialog = group },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "دخول الحلقة")
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeGroupDialog != null) {
        val grp = activeGroupDialog!!
        AlertDialog(
            onDismissRequest = { activeGroupDialog = null },
            title = { Text(text = grp.nameAr, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "المعلم المشرف: ${grp.teacherName}")
                    Text(text = "المنهج: ${grp.currentSurah}")
                    Text(text = "عدد الطلاب المسجلين: ${grp.membersCount}")
                    Spacer(modifier = Modifier.height(4.dp))
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
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الانضمام عبر تليغرام")
                        }
                    } else {
                        Text(
                            text = "رابط تليغرام الرسمي سيقوم المشرف العام بتفعيله وإضافته يدوياً قريباً.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { activeGroupDialog = null }) {
                    Text(text = "حسناً")
                }
            }
        )
    }
}
