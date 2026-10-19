package com.androidexl.ideultimate.emulator

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.data.IdeWidgetNode
import com.androidexl.ideultimate.data.ProjectImageItem
import com.androidexl.ideultimate.data.ProjectXmlPage
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.ui.screens.RenderWidgetNodeOnPhone
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val activeSimulatedApp by BlackBoxEmulatorManager.activeSimulatedApp.collectAsState()

    var selectedSubTab by remember { mutableIntStateOf(0) }
    var isSimulationScreenOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        BlackBoxEmulatorManager.checkEngineStatus(context)
    }

    LaunchedEffect(runningAppPkg) {
        if (runningAppPkg != null) {
            isSimulationScreenOpen = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp),
    ) {
        // 1. Emulator Top Header Bar with Engine Info and Setup Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "BB",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "NewBlackbox Engine",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            ),
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isInitialized) Color(0xFF059669) else Color(0xFFD97706))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = if (isInitialized) "مهيأ وجاهز ✓" else "يحتاج تهيئة",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                    Text(
                        text = if (isInitialized) "محاكي أندرويد افتراضي نشط لتشغيل حزم الـ APK" else "اضغط زر «تهيئة المحاكي» لتفعيل بيئة التشغيل",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Initialize Button
                if (!isInitialized) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
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
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                ),
                            )
                        }
                    }
                } else {
                    // Quick Launch Model Page Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981))
                            .clickable {
                                isSimulationScreenOpen = true
                                BlackBoxEmulatorManager.log("فتح صفحة المحاكاة الخاصة بالموديل: ${project.name}", "SUCCESS")
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = "تشغيل الموديل",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp,
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
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        // Initialization Progress Banner (visible during initialization)
        AnimatedVisibility(visible = isInitializing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF4F46E5), RoundedCornerShape(8.dp))
                    .padding(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = initStatusText,
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${(initProgress * 100).toInt()}%",
                        color = Color(0xFF818CF8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { initProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF6366F1),
                    trackColor = Color(0xFF334155),
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // VIEW 1: Interactive Model Simulation Screen
        if (isSimulationScreenOpen) {
            EmulatorModelSimulationScreen(
                project = project,
                activeXmlPage = selectedXmlPage,
                widgetNodes = widgetNodes,
                projectImages = projectImages,
                runningApp = activeSimulatedApp ?: installedApps.firstOrNull(),
                onClose = {
                    isSimulationScreenOpen = false
                    BlackBoxEmulatorManager.stopVirtualApp()
                },
                onRestart = {
                    BlackBoxEmulatorManager.log("إعادة تشغيل موديل التطبيق: ${project.name}", "INFO")
                    Toast.makeText(context, "تمت إعادة تشغيل الموديل داخل المحاكي ✓", Toast.LENGTH_SHORT).show()
                },
            )
        } else {
            // VIEW 2: Emulator Dashboard & Sub Tabs
            // Running App banner
            AnimatedVisibility(visible = runningAppPkg != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF064E3B))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34D399)),
                        )
                        Text(
                            text = "الموديل قيد التشغيل: $runningAppPkg",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA7F3D0),
                                fontSize = 11.5.sp,
                            ),
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF10B981))
                                .clickable { isSimulationScreenOpen = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "فتح صفحة المحاكاة",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                ),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEF4444))
                                .clickable { BlackBoxEmulatorManager.stopVirtualApp() }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "إيقاف",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                ),
                            )
                        }
                    }
                }
            }

            // Sub Tabs (Installed Apps / Engine Logs / Engine Setup)
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                        color = Color(0xFF6366F1),
                        height = 2.5.dp,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = {
                        Text(
                            text = "التطبيقات (${installedApps.size})",
                            fontSize = 11.5.sp,
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
                            fontSize = 11.5.sp,
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
                            fontSize = 11.5.sp,
                            fontWeight = if (selectedSubTab == 2) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
            }

            Spacer(Modifier.height(10.dp))

            // Sub Tab Contents
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131D31))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(12.dp),
            ) {
                when (selectedSubTab) {
                    0 -> {
                        // Apps List View
                        if (installedApps.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(48.dp),
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = if (!isInitialized) "يرجى تهيئة المحاكي أولاً" else "لا توجد تطبيقات مثبتة في المحاكي بعد",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE2E8F0),
                                    ),
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = if (!isInitialized)
                                        "اضغط على زر «تهيئة المحاكي» أعلاه لتجهيز البيئة الافتراضية ومجلدات العزل."
                                    else
                                        "عند بناء التطبيق، اضغط زر «تثبيت في المحاكي» لتثبيت الـ APK جواه المحاكي،\nأو اضغط «تشغيل الموديل» لمعاينة واجهة المشروع التفاعلية فوراً.",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                    ),
                                )
                                Spacer(Modifier.height(16.dp))
                                if (!isInitialized) {
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
                                            text = "تهيئة المحاكي الآن",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                            ),
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF10B981))
                                            .clickable { isSimulationScreenOpen = true }
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                    ) {
                                        Text(
                                            text = "فتح صفحة محاكاة الموديل (${project.name})",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                            ),
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(installedApps, key = { it.packageName }) { app ->
                                    EmulatorAppRow(
                                        app = app,
                                        isRunning = runningAppPkg == app.packageName,
                                        onLaunch = {
                                            BlackBoxEmulatorManager.launchVirtualApp(context, app)
                                            isSimulationScreenOpen = true
                                        },
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // Logs View
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "سجلات بيئة التشغيل والمحاكاة:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF94A3B8),
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
                                        color = Color(0xFF64748B),
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
                                            else -> Color(0xFFCBD5E1)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = "[${timeFormat.format(Date(entry.timestamp))}]",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B),
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
                        // Engine Setup and Info
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = "حالة محرك NewBlackbox",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                ),
                            )
                            Text(
                                text = "محرك NewBlackbox هو بيئة حاوية افتراضية (Virtual Container Engine) تسمح بتشغيل وتثبيت حزم تطبيقات الأندرويد (.apk) بشكل معزول ومستقل، وتسمح باختبار وتجربة التطبيقات المترجمة من IDE-Ultimate بدون الحاجة لجهاز خارجي.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.5.sp,
                                ),
                            )

                            Spacer(Modifier.height(4.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(
                                        text = "تطبيق NewBlackbox المضيف",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                    )
                                    Text(
                                        text = if (isReady) "مثبت ونشط على جهازك ✓" else "غير مثبت على هذا الجهاز",
                                        fontSize = 11.sp,
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
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = "مستودع GitHub",
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
                                    .background(Color(0xFF1E293B))
                                    .clickable {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(BlackBoxEmulatorManager.BLACKBOX_RELEASE_URL),
                                        )
                                        context.startActivity(intent)
                                    }
                                    .padding(12.dp),
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
                                            fontSize = 12.sp,
                                            color = Color.White,
                                        )
                                        Text(
                                            text = "للحصول على بيئة تشغيل كاملة ومستقلة على الهاتف",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8),
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
}

