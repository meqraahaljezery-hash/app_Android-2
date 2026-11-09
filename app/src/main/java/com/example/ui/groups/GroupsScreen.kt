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

    val defaultList = listOf(
        HalqahGroup(
            id = "grp_m_morning",
            nameAr = "حلقات الذكور - الفترة الصباحية (الفجر والضحى)",
            nameEn = "Brothers Halaqat - Morning Session",
            timeSlot = GroupTimeSlot.MORNING,
            gender = GenderCategory.BROTHERS,
            teacherName = "المشرف العام وهيئة الإقراء",
            isLiveNow = true,
            membersCount = 18,
            currentSurah = "سورة البقرة وتصحيح التلاوة",
            liveRoomId = "room-m-morning"
        ),
        HalqahGroup(
            id = "grp_m_evening",
            nameAr = "حلقات الذكور - الفترة المسائية (العصر والمغرب)",
            nameEn = "Brothers Halaqat - Evening Session",
            timeSlot = GroupTimeSlot.EVENING,
            gender = GenderCategory.BROTHERS,
            teacherName = "مشايخ الإقراء المعتمدون",
            isLiveNow = false,
            membersCount = 15,
            currentSurah = "سورة آل عمران والنساء",
            liveRoomId = "room-m-evening"
        ),
        HalqahGroup(
            id = "grp_m_night",
            nameAr = "حلقات الذكور - الفترة الليلية (العشاء والسحر)",
            nameEn = "Brothers Halaqat - Night Session",
            timeSlot = GroupTimeSlot.NIGHT,
            gender = GenderCategory.BROTHERS,
            teacherName = "مشايخ الحفظ والإجازة بالسند",
            isLiveNow = true,
            membersCount = 22,
            currentSurah = "عرض الختمات والمراجعة المكثفة",
            liveRoomId = "room-m-night"
        ),
        HalqahGroup(
            id = "grp_f_morning",
            nameAr = "حلقات الإناث - الفترة الصباحية (الفجر والضحى)",
            nameEn = "Sisters Halaqat - Morning Session",
            timeSlot = GroupTimeSlot.MORNING,
            gender = GenderCategory.SISTERS,
            teacherName = "المعلمات المسندات الفضليات",
            isLiveNow = true,
            membersCount = 16,
            currentSurah = "سورة البقرة والتلقين",
            liveRoomId = "room-f-morning"
        ),
        HalqahGroup(
            id = "grp_f_evening",
            nameAr = "حلقات الإناث - الفترة المسائية (العصر والمغرب)",
            nameEn = "Sisters Halaqat - Evening Session",
            timeSlot = GroupTimeSlot.EVENING,
            gender = GenderCategory.SISTERS,
            teacherName = "المعلمات المقرئات",
            isLiveNow = false,
            membersCount = 14,
            currentSurah = "الحفظ والمراجعة",
            liveRoomId = "room-f-evening"
        ),
        HalqahGroup(
            id = "grp_f_night",
            nameAr = "حلقات الإناث - الفترة الليلية (العشاء والسحر)",
            nameEn = "Sisters Halaqat - Night Session",
            timeSlot = GroupTimeSlot.NIGHT,
            gender = GenderCategory.SISTERS,
            teacherName = "نخبة المعلمات المسندات",
            isLiveNow = true,
            membersCount = 19,
            currentSurah = "عرض الختمات والإتقان",
            liveRoomId = "room-f-night"
        ),
        HalqahGroup(
            id = "grp_general",
            nameAr = "الملتقى القرآني العام (كافة الطلاب والمعلمين)",
            nameEn = "General Quranic Forum",
            timeSlot = GroupTimeSlot.MORNING,
            gender = GenderCategory.GENERAL,
            teacherName = "إدارة المقرأة وهيئة الإقراء",
            isLiveNow = true,
            membersCount = 45,
            currentSurah = "مذاكرة أحكام التجويد والمتون والفوائد",
            liveRoomId = "room-general"
        )
    )

    val groupsList = vmGroups ?: defaultList

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
                    Spacer(modifier = Modifier.height(4.dp))
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
                Button(onClick = { activeGroupDialog = null }) {
                    Text(text = "إغلاق")
                }
            }
        )
    }
}
