package com.androidexl.ideultimate.emulator

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.data.IdeWidgetNode
import com.androidexl.ideultimate.data.ProjectImageItem
import com.androidexl.ideultimate.data.ProjectXmlPage
import com.androidexl.ideultimate.data.WorkspaceItem
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Authentic BlackBox UI Emulator Screen.
 * Replaces any model simulation mockups with the genuine NewBlackbox interface:
 * - Material Toolbar (#3F3F3F) with Title "BlackBox" and User status
 * - Authentic 4-Column App Grid layout (matching BlackBox's AppsFragment & item_app.xml)
 * - Engine Initialization & Virtual Container setup
 * - Floating Action Button (+) to add APKs
 * - Live Event Logs and Setup tabs
 */
@Composable
fun IdeBlackBoxEmulatorTab(
    project: WorkspaceItem,
    selectedXmlPage: ProjectXmlPage,
    widgetNodes: List<IdeWidgetNode>,
    projectImages: List<ProjectImageItem>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val installedApps by BlackBoxEmulatorManager.installedApps.collectAsState()
    val logs by BlackBoxEmulatorManager.logs.collectAsState()
    val isReady by BlackBoxEmulatorManager.isEngineReady.collectAsState()
    val isInitialized by BlackBoxEmulatorManager.isEngineInitialized.collectAsState()
    val isInitializing by BlackBoxEmulatorManager.isInitializing.collectAsState()
    val initProgress by BlackBoxEmulatorManager.initProgress.collectAsState()
    val initStatusText by BlackBoxEmulatorManager.initStatusText.collectAsState()
    val runningAppPkg by BlackBoxEmulatorManager.currentRunningApp.collectAsState()

    var selectedSubTab by remember { mutableIntStateOf(0) }

    // APK file picker launcher for the BlackBox FAB (+)
    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val input = context.contentResolver.openInputStream(uri)
                    val tempApk = File(context.cacheDir, "imported_${System.currentTimeMillis()}.apk")
                    tempApk.outputStream().use { out -> input?.copyTo(out) }
                    val res = BlackBoxEmulatorManager.installApkToEmulator(context, tempApk, autoLaunch = true)
                    if (res.isSuccess) {
                        Toast.makeText(context, "تم تثبيت التطبيق بنجاح داخل BlackBox ✓", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "فشل التثبيت: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "خطأ أثناء قراءة ملف الـ APK: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        BlackBoxEmulatorManager.checkEngineStatus(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF2B2B2B)), // BlackBox background dark tone
    ) {
        // 1. Original BlackBox Top Toolbar (Matching NewBlackbox's view_toolbar.xml & color/primary #3F3F3F)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF3F3F3F))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "BlackBox",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                )
                Text(
                    text = if (isInitialized) "UserID: 0 • Engine Active" else "UserID: 0 • Needs Initialization",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFCCCCCC),
                        fontSize = 11.sp,
                    ),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Initialize Button
                if (!isInitialized) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF6366F1))
                            .clickable(enabled = !isInitializing) {
                                coroutineScope.launch {
                                    BlackBoxEmulatorManager.initializeEmulatorEngine(context)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (isInitializing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                            Text(
                                text = if (isInitializing) "جاري التهيئة..." else "تهيئة المحاكي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                ),
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF059669))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = "المحرك نشط",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { BlackBoxEmulatorManager.checkEngineStatus(context) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "تحديث",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        // Initialization Progress Bar (during setup)
        AnimatedVisibility(visible = isInitializing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF333333))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = initStatusText,
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                    )
                    Text(
                        text = "${(initProgress * 100).toInt()}%",
                        color = Color(0xFF818CF8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { initProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF6366F1),
                    trackColor = Color(0xFF555555),
                )
            }
        }

        // 2. Sub Tabs (BlackBox Apps / Event Logs / Engine Info)
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Color(0xFF333333),
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = Color.White,
                    height = 2.5.dp,
                )
            },
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = {
                    Text(
                        text = "التطبيقات (${installedApps.size})",
                        fontSize = 12.sp,
                        fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = {
                    Text(
                        text = "سجل الأحداث (${logs.size})",
                        fontSize = 12.sp,
                        fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = {
                    Text(
                        text = "إعداد المحرك",
                        fontSize = 12.sp,
                        fontWeight = if (selectedSubTab == 2) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
        }

        // 3. Main Body of BlackBox
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF262626)),
        ) {
            when (selectedSubTab) {
                0 -> {
                    // Authentic BlackBox Apps Grid (4 columns, matching NewBlackbox AppsFragment)
                    if (installedApps.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3F3F3F)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF999999),
                                    modifier = Modifier.size(36.dp),
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "لا توجد تطبيقات داخل محاكي BlackBox بعد",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                ),
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "بعد بناء المشروع، اختر «تثبيت في المحاكي» لتثبيت الـ APK جواه المحاكي،\nأو اضغط زر (+) بالأسفل لإضافة أي حزمة APK من الهاتف.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 12.sp,
                                ),
                            )

                            if (!isInitialized) {
                                Spacer(Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF6366F1))
                                        .clickable {
                                            coroutineScope.launch {
                                                BlackBoxEmulatorManager.initializeEmulatorEngine(context)
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = "تهيئة محرك المحاكي الآن",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                        ),
                                    )
                                }
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Running app status banner
                            if (runningAppPkg != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1E3A2F))
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF34D399)),
                                        )
                                        Text(
                                            text = "قيد التشغيل في الحاوية: $runningAppPkg",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFA7F3D0),
                                                fontWeight = FontWeight.Bold,
                                            ),
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFEF4444))
                                            .clickable { BlackBoxEmulatorManager.stopVirtualApp() }
                                            .padding(horizontal = 8.dp, vertical = 3.dp),
                                    ) {
                                        Text(
                                            text = "إيقاف",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                        )
                                    }
                                }
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(installedApps, key = { it.packageName }) { app ->
                                    BlackBoxAppItem(
                                        app = app,
                                        isRunning = runningAppPkg == app.packageName,
                                        onLaunch = {
                                            BlackBoxEmulatorManager.launchVirtualApp(context, app)
                                        },
                                    )
                                }
                            }
                        }
                    }

                    // Floating Action Button (+) matching BlackBox's activity_main.xml fab
                    FloatingActionButton(
                        onClick = { apkPickerLauncher.launch("application/vnd.android.package-archive") },
                        containerColor = Color(0xFF3F3F3F),
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "إضافة حزمة APK",
                            tint = Color.White,
                        )
                    }
                }

                1 -> {
                    // Logs Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "سجلات محرك BlackBox الافتراضي:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFCCCCCC),
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                            if (logs.isNotEmpty()) {
                                Text(
                                    text = "مسح السجل",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFEF4444),
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    modifier = Modifier.clickable { BlackBoxEmulatorManager.clearLogs() },
                                )
                            }
                        }

                        if (logs.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "لا توجد سجلات بعد.",
                                    color = Color(0xFF888888),
                                    fontSize = 12.sp,
                                )
                            }
                        } else {
                            val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                items(logs) { entry ->
                                    val logColor = when (entry.level) {
                                        "SUCCESS" -> Color(0xFF34D399)
                                        "ERROR" -> Color(0xFFF87171)
                                        "WARN" -> Color(0xFFFBBF24)
                                        else -> Color(0xFFE2E8F0)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            text = "[${timeFormat.format(Date(entry.timestamp))}]",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = Color(0xFF888888),
                                        )
                                        Text(
                                            text = entry.message,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.5.sp,
                                            color = logColor,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Engine Setup and Info Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "بيئة وحاوية NewBlackbox الأصلية",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            ),
                        )
                        Text(
                            text = "محرك NewBlackbox يوفر بيئة أندرويد افتراضية معزولة (Virtual Container) لتشغيل وفحص حزم تطبيقات الـ APK بدون التأثير على النظام الخارجي.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCCCCCC),
                                fontSize = 12.sp,
                            ),
                        )

                        Spacer(Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF333333))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = "تطبيق NewBlackbox المضيف",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                )
                                Text(
                                    text = if (isReady) "مثبت ونشط على جهازك ✓" else "غير مثبت على هذا الجهاز",
                                    fontSize = 11.5.sp,
                                    color = if (isReady) Color(0xFF34D399) else Color(0xFFF59E0B),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF6366F1))
                                    .clickable {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(BlackBoxEmulatorManager.BLACKBOX_REPO_URL),
                                        )
                                        context.startActivity(intent)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = "GitHub",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF333333))
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(BlackBoxEmulatorManager.BLACKBOX_RELEASE_URL),
                                )
                                context.startActivity(intent)
                            }
                            .padding(14.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(
                                        text = "تنزيل تطبيق NewBlackbox المضيف (APK)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White,
                                    )
                                    Text(
                                        text = "لإتاحة التزامن والتشغيل المزدوج المستقل",
                                        fontSize = 11.sp,
                                        color = Color(0xFFAAAAAA),
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF818CF8),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single App Grid Item matching NewBlackbox's item_app.xml exactly:
 * - Centered vertical layout
 * - 48dp app icon with optional corner label
 * - 12sp app name label below
 */
@Composable
private fun BlackBoxAppItem(
    app: EmulatorInstalledApp,
    isRunning: Boolean,
    onLaunch: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onLaunch() }
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center,
        ) {
            val iconBitmap = remember(app.icon) {
                app.icon?.let { drawableToBitmap(it) }
            }

            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap.asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF3F3F3F)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = app.appName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                }
            }

            // XP / Active corner label
            if (isRunning) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .align(Alignment.TopEnd),
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = app.appName,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
