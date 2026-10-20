package com.androidexl.ideultimate.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close

import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.logcat.LogcatLevel
import com.androidexl.ideultimate.logcat.ShizukuLogcatManager
import com.androidexl.ideultimate.logcat.SystemAppInfo
import com.androidexl.ideultimate.logcat.SystemLogEntry
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.IndigoDeep
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Midnight
import com.androidexl.ideultimate.ui.theme.Muted
import kotlinx.coroutines.launch

enum class AppScopeFilter(val title: String) {
    ALL("كل التطبيقات (النظام والمستخدم)"),
    USER_ONLY("تطبيقات المستخدم فقط"),
    SYSTEM_ONLY("تطبيقات النظام فقط"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeLogcatSystemScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLevelFilter by remember { mutableStateOf<LogcatLevel?>(null) }
    var selectedScopeFilter by remember { mutableStateOf(AppScopeFilter.ALL) }
    var selectedTargetApp by remember { mutableStateOf<SystemAppInfo?>(null) }

    var showFilterSheet by remember { mutableStateOf(false) }
    var showAppPickerSheet by remember { mutableStateOf(false) }

    val logs = remember { mutableStateListOf<SystemLogEntry>() }
    val installedApps = remember { mutableStateListOf<SystemAppInfo>() }
    var isLoading by remember { mutableStateOf(false) }
    var isShizukuAvailable by remember { mutableStateOf(false) }
    var hasShizukuPerm by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    fun refreshLogs() {
        isLoading = true
        scope.launch {
            isShizukuAvailable = ShizukuLogcatManager.isShizukuInstalled(context)
            hasShizukuPerm = ShizukuLogcatManager.hasShizukuPermission(context)

            // Keep log empty until Shizuku permission is granted, as requested by user
            if (!hasShizukuPerm) {
                logs.clear()
                isLoading = false
                return@launch
            }

            val list = ShizukuLogcatManager.fetchSystemLogcat(
                context = context,
                levelFilter = selectedLevelFilter,
                userAppsOnly = selectedScopeFilter == AppScopeFilter.USER_ONLY,
                systemAppsOnly = selectedScopeFilter == AppScopeFilter.SYSTEM_ONLY,
                targetPackage = selectedTargetApp?.packageName,
                searchQuery = searchQuery.ifBlank { null },
            )
            logs.clear()
            logs.addAll(list)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            installedApps.clear()
            installedApps.addAll(ShizukuLogcatManager.getInstalledApps(context))
        }
        refreshLogs()
    }

    LaunchedEffect(selectedLevelFilter, selectedScopeFilter, selectedTargetApp) {
        refreshLogs()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Midnight),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Logcat System",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMono,
                        ),
                        color = Ink,
                    )
                    // Shizuku Badge / Request Action
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (hasShizukuPerm) Color(0xFF10B981).copy(alpha = 0.2f)
                                else if (isShizukuAvailable) Color(0xFF38BDF8).copy(alpha = 0.2f)
                                else Color(0xFFF59E0B).copy(alpha = 0.2f)
                            )
                            .border(
                                1.dp,
                                if (hasShizukuPerm) Color(0xFF10B981)
                                else if (isShizukuAvailable) Color(0xFF38BDF8)
                                else Color(0xFFF59E0B),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                ShizukuLogcatManager.requestShizukuPermissionOrLaunch(context)
                            }
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = if (hasShizukuPerm) "Shizuku Granted ✓"
                            else if (isShizukuAvailable) "طلب إذن Shizuku"
                            else "تثبيت Shizuku",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMono,
                            ),
                            color = if (hasShizukuPerm) Color(0xFF34D399)
                            else if (isShizukuAvailable) Color(0xFF38BDF8)
                            else Color(0xFFFBBF24),
                        )
                    }
                }
                Text(
                    text = "مراقبة سجلات النظام الكاملة لجميع البرامج والحزم",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }

            // Clear Button
            IconButton(
                onClick = {
                    logs.clear()
                    Toast.makeText(context, "تم تفريغ القائمة", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Midnight),
            ) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Clear",
                    tint = Muted,
                    modifier = Modifier.size(18.dp),
                )
            }

            // Refresh Button
            IconButton(
                onClick = { refreshLogs() },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Midnight),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Cyan, strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = Cyan,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        // Search Bar & Filter Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("بحث باسم البرنامج أو الحزمة...", style = MaterialTheme.typography.bodySmall, color = Muted)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Muted,
                        modifier = Modifier.size(18.dp),
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            refreshLogs()
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Muted, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    focusedContainerColor = Midnight,
                    unfocusedContainerColor = Midnight,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                ),
            )

            // Filter button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selectedLevelFilter != null || selectedTargetApp != null || selectedScopeFilter != AppScopeFilter.ALL) Cyan else IndigoDeep)
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                    .clickable { showFilterSheet = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = IdeIcons.FilterList,
                    contentDescription = "Filter",
                    tint = if (selectedLevelFilter != null || selectedTargetApp != null || selectedScopeFilter != AppScopeFilter.ALL) DeepNavy else Ink,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        // Active Filters Badges Row
        if (selectedLevelFilter != null || selectedTargetApp != null || selectedScopeFilter != AppScopeFilter.ALL) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selectedTargetApp != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(IndigoDeep)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "برنامج: ${selectedTargetApp!!.appName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Cyan,
                            )
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = Cyan,
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable { selectedTargetApp = null },
                            )
                        }
                    }
                }

                if (selectedScopeFilter != AppScopeFilter.ALL) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(IndigoDeep)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = selectedScopeFilter.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = Cyan,
                            )
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = Cyan,
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable { selectedScopeFilter = AppScopeFilter.ALL },
                            )
                        }
                    }
                }

                if (selectedLevelFilter != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(IndigoDeep)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = selectedLevelFilter!!.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Cyan,
                            )
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = Cyan,
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable { selectedLevelFilter = null },
                            )
                        }
                    }
                }
            }
        }

        // Logs Display List
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (!hasShizukuPerm) {
                // If permission is not yet granted, show clear friendly permission prompt and keep logs empty
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(IndigoDeep)
                            .border(1.dp, Cyan.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "SH",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMono,
                            ),
                            color = Cyan,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "سجل النظام يتطلب إذن Shizuku",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "يظل سجل النظام فارغاً حتى يتم منح صلاحية Shizuku للتطبيق لقراءة سجلات كافة برامج النظام وحزم Android بأمان.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    Spacer(Modifier.height(18.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Cyan)
                            .clickable {
                                ShizukuLogcatManager.requestShizukuPermissionOrLaunch(context)
                            }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = if (isShizukuAvailable) "منح صلاحية Shizuku الآن" else "تثبيت مدير Shizuku",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepNavy,
                        )
                    }
                }
            } else if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isLoading) "جاري قراءة سجل النظام..." else "لا توجد سجلات تطابق الفلاتر المحددة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(logs, key = { it.id }) { logItem ->
                        SystemLogCardItem(
                            item = logItem,
                            onCopy = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("Logcat System", "${logItem.timestamp} [${logItem.appName} / ${logItem.packageName}]: ${logItem.message}"))
                                Toast.makeText(context, "تم نسخ السجل", Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = Midnight,
            contentColor = Ink,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0x44FFFFFF)),
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "فلترة سجلات النظام",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )

                // Shizuku Permission Request Card in BottomSheet
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1B4B))
                        .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable {
                            ShizukuLogcatManager.requestShizukuPermissionOrLaunch(context)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF4F46E5)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("SH", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isShizukuAvailable) "خدمة Shizuku وإذن الوصول" else "تثبيت خدمة Shizuku",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Ink,
                        )
                        Text(
                            text = if (isShizukuAvailable) "انقر لفتح Shizuku ومنح صلاحية قراءة سجلات النظام الكاملة" else "انقر لتحميل وتثبيت مدير Shizuku للحصول على صلاحيات متقدمة",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }

                // App Picker Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(IndigoDeep)
                        .clickable {
                            showFilterSheet = false
                            showAppPickerSheet = true
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(imageVector = IdeIcons.Apps, contentDescription = null, tint = Cyan)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اختيار برنامج معين لإظهار اللوج",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Ink,
                        )
                        Text(
                            text = selectedTargetApp?.appName ?: "كل البرامج (انقر للتحديد من القائمة)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Cyan,
                        )
                    }
                }

                Text(
                    text = "نطاق التطبيقات:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted,
                )

                // Scope filters
                AppScopeFilter.entries.forEach { scopeOpt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedScopeFilter == scopeOpt) IndigoDeep else Color.Transparent)
                            .clickable {
                                selectedScopeFilter = scopeOpt
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = scopeOpt.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedScopeFilter == scopeOpt) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = if (selectedScopeFilter == scopeOpt) Cyan else Ink,
                        )
                    }
                }

                Text(
                    text = "مستوى الخطأ (Log Level):",
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(
                        null to "الكل",
                        LogcatLevel.ERROR to "خطأ",
                        LogcatLevel.WARN to "تحذير",
                        LogcatLevel.INFO to "معلومات",
                        LogcatLevel.DEBUG to "تصحيح",
                    ).forEach { (lvl, title) ->
                        val isSelected = selectedLevelFilter == lvl
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Cyan else IndigoDeep)
                                .clickable { selectedLevelFilter = lvl }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) DeepNavy else Ink,
                            )
                        }
                    }
                }
            }
        }
    }

    // App Picker Bottom Sheet
    if (showAppPickerSheet) {
        var appSearchQuery by remember { mutableStateOf("") }
        val filteredApps = installedApps.filter {
            appSearchQuery.isBlank() ||
                    it.appName.contains(appSearchQuery, ignoreCase = true) ||
                    it.packageName.contains(appSearchQuery, ignoreCase = true)
        }

        val pickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAppPickerSheet = false },
            sheetState = pickerSheetState,
            containerColor = Midnight,
            contentColor = Ink,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
            ) {
                Text(
                    text = "اختر برنامجاً لعرض سجلاته",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = appSearchQuery,
                    onValueChange = { appSearchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("بحث عن تطبيق...", color = Muted) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.height(10.dp))

                // Option to reset/clear app filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(IndigoDeep)
                        .clickable {
                            selectedTargetApp = null
                            showAppPickerSheet = false
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "عرض كل التطبيقات (إلغاء التحديد)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Cyan,
                    )
                }

                Spacer(Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(filteredApps, key = { it.packageName }) { appInfo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(IndigoDeep.copy(alpha = 0.5f))
                                .clickable {
                                    selectedTargetApp = appInfo
                                    showAppPickerSheet = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            DrawableIconBox(drawable = appInfo.icon, size = 36.dp)

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = appInfo.appName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = appInfo.packageName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                                    color = Muted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            if (appInfo.isSystemApp) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Midnight)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text("نظام", style = MaterialTheme.typography.labelSmall, color = Muted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawableIconBox(drawable: Drawable?, size: androidx.compose.ui.unit.Dp) {
    val bitmap = remember(drawable) {
        drawable?.let { d ->
            if (d is BitmapDrawable && d.bitmap != null) {
                d.bitmap
            } else {
                val bmp = Bitmap.createBitmap(
                    d.intrinsicWidth.coerceAtLeast(48),
                    d.intrinsicHeight.coerceAtLeast(48),
                    Bitmap.Config.ARGB_8888,
                )
                val canvas = Canvas(bmp)
                d.setBounds(0, 0, canvas.width, canvas.height)
                d.draw(canvas)
                bmp
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp)),
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
                .background(Midnight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(IdeIcons.Apps, contentDescription = null, tint = Cyan, modifier = Modifier.size(size * 0.6f))
        }
    }
}

@Composable
private fun SystemLogCardItem(
    item: SystemLogEntry,
    onCopy: () -> Unit,
) {
    val levelColor = when (item.level) {
        LogcatLevel.ERROR, LogcatLevel.ASSERT -> Color(0xFFEF4444)
        LogcatLevel.WARN -> Color(0xFFF59E0B)
        LogcatLevel.INFO -> Color(0xFF38BDF8)
        LogcatLevel.DEBUG -> Color(0xFFA78BFA)
        LogcatLevel.VERBOSE -> Color(0xFF94A3B8)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Midnight)
            .border(1.dp, levelColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable(onClick = onCopy)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Line 1: App Icon + App Name + Date/Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DrawableIconBox(drawable = item.appIcon, size = 36.dp)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.appName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                    ),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${item.timestamp}  •  PID: ${item.pid}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 10.5.sp,
                    ),
                    color = Muted,
                )
            }

            // Level Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(levelColor.copy(alpha = 0.18f))
                    .border(1.dp, levelColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = item.level.code,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                    ),
                    color = levelColor,
                )
            }
        }

        // Line 2: Package Name & Tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "الحزمة البرمجية: ${item.packageName}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                ),
                color = Cyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (item.tag.isNotBlank()) {
                Text(
                    text = "[${item.tag}]",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                    ),
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Line 3: Error / Log message
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(IndigoDeep.copy(alpha = 0.6f))
                .padding(10.dp),
        ) {
            Text(
                text = item.message,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = JetBrainsMono,
                    lineHeight = 18.sp,
                ),
                color = if (item.level == LogcatLevel.ERROR) Color(0xFFFCA5A5) else Ink,
            )
        }
    }
}
