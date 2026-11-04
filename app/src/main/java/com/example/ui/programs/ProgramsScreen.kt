package com.example.ui.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Program
import com.example.i18n.AppLanguage
import com.example.i18n.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramsScreen(
    currentLanguage: AppLanguage
) {
    var selectedProgram by remember { mutableStateOf<Program?>(null) }
    var showEnrollDialog by remember { mutableStateOf(false) }
    var enrollmentSubmitted by remember { mutableStateOf(false) }

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
                                    AppLanguage.ENGLISH -> program.titleEn
                                    AppLanguage.AMHARIC -> program.titleAm
                                    else -> program.titleAr
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Badge {
                                Text(text = program.badgeTag)
                            }
                        }

                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.ENGLISH -> program.descriptionEn
                                else -> program.descriptionAr
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

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
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(text = Strings.get("request_enrollment", currentLanguage))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEnrollDialog && selectedProgram != null) {
        val prog = selectedProgram!!
        AlertDialog(
            onDismissRequest = { showEnrollDialog = false },
            title = { Text(text = "طلب التسجيل في البرنامج") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = prog.titleAr, fontWeight = FontWeight.Bold)
                    if (enrollmentSubmitted) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Text(text = "تم تقديم طلب التسجيل بنجاح وسيتواصل معك المشرف قريباً.", color = MaterialTheme.colorScheme.tertiary)
                        }
                    } else {
                        Text(text = "هل ترغب في تأكيد تقديم طلب الالتحاق بهذا البرنامج المبارك؟")
                    }
                }
            },
            confirmButton = {
                if (!enrollmentSubmitted) {
                    Button(onClick = { enrollmentSubmitted = true }) {
                        Text(text = "تأكيد الطلب")
                    }
                } else {
                    Button(onClick = { showEnrollDialog = false }) {
                        Text(text = "إغلاق")
                    }
                }
            },
            dismissButton = {
                if (!enrollmentSubmitted) {
                    TextButton(onClick = { showEnrollDialog = false }) {
                        Text(text = "إلغاء")
                    }
                }
            }
        )
    }
}
