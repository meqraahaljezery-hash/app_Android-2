package com.example.ui.auth

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.i18n.AppLanguage
import com.example.ui.theme.TajawalFontFamily
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldContainer
import com.example.ui.theme.IslamicGoldBorder
import com.example.ui.theme.LuxuryBackground
import com.example.ui.theme.SurfaceBorderDecor
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
    var isGoogleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNotification by remember { mutableStateOf<String?>(null) }
    var showWelcomeMessage by remember { mutableStateOf(false) }
    var signedName by remember { mutableStateOf("") }

    LaunchedEffect(showWelcomeMessage) {
        if (showWelcomeMessage) {
            delay(1200)
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
            // خلفية علوية فاخرة مع صورة المصحف الشريف
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
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
                                    Color.Black.copy(alpha = 0.70f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )
            }

            val scrollState = rememberScrollState()
            val density = LocalDensity.current
            val imeBottom = WindowInsets.ime.getBottom(density)

            LaunchedEffect(imeBottom) {
                if (imeBottom > 0) {
                    // التمرير التلقائي للأعلى فور فتح الكيبورد لإظهار حقول الإدخال والزر
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(26.dp))

                // شعار المقرأة الرسمي المؤطر بالذهب الإسلامي
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(IslamicEmerald)
                        .border(3.dp, IslamicGold, CircleShape)
                        .shadow(elevation = 6.dp, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_al_jezery),
                        contentDescription = "شعار مقرأة الإمام الجزري",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "مَقْرَأَةُ الإِمَامِ الجَزَرِيِّ",
                    fontFamily = TajawalFontFamily,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "للإقراء والإجازة القرآنية بالسند المتصل",
                    fontFamily = TajawalFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // بطاقة الترحيب المتحركة
                AnimatedVisibility(
                    visible = showWelcomeMessage,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "جاري الدخول إلى حلقات التسميع والمقرأة...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // البطاقة الرئيسية بنمط فاخر وحواف 20dp وظل 4dp
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SurfaceBorderDecor)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // تبويب تسجيل الدخول / حساب جديد
                        TabRow(
                            selectedTabIndex = selectedAuthTab,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clip(RoundedCornerShape(14.dp))
                        ) {
                            Tab(
                                selected = selectedAuthTab == 0,
                                onClick = { selectedAuthTab = 0; errorMessage = null },
                                text = { Text("تسجيل الدخول", fontWeight = FontWeight.Bold, fontFamily = TajawalFontFamily) }
                            )
                            Tab(
                                selected = selectedAuthTab == 1,
                                onClick = { selectedAuthTab = 1; errorMessage = null },
                                text = { Text("استمارة طالب جديد", fontWeight = FontWeight.Bold, fontFamily = TajawalFontFamily) }
                            )
                        }

                        // رسائل التنبيهات والأخطاء
                        if (errorMessage != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = errorMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = TajawalFontFamily,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        if (successNotification != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = successNotification ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = TajawalFontFamily,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // زر الدخول السريع بحساب Google بأمان وتصميم تجاري
                        OutlinedButton(
                            onClick = {
                                isGoogleLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    try {
                                        val credentialManager = CredentialManager.create(context)
                                        // محاولة استدعاء الدخول السريع بـ Google
                                        val googleIdOption = GetSignInWithGoogleOption.Builder("default-web-client-id")
                                            .build()
                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()
                                        val result = credentialManager.getCredential(
                                            request = request,
                                            context = context as Activity
                                        )
                                        val credential = result.credential
                                        if (credential is GoogleIdTokenCredential) {
                                            val idToken = credential.idToken
                                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                                            val authResult = auth.signInWithCredential(firebaseCredential).await()
                                            val user = authResult.user
                                            signedName = user?.displayName ?: user?.email?.substringBefore("@") ?: "طالب المقرأة"
                                            emailInput = user?.email ?: ""
                                            showWelcomeMessage = true
                                        }
                                    } catch (e: Exception) {
                                        Log.w("AuthScreen", "Google Sign-In fallback: ${e.message}")
                                        // إذا لم تتوفر خدمات Google Play في الجهاز أو لم تُهيأ يتم التوجيه بلطف لاستخدام البريد الإلكتروني
                                        errorMessage = "يرجى تسجيل الدخول بالبريد الإلكتروني وكلمة المرور أدناه لمتابعة الدراسة وحلقات التسميع."
                                    } finally {
                                        isGoogleLoading = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, IslamicGoldBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = IslamicEmerald)
                            } else {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = IslamicEmerald,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "المتابعة السريعة بحساب Google",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // فاصل أنيق
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorderDecor)
                            Text(
                                text = "أو عبر البريد الإلكتروني",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = TajawalFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorderDecor)
                        }

                        // حقول استمارة الطالب الجديد
                        if (selectedAuthTab == 1) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("الاسم الكامل الثلاثي", fontFamily = TajawalFontFamily) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("رقم الهاتف أو الواتساب", fontFamily = TajawalFontFamily) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "الجنس:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    listOf("ذكر", "أنثى").forEach { genderOption ->
                                        val isSelected = genderInput == genderOption
                                        val btnColor by animateColorAsState(
                                            targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                            label = "genderColor"
                                        )
                                        OutlinedButton(
                                            onClick = { genderInput = genderOption },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(containerColor = btnColor),
                                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                        ) {
                                            Text(
                                                text = genderOption,
                                                fontFamily = TajawalFontFamily,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // محدد المسار القرآني
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, SurfaceBorderDecor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showProgramPicker = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "المسار القرآني المرغوب:", style = MaterialTheme.typography.labelSmall, fontFamily = TajawalFontFamily)
                                        Text(
                                            text = targetProgram,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = TajawalFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        // البريد الإلكتروني
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("البريد الإلكتروني", fontFamily = TajawalFontFamily) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // كلمة المرور
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("كلمة المرور", fontFamily = TajawalFontFamily) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        if (selectedAuthTab == 1) {
                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = { confirmPasswordInput = it },
                                label = { Text("تأكيد كلمة المرور", fontFamily = TajawalFontFamily) },
                                leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        // زر الإجراء الرئيسي بتدرج زمردي فاخر وظل أنيق
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
                                            successNotification = "تم تسجيلك كطالب جديد في المقرأة بنجاح"
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (selectedAuthTab == 0) "تسجيل الدخول للمقرأة" else "إتمام تسجيل الطالب",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // نافذة منبثقة فاخرة مخصصة (Custom Dialog with 24.dp shape & colored header)
    if (showProgramPicker) {
        Dialog(
            onDismissRequest = { showProgramPicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column {
                    // ترويسة ملونة فاخرة
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(IslamicEmerald, IslamicEmeraldDark)
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = IslamicGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "اختر المسار القرآني المبارك",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // خيارات البرامج القرآنية
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        programOptions.forEach { prog ->
                            val isSelected = targetProgram == prog
                            val bgColor by animateColorAsState(
                                targetValue = if (isSelected) IslamicEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                label = "pickerColor"
                            )
                            Surface(
                                color = bgColor,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, if (isSelected) IslamicEmerald else SurfaceBorderDecor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        targetProgram = prog
                                        showProgramPicker = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            targetProgram = prog
                                            showProgramPicker = false
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = IslamicEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = prog,
                                        fontFamily = TajawalFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) IslamicEmeraldDark else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        TextButton(
                            onClick = { showProgramPicker = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                "إلغاء",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}