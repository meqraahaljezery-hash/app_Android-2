package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.i18n.AppLanguage
import com.example.ui.theme.*

/**
 * فريمات وزخارف إسلامية حديثة وذكية (Modern Decorated Islamic Frames & Components)
 * مصممة لمقرأة الإمام الجزري لتضفي فخامة إسلامية عصرية متناسقة مع شعار الجزري.
 */

@Composable
fun IslamicFrameCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = IslamicGoldBorder,
    showCornerStars: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        colors = listOf(
                            IslamicGold.copy(alpha = 0.8f),
                            IslamicEmerald.copy(alpha = 0.5f),
                            IslamicGold.copy(alpha = 0.8f)
                        )
                    )
                ),
                RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (showCornerStars) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "۞", color = IslamicGold, fontSize = 14.sp)
                    Text(text = "﷽", fontFamily = UthmanicHafsFontFamily, color = IslamicGold, fontSize = 14.sp)
                    Text(text = "۞", color = IslamicGold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            content()
        }
    }
}

/**
 * رأس الصفحة الإسلامي المزين بشعار الجزري الرسمي
 */
@Composable
fun IslamicEmblemHeader(
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        IslamicEmerald,
                        IslamicEmeraldMedium
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(top = 18.dp, bottom = 22.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // الشعار الرسمي "الجزري" / "AL-JEZERY" داخل إطار ذهبي مذهب
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(IslamicEmerald)
                .border(2.5.dp, IslamicGoldBright, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo_al_jezery),
                contentDescription = "شعار مقرأة الإمام الجزري",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "مَقْرَأَةُ الإِمَامِ الجَزَرِيِّ",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = "የኢማም አል-ጀዘሪ ቁርኣን ንባብ እና ሰነድ ማዕከል",
            fontFamily = NotoSansEthiopicFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = IslamicGoldBright,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Quranic Recitation & Ijazah with Connected Sanad",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * فاصل مزخرف إسلامي حديث
 */
@Composable
fun IslamicOrnamentDivider(
    modifier: Modifier = Modifier,
    color: Color = IslamicGoldBorder
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = color.copy(alpha = 0.5f))
        Text(
            text = "  ۞  ",
            color = IslamicGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = color.copy(alpha = 0.5f))
    }
}
