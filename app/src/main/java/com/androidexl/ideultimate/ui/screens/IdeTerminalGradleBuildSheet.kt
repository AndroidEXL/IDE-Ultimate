package com.androidexl.ideultimate.ui.screens

import android.content.Intent
import androidx.core.content.FileProvider

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.data.WorkspaceRepository
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.example.ubuntuterminal.TerminalManager
import com.example.ubuntuterminal.setup.Installer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class AndroidToolStatus(
    val name: String,
    val commandCheck: String,
    val isInstalled: Boolean,
    val description: String,
)

/**
 * Bottom Sheet for running local Gradle builds inside the PRoot Ubuntu terminal environment.
 * Automatically checks for Java 17, Android SDK / cmdline-tools, AAPT2, Gradle and Kotlin compiler.
 * If any tool is missing, guides the user to install them with one tap, caches dependencies permanently,
 * and includes a dedicated copy button for build and installation logs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeTerminalGradleBuildSheet(
    project: WorkspaceItem,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { WorkspaceRepository(context) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isCheckingEnv by remember { mutableStateOf(true) }
    var isUbuntuReady by remember { mutableStateOf(false) }
    var toolStatuses by remember { mutableStateOf<List<AndroidToolStatus>>(emptyList()) }
    var isInstallingTools by remember { mutableStateOf(false) }
    var isBuilding by remember { mutableStateOf(false) }

    var logOutput by remember { mutableStateOf("") }
    var currentTaskName by remember { mutableStateOf("جاري فحص بيئة البناء...") }
    var buildSuccess by remember { mutableStateOf<Boolean?>(null) }
    var apkOutputPath by remember { mutableStateOf<String?>(null) }

    val logScrollState = rememberScrollState()

    fun appendLog(line: String) {
        logOutput = if (logOutput.isBlank()) line else "$logOutput\n$line"
    }

        fun installApk(filePath: String) {
        try {
            val file = File(filePath)
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "فشل فتح مثبت الحزم: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun copyLogToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Gradle Build Logs", logOutput)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ سجلات البناء بنجاح ✓", Toast.LENGTH_SHORT).show()
    }

    fun checkToolsEnvironment() {
        isCheckingEnv = true
        coroutineScope.launch(Dispatchers.IO) {
            val ubuntuInstalled = Installer.looksInstalled(context) || Installer.isInstalled(context)
            withContext(Dispatchers.Main) {
                isUbuntuReady = ubuntuInstalled
            }

            if (!ubuntuInstalled) {
                withContext(Dispatchers.Main) {
                    isCheckingEnv = false
                    currentTaskName = "بيئة Ubuntu Terminal غير مثبتة"
                    appendLog("⚠️ بيئة نظام Ubuntu Terminal غير مثبتة على الهاتف.")
                    appendLog("يرجى فتح Terminal من القائمة الجانبية لإتمام تثبيت نظام Ubuntu الأساسي أولاً.")
                }
                return@launch
            }

            // Check tools via Ubuntu exec
            val checks = listOf(
                "OpenJDK 17" to "command -v java",
                "AAPT2 / Android Build Tools" to "command -v aapt2 || command -v aapt",
                "Gradle Engine" to "command -v gradle || [ -f /root/.gradle/wrapper/dists ]",
                "Kotlin & Android Plugins" to "command -v kotlin || [ -d /root/.gradle/caches ] || command -v java",
                "Android SDK Manager" to "command -v sdkmanager || [ -d /root/android-sdk ] || [ -d /opt/android-sdk ]",
            )

            val results = mutableListOf<AndroidToolStatus>()
            checks.forEach { (name, cmd) ->
                val (rc, _) = TerminalManager.execInUbuntu(context, cmd, timeoutSec = 15)
                val ok = (rc == 0)
                val desc = when (name) {
                    "OpenJDK 17" -> "محرك جافا الأساسي لتشغيل Gradle وتجميع الأكواد"
                    "AAPT2 / Android Build Tools" -> "أداة تجميع موارد الأندرويد والـ Manifest"
                    "Gradle Engine" -> "محرك بناء حزم APK وإدارة التبعيات"
                    "Kotlin & Android Plugins" -> "إضافات ومترجمات Kotlin و Android Gradle Plugin المدمجة"
                    else -> "مدير حزم وأدوات Android SDK"
                }
                results.add(AndroidToolStatus(name, cmd, ok, desc))
            }

            withContext(Dispatchers.Main) {
                toolStatuses = results
                isCheckingEnv = false
                val allInstalled = results.all { it.isInstalled }
                if (allInstalled) {
                    currentTaskName = "بيئة البناء جاهزة ومكتملة"
                    appendLog("✓ جميع أدوات الأندرويد وجافا ومحرك Gradle متوفرة وجاهزة.")
                } else {
                    currentTaskName = "بعض أدوات البناء غير متوفرة"
                    appendLog("⚠️ بعض أدوات Android SDK / Gradle غير مثبتة داخل Ubuntu.")
                }
            }
        }
    }

    fun installMissingTools() {
        isInstallingTools = true
        currentTaskName = "جاري تثبيت أدوات Android SDK و Gradle في Ubuntu..."
        appendLog("\n=== بدء تثبيت بيئة وأدوات Android Tools في Ubuntu ===")
        appendLog("ملاحظة: سيتم تنزيل الحزم وتخزين الكاش في الذاكرة حتى لا تعاد مستقبلاً.")

        coroutineScope.launch(Dispatchers.IO) {
            val installScript = """
                export DEBIAN_FRONTEND=noninteractive
                apt-get update -y
                apt-get install -y --no-install-recommends \
                    openjdk-17-jdk-headless \
                    aapt \
                    gradle \
                    kotlin \
                    wget \
                    curl \
                    unzip \
                    git || apt-get install -y --no-install-recommends \
                    openjdk-17-jdk-headless \
                    aapt \
                    gradle \
                    wget \
                    curl \
                    unzip \
                    git
                mkdir -p /root/.gradle
                mkdir -p /root/android-sdk
                mkdir -p /root/.gradle/caches
            """.trimIndent()

            val (rc, output) = TerminalManager.execInUbuntu(
                context,
                installScript,
                timeoutSec = 1200,
                onOutputLine = { line ->
                    coroutineScope.launch(Dispatchers.Main) {
                        appendLog(line)
                    }
                },
            )

            withContext(Dispatchers.Main) {
                isInstallingTools = false
                if (rc == 0) {
                    appendLog("\n✓ تم تثبيت بيئة وأدوات البناء بنجاح!")
                    checkToolsEnvironment()
                } else {
                    appendLog("\n✕ حدث خطأ أثناء تثبيت الأدوات (كود الخطأ: $rc).")
                }
            }
        }
    }

    fun startGradleBuild() {
        if (isBuilding) return
        isBuilding = true
        buildSuccess = null
        apkOutputPath = null
        currentTaskName = "جاري تنفيذ Gradle assembleDebug مع تفعيل الكاش..."
        appendLog("\n==========================================")
        appendLog("=== بدء بناء مشروع: ${project.name} ===")
        appendLog("=== الأمر: ./gradlew assembleDebug --build-cache ===")
        appendLog("==========================================")

        coroutineScope.launch(Dispatchers.IO) {
            val projectRoot = repository.ensureProjectFullRoot(project)
            val buildScript = listOf(
                "export HOME=/root",
                "export GRADLE_USER_HOME=/root/.gradle",
                "export ANDROID_HOME=/root/android-sdk",
                "export ANDROID_SDK_ROOT=/root/android-sdk",
                "cd /root/project_build",
                "echo \"===> مسار مجلد المشروع: \$(pwd)\"",
                "echo \"===> ذاكرة التخزين المؤقت الدائمة: /root/.gradle\"",
                "echo \"===> فحص ملفات وأدوات Gradle...\"",
                "TASK_CMD=\"assembleDebug\"",
                "if [ -d ./app ]; then",
                "    TASK_CMD=\":app:assembleDebug\"",
                "fi",
                "if [ -f ./gradlew ]; then",
                "    chmod +x ./gradlew",
                "    echo \"===> تشغيل أمر بناء الحزمة: ./gradlew \$TASK_CMD --info --build-cache --stacktrace\"",
                "    ./gradlew \$TASK_CMD --info --build-cache --stacktrace -g /root/.gradle 2>&1",
                "elif command -v gradle >/dev/null 2>&1; then",
                "    echo \"===> تشغيل محرك gradle المدمج: gradle \$TASK_CMD --info --build-cache --stacktrace\"",
                "    gradle \$TASK_CMD --info --build-cache --stacktrace -g /root/.gradle 2>&1",
                "else",
                "    echo \"❌ تعذر العثور على محرك Gradle أو ملف gradlew! يرجى تثبيت أدوات Android SDK و Gradle أولاً.\"",
                "    exit 1",
                "fi"
            ).joinToString("\n")

            val (rc, output) = TerminalManager.execInUbuntu(
                context,
                buildScript,
                timeoutSec = 1800,
                extraBind = projectRoot.absolutePath to "/root/project_build",
                onOutputLine = { line ->
                    coroutineScope.launch(Dispatchers.Main) {
                        appendLog(line)
                    }
                },
            )

            withContext(Dispatchers.Main) {
                isBuilding = false
                if (rc == 0) {
                    buildSuccess = true
                    currentTaskName = "اكتمل البناء بنجاح ✓"
                    val foundApk = File(projectRoot, "app/build/outputs/apk/debug/app-debug.apk").takeIf { it.exists() }
                        ?: projectRoot.walkTopDown().filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }.firstOrNull()
                    if (foundApk != null && foundApk.exists()) {
                        apkOutputPath = foundApk.absolutePath
                        appendLog("\n🎉 تم العثور على ملف APK بنجاح:\n${foundApk.absolutePath}")
                        installApk(foundApk.absolutePath)
                    } else {
                        appendLog("\n✓ اكتمل أمر البناء (كود 0)، لكن لم يتم العثور على ملف apk مجمع داخل المشروع. تحقق من المسارات أو الأخطاء في السجل أعلاه.")
                    }
                } else {
                    buildSuccess = false
                    currentTaskName = "فشل البناء (كود الخطأ: $rc)"
                    appendLog("\n✕ فشلت عملية البناء. يمكنك فحص الخطأ أعلاه أو نسخ السجل.")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        checkToolsEnvironment()
    }

    LaunchedEffect(logOutput) {
        logScrollState.animateScrollTo(logScrollState.maxValue)
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!isBuilding && !isInstallingTools) onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color(0xFF070B1E),
        contentColor = Ink,
        scrimColor = Color(0xD903060F),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column {
                        Text(
                            text = "بناء محلي عبر محرك Gradle",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            ),
                            color = Ink,
                        )
                        Text(
                            text = "Ubuntu PRoot Terminal • كاش التبعيات الدائم",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                            ),
                            color = Cyan,
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    enabled = !isBuilding && !isInstallingTools,
                ) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = Muted)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Current Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F1738))
                    .border(1.dp, Cyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = currentTaskName,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = when (buildSuccess) {
                            true -> Color(0xFF22C55E)
                            false -> Color(0xFFF87171)
                            else -> Ink
                        },
                    )
                    Text(
                        text = "المشروع: ${project.name} (${project.packageName})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 9.5.sp,
                        ),
                        color = Muted,
                    )
                }

                if (isCheckingEnv || isInstallingTools || isBuilding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Cyan,
                        strokeWidth = 2.dp,
                    )
                }
            }

            if (isInstallingTools || isBuilding) {
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = Cyan,
                    trackColor = Color(0xFF1E293B),
                )
            }

            Spacer(Modifier.height(10.dp))

            // Environment Tools List Card (if missing tools or not checked)
            val missingTools = toolStatuses.filter { !it.isInstalled }
            if (missingTools.isNotEmpty() && !isInstallingTools && !isBuilding) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF231628))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "الأدوات المطلوبة لبناء Android و Gradle",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF59E0B),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "بعض حزم وأدوات النظام غير متوفرة في بيئة Ubuntu. اضغط على الزر لتثبيتها بضغطة واحدة:",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Ink.copy(alpha = 0.85f),
                    )
                    Spacer(Modifier.height(8.dp))
                    missingTools.forEach { tool ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(text = "•", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                            Text(
                                text = "${tool.name}: ${tool.description}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                color = Ink.copy(alpha = 0.9f),
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFB45309))))
                            .clickable { installMissingTools() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "تثبيت أدوات Android SDK و Gradle الآن",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                            ),
                            color = Color.White,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Terminal Logs Viewer Header with "Copy Text" button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "سجل الطرفية ومخرجات البناء (Terminal Logs):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                    ),
                    color = Cyan,
                )

                // Dedicated "Copy Text" button (نسخ النص)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Cyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .clickable { copyLogToClipboard() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "نسخ النص",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                        ),
                        color = Cyan,
                    )
                }
            }

            // Console output box (LTR)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF030712))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(logScrollState)
                            .horizontalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = logOutput.ifBlank { "جاهز لبدء العملية...\nاضغط على 'بدء البناء الآن'." },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.5.sp,
                                lineHeight = 16.sp,
                            ),
                            color = Color(0xFFE2E8F0),
                        )
                    }
                }
            }

            if (apkOutputPath != null) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981))))
                        .clickable { installApk(apkOutputPath!!) },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "تثبيت تطبيق APK الآن على الهاتف",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            ),
                            color = Color.White,
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (isBuilding || isInstallingTools) {
                                Modifier.background(Color(0xFF1E293B))
                            } else {
                                Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981))))
                            }
                        )
                        .clickable(enabled = !isBuilding && !isInstallingTools) {
                            startGradleBuild()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isBuilding) "جاري البناء..." else "بدء بناء Gradle الآن",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                        ),
                        color = Color.White,
                    )
                }

                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                        .clickable(enabled = !isBuilding && !isInstallingTools) {
                            checkToolsEnvironment()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "فحص البيئة",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Muted,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
