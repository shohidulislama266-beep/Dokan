package com.example.ui.screens.subscription

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlanTier
import com.example.data.local.SubscriptionManager
import com.example.ui.components.AccessAllFeaturesDialog
import com.example.ui.components.GoldPremiumDot
import com.example.ui.localization.AppLanguage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isBangla = currentLanguage == AppLanguage.BANGLA

    val currentTier = SubscriptionManager.currentTier
    val expiryTimestamp = SubscriptionManager.expiryDateTimestamp
    val isTrialUsed = SubscriptionManager.isTrialUsed
    val pendingSubmissions = SubscriptionManager.pendingSubmissions

    var selectedTier by remember { mutableStateOf(PlanTier.MOBILE_PLUS_WEB) }
    var selectedMonths by remember { mutableIntStateOf(12) }
    var couponInput by remember { mutableStateOf("") }
    var appliedCoupon by remember { mutableStateOf("") }
    var discountPercent by remember { mutableIntStateOf(0) }

    var senderPhoneInput by remember { mutableStateOf("") }
    var trxIdInput by remember { mutableStateOf("") }

    var showOwnerModal by remember { mutableStateOf(false) }
    var ownerPinInput by remember { mutableStateOf("") }
    var ownerAuthenticated by remember { mutableStateOf(false) }
    var showAllFeaturesDialog by remember { mutableStateOf(false) }

    val basePrice = when (selectedTier) {
        PlanTier.MOBILE_ONLY -> when (selectedMonths) {
            3 -> SubscriptionManager.mobileOnly3Mo
            6 -> SubscriptionManager.mobileOnly6Mo
            else -> SubscriptionManager.mobileOnly12Mo
        }
        else -> when (selectedMonths) {
            3 -> SubscriptionManager.mobilePlusWeb3Mo
            6 -> SubscriptionManager.mobilePlusWeb6Mo
            else -> SubscriptionManager.mobilePlusWeb12Mo
        }
    }

    val finalPrice = if (discountPercent > 0) {
        basePrice - (basePrice * discountPercent / 100.0)
    } else {
        basePrice
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (isBangla) "প্রিমিয়াম সাবস্ক্রিপশন" else "Premium Subscription",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        GoldPremiumDot()
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showOwnerModal = true }) {
                        Icon(Icons.Outlined.AdminPanelSettings, contentDescription = "Owner Portal")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Current Plan Card
            item {
                val isPremiumActive = currentTier != PlanTier.FREE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPremiumActive) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBangla) "বর্তমান প্ল্যান: ${currentTier.titleBn}" else "Current Plan: ${currentTier.titleEn}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPremiumActive) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isPremiumActive && expiryTimestamp > 0) {
                                        "মেয়াদ শেষ: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expiryTimestamp))}"
                                    } else {
                                        if (isBangla) "বেসিক সংস্করণ সক্রিয় আছে" else "Free version active"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }
                            if (isPremiumActive) {
                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isBangla) "সক্রিয়" else "ACTIVE",
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PlanFeatureChip(label = if (currentTier == PlanTier.MOBILE_PLUS_WEB) "সীমাহীন প্রোফাইল" else "১টি দোকান")
                            PlanFeatureChip(label = if (currentTier == PlanTier.MOBILE_PLUS_WEB) "সীমাহীন কর্মী" else "৩ জন কর্মী")
                            PlanFeatureChip(label = if (currentTier == PlanTier.MOBILE_PLUS_WEB) "ওয়েব অন্তর্ভুক্ত" else "মোবাইল অনলি")
                        }

                        if (!isPremiumActive) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val ok = SubscriptionManager.activateFreeTrial(context)
                                        if (ok) {
                                            Toast.makeText(context, "৭ দিনের ফ্রি ট্রায়াল সক্রিয় করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "আপনি ইতোমধ্যেই ফ্রি ট্রায়াল ব্যবহার করেছেন!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBangla) "৭ দিনের ফ্রি ট্রায়াল" else "Try Free Trial", maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = { showAllFeaturesDialog = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (isBangla) "ফিচার তালিকা" else "All Features", maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            // Tier Selection (Mobile-only vs Mobile + Web)
            item {
                Text(
                    text = if (isBangla) "প্ল্যান নির্বাচন করুন" else "Select Subscription Plan",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(PlanTier.MOBILE_ONLY, PlanTier.MOBILE_PLUS_WEB).forEach { tier ->
                        val isSelected = selectedTier == tier
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTier = tier }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF10B981) else Color.LightGray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF10B981).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedTier = tier }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isBangla) tier.titleBn else tier.titleEn,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (tier == PlanTier.MOBILE_PLUS_WEB) {
                                        if (isBangla) "মোবাইল + ডেস্কটপ ওয়েব\nসীমাহীন স্টাফ ও ব্যাকআপ" else "Mobile + Desktop Web\nUnlimited Staff Sync"
                                    } else {
                                        if (isBangla) "১টি দোকান প্রোফাইল\n৩ জন কর্মী অ্যাকাউন্ট" else "1 Store Profile\n3 Staff Accounts"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Duration Selection (3, 6, 12 months)
            item {
                Text(
                    text = if (isBangla) "মেয়াদ নির্বাচন করুন" else "Select Duration",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                val p3 = if (selectedTier == PlanTier.MOBILE_ONLY) SubscriptionManager.mobileOnly3Mo else SubscriptionManager.mobilePlusWeb3Mo
                val p6 = if (selectedTier == PlanTier.MOBILE_ONLY) SubscriptionManager.mobileOnly6Mo else SubscriptionManager.mobilePlusWeb6Mo
                val p12 = if (selectedTier == PlanTier.MOBILE_ONLY) SubscriptionManager.mobileOnly12Mo else SubscriptionManager.mobilePlusWeb12Mo

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DurationChip(
                        label = if (isBangla) "৩ মাস" else "3 Months",
                        price = "৳${p3.toInt()}",
                        discount = "",
                        selected = selectedMonths == 3,
                        onClick = { selectedMonths = 3 },
                        modifier = Modifier.weight(1f)
                    )
                    DurationChip(
                        label = if (isBangla) "৬ মাস" else "6 Months",
                        price = "৳${p6.toInt()}",
                        discount = if (isBangla) "১০% ছাড়" else "10% OFF",
                        selected = selectedMonths == 6,
                        onClick = { selectedMonths = 6 },
                        modifier = Modifier.weight(1f)
                    )
                    DurationChip(
                        label = if (isBangla) "১ বছর" else "1 Year",
                        price = "৳${p12.toInt()}",
                        discount = if (isBangla) "২৫% সেরা" else "25% BEST",
                        selected = selectedMonths == 12,
                        onClick = { selectedMonths = 12 },
                        modifier = Modifier.weight(1.1f)
                    )
                }
            }

            // Coupon Code
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = couponInput,
                                onValueChange = { couponInput = it.uppercase() },
                                label = { Text(if (isBangla) "কুপন কোড (যেমন: DOKAN2026)" else "Coupon Code (e.g. DOKAN2026)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    val disc = SubscriptionManager.validCoupons[couponInput.trim().uppercase()] ?: 0
                                    if (disc > 0) {
                                        discountPercent = disc
                                        appliedCoupon = couponInput.trim().uppercase()
                                        Toast.makeText(context, "$disc% ডিসকাউন্ট যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "অবৈধ বা মেয়াদোত্তীর্ণ কুপন কোড", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text(if (isBangla) "প্রয়োগ" else "Apply", maxLines = 1)
                            }
                        }
                        if (discountPercent > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "✓ কুপন '$appliedCoupon' প্রয়োগ করা হয়েছে: $discountPercent% ছাড়!",
                                color = Color(0xFF10B981),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Payment by QR Code Card (Item 135)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "পরিশোধের পরিমাণ: ৳${finalPrice.toInt()}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                color = Color(0xFFE11D48).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "bKash / Nagad / Bank",
                                    color = Color(0xFFE11D48),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // QR Code Visual
                        Surface(
                            modifier = Modifier
                                .size(170.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.QrCode2,
                                        contentDescription = "পেমেন্ট QR",
                                        tint = Color.Black,
                                        modifier = Modifier.size(120.dp)
                                    )
                                    Text(
                                        "০১৭১১-২২৩৩৪৪ (মার্চেন্ট)",
                                        color = Color.DarkGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("01711223344"))
                                    Toast.makeText(context, "মার্চেন্ট নম্বর কপি হয়েছে: 01711223344", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("নম্বর কপি", maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Dokan Subscription Payment")
                                        putExtra(Intent.EXTRA_TEXT, "দোকান সাবস্ক্রিপশন ফি ৳${finalPrice.toInt()} পাঠাতে বিকাশ/নগদ মার্চেন্ট নম্বর: 01711223344। রেফারেন্সে আপনার ফোন নম্বর দিন।")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "পেমেন্ট তথ্য শেয়ার"))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("শেয়ার করুন", maxLines = 1)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text("সহজ ৪ ধাপে পেমেন্ট সম্পন্ন করুন:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("১. বিকাশ/নগদ অ্যাপে গিয়ে 'Send Money' বা 'Payment' নির্বাচন করুন।", fontSize = 11.sp, color = Color.Gray)
                            Text("২. মার্চেন্ট নম্বর 01711223344 এ ৳${finalPrice.toInt()} পাঠান।", fontSize = 11.sp, color = Color.Gray)
                            Text("৩. মেসেজে প্রাপ্ত Transaction ID (TrxID) সংরক্ষণ করুন।", fontSize = 11.sp, color = Color.Gray)
                            Text("৪. নিচের ফর্মে TrxID লিখে সাবমিট করুন। ২৪ ঘণ্টার মধ্যে যাচাই করা হবে।", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Manual Payment Verification Form (Item 136)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("পেমেন্ট যাচাইয়ের অনুরোধ পাঠান", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = senderPhoneInput,
                            onValueChange = { senderPhoneInput = it },
                            label = { Text("যে নম্বর থেকে টাকা পাঠিয়েছেন") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = trxIdInput,
                            onValueChange = { trxIdInput = it.uppercase() },
                            label = { Text("Transaction ID (TrxID)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (senderPhoneInput.isBlank() || trxIdInput.isBlank()) {
                                    Toast.makeText(context, "নম্বর এবং TrxID পূরণ করুন", Toast.LENGTH_SHORT).show()
                                } else {
                                    SubscriptionManager.submitManualPayment(
                                        context = context,
                                        tier = selectedTier,
                                        durationMonths = selectedMonths,
                                        amount = finalPrice,
                                        trxId = trxIdInput,
                                        senderPhone = senderPhoneInput
                                    )
                                    Toast.makeText(context, "পেমেন্ট অনুরোধ পাঠানো হয়েছে! ২৪ ঘণ্টার মধ্যে স্বয়ংক্রিয় সক্রিয় হবে।", Toast.LENGTH_LONG).show()
                                    senderPhoneInput = ""
                                    trxIdInput = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("পেমেন্ট যাচাইয়ের অনুরোধ পাঠান", maxLines = 1)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "যেকোনো সমস্যায় কল করুন: ০১৭০০-০০০০০০ (২৪/৭ হেল্পলাইন)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Owner Portal Dialog (Item 140)
    if (showOwnerModal) {
        AlertDialog(
            onDismissRequest = {
                showOwnerModal = false
                ownerAuthenticated = false
                ownerPinInput = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("মালিক ও অ্যাডমিন পোর্টাল", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                if (!ownerAuthenticated) {
                    Column {
                        Text("মালিকের নিরাপত্তা পিন দিয়ে প্রবেশ করুন (ডিফল্ট: 1234):", fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = ownerPinInput,
                            onValueChange = { ownerPinInput = it },
                            label = { Text("মালিক PIN") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                    ) {
                        Text("পেমেন্ট অনুমোদন (${pendingSubmissions.size}টি রেকর্ড)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (pendingSubmissions.isEmpty()) {
                            Text("বর্তমানে কোনো অপেক্ষমান পেমেন্ট নেই।", color = Color.Gray, fontSize = 12.sp)
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(pendingSubmissions) { req ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("নম্বর: ${req.senderPhone} | TrxID: ${req.trxId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("প্ল্যান: ${req.planTier.titleBn} (${req.durationMonths} মাস) - ৳${req.amountBdt.toInt()} [${req.status}]", fontSize = 11.sp, color = Color(0xFF10B981))
                                            if (req.status == "PENDING") {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(
                                                        onClick = {
                                                            SubscriptionManager.approveSubmission(context, req.id)
                                                            Toast.makeText(context, "অনুমোদিত এবং প্ল্যান সক্রিয় করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("অনুমোদন", fontSize = 12.sp, maxLines = 1)
                                                    }
                                                    OutlinedButton(
                                                        onClick = {
                                                            SubscriptionManager.rejectSubmission(context, req.id)
                                                            Toast.makeText(context, "অনুরোধ বাতিল করা হয়েছে", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("বাতিল", fontSize = 12.sp, maxLines = 1)
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
            },
            confirmButton = {
                if (!ownerAuthenticated) {
                    Button(
                        onClick = {
                            if (ownerPinInput == "1234" || ownerPinInput == "0000") {
                                ownerAuthenticated = true
                            } else {
                                Toast.makeText(context, "ভুল পিন কোড", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("প্রবেশ করুন", maxLines = 1)
                    }
                } else {
                    Button(onClick = { showOwnerModal = false }) {
                        Text("বন্ধ করুন", maxLines = 1)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showOwnerModal = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    if (showAllFeaturesDialog) {
        AccessAllFeaturesDialog(
            currentLanguage = currentLanguage,
            onDismiss = { showAllFeaturesDialog = false },
            onNavigateToPlans = { showAllFeaturesDialog = false }
        )
    }
}

@Composable
private fun PlanFeatureChip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DurationChip(
    label: String,
    price: String,
    discount: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color(0xFF10B981) else Color.LightGray.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (selected) Color(0xFF10B981).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(price, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            if (discount.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        discount,
                        color = Color(0xFFD97706),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}
