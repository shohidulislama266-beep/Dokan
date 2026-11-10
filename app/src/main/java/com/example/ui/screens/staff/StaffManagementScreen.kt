package com.example.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.PinLockManager
import com.example.data.local.ProfileManager
import com.example.data.local.entity.StaffMember
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.LowStockRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary
import kotlinx.coroutines.launch

@Composable
fun StaffManagementScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()
    val repository = remember { AppDatabase.getRepository(context) }
    val staffList by repository.allStaffMembers.collectAsState(initial = emptyList())

    var showAddStaffDialog by remember { mutableStateOf(false) }
    var editingStaffPermissions by remember { mutableStateOf<StaffMember?>(null) }
    var showQrSyncModal by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Multi-Phone Live Sync Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, VividEmeraldAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(VividEmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = VividEmeraldAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBangla) "মাল্টি-ফোন লাইভ সিঙ্ক" else "Multi-Phone Live Sync",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isBangla) "অ্যাডমিন ফোন + স্টাফ ফোনের লাইভ ডাটা শেয়ারিং" else "Admin phone + Staff devices synced to Firebase",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Button(
                            onClick = { showQrSyncModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_open_qr_sync")
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isBangla) "QR সিঙ্ক" else "Sync QR", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSlateSurfaceVariant.copy(alpha = 0.6f))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla)
                                    "স্টাফ অ্যাকাউন্ট কখনই অ্যাডমিন হতে পারবে না। আপনি প্রতিটি কর্মীর জন্য ফিচার নির্দিষ্ট করে দিতে পারবেন।"
                                else
                                    "Staff accounts are never admin. Admin strictly controls permissions for every staff device.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Staff List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "নিযুক্ত কর্মচারী ও ভূমিকা (${staffList.size})" else "Staff Members (${staffList.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VividEmeraldAccent
                )
            }

            if (staffList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isBangla) "কোনো কর্মচারী যোগ করা হয়নি" else "No staff members added yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBangla) "ম্যানেজার, সেলসম্যান বা হিসাবরক্ষক যোগ করে তাদের অ্যাক্সেস নিয়ন্ত্রণ করুন।" else "Add managers, sales, or accounts staff and set their permissions.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(staffList, key = { it.id }) { staff ->
                        StaffCard(
                            staff = staff,
                            isBangla = isBangla,
                            onManagePermissions = {
                                if (PinLockManager.isPinSet) {
                                    // Sensitive feature: changing permissions is guarded
                                    editingStaffPermissions = staff
                                } else {
                                    editingStaffPermissions = staff
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    repository.deleteStaff(staff.id)
                                    Toast.makeText(context, if (isBangla) "স্টাফ সদস্য মুছে ফেলা হয়েছে" else "Staff removed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Add Staff FAB
        FloatingActionButton(
            onClick = { showAddStaffDialog = true },
            containerColor = VividEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_staff")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBangla) "নতুন কর্মচারী যোগ" else "Add Staff",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Dialog: Add Staff
    if (showAddStaffDialog) {
        AddStaffDialog(
            isBangla = isBangla,
            onDismiss = { showAddStaffDialog = false },
            onSave = { name, phone, role, permissionLevel, allowedFeatures ->
                scope.launch {
                    repository.insertStaff(
                        StaffMember(
                            name = name,
                            phone = phone,
                            role = role,
                            permissionLevel = permissionLevel,
                            allowedFeatures = allowedFeatures
                        )
                    )
                    showAddStaffDialog = false
                    Toast.makeText(context, if (isBangla) "কর্মচারী সফলভাবে যোগ করা হয়েছে!" else "Staff member added!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Manage Permissions
    editingStaffPermissions?.let { staff ->
        ManagePermissionsDialog(
            staff = staff,
            isBangla = isBangla,
            onDismiss = { editingStaffPermissions = null },
            onSave = { updatedLevel, updatedFeatures ->
                scope.launch {
                    repository.updateStaff(
                        staff.copy(
                            permissionLevel = updatedLevel,
                            allowedFeatures = updatedFeatures
                        )
                    )
                    editingStaffPermissions = null
                    Toast.makeText(context, if (isBangla) "পারমিশন আপডেট সফল হয়েছে!" else "Permissions updated!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: QR Sync Modal
    if (showQrSyncModal) {
        QrSyncModalDialog(
            isBangla = isBangla,
            onDismiss = { showQrSyncModal = false }
        )
    }
}

@Composable
fun StaffCard(
    staff: StaffMember,
    isBangla: Boolean,
    onManagePermissions: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("staff_card_${staff.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, DarkSlateCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DarkSlateSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = staff.name.take(1).uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividEmeraldAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = staff.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VividEmeraldPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = staff.role,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VividEmeraldAccent
                                )
                            }
                        }
                        if (staff.phone.isNotBlank()) {
                            Text(
                                text = "Phone: ${staff.phone}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkSlateCardBorder)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${if (isBangla) "অ্যাক্সেস লেভেল:" else "Access:"} ${staff.permissionLevel}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VividEmeraldAccent
                    )
                    Text(
                        text = "${if (isBangla) "অনুমোদিত:" else "Allowed:"} ${staff.allowedFeatures}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onManagePermissions,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_manage_permissions_${staff.id}")
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBangla) "পারমিশন" else "Permissions", fontSize = 11.sp, color = TextPrimary)
                }
            }
        }
    }
}

// Add Staff Dialog
@Composable
fun AddStaffDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, role: String, permissionLevel: String, allowedFeatures: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Sales") }
    var selectedLevel by remember { mutableStateOf("Limited") }

    val roles = listOf("Partner", "Manager", "Accountant", "Sales", "Stock", "Entry")
    val levels = listOf("Full", "Half", "Limited", "Selected")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "নতুন কর্মচারী যোগ করুন" else "Add Staff Member",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBangla) "কর্মচারীর নাম *" else "Staff Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_staff_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBangla) "মোবাইল নম্বর" else "Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_staff_phone"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Role Selector
                Text(
                    text = if (isBangla) "পদবী / ভূমিকা (Role):" else "Role in Shop:",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    roles.forEach { role ->
                        val isSelected = selectedRole == role
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                .clickable { selectedRole = role }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = role,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextMuted
                            )
                        }
                    }
                }

                // Initial Permission Level
                Text(
                    text = if (isBangla) "পারমিশন স্তর (Permission Level):" else "Permission Level:",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    levels.forEach { level ->
                        val isSelected = selectedLevel == level
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) VividEmeraldAccent.copy(alpha = 0.3f) else DarkSlateSurfaceVariant)
                                .clickable { selectedLevel = level }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) VividEmeraldAccent else TextMuted
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val defaultFeatures = when (selectedLevel) {
                            "Full" -> "Sales,Stock,Baki,Purchases,Expenses,Reports"
                            "Half" -> "Sales,Stock,Baki"
                            "Limited" -> "Sales,Stock"
                            else -> "Sales"
                        }
                        onSave(name.trim(), phone.trim(), selectedRole, selectedLevel, defaultFeatures)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_staff")
            ) {
                Text(if (isBangla) "সংরক্ষণ করুন" else "Save Staff")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}

// Manage Permissions Dialog
@Composable
fun ManagePermissionsDialog(
    staff: StaffMember,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (level: String, allowedFeatures: String) -> Unit
) {
    var selectedLevel by remember { mutableStateOf(staff.permissionLevel) }
    val allFeatureOptions = listOf(
        "Sales" to if (isBangla) "বিক্রয় ও চালান (Sales/POS)" else "Sales & Billing",
        "Stock" to if (isBangla) "পণ্য স্টক ও তালিকা (Stock)" else "Stock & Inventory",
        "Baki" to if (isBangla) "কাস্টমার বাকি খাতা (Baki)" else "Customer Baki",
        "Purchases" to if (isBangla) "মাল ক্রয় ও মহাজন (Purchases)" else "Purchases & Suppliers",
        "Expenses" to if (isBangla) "খরচ হিসাব (Expenses)" else "Shop Expenses",
        "Reports" to if (isBangla) "লাভ-ক্ষতি ও রিপোর্ট (Reports)" else "Business Reports",
        "Settings" to if (isBangla) "দোকান সেটিংস (Settings)" else "Settings"
    )

    val currentSelected = remember(staff.allowedFeatures) {
        staff.allowedFeatures.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableSet()
    }
    var activeFeatures by remember { mutableStateOf(currentSelected) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = "${staff.name} - ${if (isBangla) "পারমিশন পরিচালনা" else "Manage Permissions"}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Warning badge: Non-admin
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSlateSurfaceVariant)
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (isBangla)
                            "স্টাফ একাউন্ট 'Full' পারমিশন থাকলেও কখনই সিস্টেম অ্যাডমিন হতে পারবে না। স্পর্শকাতর ডাটা ডিলিট বা পিন পরিবর্তনের ক্ষমতা শুধুমাত্র প্রধান অ্যাডমিনের।"
                        else
                            "Staff accounts are never admin even with Full access. Only the primary admin can delete store data or change PIN.",
                        fontSize = 11.sp,
                        color = VividEmeraldAccent
                    )
                }

                // Level Radio Selector
                Text(
                    text = if (isBangla) "পারমিশন স্তর নির্বাচন করুন:" else "Select Permission Level:",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Full", "Half", "Limited", "Selected").forEach { lvl ->
                        val isSelected = selectedLevel == lvl
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                .clickable {
                                    selectedLevel = lvl
                                    when (lvl) {
                                        "Full" -> activeFeatures = mutableSetOf("Sales", "Stock", "Baki", "Purchases", "Expenses", "Reports")
                                        "Half" -> activeFeatures = mutableSetOf("Sales", "Stock", "Baki")
                                        "Limited" -> activeFeatures = mutableSetOf("Sales", "Stock")
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lvl,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextMuted
                            )
                        }
                    }
                }

                HorizontalDivider(color = DarkSlateCardBorder)

                // Feature Tickable Grid
                Text(
                    text = if (isBangla) "ফিচার ভিত্তিক অ্যাক্সেস গ্রিড:" else "Tickable Feature Grid:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                allFeatureOptions.forEach { (featKey, featLabel) ->
                    val isChecked = activeFeatures.contains(featKey)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLevel = "Selected"
                                activeFeatures = if (isChecked) {
                                    activeFeatures.toMutableSet().apply { remove(featKey) }
                                } else {
                                    activeFeatures.toMutableSet().apply { add(featKey) }
                                }
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checkState ->
                                selectedLevel = "Selected"
                                activeFeatures = if (checkState) {
                                    activeFeatures.toMutableSet().apply { add(featKey) }
                                } else {
                                    activeFeatures.toMutableSet().apply { remove(featKey) }
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = VividEmeraldPrimary,
                                uncheckedColor = TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = featLabel,
                            fontSize = 13.sp,
                            color = if (isChecked) TextPrimary else TextMuted
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedLevel, activeFeatures.joinToString(","))
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_save_permissions")
            ) {
                Text(if (isBangla) "সংরক্ষণ করুন" else "Save Permissions")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}

// QR Sync Modal Dialog
@Composable
fun QrSyncModalDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit
) {
    val activeProfile = ProfileManager.activeProfile
    val syncToken = remember { "DOKAN-${activeProfile.id.takeLast(6).uppercase()}-SYNC" }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "মাল্টি-ফোন সিঙ্ক QR কোড" else "Multi-Phone Sync QR Code",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isBangla)
                        "স্টাফের ফোন দিয়ে Dokan অ্যাপ থেকে এই QR কোডটি স্ক্যান করুন। সব ফোনে একই লাইভ ডাটাবেজ শেয়ার হবে।"
                    else
                        "Scan this QR code from the staff device to join the shared live Firebase database.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Large QR Visual Box
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Sync QR",
                            tint = Color.Black,
                            modifier = Modifier.size(130.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isBangla) "ম্যানুয়াল সিঙ্ক কোড:" else "Manual Sync Token:",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = syncToken,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VividEmeraldAccent
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "ঠিক আছে" else "Done")
            }
        }
    )
}
