package com.example.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.Strings

data class OnboardingPageData(
    val titleAr: String,
    val titleEn: String,
    val titleAm: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val descriptionAm: String,
    val icon: ImageVector,
    val highlightAr: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    currentLanguage: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit,
    onFinish: () -> Unit
) {
    var currentPageIndex by remember { mutableStateOf(0) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val pages = listOf(
        OnboardingPageData(
            titleAr = "مَقْرَأَةُ الإِمَامِ الجَزَرِيِّ لِلإِقْرَاءِ وَالإِجَازَةِ بِالسَّنَد",
            titleEn = "Imam Al-Jazari Maqraah for Recitation & Ijazah with Sanad",
            titleAm = "የኢማም አል-ጀዘሪ የቁርኣን ንባብ እና የሰነድ ኢጃዛ ማዕከል",
            descriptionAr = "منصة قرآنية عالمية متخصصة تهدف إلى صيانة كتاب الله وتلقين آياته وضبط أدائه بالسند المتصل إلى خاتم الأنبياء والمرسلين ﷺ، بإشراف ثلة من كبار المقرئين المسندين.",
            descriptionEn = "A global Quranic academy dedicated to the oral transmission and mastery of the Holy Quran with authentic connected Sanad back to the Prophet Muhammad ﷺ.",
            descriptionAm = "የተከበረውን ቁርኣን በቀጥታ ሰነድ እና በትክክለኛ ተጅዊድ ወደ ነቢዩ ﷺ በሚደርስ ሰንሰለት ለማስተማር የተዘጋጀ ዓለም አቀፍ ማዕከል::",
            icon = Icons.Default.MenuBook,
            highlightAr = "السند المتصل والمشافهة العالية"
        ),
        OnboardingPageData(
            titleAr = "التلقي والمشافهة وتصحيح التلاوة",
            titleEn = "Oral Transmission & Recitation Precision",
            titleAm = "የቀጥታ ንባብ እና አነባበብ ማስተካከያ",
            descriptionAr = "تحقيق مخارج الحروف العربية وضبط صفاتها الذاتية والعارضة، مع التطبيق العملي الصارم لأحكام التجويد من خلال جلسات تسميع فردية وتوجيه دقيق من المشايخ المتقنين.",
            descriptionEn = "Mastering phonetic articulation points and Tajweed rules through direct individual recitation and scholar feedback.",
            descriptionAm = "የፊደላት መውጫዎችን እና የተጅዊድ ህጎችን በቀጥታ ከመምህራን ጋር በግል ንባብ ማስተካከል::",
            icon = Icons.Default.Hearing,
            highlightAr = "إتقان المخارج والصفات ودقائق التجويد"
        ),
        OnboardingPageData(
            titleAr = "الحفظ المكثف وتثبيت المحفوظ",
            titleEn = "Intensive Memorization & Daily Revision",
            titleAm = "የተጠናከረ የቁርኣን ሂፍዝ እና ክለሳ",
            descriptionAr = "خطط حفظ مدروسة تراعي أوقات الطلاب ومستوياتهم، تعتمد على التكرار والاستظهار والمراجعة الدورية الصارمة ليكون القرآن الكريم في صدر الحافظ كالفاتحة.",
            descriptionEn = "Structured daily memorization and cumulative revision plans ensuring the Holy Quran is firmly etched in the student's heart.",
            descriptionAm = "ቁርኣንን በአስተማማኝ ሁኔታ በልብ ለማስቀመጥ የሚያስችል የቀን ተቀን የሂፍዝ እና የክለሳ ፕሮግራም::",
            icon = Icons.Default.Psychology,
            highlightAr = "حفظ راسخ كالنقش على الحجر"
        ),
        OnboardingPageData(
            titleAr = "الإجازة بالسند المتصل والقراءات العشر",
            titleEn = "Connected Sanad Ijazah & Ten Qira'at",
            titleAm = "የሰነድ ኢጃዛ እና የዐሽር ንባቦች",
            descriptionAr = "ختمة كاملة غيباً من الفاتحة إلى الناس للحصول على شهادة الإجازة بالسند المتصل برواية حفص عن عاصم، وبرامج متخصصة لجمع القراءات السبع والعشر الصغرى والكبرى.",
            descriptionEn = "Complete memorized recitation from Al-Fatihah to An-Nas to earn authentic connected Sanad Ijazah in Hafs and the Ten Qira'at.",
            descriptionAm = "ሙሉ ቁርኣንን በቃላቸው አንብበው ለሚያጠናቅቁ የሃፍስ እና የዐሽር ንባቦች ህጋዊ የቀጥታ ሰነድ ምስክር ወረቀት::",
            icon = Icons.Default.WorkspacePremium,
            highlightAr = "سند متصل إلى رسول الله ﷺ"
        )
    )

    val page = pages[currentPageIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    TextButton(onClick = { showLanguageDialog = true }) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentLanguage.displayName,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (currentPageIndex < pages.size - 1) {
                        TextButton(onClick = onFinish) {
                            Text(
                                text = Strings.get("skip", currentLanguage),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPageIndex > 0) {
                        OutlinedButton(
                            onClick = { currentPageIndex-- },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = Strings.get("previous", currentLanguage))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }

                    // Progress Dots
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pages.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .size(
                                        width = if (index == currentPageIndex) 24.dp else 8.dp,
                                        height = 8.dp
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        if (index == currentPageIndex)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }

                    if (currentPageIndex < pages.size - 1) {
                        Button(
                            onClick = { currentPageIndex++ },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = Strings.get("next", currentLanguage), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onFinish,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = Strings.get("start_now", currentLanguage), fontWeight = FontWeight.Bold)
                        }
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = page.highlightAr,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (currentLanguage) {
                    AppLanguage.ENGLISH -> page.titleEn
                    AppLanguage.AMHARIC -> page.titleAm
                    else -> page.titleAr
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (currentLanguage) {
                    AppLanguage.ENGLISH -> page.descriptionEn
                    AppLanguage.AMHARIC -> page.descriptionAm
                    else -> page.descriptionAr
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = "اختر لغة التطبيق / Select Language") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.values().forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (lang == currentLanguage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = lang == currentLanguage,
                                onClick = {
                                    onLanguageChanged(lang)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lang.displayName, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(text = "تم / Done")
                }
            }
        )
    }
}
