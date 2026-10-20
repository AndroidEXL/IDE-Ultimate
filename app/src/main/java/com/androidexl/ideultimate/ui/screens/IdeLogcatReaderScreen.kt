package com.androidexl.ideultimate.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
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
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.logcat.LogcatEntry
import com.androidexl.ideultimate.logcat.LogcatLevel
import com.androidexl.ideultimate.logcat.ProjectLogcatBridge
import com.androidexl.ideultimate.ui.components.ProjectIconView
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.IndigoDeep
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Midnight
import com.androidexl.ideultimate.ui.theme.Muted
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeLogcatReaderScreen(
    project: WorkspaceItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLevelFilter by remember { mutableStateOf<LogcatLevel?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showLoggerHelperDialog by remember { mutableStateOf(false) }

    val logs = remember { mutableStateListOf<LogcatEntry>() }
    var isLoading by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    fun refreshLogs() {
        isLoading = true
        scope.launch {
            val list = ProjectLogcatBridge.fetchSystemLogcatForPackage(
                context = context,
                appName = project.name,
                packageName = project.packageName,
            )
            logs.clear()
            logs.addAll(list)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshLogs()
        ProjectLogcatBridge.logFlow.collect { entry ->
            if (entry.packageName.equals(project.packageName, ignoreCase = true) ||
                entry.appName.equals(project.name, ignoreCase = true)) {
                logs.add(0, entry)
            }
        }
    }

    val filteredLogs = logs.filter { item ->
        val matchLevel = selectedLevelFilter == null || item.level == selectedLevelFilter || item.level.priority >= selectedLevelFilter!!.priority
        val matchSearch = searchQuery.isBlank() ||
                item.message.contains(searchQuery, ignoreCase = true) ||
                item.tag.contains(searchQuery, ignoreCase = true) ||
                item.packageName.contains(searchQuery, ignoreCase = true)
        matchLevel && matchSearch
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
                Text(
                    text = "Logcat Reader App",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                    ),
                    color = Ink,
                )
                Text(
                    text = "${project.name} (${project.packageName})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // AppLogger info / code button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF6366F1).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF6366F1), RoundedCornerShape(8.dp))
                    .clickable { showLoggerHelperDialog = true }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "كلاس AppLogger",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFA5B4FC),
                )
            }

            // Clear Button
            IconButton(
                onClick = {
                    ProjectLogcatBridge.clear()
                    logs.clear()
                    Toast.makeText(context, "تم مسح السجلات", Toast.LENGTH_SHORT).show()
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
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("بحث في السجلات أو الرسالة...", style = MaterialTheme.typography.bodySmall, color = Muted)
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
                        IconButton(onClick = { searchQuery = "" }) {
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
                    .background(if (selectedLevelFilter != null) Cyan else IndigoDeep)
                    .border(1.dp, if (selectedLevelFilter != null) Cyan else Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                    .clickable { showFilterSheet = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = IdeIcons.FilterList,
                    contentDescription = "Filter",
                    tint = if (selectedLevelFilter != null) DeepNavy else Ink,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        // Active Filter indicator badge
        if (selectedLevelFilter != null) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IndigoDeep)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "فلترة المستوى: ${selectedLevelFilter!!.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cyan,
                )
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove Filter",
                    tint = Cyan,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { selectedLevelFilter = null },
                )
            }
        }

        // Logs Display List
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (filteredLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isLoading) "جاري قراءة السجلات..." else "لا توجد سجلات مطابقة",
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
                    items(filteredLogs, key = { it.id }) { logItem ->
                        LogcatCardItem(
                            project = project,
                            logItem = logItem,
                            onCopy = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("Logcat", "${logItem.timestamp} [${logItem.level.code}/${logItem.tag}]: ${logItem.message}"))
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "فلترة نوع وسجل الإظهار (Log Level)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )

                // All option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedLevelFilter == null) IndigoDeep else Color.Transparent)
                        .clickable {
                            selectedLevelFilter = null
                            showFilterSheet = false
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "الكل (All Logs)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (selectedLevelFilter == null) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = if (selectedLevelFilter == null) Cyan else Ink,
                    )
                }

                // Error
                LogcatFilterOptionRow(
                    label = "خطأ فقط (Error)",
                    level = LogcatLevel.ERROR,
                    isSelected = selectedLevelFilter == LogcatLevel.ERROR,
                    accentColor = Color(0xFFEF4444),
                ) {
                    selectedLevelFilter = LogcatLevel.ERROR
                    showFilterSheet = false
                }

                // Warn
                LogcatFilterOptionRow(
                    label = "تحذير (Warning)",
                    level = LogcatLevel.WARN,
                    isSelected = selectedLevelFilter == LogcatLevel.WARN,
                    accentColor = Color(0xFFF59E0B),
                ) {
                    selectedLevelFilter = LogcatLevel.WARN
                    showFilterSheet = false
                }

                // Info
                LogcatFilterOptionRow(
                    label = "معلومات (Info)",
                    level = LogcatLevel.INFO,
                    isSelected = selectedLevelFilter == LogcatLevel.INFO,
                    accentColor = Color(0xFF38BDF8),
                ) {
                    selectedLevelFilter = LogcatLevel.INFO
                    showFilterSheet = false
                }

                // Debug
                LogcatFilterOptionRow(
                    label = "تصحيح (Debug)",
                    level = LogcatLevel.DEBUG,
                    isSelected = selectedLevelFilter == LogcatLevel.DEBUG,
                    accentColor = Color(0xFFA78BFA),
                ) {
                    selectedLevelFilter = LogcatLevel.DEBUG
                    showFilterSheet = false
                }

                // Verbose
                LogcatFilterOptionRow(
                    label = "تفصيلي (Verbose)",
                    level = LogcatLevel.VERBOSE,
                    isSelected = selectedLevelFilter == LogcatLevel.VERBOSE,
                    accentColor = Color(0xFF94A3B8),
                ) {
                    selectedLevelFilter = LogcatLevel.VERBOSE
                    showFilterSheet = false
                }
            }
        }
    }

    // AppLogger instructions dialog
    if (showLoggerHelperDialog) {
        val sampleCode = if (project.language == com.androidexl.ideultimate.data.ProjectLanguage.JAVA) {
            """// كلاس AppLogger تم إنشاؤه تلقائياً في مشروعك!
// لاستخدامه لإرسال اللوج إلى IDE Ultimate:

AppLogger.d(this, "MyTag", "رسالة تصحيح من تطبيقي");
AppLogger.i(this, "MyTag", "معلومات مهمة");
AppLogger.e(this, "MyTag", "حدث خطأ غير متوقع", exception);"""
        } else {
            """// كلاس AppLogger تم إنشاؤه تلقائياً في مشروعك!
// لاستخدامه لإرسال اللوج إلى IDE Ultimate:

AppLogger.d(this, "MyTag", "رسالة تصحيح من تطبيقي")
AppLogger.i(this, "MyTag", "معلومات مهمة")
AppLogger.e(this, "MyTag", "حدث خطأ غير متوقع", exception)"""
        }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLoggerHelperDialog = false },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            title = {
                Text("كلاس AppLogger لإرسال السجلات", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "يقوم IDE Ultimate تلقائياً بإضافة كلاس AppLogger داخل حزمة مشروعك (${project.packageName}). يمكنك استدعاء دواله من أي Activity أو Service لإرسال السجلات مباشرة إلى هذه الشاشة:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(IndigoDeep)
                            .padding(10.dp),
                    ) {
                        Text(
                            text = sampleCode,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 11.sp,
                                color = Cyan,
                            ),
                        )
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cb.setPrimaryClip(ClipData.newPlainText("AppLogger", sampleCode))
                        Toast.makeText(context, "تم نسخ الكود", Toast.LENGTH_SHORT).show()
                        showLoggerHelperDialog = false
                    },
                ) {
                    Text("نسخ الكود", color = Cyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showLoggerHelperDialog = false }) {
                    Text("إغلاق", color = Muted)
                }
            },
        )
    }
}

@Composable
private fun LogcatFilterOptionRow(
    label: String,
    level: LogcatLevel,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) IndigoDeep else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(accentColor),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            ),
            color = if (isSelected) Cyan else Ink,
        )
    }
}

@Composable
private fun LogcatCardItem(
    project: WorkspaceItem,
    logItem: LogcatEntry,
    onCopy: () -> Unit,
) {
    val levelColor = when (logItem.level) {
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
            ProjectIconView(
                iconPreset = project.iconPreset,
                customIconPath = project.customIconPath,
                modifier = Modifier.size(36.dp),
                cornerRadius = 10.dp,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = logItem.appName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                    ),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = logItem.timestamp,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
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
                    text = logItem.level.code,
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
                text = "الحزمة: ${logItem.packageName}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                ),
                color = Cyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (logItem.tag.isNotBlank()) {
                Text(
                    text = "[${logItem.tag}]",
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
                text = logItem.message,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = JetBrainsMono,
                    lineHeight = 18.sp,
                ),
                color = if (logItem.level == LogcatLevel.ERROR) Color(0xFFFCA5A5) else Ink,
            )
        }
    }
}
