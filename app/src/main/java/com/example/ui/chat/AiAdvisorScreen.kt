package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatMessage
import com.example.data.model.UserRole
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAdvisorScreen(
    currentLanguage: AppLanguage
) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "msg_1",
                conversationId = "ai_chat",
                senderId = "ai_bot",
                senderName = "المستشار الذكي (مقرأة الجزري)",
                senderRole = UserRole.TEACHER,
                content = Strings.get("ai_advisor_intro", currentLanguage),
                timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                isMine = false
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = Strings.get("tab_chat", currentLanguage), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(text = "اكتب سؤالك في أحكام التجويد أو البرامج...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp)
                    )
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                val userMsg = messageText
                                messages.add(
                                    ChatMessage(
                                        id = UUID.randomUUID().toString(),
                                        conversationId = "ai_chat",
                                        senderId = "student_me",
                                        senderName = "أنا",
                                        senderRole = UserRole.STUDENT,
                                        content = userMsg,
                                        timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                        isMine = true
                                    )
                                )
                                messageText = ""
                                // Simulated AI response
                                messages.add(
                                    ChatMessage(
                                        id = UUID.randomUUID().toString(),
                                        conversationId = "ai_chat",
                                        senderId = "ai_bot",
                                        senderName = "المستشار الذكي",
                                        senderRole = UserRole.TEACHER,
                                        content = "أهلاً بك. بالنسبة لسؤالك الكريم حول أحكام التجويد، ننصحك بالانضمام إلى حلقة الإمام الشاطبي الصباحية ومراجعة مخارج الحروف بعناية.",
                                        timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                        isMine = false
                                    )
                                )
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (msg.isMine) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 300.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!msg.isMine) {
                                Text(
                                    text = msg.senderName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = msg.content,
                                style = MaterialTheme.typography.bodyMedium
                            )
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
}