/**
 * Dedicated Interactive Screen for the Virtual Model Simulation
 */
@Composable
private fun EmulatorModelSimulationScreen(
    project: WorkspaceItem,
    activeXmlPage: ProjectXmlPage,
    widgetNodes: List<IdeWidgetNode>,
    projectImages: List<ProjectImageItem>,
    runningApp: EmulatorInstalledApp?,
    onClose: () -> Unit,
    onRestart: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF131D31))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .padding(10.dp),
    ) {
        // Control Bar above the phone frame
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                )
                Text(
                    text = "صفحة محاكاة الموديل: ${runningApp?.appName ?: project.name}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF334155))
                        .clickable { onRestart() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = "إعادة تشغيل",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontSize = 10.sp,
                            ),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFEF4444))
                        .clickable { onClose() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = "إغلاق",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontSize = 10.sp,
                            ),
                        )
                    }
                }
            }
        }

        // Virtual Phone Screen Mockup Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, Color(0xFF6366F1), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
            ) {
                // 1. Android Status Bar Mockup
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "12:00",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Text(
                        text = "100% 🔋  4G 📶",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = Color.White,
                        ),
                    )
                }

                // 2. Application Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFF4F46E5))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = runningApp?.appName ?: project.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = activeXmlPage.activityName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = Color(0xFFC7D2FE),
                        ),
                    )
                }

                // 3. Application View Content (Render actual interactive widget nodes)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.White)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        if (widgetNodes.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "مرحباً بك في تطبيق ${runningApp?.appName ?: project.name} 🎉",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B),
                                    ),
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "الصفحة جاهزة وتعمل داخل محاكي NewBlackbox الافتراضي بنجاح.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF64748B),
                                        fontSize = 12.sp,
                                    ),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        } else {
                            widgetNodes.forEach { node ->
                                RenderWidgetNodeOnPhone(
                                    node = node,
                                    projectImages = projectImages,
                                    selectedWidgetUid = null,
                                    draggingType = null,
                                    draggingExistingUid = null,
                                    hoveredContainerUid = null,
                                    dropInsertIndex = -1,
                                    onSelectWidget = { clickedNode ->
                                        Toast.makeText(context, "تفاعل مع: ${clickedNode.text.ifBlank { clickedNode.type.name }}", Toast.LENGTH_SHORT).show()
                                    },
                                    onDoubleTapWidget = {},
                                    onStartMoveExistingWidget = { _, _ -> },
                                    onUpdateMoveDragPos = {},
                                    onEndMoveExistingWidget = {},
                                    onCancelMoveExistingWidget = {},
                                    onContainerPositioned = { _, _ -> },
                                    onWidgetPositioned = { _, _ -> },
                                )
                            }
                        }
                    }
                }

                // 4. Android Bottom Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(Color(0xFF0F172A)),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Back
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onClose() },
                    )
                    // Home
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onClose() },
                    )
                    // Recents
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .border(1.5.dp, Color.White, RoundedCornerShape(2.dp)),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmulatorAppRow(
    app: EmulatorInstalledApp,
    isRunning: Boolean,
    onLaunch: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E293B))
            .border(
                1.dp,
                if (isRunning) Color(0xFF10B981) else Color(0xFF334155),
                RoundedCornerShape(10.dp),
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            // App Icon
            val iconBitmap = remember(app.icon) {
                app.icon?.let { drawableToBitmap(it) }
            }

            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap.asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF3B82F6)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = app.appName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }

            Column {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${app.packageName} (v${app.versionName})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isRunning) Color(0xFF059669) else Color(0xFF6366F1))
                .clickable { onLaunch() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = if (isRunning) "قيد التشغيل" else "تشغيل الموديل",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp,
                    ),
                )
            }
        }
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
