package com.example.ui.auth

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.i18n.AppLanguage
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.NotoSansEthiopicFontFamily
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicEmerald
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    currentLanguage: AppLanguage,
    onSignIn: (email: String, name: String, phone: String, gender: String, targetProgram: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth: FirebaseAuth = remember { Firebase.auth }

    var selectedAuthTab by remember { mutableStateOf(0) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var genderInput by remember { mutableStateOf("ذكر") }
    var targetProgram by remember { mutableStateOf("حلقات التلقين وتصحيح الأداء") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var showProgramPicker by remember { mutableStateOf(false) }
    val programOptions = listOf(
        "حلقات التلقين وتصحيح الأداء",
        "الحفظ المكثف وتثبيت المحفوظ",
        "برنامج الإجازة برواية حفص عن عاصم",
        "دورة إتقان التجويد وشرح الجزرية"
    )

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNotification by remember { mutableStateOf<String?>(null) }
    var showWelcomeMessage by remember { mutableStateOf(false) }
    var signedName by remember { mutableStateOf("") }

    LaunchedEffect(showWelcomeMessage) {
        if (showWelcomeMessage) {
            delay(1000) // تأخير بسيط لقراءة الرسالة
            onSignIn(
                emailInput.trim(),
                signedName.trim(),
                phoneInput.trim(),
                genderInput.trim(),
                targetProgram.trim()
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_quran_hero),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.65f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(IslamicEmerald)
                        .border(2.5.dp, IslamicGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_al_jezery),
                        contentDescription = "شعار مقرأة الإمام الجزري",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "مَقْرَأَةُ الإِمَامِ الجَزَرِيِّ",
                    fontFamily = CairoFontFamily,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "للإقراء والإجازة القرآنية بالسند المتصل",
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = showWelcomeMessage, enter = fadeIn(), exit = fadeOut()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "مرحباً بك يا $signedName في رحاب القرآن الكريم",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "جاري الدخول إلى حلقات التسميع والمقرأة...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        TabRow(
                            selectedTabIndex = selectedAuthTab,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = selectedAuthTab == 0,
                                onClick = { selectedAuthTab = 0; errorMessage = null },
                                text = { Text("تسجيل الدخول", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = selectedAuthTab == 1,
                                onClick = { selectedAuthTab = 1; errorMessage = null },
                                text = { Text("استمارة طالب جديد", fontWeight = FontWeight.Bold) }
                            )
                        }

                        if (errorMessage != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = errorMessage ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }

                        if (successNotification != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = successNotification ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }

                        // تم إخفاء زر جوجل مؤقتاً لمنع الانهيار
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "تسجيل الدخول بحساب Google متوقف مؤقتاً للتحديث. يرجى استخدام البريد الإلكتروني أدناه.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = "الدخول بالبريد الإلكتروني",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        if (selectedAuthTab == 1) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("الاسم الكامل الثلاثي") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("رقم الهاتف أو الواتساب") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "الجنس:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    listOf("ذكر", "أنثى").forEach { genderOption ->
                                        val isSelected = genderInput == genderOption
                                        OutlinedButton(
                                            onClick = { genderInput = genderOption },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
                                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                        ) {
                                            Text(text = genderOption, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                }
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().clickable { showProgramPicker = true }
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "المسار القرآني:", style = MaterialTheme.typography.labelSmall)
                                        Text(text = targetProgram, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("البريد الإلكتروني") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("كلمة المرور") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (selectedAuthTab == 1) {
                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = { confirmPasswordInput = it },
                                label = { Text("تأكيد كلمة المرور") },
                                leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Button(
                            onClick = {
                                val cleanEmail = emailInput.trim()
                                if (cleanEmail.isBlank() || passwordInput.length < 6) {
                                    errorMessage = "البريد مطلوب، وكلمة المرور يجب أن تكون 6 أحرف على الأقل"
                                    return@Button
                                }
                                if (selectedAuthTab == 1 && passwordInput != confirmPasswordInput) {
                                    errorMessage = "كلمتا المرور غير متطابقتين"
                                    return@Button
                                }

                                errorMessage = null
                                isLoading = true

                                coroutineScope.launch {
                                    try {
                                        if (selectedAuthTab == 1) {
                                            try {
                                                auth.createUserWithEmailAndPassword(cleanEmail, passwordInput).await()
                                            } catch (e: Exception) {
                                                if (e.message?.contains("already in use") == true) {
                                                    auth.signInWithEmailAndPassword(cleanEmail, passwordInput).await()
                                                } else { throw e }
                                            }
                                            signedName = nameInput.trim()
                                            successNotification = "تم تسجيلك كطالب جديد بنجاح"
                                            showWelcomeMessage = true
                                        } else {
                                            try {
                                                auth.signInWithEmailAndPassword(cleanEmail, passwordInput).await()
                                            } catch (e: Exception) {
                                                errorMessage = "كلمة المرور غير صحيحة أو الحساب غير موجود."
                                                isLoading = false
                                                return@launch
                                            }
                                            signedName = nameInput.ifBlank { cleanEmail.substringBefore("@") }
                                            showWelcomeMessage = true
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = e.localizedMessage ?: "حدث خطأ أثناء المصادقة"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "دخول / تسجيل", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showProgramPicker) {
        AlertDialog(
            onDismissRequest = { showProgramPicker = false },
            title = { Text("اختر البرنامج", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    programOptions.forEach { prog ->
                        val isSelected = targetProgram == prog
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().clickable { targetProgram = prog; showProgramPicker = false }
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = isSelected, onClick = { targetProgram = prog; showProgramPicker = false })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = prog, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showProgramPicker = false }) { Text("إغلاق") } }
        )
    }
}