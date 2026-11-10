package com.example.ui.programs

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.Program
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.viewmodel.MaqraahViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramsScreen(
    currentLanguage: AppLanguage,
    maqraahViewModel: MaqraahViewModel
) {
    var selectedProgram by remember { mutableStateOf<Program?>(null) }
    var showEnrollDialog by remember { mutableStateOf(false) }
    var enrollmentSubmitted by remember { mutableStateOf(false) }

    val currentUser by maqraahViewModel.currentUser.collectAsState()
    
    // متغيرات النموذج
    var studentName by remember { mutableStateOf(currentUser?.name ?: "") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("ذكر") }
    var city by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var memorization by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var timeSlot by remember { mutableStateOf("صباحي") }

    val programsList = listOf(
        Program(
            id = "prog_hafs_ijazah",
            titleAr = "برنامج الإجازة برواية حفص عن عاصم",
            titleEn = "Hafs An Asim Ijazah Program",
            titleAm = "የሃፍስ ከዓስም የኢጃዛ ፕሮግራም",
            descriptionAr = "إقراء كامل للقرآن الكريم برواية حفص من طريق الشاطبية مع تحقيق المخارج والصفات وإثبات الأداء بالسند.",
            descriptionEn = "Complete Quran recitation with Hafs narration via Shatibiyyah with rigorous Tajweed and Sanad.",
            instructorName = "الشيخ د. القارئ إبراهيم أحمد",
            instructorSanad = "سند متصل إلى الإمام عاصم والشاطبي",
            durationText = "6 - 12 شهر",
            totalHalaqat = 48,
            prerequisiteAr = "إتقان أحكام التجويد النظرية والتطبيقية",
            badgeTag = "إجازة بالسند المتصل",
            enrolledCount = 42
        ),
        Program(
            id = "prog_tajweed",
            titleAr = "دورة إتقان التجويد والمخارج والصفات",
            titleEn = "Mastering Tajweed & Articulation Points",
            titleAm = "የተጅዊድ እና የፊደላት መውጫ ትምህርት",
            descriptionAr = "شرح متن المقدمة الجزرية وتطبيق أحكام النون والميم الساكنتين والمدود ومخارج حروف الهجاء بدقة.",
            descriptionEn = "Detailed study of Al-Jazariyyah text, articulation points, and Tajweed rules.",
            instructorName = "الشيخ عبد الله محمد",
            instructorSanad = "مسند في المتون العشرة",
            durationText = "3 أشهر",
            totalHalaqat = 24,
            prerequisiteAr = "معرفة القراءة العربية الأساسية",
            badgeTag = "دورة تأسيسية",
            enrolledCount = 28
        ),
        Program(
            id = "prog_qiraat_ashar",
            titleAr = "برنامج القراءات العشر الكبرى والصغرى",
            titleEn = "Ten Qira'at (Sughra & Kubra)",
            titleAm = "የዐሽር ቁርኣን ንባቦች ፕሮግራም",
            descriptionAr = "جمع القراءات العشر من الشاطبية والدرة وطيبة النشر بإشراف كبار المقرئين المسندين.",
            descriptionEn = "Recitation of the Ten Qira'at via Shatibiyyah, Durrah, and Tayyibat An-Nashr.",
            instructorName = "فضيلة الشيخ المقرئ الجامع",
            instructorSanad = "إجازة عالية بالقراءات العشر",
            durationText = "24 شهر",
            totalHalaqat = 120,
            prerequisiteAr = "ختمة كاملة متقنة برواية حفص",
            badgeTag = "عالي الإسناد",
            enrolledCount = 15
        ),
        Program(
            id = "prog_memorization",
            titleAr = "حلقة الحفظ والمراجعة وتثبيت المحفوظ",
            titleEn = "Memorization & Revision Circle",
            titleAm = "የቁርኣን ሂፍዝ እና ክለሳ ክበብ",
            descriptionAr = "متابعة يومية للحفظ الجديد والمراجعة الدورية للمتون والقرآن الكريم بإشراف محفظين مجازين.",
            descriptionEn = "Daily follow-up for Quran memorization and systematic revision.",
            instructorName = "الشيخ محمد أمين",
            instructorSanad = "حفظ متقن وسند عالي",
            durationText = "مستمر",
            totalHalaqat = 150,
            prerequisiteAr = "الهمة والرغبة الجادة",
            badgeTag = "تثبيت ومتابعة",
            enrolledCount = 60
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = Strings.get("tab_programs", currentLanguage), fontWeight = FontWeight.Bold) },
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
            items(programsList) { program ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column {
                        val bannerImage = if (program.id.contains("ijazah")) R.drawable.img_ijazah_sanad else R.drawable.img_halaqah_study
                        Image(
                            painter = painterResource(id = bannerImage),
                            contentDescription = program.titleAr,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text(text = program.badgeTag, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }

                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.ENGLISH -> program.descriptionEn
                                    else -> program.descriptionAr
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 22.sp
                            )

                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "المشرف: ${program.instructorName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "المدة: ${program.durationText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Button(
                                    onClick = {
                                        selectedProgram = program
                                        showEnrollDialog = true
                                        enrollmentSubmitted = false
                                        studentName = currentUser?.name ?: ""
                                        age = ""
                                        city = ""
                                        phone = currentUser?.phone ?: ""
                                        memorization = ""
                                        experience = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(text = Strings.get("request_enrollment", currentLanguage), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEnrollDialog && selectedProgram != null) {
        val prog = selectedProgram!!
        Dialog(
            onDismissRequest = { showEnrollDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f)
                    .imePadding()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                if (enrollmentSubmitted) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp).background(Color(0xFFE8F5E9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(50.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = "تم التسجيل بنجاح!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "تم تقديم طلبك للالتحاق ببرنامج\n${prog.titleAr}\nوسيتواصل معك المشرف قريباً بإذن الله.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = { showEnrollDialog = false }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                            Text("العودة للبرامج", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))),
                                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { showEnrollDialog = false }) { Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White) }
                                    Icon(Icons.Default.School, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "استمارة التسجيل", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = prog.titleAr, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(value = studentName, onValueChange = { studentName = it }, label = { Text("الاسم الكامل") }, leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
                            OutlinedTextField(value = age, onValueChange = { if (it.all { char -> char.isDigit() }) age = it }, label = { Text("العمر") }, leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
                            
                            Text("الجنس", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf("ذكر", "أنثى").forEach { option ->
                                    val isSelected = gender == option
                                    val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, label = "bg")
                                    val borderColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, label = "border")
                                    Surface(
                                        modifier = Modifier.weight(1f).height(50.dp).clickable { gender = option },
                                        shape = RoundedCornerShape(12.dp), color = bgColor, border = BorderStroke(1.dp, borderColor)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) { Text(text = option, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
                                    }
                                }
                            }

                            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("المدينة") }, leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
                            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم الهاتف") }, leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
                            OutlinedTextField(value = memorization, onValueChange = { memorization = it }, label = { Text("مقدار الحفظ الحالي") }, leadingIcon = { Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
                            OutlinedTextField(value = experience, onValueChange = { experience = it }, label = { Text("الخبرة السابقة") }, leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), maxLines = 2)

                            Text("الوقت المفضل", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("صباحي", "مسائي", "ليلي").forEach { option ->
                                    val isSelected = timeSlot == option
                                    val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface, label = "bg")
                                    val borderColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant, label = "border")
                                    Surface(
                                        modifier = Modifier.weight(1f).height(45.dp).clickable { timeSlot = option },
                                        shape = RoundedCornerShape(10.dp), color = bgColor, border = BorderStroke(1.dp, borderColor)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) { Text(text = option, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface) }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                            Button(
                                onClick = {
                                    if (studentName.isNotBlank() && age.isNotBlank() && city.isNotBlank() && phone.isNotBlank()) {
                                        val isSuccess = maqraahViewModel.submitEnrollmentRequest(prog, age.toIntOrNull() ?: 20, gender, city, phone, memorization.ifBlank { "لا يوجد" }, experience.ifBlank { "لا يوجد" }, timeSlot)
                                        if (isSuccess) enrollmentSubmitted = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                            ) {
                                Text("تأكيد وإرسال الطلب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}