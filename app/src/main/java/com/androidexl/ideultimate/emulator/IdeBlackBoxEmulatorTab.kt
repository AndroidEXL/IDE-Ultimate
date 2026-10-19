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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.model.IdeProject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IdeBlackBoxEmulatorTab(
    project: IdeProject,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val installedApps by BlackBoxEmulatorManager.installedApps.collectAsState()
    val logs by BlackBoxEmulatorManager.logs.collectAsState()
    val isReady by BlackBoxEmulatorManager.isEngineReady.collectAsState()
    val isInstalling by BlackBoxEmulatorManager.isInstalling.collectAsState()
    val runningAppPkg by BlackBoxEmulatorManager.currentRunningApp.collectAsState()

    var selectedSubTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        BlackBoxEmulatorManager.checkEngineStatus(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp),
    ) {
        // 1. Emulator Top Header Bar
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
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "NewBlackbox Virtual Engine",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            ),
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isReady) Color(0xFF065F46) else Color(0xFF3B82F6))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = if (isReady) "جاهز (المضيف مثبت)" else "مدمج (Virtual Container)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                    Text(
                        text = "محاكي أندرويد افتراضي لتشغيل تطبيقات الـ APK المعزولة",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                        ),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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

        Spacer(Modifier.height(10.dp))

        // 2. Running App Indicator (if active)
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
                        text = "التطبيق قيد التشغيل الافتراضي: $runningAppPkg",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA7F3D0),
                            fontSize = 11.5.sp,
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

        // 3. Sub Tabs (Installed Apps / Engine Logs / Engine Setup)
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
                        text = "التطبيقات المثبتة (${installedApps.size})",
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

        // 4. Tab Contents
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
                                text = "لا توجد تطبيقات مثبتة في المحاكي بعد",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE2E8F0),
                                ),
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "بعد ترجمة المشروع عبر Gradle أو GitHub Actions، اختر زر\n«تثبيت في المحاكي» لتثبيت التطبيق وتشغيله هنا مباشرة.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                ),
                            )
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
                                    onLaunch = { BlackBoxEmulatorManager.launchVirtualApp(context, app) },
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
                    text = if (isRunning) "قيد التشغيل" else "تشغيل",
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
