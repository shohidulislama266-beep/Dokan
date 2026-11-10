package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SubscriptionManager
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary

// Gold indicator color for premium-locked features (Item 139)
val GoldPremiumDotColor = Color(0xFFF59E0B)

@Composable
fun GoldPremiumDot(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(GoldPremiumDotColor)
            .testTag("gold_premium_dot")
    )
}

@Composable
fun PremiumFeatureRow(
    title: String,
    subtitle: String? = null,
    isLocked: Boolean,
    onLockedClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isLocked) { onLockedClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (isLocked) {
                GoldPremiumDot(modifier = Modifier.padding(end = 8.dp))
            }
            Column {
                Text(text = title, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                if (!subtitle.isNullOrBlank()) {
                    Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
                }
            }
        }
        content()
    }
}

// Access All Features Popup (Item 137)
@Composable
fun AccessAllFeaturesDialog(
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onNavigateToPlans: () -> Unit
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GoldPremiumDotColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPremiumDotColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isBangla) "সকল প্রিমিয়াম ফিচার আনলক করুন" else "Access All Features",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isBangla) "দোকানের সকল অ্যাডভান্সড টুলস ব্যবহার করুন" else "Supercharge your business with Dokan Pro",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val features = listOf(
                    if (isBangla) "বারকোড স্ক্যানার ও দ্রুত সেল" else "Barcode Scanner & Fast Checkout",
                    if (isBangla) "পাইকারি ও MRP প্রাইসিং কন্ট্রোল" else "Wholesale Price & MRP Controls",
                    if (isBangla) "বিলের ছবি আপলোড ও বিল গ্যালারি" else "Bill Gallery & Invoice Image Storage",
                    if (isBangla) "দোকানের ব্র্যান্ডিং রিমুভ ও কাস্টম ইনভয়েস" else "Hide App Branding & Extra Invoices",
                    if (isBangla) "AI কার্ড মেকার ও ডিজিটাল ভিজিটিং কার্ড" else "Gemini AI Cards & Digital Visiting Card",
                    if (isBangla) "মাল্টি-ফোন স্টাফ রিয়েল-টাইম সিঙ্ক" else "Multi-Device Staff Real-Time Sync"
                )

                features.forEach { feat ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = feat, fontSize = 12.sp, color = TextPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onNavigateToPlans()
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_upgrade_from_modal")
            ) {
                Text(if (isBangla) "প্ল্যান দেখুন ও আপগ্রেড করুন" else "View Plans & Upgrade", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_close_feature_modal")
            ) {
                Text(if (isBangla) "বন্ধ করুন" else "Close", color = TextMuted)
            }
        }
    )
}
