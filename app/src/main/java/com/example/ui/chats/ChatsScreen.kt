package com.example.ui.chats

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
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.border
import kotlin.math.ceil
import com.example.util.LiveKitManager
import com.example.util.SmartNotificationManager
import com.example.R
import com.example.data.model.*
import com.example.i18n.AppLanguage
import com.example.ui.theme.TelegramBlue
import com.example.viewmodel.MaqraahViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    currentLanguage: AppLanguage,
    maqraahViewModel: MaqraahViewModel
) {
    val context = LocalContext.current
    val currentUser by maqraahViewModel.currentUser.collectAsState()
    val conversations by maqraahViewModel.conversations.collectAsState()
    val groups by maqraahViewModel.groups.collectAsState()
    val allMessages by maqraahViewModel.messages.collectAsState()

    val isAdmin = maqraahViewModel.isAdminEmail(currentUser?.email) || currentUser?.role == UserRole.ADMIN

    var selectedTab by remember { mutableStateOf(0) } // 0: محادثات خاصة, 1: حلقات ومجموعات
    var activeConversation by remember { mutableStateOf<Conversation?>(null) }
    var activeGroup by remember { mutableStateOf<HalqahGroup?>(null) }
    var showParticipantProfileDialog by remember { mutableStateOf<Conversation?>(null) }

    // Group-specific dialog states
    var showGroupMembersDialog by remember { mutableStateOf(false) }
    var showEditTelegramDialog by remember { mutableStateOf(false) }
    var telegramInputText by remember { mutableStateOf("") }

    // Admin Group Creation state
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var newGroupNameAr by remember { mutableStateOf("") }
    var newGroupNameEn by remember { mutableStateOf("") }
    var newGroupGender by remember { mutableStateOf(GenderCategory.BROTHERS) }
    var newGroupSlot by remember { mutableStateOf(GroupTimeSlot.MORNING) }
    var newGroupTeacher by remember { mutableStateOf("") }
    var newGroupSurah by remember { mutableStateOf("") }
    var newGroupTelegramLink by remember { mutableStateOf("") }
    var newGroupDesc by remember { mutableStateOf("") }

    // ========================================================
    // 1. ACTIVE TELEGRAM-STYLE GROUP CHAT SCREEN
    // ========================================================
    if (activeGroup != null) {
        val grp = groups.find { it.id == activeGroup!!.id } ?: activeGroup!!
        val msgs = allMessages[grp.id] ?: emptyList()
        var groupTextToSend by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showGroupMembersDialog = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (grp.gender) {
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
                                    tint = if (grp.gender == GenderCategory.GENERAL) TelegramBlue else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = grp.nameAr,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${grp.membersCount} عضواً • انقر لعرض الأعضاء",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { activeGroup = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        // علامة البث المباشر في Header اليسار في الأعلى
                        if (isAdmin) {
                            if (!grp.isLiveNow) {
                                FilledTonalButton(
                                    onClick = {
                                        maqraahViewModel.startGroupBroadcast(grp.id, context)
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFFDC2626),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Icon(Icons.Default.Sensors, contentDescription = "بدء البث", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("بدء البث 🔴", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        maqraahViewModel.endGroupBroadcast(grp.id, context)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = "إنهاء البث", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إنهاء البث 🛑", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Surface(
                                color = if (grp.isLiveNow) Color(0xFFDC2626) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(if (grp.isLiveNow) Color.White else Color.Gray, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (grp.isLiveNow) "مباشر 🔴" else "متوقف ⚪",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (grp.isLiveNow) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { showGroupMembersDialog = true }) {
                            Icon(Icons.Default.Group, contentDescription = "أعضاء المجموعة", tint = MaterialTheme.colorScheme.primary)
                        }
                        if (isAdmin) {
                            IconButton(onClick = {
                                telegramInputText = grp.telegramLink ?: ""
                                showEditTelegramDialog = true
                            }) {
                                Icon(Icons.Default.Link, contentDescription = "تعديل رابط تليغرام", tint = TelegramBlue)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = {
                            Toast.makeText(context, "خاصية إرفاق التسجيل الصوتي أو المتن للتسميع", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.AttachFile, contentDescription = "إرفاق", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedTextField(
                            value = groupTextToSend,
                            onValueChange = { groupTextToSend = it },
                            placeholder = { Text("اكتب رسالة في حلقة التسميع...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )
                        IconButton(
                            onClick = {
                                if (groupTextToSend.isNotBlank()) {
                                    maqraahViewModel.sendGroupChatMessage(grp.id, groupTextToSend)
                                    groupTextToSend = ""
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "إرسال")
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // --- 1-ON-1 LIVEKIT VOICE ROOM & RECITATION QUEUE PANEL ---
                LiveVoiceRoomAndQueuePanel(
                    grp = grp,
                    isAdmin = isAdmin,
                    currentUser = currentUser,
                    maqraahViewModel = maqraahViewModel,
                    context = context
                )

                // --- TOP PROMINENT TELEGRAM LINK BOX ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(TelegramBlue, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = "Telegram",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "مجموعة تليغرام الرسمية للحلقة",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "للتواصل السريع والإعلانات والتنبيهات المباشرة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isAdmin) {
                                TextButton(onClick = {
                                    telegramInputText = grp.telegramLink ?: ""
                                    showEditTelegramDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = TelegramBlue)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (grp.telegramLink.isNullOrBlank()) "إضافة الرابط" else "تعديل",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TelegramBlue
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!grp.telegramLink.isNullOrBlank()) {
                            Text(
                                text = grp.telegramLink!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = TelegramBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(grp.telegramLink))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "الرابط: ${grp.telegramLink}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("الانضمام إلى مجموعة تليغرام", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Link is NOT set:
                            // "بزر الرابط لا يظهر يكون المشرف هو الذي يضعه يدويا"
                            if (isAdmin) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "لم تقم بتعيين رابط تليغرام لهذه المجموعة بعد. انقر على الزر بالأسفل لإدخال الرابط يدوياً ليظهر للطلاب.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                telegramInputText = ""
                                                showEditTelegramDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("إضافة رابط تليغرام يدوي لهذه الحلقة")
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "ملاحظة: رابط مجموعة التليغرام سيقوم المشرف العام بتحديده وإتاحته قريباً.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // --- TELEGRAM-STYLE GROUP MESSAGES LIST ---
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (msgs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.Forum,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "مرحباً بكم في ${grp.nameAr}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "المعلم المشرف: ${grp.teacherName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "المنهج والتلاوة: ${grp.currentSurah}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = grp.description.ifBlank { "اكتب رسالتك أو استفسارك هنا لبدء مدارسة القرآن والتسميع." },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(msgs) { msg ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (msg.isMine) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Surface(
                                color = if (msg.isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (msg.isMine) 16.dp else 4.dp,
                                    bottomEnd = if (msg.isMine) 4.dp else 16.dp
                                ),
                                tonalElevation = if (msg.isMine) 0.dp else 2.dp,
                                modifier = Modifier.widthIn(max = 290.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (!msg.isMine) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = msg.senderName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (msg.senderRole == UserRole.ADMIN) Color(0xFFD97706) else MaterialTheme.colorScheme.primary
                                            )
                                            if (msg.senderRole == UserRole.ADMIN) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Surface(
                                                    color = Color(0xFFFEF3C7),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "مشرف",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF92400E),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                    }
                                    Text(text = msg.content, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.align(Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = msg.timestamp,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                        if (msg.isMine) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.DoneAll,
                                                contentDescription = "مقروء",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
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

        // --- DIALOG: Group Members List Sheet ---
        if (showGroupMembersDialog) {
            AlertDialog(
                onDismissRequest = { showGroupMembersDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "أعضاء الحلقة (${grp.membersCount})", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showGroupMembersDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "المعلم المشرف: ${grp.teacherName}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(text = "المنهج: ${grp.currentSurah}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        items(grp.members) { member ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = member.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = member.role, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (member.isOnline) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color(0xFF22C55E), shape = CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showGroupMembersDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("إغلاق")
                    }
                }
            )
        }

        // --- DIALOG: Admin Edit Telegram Link ---
        if (showEditTelegramDialog) {
            AlertDialog(
                onDismissRequest = { showEditTelegramDialog = false },
                title = { Text(text = "تعيين رابط تليغرام للحلقة", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "ضع رابط مجموعة أو قناة التليغرام المخصصة لهذه الحلقة ليظهر للطلاب:")
                        OutlinedTextField(
                            value = telegramInputText,
                            onValueChange = { telegramInputText = it },
                            label = { Text("رابط التليغرام (مثال: https://t.me/maqraah_group)") },
                            placeholder = { Text("https://t.me/...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            maqraahViewModel.updateGroupTelegramLink(grp.id, telegramInputText)
                            showEditTelegramDialog = false
                            Toast.makeText(context, "تم حفظ رابط التليغرام بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E))
                    ) {
                        Text("حفظ الرابط")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showEditTelegramDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("إلغاء")
                    }
                }
            )
        }

        return
    }

    // ========================================================
    // 2. ACTIVE PRIVATE CHAT SCREEN
    // ========================================================
    if (activeConversation != null) {
        val conv = activeConversation!!
        val msgs = allMessages[conv.id] ?: emptyList()
        var textToSend by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showParticipantProfileDialog = conv }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = conv.participantName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(text = "متصل الآن • انقر لعرض الملف", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { activeConversation = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = textToSend,
                            onValueChange = { textToSend = it },
                            placeholder = { Text("اكتب رسالتك هنا...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )
                        IconButton(
                            onClick = {
                                if (textToSend.isNotBlank()) {
                                    maqraahViewModel.sendChatMessage(conv.id, textToSend)
                                    textToSend = ""
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "إرسال")
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(msgs) { msg ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (msg.isMine) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            color = if (msg.isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = if (msg.isMine) 0.dp else 1.dp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (!msg.isMine) {
                                    Text(text = msg.senderName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                                Text(text = msg.content, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.timestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // ========================================================
    // 3. MAIN TABS (PRIVATE CHATS & 7 GROUP HALAQAT)
    // ========================================================
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "المحادثات وحلقات المقرأة", fontWeight = FontWeight.Bold) },
                actions = {
                    if (selectedTab == 1 && isAdmin) {
                        IconButton(onClick = { showCreateGroupDialog = true }) {
                            Icon(Icons.Default.AddComment, contentDescription = "إنشاء مجموعة", tint = MaterialTheme.colorScheme.primary)
                        }
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
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("المحادثات الخاصة", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Chat, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("حلقات المجموعات (7)", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Groups, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                // Private Chats List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(conversations) { conv ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeConversation = conv },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(if (conv.isOfficial) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (conv.isOfficial) Icons.Default.Verified else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (conv.isOfficial) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = conv.participantName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = conv.lastMessageTime,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = conv.lastMessage,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (conv.unreadCount > 0) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape,
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${conv.unreadCount}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        fontWeight = FontWeight.Bold
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
            } else {
                // The 7 Groups List (Gender x Time Slot + General Forum)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Admin Action to create new group
                    if (isAdmin) {
                        item {
                            Button(
                                onClick = { showCreateGroupDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إنشاء حلقة / مجموعة جديدة داخل التطبيق", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "حلقات المقرأة مقسمة حسب الجنس والفترة (صباحي/مسائي/ليلي) إضافة للملتقى القرآني العام لتوفير بيئة تعليمية خاصة وميسرة لجميع الطلاب والمعلمين.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    items(groups) { group ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeGroup = group },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column {
                                Image(
                                    painter = painterResource(id = R.drawable.img_halaqah_study),
                                    contentDescription = group.nameAr,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = group.nameAr,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (group.isLiveNow) {
                                            Surface(
                                                color = Color(0xFFDC2626),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "بث مباشر 🔴",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                        Surface(
                                            color = when (group.gender) {
                                                GenderCategory.BROTHERS -> MaterialTheme.colorScheme.primaryContainer
                                                GenderCategory.SISTERS -> MaterialTheme.colorScheme.secondaryContainer
                                                GenderCategory.GENERAL -> TelegramBlue.copy(alpha = 0.15f)
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = group.gender.labelAr,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = when (group.gender) {
                                                    GenderCategory.BROTHERS -> MaterialTheme.colorScheme.primary
                                                    GenderCategory.SISTERS -> Color(0xFF92400E)
                                                    GenderCategory.GENERAL -> TelegramBlue
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "المعلم المشرف: ${group.teacherName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "التلاوة والمنهج: ${group.currentSurah}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (group.isLiveNow) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (group.activeSession != null) {
                                        Text(
                                            text = "🎙️ في البث الآن: ${group.activeSession.studentName} (${group.activeSession.studentCode})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A)
                                        )
                                    }
                                    Text(
                                        text = "⏳ طابور الانتظار: ${group.queue.size} طلاب ينتظرون دورهم",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${group.membersCount} عضواً",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!group.telegramLink.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = TelegramBlue.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "تليغرام متاح ✓",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TelegramBlue,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { activeGroup = group },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "دخول الحلقة")
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }
        }
    }

    // Modal: Participant Profile Details on clicking header
    if (showParticipantProfileDialog != null) {
        val p = showParticipantProfileDialog!!
        AlertDialog(
            onDismissRequest = { showParticipantProfileDialog = null },
            title = {
                Text(text = p.participantName, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "الرتبة: ${p.participantRole}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "كود العضو: ${p.participantCode}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "النبذة التعريفية:", fontWeight = FontWeight.Bold)
                    Text(text = p.participantBio, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "البرامج والمسارات:", fontWeight = FontWeight.Bold)
                    p.enrolledPrograms.forEach { prog ->
                        Text(text = "• $prog", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showParticipantProfileDialog = null }) {
                    Text(text = "إغلاق")
                }
            }
        )
    }

    // --- DIALOG: Admin Create Group ---
    if (showCreateGroupDialog) {
        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = { Text(text = "إنشاء حلقة / مجموعة جديدة", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = newGroupNameAr,
                            onValueChange = { newGroupNameAr = it },
                            label = { Text("اسم الحلقة بالعربية *") },
                            placeholder = { Text("مثال: حلقات الذكور - حفظ الجزرية") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text(text = "الفئة والجنس:", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = newGroupGender == GenderCategory.BROTHERS,
                                onClick = { newGroupGender = GenderCategory.BROTHERS },
                                label = { Text("ذكور") }
                            )
                            FilterChip(
                                selected = newGroupGender == GenderCategory.SISTERS,
                                onClick = { newGroupGender = GenderCategory.SISTERS },
                                label = { Text("إناث") }
                            )
                            FilterChip(
                                selected = newGroupGender == GenderCategory.GENERAL,
                                onClick = { newGroupGender = GenderCategory.GENERAL },
                                label = { Text("ملتقى عام") }
                            )
                        }
                    }
                    item {
                        Text(text = "الفترة الزمنية:", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = newGroupSlot == GroupTimeSlot.MORNING,
                                onClick = { newGroupSlot = GroupTimeSlot.MORNING },
                                label = { Text("صباحي") }
                            )
                            FilterChip(
                                selected = newGroupSlot == GroupTimeSlot.EVENING,
                                onClick = { newGroupSlot = GroupTimeSlot.EVENING },
                                label = { Text("مسائي") }
                            )
                            FilterChip(
                                selected = newGroupSlot == GroupTimeSlot.NIGHT,
                                onClick = { newGroupSlot = GroupTimeSlot.NIGHT },
                                label = { Text("ليلي") }
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = newGroupTeacher,
                            onValueChange = { newGroupTeacher = it },
                            label = { Text("المعلم المشرف") },
                            placeholder = { Text("مثال: الشيخ د. إبراهيم أحمد") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = newGroupSurah,
                            onValueChange = { newGroupSurah = it },
                            label = { Text("المنهج أو السورة") },
                            placeholder = { Text("مثال: سورة البقرة والمتون") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = newGroupTelegramLink,
                            onValueChange = { newGroupTelegramLink = it },
                            label = { Text("رابط تليغرام (اختياري)") },
                            placeholder = { Text("https://t.me/...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = newGroupDesc,
                            onValueChange = { newGroupDesc = it },
                            label = { Text("وصف الحلقة ومواعيدها") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGroupNameAr.isNotBlank()) {
                            maqraahViewModel.createGroup(
                                nameAr = newGroupNameAr,
                                nameEn = newGroupNameEn.ifBlank { newGroupNameAr },
                                gender = newGroupGender,
                                timeSlot = newGroupSlot,
                                teacherName = newGroupTeacher.ifBlank { "إدارة المقرأة" },
                                currentSurah = newGroupSurah.ifBlank { "القرآن الكريم" },
                                telegramLink = newGroupTelegramLink.ifBlank { null },
                                description = newGroupDesc
                            )
                            showCreateGroupDialog = false
                            newGroupNameAr = ""
                            newGroupTeacher = ""
                            newGroupSurah = ""
                            newGroupTelegramLink = ""
                            newGroupDesc = ""
                            Toast.makeText(context, "تم إنشاء الحلقة بنجاح", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "يرجى كتابة اسم الحلقة", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E))
                ) {
                    Text("إنشاء الحلقة")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCreateGroupDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}

/**
 * مؤشر نبضات ومستويات الصوت المباشر المشافهة
 */
@Composable
fun AudioWaveVisualizer(
    isSpeaking: Boolean,
    audioLevel: Float,
    isMuted: Boolean,
    color: Color = Color(0xFF16A34A)
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(18.dp)
    ) {
        val bar1Height = if (isMuted) 3.dp else if (isSpeaking) (6 + (audioLevel * 8)).dp else 4.dp
        val bar2Height = if (isMuted) 3.dp else if (isSpeaking) (8 + (audioLevel * 12)).dp else 6.dp
        val bar3Height = if (isMuted) 3.dp else if (isSpeaking) (11 + (audioLevel * 14)).dp else 7.dp
        val bar4Height = if (isMuted) 3.dp else if (isSpeaking) (7 + (audioLevel * 10)).dp else 5.dp

        Box(modifier = Modifier.width(3.dp).height(bar1Height).background(if (isMuted) Color.Gray else color, RoundedCornerShape(2.dp)))
        Box(modifier = Modifier.width(3.dp).height(bar2Height).background(if (isMuted) Color.Gray else color, RoundedCornerShape(2.dp)))
        Box(modifier = Modifier.width(3.dp).height(bar3Height).background(if (isMuted) Color.Gray else color, RoundedCornerShape(2.dp)))
        Box(modifier = Modifier.width(3.dp).height(bar4Height).background(if (isMuted) Color.Gray else color, RoundedCornerShape(2.dp)))
    }
}

/**
 * لوحة البث الصوتي المباشر وطابور التسميع الحقيقي (LiveKit WebRTC 1-on-1):
 * - حصر المشاركين الصوتين باثنين فقط (الشيخ المقرئ + الطالب الحالي) توفيراً للموارد وحسب سياسة البث.
 * - طابور قراءة تسلسلي بالاسم والكود (#1, #2, #3, ...) يظهر لجميع أعضاء المجموعة.
 * - أزرار تحكم المعلم الأربعة: بدء البث، بدء الجلسة والعداد، إنهاء الجلسة والخصم التلقائي، إنهاء البث.
 * - زر "أضف للبث 🎙️" بجانب كل طالب في الطابور للمعلم.
 */
@Composable
fun LiveVoiceRoomAndQueuePanel(
    grp: HalqahGroup,
    isAdmin: Boolean,
    currentUser: User?,
    maqraahViewModel: MaqraahViewModel,
    context: Context
) {
    val isLocalMicMuted by LiveKitManager.isLocalMicMuted.collectAsState()
    val teacherParticipant by LiveKitManager.teacherParticipant.collectAsState()
    val studentParticipant by LiveKitManager.studentParticipant.collectAsState()

    var isQueueExpanded by remember { mutableStateOf(true) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var targetSurahText by remember { mutableStateOf("سورة البقرة") }

    val myQueueItem = grp.queue.find { it.studentId == currentUser?.id }
    val isMyTurnInSession = grp.activeSession?.studentId == currentUser?.id

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (grp.isLiveNow) Color(0xFF022B21) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (grp.isLiveNow) Color(0xFFEF4444) else Color.Gray,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (grp.isLiveNow) "البث الصوتي المباشر مشافهة (LiveKit)" else "غرفة البث الصوتي المباشر",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (grp.isLiveNow) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (grp.isLiveNow)
                                "جلسة مشافهة ثنائية (1-on-1): الشيخ المقرئ + طالب واحد فقط"
                            else
                                "البث مغلق حالياً • يبدأه المعلم أو المشرف",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (grp.isLiveNow) Color(0xFF99F6E4) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (grp.isLiveNow) {
                    Surface(
                        color = Color(0xFFDC2626),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "LIVE 🔴",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            if (!grp.isLiveNow) {
                // When broadcast is not live yet
                if (isAdmin) {
                    Button(
                        onClick = {
                            maqraahViewModel.startGroupBroadcast(grp.id, context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("بدء البث الصوتي المباشر للحلقة 🔴", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "سيبدأ البث الصوتي فور دخول المعلم المشرف. ستصلك إشعارات فورية على شريط إشعارات الجوال فور الانطلاق.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            } else {
                // When broadcast is LIVE NOW
                // --- 1. Two Participants Pods (1-on-1: Teacher & Active Student Only) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Teacher Pod
                    Surface(
                        color = Color(0xFF044737),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF0F766E), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = grp.teacherName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "الشيخ المقرئ 🎓",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF5EEAD4)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            AudioWaveVisualizer(
                                isSpeaking = teacherParticipant?.isSpeaking ?: true,
                                audioLevel = teacherParticipant?.audioLevel ?: 0.6f,
                                isMuted = false,
                                color = Color(0xFF34D399)
                            )
                        }
                    }

                    // Student Pod (1-on-1)
                    val activeSession = grp.activeSession
                    Surface(
                        color = if (activeSession != null) Color(0xFF044737) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (activeSession != null) Color(0xFF16A34A) else Color(0xFF334155),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (activeSession != null) Icons.Default.Mic else Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeSession?.studentName ?: "بانتظار طالب",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = activeSession?.studentCode ?: "من طابور الانتظار",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (activeSession != null) Color(0xFF86EFAC) else Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (activeSession != null) {
                                AudioWaveVisualizer(
                                    isSpeaking = studentParticipant?.isSpeaking ?: true,
                                    audioLevel = studentParticipant?.audioLevel ?: 0.7f,
                                    isMuted = false,
                                    color = Color(0xFF4ADE80)
                                )
                            } else {
                                Text(
                                    text = "بانتظار الإذن",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                // --- 2. Live Session Stopwatch & Automated Deduction Preview ---
                if (grp.activeSession != null) {
                    val s = grp.activeSession
                    val elapsedSec = s.elapsedSeconds
                    val mins = elapsedSec / 60
                    val secs = elapsedSec % 60
                    val timeFormatted = String.format("%02d:%02d", mins, secs)
                    val exactDeducted = maxOf(1, ceil(elapsedSec / 60.0).toInt())

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = timeFormatted,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                                Text(
                                    text = if (s.isRunning) "(يعد بالثواني ⏱️)" else "(مؤقت متوقف ⏸️)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Text(
                                text = "المقرر: ${s.surahName} • المحتسب تلقائياً: $exactDeducted دقيقة تخصم مباشرة من رصيد ${s.studentName} عند الإنهاء بدون كتابة يدوية",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE2E8F0),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // --- 3. The 4 Teacher Controls Bar (المشرف / المعلم) ---
                if (isAdmin) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "لوحة تحكم المعلم المشرف (النقرات الأساسية الأربع):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5EEAD4)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Button 1: Start/Pause Session Timer
                            Button(
                                onClick = { maqraahViewModel.toggleSessionTimer(grp.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    if (grp.activeSession?.isRunning == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (grp.activeSession?.isRunning == true) "إيقاف ⏸️" else "تشغيل العداد ▶️",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Button 2: End Student Session & Automated Deduction
                            Button(
                                onClick = {
                                    maqraahViewModel.endStudentSessionAndDeduct(
                                        grp.id,
                                        "إنهاء التسميع والخصم الآلي",
                                        context
                                    )
                                },
                                enabled = grp.activeSession != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEA580C),
                                    disabledContainerColor = Color(0xFF475569)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.3f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "إنهاء وخصم آلي ⏹️",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Button 3: Mic Mute Toggle
                            OutlinedButton(
                                onClick = { LiveKitManager.toggleMicrophone() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    if (isLocalMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = if (isLocalMicMuted) Color(0xFFEF4444) else Color(0xFF22C55E),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isLocalMicMuted) "المايك مكتوم" else "المايك يعمل",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            // Button 4: End General Broadcast
                            Button(
                                onClick = { maqraahViewModel.endGroupBroadcast(grp.id, context) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "إنهاء البث 🛑",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // --- 4. Recitation Queue Section («طابور التسميع») ---
                Surface(
                    color = Color(0xFF063B2F),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isQueueExpanded = !isQueueExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.FormatListNumbered,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "طابور التسميع (${grp.queue.size} طلاب ينتظرون)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Icon(
                                if (isQueueExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        if (isQueueExpanded) {
                            // Student actions: take turn or show status
                            if (!isAdmin) {
                                if (isMyTurnInSession) {
                                    Surface(
                                        color = Color(0xFF16A34A),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "🎙️ أنت الآن في البث المباشر مع المعلم! صوتك مسموع للمقرئ، تفضل بالتسميع.",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                } else if (myQueueItem != null) {
                                    Surface(
                                        color = if (myQueueItem.isAdmitted) Color(0xFF15803D) else Color(0xFFD97706),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = if (myQueueItem.isAdmitted)
                                                        "تم قبولك! دورك في البث الآن 🎙️"
                                                    else
                                                        "أنت في الطابور برقم #${myQueueItem.queueNumber} ⏳",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = "المقرر: ${myQueueItem.targetSurah}",
                                                    color = Color(0xFFFEF3C7),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                            TextButton(
                                                onClick = { maqraahViewModel.leaveRecitationQueue(grp.id) },
                                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                                            ) {
                                                Text("مغادرة ❌", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                } else {
                                    // Student is NOT in queue: Show "خذ طابور"
                                    Button(
                                        onClick = { showJoinDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("خذ طابورك في التسميع (احجز دورك) 📝", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // The Queue List items
                            if (grp.queue.isEmpty()) {
                                Text(
                                    text = "لا يوجد طلاب في طابور التسميع حالياً.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF99F6E4),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    grp.queue.forEach { item ->
                                        Surface(
                                            color = if (item.isAdmitted) Color(0xFF044E3F) else Color(0xFF0B2922),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Surface(
                                                        color = if (item.isAdmitted) Color(0xFF22C55E) else Color(0xFFF59E0B),
                                                        shape = CircleShape,
                                                        modifier = Modifier.size(26.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = "#${item.queueNumber}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.Black
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = item.studentName,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                        Text(
                                                            text = "${item.studentCode} • ${item.targetSurah} • ${item.joinedAt}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color(0xFF99F6E4)
                                                        )
                                                    }
                                                }

                                                // Teacher button: "أضف" / "أضف إلى البث 🎙️"
                                                if (isAdmin) {
                                                    Button(
                                                        onClick = {
                                                            maqraahViewModel.admitStudentToLiveSession(
                                                                grp.id,
                                                                item.studentId,
                                                                context
                                                            )
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (item.isAdmitted) Color(0xFF10B981) else Color(0xFF22C55E)
                                                        ),
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Mic,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp),
                                                            tint = Color.White
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = if (item.isAdmitted) "في البث 🎙️" else "أضف للبث 🎙️",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
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
                }
            }
        }
    }

    // Modal to pick surah when joining queue
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text(text = "حجز دور في طابور التسميع", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "اختر أو اكتب السورة والمنهج المراد تسميعه أمام الشيخ:")
                    OutlinedTextField(
                        value = targetSurahText,
                        onValueChange = { targetSurahText = it },
                        label = { Text("السورة / المقرر") },
                        placeholder = { Text("مثال: سورة البقرة من الآية 1 إلى 50") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        maqraahViewModel.joinRecitationQueue(grp.id, targetSurahText)
                        showJoinDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18794E))
                ) {
                    Text("تأكيد حجز الدور")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showJoinDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}
