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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.NotoSansEthiopicFontFamily
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBorder
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.UthmanicHafsFontFamily
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * شاشة تسجيل الدخول واستمارة التسجيل المتكاملة لمقرأة الإمام الجزري:
 * - استمارة متكاملة وجميلة تليق بالمقرأة القرآنية (اسم، هاتف، جنس، مسار الحفظ، كلمة المرور).
 * - تسجيل الدخول الحقيقي عبر Google Sign-In مع فحص ذكي للـ Client ID.
 * - حماية كاملة لحساب الإدارة: لا يوجد أي دخول مباشر بدون مصادقة وكلمة مرور.
 * - تسجيل الدخول الآمن بالبريد وكلمة المرور عبر Firebase Auth.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    currentLanguage: AppLanguage,
    onSignIn: (email: String, name: String, phone: String, gender: String, targetProgram: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth: FirebaseAuth = remember { Firebase.auth }
    val credentialManager = remember { CredentialManager.create(context) }

    // Mode: 0 = تسجيل الدخول (Sign In), 1 = استمارة طالب جديد (New Student Registration)
    var selectedAuthTab by remember { mutableStateOf(0) }

    // Form Fields
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var genderInput by remember { mutableStateOf("ذكر") } // ذكر / أنثى
    var targetProgram by remember { mutableStateOf("حلقات التلقين وتصحيح الأداء") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Dialog state for program selection
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
            // Subtle Islamic Top Header Background
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

                // Official Al-Jezery Brand Header with Modern Typography
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
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
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
                    text = "የኢማም አል-ጀዘሪ ቁርኣን ንባብ እና የሰነድ ኢጃዛ ማዕከል",
                    fontFamily = NotoSansEthiopicFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IslamicGold,
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

                // Welcome transition card
                AnimatedVisibility(visible = showWelcomeMessage, enter = fadeIn(), exit = fadeOut()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
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

                // Main Authentication Card
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
                        // Switch Tabs: تسجيل الدخول vs استمارة طالب جديد
                        TabRow(
                            selectedTabIndex = selectedAuthTab,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = selectedAuthTab == 0,
                                onClick = {
                                    selectedAuthTab = 0
                                    errorMessage = null
                                },
                                text = { Text("تسجيل الدخول", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = selectedAuthTab == 1,
                                onClick = {
                                    selectedAuthTab = 1
                                    errorMessage = null
                                },
                                text = { Text("استمارة طالب جديد", fontWeight = FontWeight.Bold) }
                            )
                        }

                        // Notifications / Errors
                        if (errorMessage != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = errorMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        if (successNotification != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = successNotification ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // 1. زر الدخول الآمن بحساب Google
                        Button(
                            onClick = {
                                errorMessage = null
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val serverClientId = try {
                                            context.getString(R.string.default_web_client_id)
                                        } catch (_: Exception) {
                                            null
                                        }

                                        if (serverClientId.isNullOrBlank() || serverClientId.contains("dummy")) {
                                            // تنبيه إذا لم يتم تنزيل google-services.json الحقيقي بعد
                                            errorMessage = "يرجى ربط بصمة SHA-1 في Firebase Console وتنزيل google-services.json لتفعيل نافذة Google، أو استخدم البريد وكلمة المرور أدناه."
                                            isLoading = false
                                            return@launch
                                        }

                                        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build()
                                        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

                                        val result = credentialManager.getCredential(context as Activity, request)
                                        val credential = result.credential
                                        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                            val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                            val authResult = auth.signInWithCredential(authCredential).await()
                                            val user = authResult.user

                                            if (user != null) {
                                                emailInput = user.email ?: ""
                                                signedName = user.displayName ?: user.email?.substringBefore("@") ?: "طالب المقرأة"
                                                showWelcomeMessage = true
                                            } else {
                                                errorMessage = "تعذر استلام بيانات الحساب من Google"
                                            }
                                        } else {
                                            errorMessage = "نوع الهوية غير مدعوم في هذا الجهاز"
                                        }
                                    } catch (e: GetCredentialCancellationException) {
                                        Log.w("Auth", "تم إلغاء اختيار حساب Google من المستخدم")
                                    } catch (e: Exception) {
                                        Log.e("Auth", "Google Sign-In failed", e)
                                        errorMessage = "تعذر الاتصال بـ Google (${e.localizedMessage ?: "تأكد من خدمات Google Play"}). يمكنك تسجيل الدخول بالبريد وكلمة المرور."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تسجيل الدخول بحساب Google",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // خط فاصل
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = if (selectedAuthTab == 0) "أو بالبريد وكلمة المرور" else "أو إكمال استمارة التسجيل",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        // ==============================================================
                        // الحقول الخاصة بـ (استمارة طالب جديد)
                        // ==============================================================
                        if (selectedAuthTab == 1) {
                            // الاسم الكامل
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("الاسم الكامل الثلاثي") },
                                placeholder = { Text("مثال: عبد الرحمن محمد عمر") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // رقم الهاتف / الواتساب
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("رقم الهاتف أو الواتساب") },
                                placeholder = { Text("+251 9... أو +966...") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // اختيار الجنس (ذكر / أنثى) لحلقات الإقراء المنفصلة
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "الجنس (لتحديد الحلقة المناسبة):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    listOf("ذكر", "أنثى").forEach { genderOption ->
                                        val isSelected = genderInput == genderOption
                                        OutlinedButton(
                                            onClick = { genderInput = genderOption },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                            ),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                        ) {
                                            Text(
                                                text = genderOption,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // البرنامج القرآني المرغوب
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showProgramPicker = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "المسار / البرنامج القرآني:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = targetProgram,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // البريد الإلكتروني
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("البريد الإلكتروني") },
                            placeholder = { Text("example@gmail.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // كلمة المرور
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("كلمة المرور") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isPasswordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور"
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // تأكيد كلمة المرور في الاستمارة
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

                        // ==============================================================
                        // زر الإجراء الرئيسي: إما تسجيل الدخول أو إرسال الاستمارة
                        // ==============================================================
                        Button(
                            onClick = {
                                val cleanEmail = emailInput.trim()
                                if (cleanEmail.isBlank()) {
                                    errorMessage = "يرجى كتابة البريد الإلكتروني"
                                    return@Button
                                }
                                if (passwordInput.isBlank() || passwordInput.length < 6) {
                                    errorMessage = "كلمة المرور يجب أن تتكون من 6 أحرف على الأقل لحماية حسابك"
                                    return@Button
                                }

                                if (selectedAuthTab == 1) {
                                    if (nameInput.isBlank()) {
                                        errorMessage = "يرجى إدخال اسمك الكامل في الاستمارة"
                                        return@Button
                                    }
                                    if (passwordInput != confirmPasswordInput) {
                                        errorMessage = "كلمتا المرور غير متطابقتين، يرجى التأكد"
                                        return@Button
                                    }
                                }

                                errorMessage = null
                                isLoading = true

                                coroutineScope.launch {
                                    try {
                                        if (selectedAuthTab == 1) {
                                            // إنشاء حساب جديد في Firebase Auth
                                            try {
                                                auth.createUserWithEmailAndPassword(cleanEmail, passwordInput).await()
                                            } catch (e: Exception) {
                                                // إذا الحساب موجود بالفعل يتم الدخول به
                                                if (e.message?.contains("already in use") == true) {
                                                    auth.signInWithEmailAndPassword(cleanEmail, passwordInput).await()
                                                } else {
                                                    throw e
                                                }
                                            }
                                            signedName = nameInput.trim()
                                            successNotification = "تم تسجيلك كطالب جديد بنجاح في $targetProgram"
                                            showWelcomeMessage = true
                                        } else {
                                            // تسجيل دخول بالبريد وكلمة المرور
                                            try {
                                                auth.signInWithEmailAndPassword(cleanEmail, passwordInput).await()
                                            } catch (e: Exception) {
                                                // فحص حالة الخطأ
                                                val msg = e.localizedMessage ?: ""
                                                if (msg.contains("password") || msg.contains("invalid-credential")) {
                                                    errorMessage = "كلمة المرور غير صحيحة، يرجى التأكد وإعادة المحاولة."
                                                    isLoading = false
                                                    return@launch
                                                } else {
                                                    // في حال لم يكن الحساب مسجلاً مسبقاً في Firebase ولكن كلمة المرور صحيحة
                                                    auth.createUserWithEmailAndPassword(cleanEmail, passwordInput).await()
                                                }
                                            }
                                            signedName = nameInput.ifBlank { cleanEmail.substringBefore("@") }
                                            showWelcomeMessage = true
                                        }
                                    } catch (e: Exception) {
                                        Log.e("Auth", "Email auth failed", e)
                                        errorMessage = e.localizedMessage ?: "حدث خطأ أثناء المصادقة"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedAuthTab == 1) Color(0xFF18794E) else MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (selectedAuthTab == 1) Icons.Default.AppRegistration else Icons.Default.Login,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedAuthTab == 1) "تسجيل وإرسال الاستمارة" else "دخول بالبريد وكلمة المرور",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // الأمان والخصوصية
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "نظام مشفر وآمن: صلاحيات الإدارة محمية ولا يمكن لأي مستخدم الوصول للوحة التحكم إلا بكلمة المرور الخاصة بالمشرف المعتمد.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet لاختيار المسار القرآني في الاستمارة
    if (showProgramPicker) {
        AlertDialog(
            onDismissRequest = { showProgramPicker = false },
            title = { Text("اختر البرنامج أو المسار القرآني", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    programOptions.forEach { prog ->
                        val isSelected = targetProgram == prog
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    targetProgram = prog
                                    showProgramPicker = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        targetProgram = prog
                                        showProgramPicker = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = prog,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProgramPicker = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}
