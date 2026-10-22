package com.androidexl.ideultimate.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import com.androidexl.ideultimate.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.androidexl.ideultimate.data.ProjectLanguage
import com.androidexl.ideultimate.data.TerminalBuildManager
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.data.WorkspaceRepository
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.GlassStrokeTop
import com.androidexl.ideultimate.ui.theme.IndigoDeep
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Midnight
import com.androidexl.ideultimate.ui.theme.Muted
import com.example.ubuntuterminal.TerminalManager
import com.example.ubuntuterminal.setup.Installer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Pattern

data class AndroidToolStatus(
    val name: String,
    val commandCheck: String,
    val isInstalled: Boolean,
    val description: String,
)

enum class BuildTaskStepStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED,
    SKIPPED,
}

data class BuildTaskStep(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val status: BuildTaskStepStatus = BuildTaskStepStatus.PENDING,
    val logs: List<String> = emptyList(),
)

/**
 * Activity-style Full Screen for running Terminal/Gradle/Flutter builds in PRoot Ubuntu.
 * Organizes execution steps into an interactive Tree/ListView where tapping an operation
 * displays its details inline or opens an interactive Bottom Sheet with full verbose logs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeTerminalGradleBuildSheet(
    project: WorkspaceItem,
    onBack: () -> Unit = {},
    onDismiss: () -> Unit = onBack,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { WorkspaceRepository(context) }
    val isFlutter = (project.language == ProjectLanguage.FLUTTER)

    val buildMgr = TerminalBuildManager
    val isBuilding = buildMgr.isBuilding
    val isInstallingTools = buildMgr.isInstallingTools
    val isCheckingEnv = buildMgr.isCheckingEnv
    val isUbuntuReady = buildMgr.isUbuntuReady
    val toolStatuses = buildMgr.toolStatuses
    val rawLogOutput = buildMgr.rawLogOutput
    val currentTaskName = buildMgr.currentTaskName
    val buildSuccess = buildMgr.buildSuccess
    val apkOutputPath = buildMgr.apkOutputPath
    val buildSteps = buildMgr.buildSteps

    var selectedSheetStep by remember { mutableStateOf<BuildTaskStep?>(null) }
    var expandedStepIds by remember { mutableStateOf(setOf<String>()) }
    var buildViewTab by remember { mutableStateOf(0) } // 0: Tasks Tree, 1: Raw Log
    val terminalScrollState = rememberScrollState()

    fun performCloseAndCleanup() {
        if (!isBuilding && !isInstallingTools) {
            coroutineScope.launch(Dispatchers.IO) {
                repository.cleanProjectBuildArtifacts(project)
            }
        }
        onBack()
        onDismiss()
    }

    // Keep running in background when user navigates back, or clean up if idle!
    BackHandler(enabled = true) {
        performCloseAndCleanup()
    }

    fun abortBuildAndClose() {
        buildMgr.abortBuild()
        performCloseAndCleanup()
    }

    fun appendLog(line: String) {
        buildMgr.appendLog(line, isFlutter)
        if (selectedSheetStep != null) {
            val updated = buildMgr.buildSteps.firstOrNull { it.id == selectedSheetStep?.id }
            if (updated != null) {
                selectedSheetStep = updated
            }
        }
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

    fun copyLogToClipboard(textToCopy: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Build Logs", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ السجل بنجاح ✓", Toast.LENGTH_SHORT).show()
    }

    fun checkToolsEnvironment() {
        buildMgr.isCheckingEnv = true
        coroutineScope.launch(Dispatchers.IO) {
            val projectRoot = repository.ensureProjectFullRoot(project)
            val ubuntuInstalled = Installer.looksInstalled(context) || Installer.isInstalled(context)
            withContext(Dispatchers.Main) {
                buildMgr.isUbuntuReady = ubuntuInstalled
            }

            if (!ubuntuInstalled) {
                withContext(Dispatchers.Main) {
                    buildMgr.isCheckingEnv = false
                    buildMgr.currentTaskName = "بيئة Ubuntu Terminal غير مثبتة"
                    appendLog("⚠️ بيئة نظام Ubuntu Terminal غير مثبتة على الهاتف.")
                    appendLog("يرجى فتح Terminal من القائمة الجانبية لإتمام تثبيت نظام Ubuntu الأساسي أولاً.")
                }
                return@launch
            }

            val checks = if (isFlutter) {
                listOf(
                    "Flutter SDK" to "command -v flutter || [ -f /root/flutter/bin/flutter ]",
                    "Dart SDK" to "command -v dart || [ -f /root/flutter/bin/cache/dart-sdk/bin/dart ]",
                    "OpenJDK 17" to "command -v java || [ -f /usr/bin/java ]",
                    "Android SDK (Platform 34/35)" to "[ -d /root/android-sdk/platforms ] || [ -d /root/Android/Sdk/platforms ]",
                    "Android Build Tools (aapt2/zipalign)" to "[ -d /root/android-sdk/build-tools ] || [ -d /root/Android/Sdk/build-tools ] || command -v aapt2 || [ -f /root/android-arm-tools/build-tools/aapt2 ]",
                )
            } else {
                listOf(
                    "OpenJDK 17" to "command -v java || [ -f /usr/bin/java ]",
                    "Android SDK (Platform 34/35)" to "[ -d /root/android-sdk/platforms ] || [ -d /root/Android/Sdk/platforms ]",
                    "Android Build Tools (aapt2/zipalign)" to "[ -d /root/android-sdk/build-tools ] || [ -d /root/Android/Sdk/build-tools ] || command -v aapt2 || [ -f /root/android-arm-tools/build-tools/aapt2 ]",
                    "Commandline Tools & sdkmanager" to "command -v sdkmanager || [ -f /root/android-sdk/cmdline-tools/latest/bin/sdkmanager ] || [ -f /root/Android/Sdk/cmdline-tools/latest/bin/sdkmanager ]",
                    "Gradle Engine & Wrapper" to "command -v gradle || [ -f /usr/bin/gradle ] || [ -d /root/.gradle/wrapper/dists ] || [ -f /root/project_build/gradlew ] || [ -f /root/project_build/gradle/wrapper/gradle-wrapper.jar ] || [ -f /root/gradle-wrapper/gradlew ]",
                )
            }

            val results = mutableListOf<AndroidToolStatus>()
            checks.forEach { (name, cmd) ->
                val (rc, _) = TerminalManager.execInUbuntu(
                    context,
                    cmd,
                    timeoutSec = 15,
                    extraBind = projectRoot.absolutePath to "/root/project_build",
                )
                val ok = (rc == 0)
                val desc = when (name) {
                    "Flutter SDK" -> "محرك Flutter الرسمي لتطوير وبناء التطبيقات"
                    "Dart SDK" -> "محرك لغة Dart الرسمي المدمج مع Flutter"
                    "OpenJDK 17" -> "محرك جافا الرسمي لتشغيل Gradle وتجميع الأكواد"
                    "Android SDK (Platform 34/35)" -> "منصات ومكتبات أندرويد الرسمية لبناء الحزم"
                    "Android Build Tools (aapt2/zipalign)" -> "أدوات بناء وضغط وتوقيع ملفات الـ APK"
                    "Commandline Tools & sdkmanager" -> "أدوات Google الرسمية لإدارة وتنزيل الحزم"
                    else -> "بيئة تشغيل ومحرك Gradle Wrapper"
                }
                results.add(AndroidToolStatus(name, cmd, ok, desc))
            }

            withContext(Dispatchers.Main) {
                buildMgr.toolStatuses = results
                buildMgr.isCheckingEnv = false
                val allInstalled = results.all { it.isInstalled }
                if (allInstalled) {
                    buildMgr.currentTaskName = "بيئة البناء جاهزة ومكتملة"
                    appendLog("✓ جميع أدوات الأندرويد وجافا ومحرك Gradle متوفرة وجاهزة.")
                } else {
                    val missingNames = results.filter { !it.isInstalled }.joinToString { it.name }
                    buildMgr.currentTaskName = "أدوات البناء غير مكتملة ($missingNames)"
                    appendLog("⚠️ يلزم تثبيت الأدوات التالية قبل تشغيل البناء: $missingNames")
                }
            }
        }
    }

    fun installMissingTools() {
        buildMgr.isInstallingTools = true
        buildMgr.currentTaskName = if (isFlutter) "جاري تثبيت بيئة Flutter SDK و Android Tools في Ubuntu..." else "جاري تثبيت أدوات Android SDK و Gradle في Ubuntu..."
        appendLog(if (isFlutter) "\n=== بدء تثبيت بيئة Flutter SDK و Android Tools في Ubuntu ===" else "\n=== بدء تثبيت بيئة وأدوات Android Tools في Ubuntu ===")
        appendLog("ملاحظة: سيتم تنزيل الحزم وتخزين الكاش في الذاكرة حتى لا تعاد مستقبلاً.")

        buildMgr.activeJob = coroutineScope.launch(Dispatchers.IO) {
            val installScript = listOf(
                "export DEBIAN_FRONTEND=noninteractive",
                "echo '===> تنظيف حزم Gradle و Kotlin القديمة من apt...'",
                "apt-get remove -y gradle kotlin ant 2>/dev/null || true",
                "apt-get autoremove -y 2>/dev/null || true",
                "echo '===> تثبيت OpenJDK 17 والأدوات الأساسية...'",
                "apt-get update -y",
                "apt-get install -y --no-install-recommends openjdk-17-jdk-headless wget curl unzip git ca-certificates",
                "echo '===> تجهيز مسارات Android SDK...'",
                "mkdir -p /root/android-sdk/cmdline-tools",
                "mkdir -p /root/Android/Sdk/cmdline-tools",
                "mkdir -p /root/.gradle/caches",
                "mkdir -p /root/.gradle/wrapper/dists",
                "if [ -d /root/project_build ] && [ -f /root/project_build/gradlew ]; then",
                "    chmod +x /root/project_build/gradlew 2>/dev/null || true",
                "    mkdir -p /root/gradle-wrapper",
                "    cp -f /root/project_build/gradlew /root/gradle-wrapper/gradlew 2>/dev/null || true",
                "    chmod +x /root/gradle-wrapper/gradlew 2>/dev/null || true",
                "fi",
                "cd /root/android-sdk/cmdline-tools",
                "if [ ! -d latest ]; then",
                "    echo '===> تحميل وتثبيت Google Android Commandline Tools الرسمي...'",
                "    wget -q --show-progress -O cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip || curl -L -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip",
                "    unzip -q -o cmdline-tools.zip",
                "    rm -rf latest",
                "    mv cmdline-tools latest",
                "    rm -f cmdline-tools.zip",
                "fi",
                "export ANDROID_HOME=/root/android-sdk",
                "export ANDROID_SDK_ROOT=/root/android-sdk",
                "export PATH=/root/android-sdk/cmdline-tools/latest/bin:/root/android-sdk/platform-tools:/root/android-sdk/build-tools/34.0.0:\$PATH",
                "echo '===> تثبيت منصات الأندرويد و Build Tools المطلوبة...'",
                "yes | /root/android-sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=/root/android-sdk --install \"platform-tools\" \"platforms;android-34\" \"build-tools;34.0.0\" 2>&1 || true",
                "echo 'export ANDROID_HOME=/root/android-sdk' >> /root/.bashrc",
                "echo 'export ANDROID_SDK_ROOT=/root/android-sdk' >> /root/.bashrc",
                "echo 'export PATH=/root/android-sdk/cmdline-tools/latest/bin:/root/android-sdk/platform-tools:\$PATH' >> /root/.bashrc",
                "echo '===> تجهيز وتثبيت أدوات AAPT2 المتوافقة مع معمارية الهاتف (aapt2 aarch64)...'",
                "apt-get install -y --no-install-recommends aapt || true",
                "mkdir -p /root/android-sdk/build-tools/34.0.0",
                "mkdir -p /root/android-arm-tools",
                "if [ ! -f /root/android-arm-tools/build-tools/aapt2 ]; then",
                "    echo '===> تحميل حزمة Android SDK Tools الرسمية المترجمة لمعمارية aarch64...' ",
                "    wget -q --show-progress -O /root/android-arm-tools/tools.zip https://github.com/lzhiyong/android-sdk-tools/releases/download/35.0.2/android-sdk-tools-static-aarch64.zip || curl -L -o /root/android-arm-tools/tools.zip https://github.com/lzhiyong/android-sdk-tools/releases/download/35.0.2/android-sdk-tools-static-aarch64.zip || true",
                "    if [ -f /root/android-arm-tools/tools.zip ]; then",
                "        unzip -q -o /root/android-arm-tools/tools.zip -d /root/android-arm-tools/ || true",
                "        chmod +x /root/android-arm-tools/build-tools/* /root/android-arm-tools/platform-tools/* 2>/dev/null || true",
                "    fi",
                "fi",
                "if [ -f /root/android-arm-tools/build-tools/aapt2 ]; then",
                "    NATIVE_AAPT=\"/root/android-arm-tools/build-tools/aapt2\"",
                "    cp -f /root/android-arm-tools/build-tools/* /root/android-sdk/build-tools/34.0.0/ 2>/dev/null || true",
                "elif command -v aapt2 >/dev/null 2>&1; then",
                "    NATIVE_AAPT=\$(command -v aapt2)",
                "elif command -v aapt >/dev/null 2>&1; then",
                "    NATIVE_AAPT=\$(command -v aapt)",
                "else",
                "    NATIVE_AAPT=\"\"",
                "fi",
                "if [ -n \"\$NATIVE_AAPT\" ]; then",
                "    cp -f \"\$NATIVE_AAPT\" /root/android-sdk/build-tools/34.0.0/aapt2 2>/dev/null || true",
                "    chmod +x /root/android-sdk/build-tools/34.0.0/aapt2",
                "    AAPT_PATH=\"/root/android-sdk/build-tools/34.0.0/aapt2\"",
                "    mkdir -p /root/.gradle",
                "    for prop in \"/root/.gradle/gradle.properties\"; do",
                "        touch \"\$prop\"",
                "        grep -v \"android.aapt2\" \"\$prop\" > \"\$prop.tmp\" 2>/dev/null || cp \"\$prop\" \"\$prop.tmp\"",
                "        echo \"android.aapt2FromMaven=false\" >> \"\$prop.tmp\"",
                "        echo \"android.aapt2FromMavenOverride=\$AAPT_PATH\" >> \"\$prop.tmp\"",
                "        echo \"android.aapt2Executable=\$AAPT_PATH\" >> \"\$prop.tmp\"",
                "        mv \"\$prop.tmp\" \"\$prop\"",
                "    done",
                "fi",
                "echo '✓ تم تجهيز بيئة وأدوات Android SDK الرسمية بنجاح!'",
                if (isFlutter) "echo '===> تجهيز وتحميل بيئة محرك Flutter SDK الرسمي...'" else "echo ''",
                if (isFlutter) "git config --global --add safe.directory /root/flutter 2>/dev/null || true" else "echo ''",
                if (isFlutter) "if [ ! -d /root/flutter ]; then echo '===> تحميل حزمة Flutter SDK من المستودع الرسمي (stable branch)...'; git clone -b stable --depth 1 https://github.com/flutter/flutter.git /root/flutter 2>&1 || true; fi" else "echo ''",
                if (isFlutter) "export PATH=/root/flutter/bin:\$PATH; echo 'export PATH=/root/flutter/bin:\$PATH' >> /root/.bashrc" else "echo ''",
                if (isFlutter) "if [ -f android/local.properties ]; then grep -q '^flutter.sdk=' android/local.properties || echo 'flutter.sdk=/root/flutter' >> android/local.properties; fi" else "echo ''",
                if (isFlutter) "echo '===> ضبط إعدادات Flutter وتحديد مسار Android SDK...'; /root/flutter/bin/flutter config --no-analytics 2>/dev/null || true; /root/flutter/bin/flutter config --android-sdk /root/android-sdk 2>/dev/null || true" else "echo ''",
                if (isFlutter) "echo '===> الموافقة على تراخيص Android SDK...'; yes | /root/flutter/bin/flutter doctor --android-licenses 2>/dev/null || true" else "echo ''",
                if (isFlutter) "echo '===> تنزيل وتخزين محرك ومكونات Dart و Flutter مسبقاً (precache)...'; /root/flutter/bin/flutter precache --android 2>&1 || true" else "echo ''",
                if (isFlutter) "echo '✓ اكتمل تحميل وتثبيت بيئة Flutter SDK بنجاح!'" else "echo ''",
            ).joinToString("\n")

            val projectRoot = repository.ensureProjectFullRoot(project)
            val (rc, output) = TerminalManager.execInUbuntu(
                context,
                installScript,
                timeoutSec = 1200,
                extraBind = projectRoot.absolutePath to "/root/project_build",
                onOutputLine = { line ->
                    coroutineScope.launch(Dispatchers.Main) {
                        appendLog(line)
                    }
                },
                onProcessStarted = { proc ->
                    buildMgr.activeProcess = proc
                },
            )
            buildMgr.activeProcess = null

            withContext(Dispatchers.Main) {
                buildMgr.isInstallingTools = false
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
        if (isBuilding || isInstallingTools) return
        val missing = toolStatuses.filter { !it.isInstalled }
        if (missing.isNotEmpty()) {
            appendLog("\n⚠️ بعض أدوات البناء غير متوفرة (${missing.joinToString { it.name }}).")
            appendLog("===> جاري تحميل وتثبيت الأدوات المطلوبة تلقائيًا أولاً قبل البناء...")
            installMissingTools()
            return
        }
        buildMgr.isBuilding = true
        buildMgr.buildSuccess = null
        buildMgr.apkOutputPath = null
        buildSteps.clear()
        expandedStepIds = emptySet()
        buildMgr.currentTaskName = if (isFlutter) "جاري تنفيذ flutter build apk --debug -v..." else "جاري تنفيذ Gradle assembleDebug مع تفعيل الكاش والتفاصيل..."
        appendLog("\n==========================================")
        appendLog("=== بدء بناء مشروع: ${project.name} ===")
        appendLog(if (isFlutter) "=== الأمر: flutter build apk --debug -v ===" else "=== الأمر: ./gradlew assembleDebug --info --build-cache --stacktrace ===")
        appendLog("==========================================")

        buildMgr.activeJob = coroutineScope.launch(Dispatchers.IO) {
            val projectRoot = repository.ensureProjectFullRoot(project)
            val buildScript = listOf(
                "export HOME=/root",
                "export GRADLE_USER_HOME=/root/.gradle",
                "export ANDROID_HOME=/root/android-sdk",
                "export ANDROID_SDK_ROOT=/root/android-sdk",
                "export PATH=/root/android-sdk/cmdline-tools/latest/bin:/root/android-sdk/platform-tools:/root/android-sdk/build-tools/34.0.0:\$PATH",
                "cd /root/project_build",
                "echo \"===> مسار مجلد المشروع: \$(pwd)\"",
                "echo \"===> ذاكرة التخزين المؤقت الدائمة: /root/.gradle\"",
                "echo \"===> فحص ملفات وأدوات Gradle...\"",
                "echo \"===> التحقق من بنية مشروع Android و Gradle...\"",
                "if [ ! -f pubspec.yaml ] && [ ! -d ./app ]; then",
                "    echo \"⚠️ مجلد app غير موجود، جاري إنشاء بنية الموديول...\"",
                "    mkdir -p app",
                "    if [ -f build.gradle.kts ] && grep -q \"com.android.application\" build.gradle.kts; then",
                "        mv build.gradle.kts app/",
                "    elif [ -f build.gradle ] && grep -q \"com.android.application\" build.gradle; then",
                "        mv build.gradle app/",
                "    fi",
                "    if [ -d src ]; then",
                "        mv src app/",
                "    fi",
                "fi",
                "echo '===> فحص مستودعات Plugin Management في settings.gradle...' ",
                "if [ ! -f pubspec.yaml ] && [ ! -f settings.gradle.kts ] && [ ! -f settings.gradle ]; then",
                "    cat << 'EOF_SET' > settings.gradle.kts",
                "pluginManagement {",
                "    repositories {",
                "        google()",
                "        mavenCentral()",
                "        gradlePluginPortal()",
                "    }",
                "}",
                "dependencyResolutionManagement {",
                "    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)",
                "    repositories {",
                "        google()",
                "        mavenCentral()",
                "    }",
                "}",
                "include(\":app\")",
                "EOF_SET",
                "elif [ -f settings.gradle.kts ]; then",
                "    if ! grep -q \"pluginManagement\" settings.gradle.kts; then",
                "        cat << 'EOF_PM' > settings.gradle.kts.tmp",
                "pluginManagement {",
                "    repositories {",
                "        google()",
                "        mavenCentral()",
                "        gradlePluginPortal()",
                "    }",
                "}",
                "dependencyResolutionManagement {",
                "    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)",
                "    repositories {",
                "        google()",
                "        mavenCentral()",
                "    }",
                "}",
                "EOF_PM",
                "        cat settings.gradle.kts >> settings.gradle.kts.tmp",
                "        mv settings.gradle.kts.tmp settings.gradle.kts",
                "    fi",
                "    if ! grep -q \":app\" settings.gradle.kts; then",
                "        echo \"include(\\\":app\\\")\" >> settings.gradle.kts",
                "    fi",
                "elif [ -f settings.gradle ]; then",
                "    if ! grep -q \"pluginManagement\" settings.gradle; then",
                "        cat << 'EOF_PM_GROOVY' > settings.gradle.tmp",
                "pluginManagement {",
                "    repositories {",
                "        google()",
                "        mavenCentral()",
                "        gradlePluginPortal()",
                "    }",
                "}",
                "dependencyResolutionManagement {",
                "    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)",
                "    repositories {",
                "        google()",
                "        mavenCentral()",
                "    }",
                "}",
                "EOF_PM_GROOVY",
                "        cat settings.gradle >> settings.gradle.tmp",
                "        mv settings.gradle.tmp settings.gradle",
                "    fi",
                "    if ! grep -q \":app\" settings.gradle; then",
                "        echo \"include ':app'\" >> settings.gradle",
                "    fi",
                "fi",
                "if [ ! -f build.gradle ] && [ ! -f build.gradle.kts ]; then",
                "    echo \"// Root build file\" > build.gradle",
                "fi",
                "echo \"===> التحقق من موارد التطبيق الأساسية (App Icons & Values)...\"",
                "RES_DIR=\"app/src/main/res\"",
                "if [ ! -d \"\$RES_DIR\" ]; then",
                "    mkdir -p \"\$RES_DIR\"",
                "fi",
                "mkdir -p \"\$RES_DIR/drawable\" \"\$RES_DIR/mipmap-anydpi-v26\" \"\$RES_DIR/values\"",
                "if [ ! -f \"\$RES_DIR/values/colors.xml\" ]; then",
                "    cat << 'EOF_COLORS' > \"\$RES_DIR/values/colors.xml\"",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>",
                "<resources>",
                "    <color name=\"colorPrimary\">#38BDF8</color>",
                "    <color name=\"colorPrimaryDark\">#050816</color>",
                "    <color name=\"colorAccent\">#22C55E</color>",
                "    <color name=\"primary\">#38BDF8</color>",
                "    <color name=\"primary_variant\">#0284C7</color>",
                "    <color name=\"secondary\">#22C55E</color>",
                "</resources>",
                "EOF_COLORS",
                "else",
                "    if ! grep -q \"colorPrimary\" \"\$RES_DIR/values/colors.xml\"; then",
                "        sed -i 's/<\\/resources>/    <color name=\"colorPrimary\">#38BDF8<\\/color>\\n    <color name=\"colorPrimaryDark\">#050816<\\/color>\\n    <color name=\"colorAccent\">#22C55E<\\/color>\\n<\\/resources>/' \"\$RES_DIR/values/colors.xml\" 2>/dev/null || true",
                "    fi",
                "fi",
                "if [ ! -f \"\$RES_DIR/values/styles.xml\" ] && [ ! -f \"\$RES_DIR/values/themes.xml\" ]; then",
                "    cat << 'EOF_STYLES' > \"\$RES_DIR/values/styles.xml\"",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>",
                "<resources>",
                "    <style name=\"AppTheme\" parent=\"Theme.MaterialComponents.DayNight.NoActionBar\">",
                "        <item name=\"colorPrimary\">@color/colorPrimary</item>",
                "        <item name=\"colorPrimaryDark\">@color/colorPrimaryDark</item>",
                "        <item name=\"colorAccent\">@color/colorAccent</item>",
                "    </style>",
                "</resources>",
                "EOF_STYLES",
                "else",
                "    for sf in \"\$RES_DIR/values/styles.xml\" \"\$RES_DIR/values/themes.xml\"; do",
                "        if [ -f \"\$sf\" ]; then",
                "            sed -i 's/parent=\"android:Theme.Material.Light.NoActionBar\"/parent=\"Theme.MaterialComponents.DayNight.NoActionBar\"/g' \"\$sf\" 2>/dev/null || true",
                "            sed -i 's/parent=\"android:Theme.Material.NoActionBar\"/parent=\"Theme.MaterialComponents.DayNight.NoActionBar\"/g' \"\$sf\" 2>/dev/null || true",
                "            sed -i 's/parent=\"android:Theme.Material\"/parent=\"Theme.MaterialComponents.DayNight\"/g' \"\$sf\" 2>/dev/null || true",
                "        fi",
                "    done",
                "fi",
                "if [ ! -f \"\$RES_DIR/drawable/ic_launcher_background.xml\" ] && [ ! -f \"\$RES_DIR/mipmap-hdpi/ic_launcher.png\" ] && [ ! -f \"\$RES_DIR/drawable/ic_project_launcher.png\" ]; then",
                "    cat << 'EOF_BG' > \"\$RES_DIR/drawable/ic_launcher_background.xml\"",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>",
                "<vector xmlns:android=\"http://schemas.android.com/apk/res/android\"",
                "    android:width=\"108dp\"",
                "    android:height=\"108dp\"",
                "    android:viewportWidth=\"108\"",
                "    android:viewportHeight=\"108\">",
                "    <path android:fillColor=\"#FF0F172A\" android:pathData=\"M0,0h108v108h-108z\" />",
                "</vector>",
                "EOF_BG",
                "fi",
                "if [ ! -f \"\$RES_DIR/drawable/ic_launcher_foreground.xml\" ] && [ ! -f \"\$RES_DIR/mipmap-hdpi/ic_launcher.png\" ] && [ ! -f \"\$RES_DIR/drawable/ic_project_launcher.png\" ]; then",
                "    cat << 'EOF_FG' > \"\$RES_DIR/drawable/ic_launcher_foreground.xml\"",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>",
                "<vector xmlns:android=\"http://schemas.android.com/apk/res/android\"",
                "    android:width=\"108dp\"",
                "    android:height=\"108dp\"",
                "    android:viewportWidth=\"108\"",
                "    android:viewportHeight=\"108\">",
                "    <path android:fillColor=\"#FF38BDF8\" android:pathData=\"M38,34 L26,54 L38,74 M70,34 L82,54 L70,74 M59,30 L49,78\" android:strokeColor=\"#FF38BDF8\" android:strokeLineCap=\"round\" android:strokeLineJoin=\"round\" android:strokeWidth=\"5\" />",
                "</vector>",
                "EOF_FG",
                "fi",
                "if [ ! -f \"\$RES_DIR/mipmap-anydpi-v26/ic_launcher.xml\" ] && [ ! -f \"\$RES_DIR/mipmap/ic_launcher.png\" ] && [ ! -f \"\$RES_DIR/mipmap-hdpi/ic_launcher.png\" ]; then",
                "    cat << 'EOF_IC' > \"\$RES_DIR/mipmap-anydpi-v26/ic_launcher.xml\"",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>",
                "<adaptive-icon xmlns:android=\"http://schemas.android.com/apk/res/android\">",
                "    <background android:drawable=\"@drawable/ic_launcher_background\" />",
                "    <foreground android:drawable=\"@drawable/ic_launcher_foreground\" />",
                "</adaptive-icon>",
                "EOF_IC",
                "fi",
                "if [ ! -f \"\$RES_DIR/mipmap-anydpi-v26/ic_launcher_round.xml\" ] && [ ! -f \"\$RES_DIR/mipmap/ic_launcher_round.png\" ] && [ ! -f \"\$RES_DIR/mipmap-hdpi/ic_launcher_round.png\" ]; then",
                "    cat << 'EOF_ICR' > \"\$RES_DIR/mipmap-anydpi-v26/ic_launcher_round.xml\"",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>",
                "<adaptive-icon xmlns:android=\"http://schemas.android.com/apk/res/android\">",
                "    <background android:drawable=\"@drawable/ic_launcher_background\" />",
                "    <foreground android:drawable=\"@drawable/ic_launcher_foreground\" />",
                "</adaptive-icon>",
                "EOF_ICR",
                "fi",
                "echo \"===> التحقق من إعدادات توافقية Java و Kotlin JVM Targets...\"",
                "APP_BUILD_GRADLE=\"\"",
                "if [ -f app/build.gradle.kts ]; then APP_BUILD_GRADLE=\"app/build.gradle.kts\";",
                "elif [ -f app/build.gradle ]; then APP_BUILD_GRADLE=\"app/build.gradle\"; fi",
                "if [ -n \"\$APP_BUILD_GRADLE\" ]; then",
                "    if ! grep -q \"compileOptions\" \"\$APP_BUILD_GRADLE\"; then",
                "        echo '===> إضافة compileOptions (JavaVersion.VERSION_17) إلى app build file...' ",
                "        if grep -q \"defaultConfig\" \"\$APP_BUILD_GRADLE\"; then",
                "            sed -i '/defaultConfig {/,/}/ { /}/a \\    compileOptions {\\n        sourceCompatibility = JavaVersion.VERSION_17\\n        targetCompatibility = JavaVersion.VERSION_17\\n    }' \"\$APP_BUILD_GRADLE\" 2>/dev/null || true",
                "        fi",
                "    fi",
                "    HAS_KT=\$(find app/src/main -type f -name \"*.kt\" 2>/dev/null | head -n 1)",
                "    HAS_JAVA=\$(find app/src/main -type f -name \"*.java\" 2>/dev/null | head -n 1)",
                "    if [ -n \"\$HAS_JAVA\" ] && [ -z \"\$HAS_KT\" ]; then",
                "        echo '===> مشروع Java خالص، إزالة بلاجن Kotlin لمنع تعارض الـ JVM...' ",
                "        sed -i '/org.jetbrains.kotlin.android/d' \"\$APP_BUILD_GRADLE\" 2>/dev/null || true",
                "        # Remove any stray AppLogger.kt in pure Java projects so kotlinc doesn't complain",
                "        find app/src/main/java -name \"AppLogger.kt\" -delete 2>/dev/null || true",
                "    elif [ -n \"\$HAS_KT\" ] && [ -n \"\$HAS_JAVA\" ]; then",
                "        # If project has both, keep the one matching language to prevent duplicate classes",
                "        if grep -q \"org.jetbrains.kotlin.android\" \"\$APP_BUILD_GRADLE\"; then",
                "            find app/src/main/java -name \"AppLogger.java\" -delete 2>/dev/null || true",
                "        else",
                "            find app/src/main/java -name \"AppLogger.kt\" -delete 2>/dev/null || true",
                "        fi",
                "        # If project only has auto-generated empty activities in the other language, clean up duplicates",
                "        for kt_file in \$(find app/src/main/java -type f -name \"*.kt\" 2>/dev/null); do",
                "            j_equiv=\"\${kt_file%.kt}.java\"",
                "            if [ -f \"\$j_equiv\" ]; then",
                "                if grep -q \"public class\" \"\$j_equiv\" && grep -q \"class .* : AppCompatActivity\" \"\$kt_file\"; then",
                "                    echo \"===> تنظيف ملف Kotlin مكرر لمشروع Java: \$kt_file\"",
                "                    rm -f \"\$kt_file\"",
                "                fi",
                "            fi",
                "        done",
                "    fi",
                "fi",
                "# If AppLogger exists in project but lacks full logcat streaming or init, upgrade it seamlessly",
                "for f_logger in \$(find app/src/main/java -name \"AppLogger.kt\" 2>/dev/null); do",
                "    if [ -f \"\$f_logger\" ] && ! grep -q \"FLAG_INCLUDE_STOPPED_PACKAGES\" \"\$f_logger\"; then",
                "        echo \"===> تحديث كلاس AppLogger.kt لدعم البث المباشر وأعلام Android...\"",
                "        sed -i \"/ACTION_LOG/a \\    private const val ACTION_LOG_EVENT = \\\"com.androidexl.ideultimate.ACTION_LOG_EVENT\\\"\" \"\$f_logger\" 2>/dev/null || true",
                "        sed -i \"/com.androidexl.ideultimate/a \\                addFlags(android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES)\" \"\$f_logger\" 2>/dev/null || true",
                "    fi",
                "    if [ -f \"\$f_logger\" ] && ! grep -q \"fun init(\" \"\$f_logger\"; then",
                "        echo \"===> تحديث كلاس AppLogger.kt لدعم init(Context)...\"",
                "        sed -i \"/object AppLogger {/a \\    fun init(context: android.content.Context?) { send(context, \\\"I\\\", \\\"AppLogger\\\", \\\"Initialized\\\") }\" \"\$f_logger\" 2>/dev/null || true",
                "    fi",
                "done",
                "for f_logger in \$(find app/src/main/java -name \"AppLogger.java\" 2>/dev/null); do",
                "    if [ -f \"\$f_logger\" ] && ! grep -q \"FLAG_INCLUDE_STOPPED_PACKAGES\" \"\$f_logger\"; then",
                "        echo \"===> تحديث كلاس AppLogger.java لدعم البث المباشر وأعلام Android...\"",
                "        sed -i \"/intent.setPackage/a \\            intent.addFlags(android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES);\" \"\$f_logger\" 2>/dev/null || true",
                "    fi",
                "    if [ -f \"\$f_logger\" ] && ! grep -q \"public static void init(\" \"\$f_logger\"; then",
                "        echo \"===> تحديث كلاس AppLogger.java لدعم init(Context)...\"",
                "        sed -i \"/public class AppLogger {/a \\    public static void init(android.content.Context context) { send(context, \\\"I\\\", \\\"AppLogger\\\", \\\"Initialized\\\"); }\" \"\$f_logger\" 2>/dev/null || true",
                "    fi",
                "done",
                "for prop in \"/root/.gradle/gradle.properties\" \"gradle.properties\"; do",
                "    touch \"\$prop\"",
                "    if ! grep -q \"kotlin.jvm.target.validation.mode\" \"\$prop\"; then",
                "        echo \"kotlin.jvm.target.validation.mode=warning\" >> \"\$prop\"",
                "    fi",
                "done",
                "echo \"===> التحقق من إعدادات AAPT2 وتوافقية المعمارية (ARM64)...\"",
                "mkdir -p /root/.gradle",
                "mkdir -p /root/android-sdk/build-tools/34.0.0",
                "mkdir -p /root/android-arm-tools",
                "if [ ! -f /root/android-arm-tools/build-tools/aapt2 ]; then",
                "    echo '===> تحميل حزمة Android SDK Tools المترجمة لمعمارية aarch64...' ",
                "    wget -q --show-progress -O /root/android-arm-tools/tools.zip https://github.com/lzhiyong/android-sdk-tools/releases/download/35.0.2/android-sdk-tools-static-aarch64.zip || curl -L -o /root/android-arm-tools/tools.zip https://github.com/lzhiyong/android-sdk-tools/releases/download/35.0.2/android-sdk-tools-static-aarch64.zip || true",
                "    if [ -f /root/android-arm-tools/tools.zip ]; then",
                "        unzip -q -o /root/android-arm-tools/tools.zip -d /root/android-arm-tools/ || true",
                "        chmod +x /root/android-arm-tools/build-tools/* /root/android-arm-tools/platform-tools/* 2>/dev/null || true",
                "    fi",
                "fi",
                "if [ -f /root/android-arm-tools/build-tools/aapt2 ]; then",
                "    NATIVE_AAPT=\"/root/android-arm-tools/build-tools/aapt2\"",
                "    cp -f /root/android-arm-tools/build-tools/* /root/android-sdk/build-tools/34.0.0/ 2>/dev/null || true",
                "elif command -v aapt2 >/dev/null 2>&1; then",
                "    NATIVE_AAPT=\$(command -v aapt2)",
                "elif command -v aapt >/dev/null 2>&1; then",
                "    NATIVE_AAPT=\$(command -v aapt)",
                "else",
                "    NATIVE_AAPT=\"\"",
                "fi",
                "if [ -n \"\$NATIVE_AAPT\" ]; then",
                "    cp -f \"\$NATIVE_AAPT\" /root/android-sdk/build-tools/34.0.0/aapt2 2>/dev/null || true",
                "    chmod +x /root/android-sdk/build-tools/34.0.0/aapt2",
                "    AAPT_PATH=\"/root/android-sdk/build-tools/34.0.0/aapt2\"",
                "    for prop in \"/root/.gradle/gradle.properties\" \"gradle.properties\"; do",
                "        touch \"\$prop\"",
                "        grep -v \"android.aapt2\" \"\$prop\" > \"\$prop.tmp\" 2>/dev/null || cp \"\$prop\" \"\$prop.tmp\"",
                "        echo \"android.aapt2FromMaven=false\" >> \"\$prop.tmp\"",
                "        echo \"android.aapt2FromMavenOverride=\$AAPT_PATH\" >> \"\$prop.tmp\"",
                "        echo \"android.aapt2Executable=\$AAPT_PATH\" >> \"\$prop.tmp\"",
                "        mv \"\$prop.tmp\" \"\$prop\"",
                "    done",
                "    find /root/.gradle/caches -name \"aapt2\" -type f 2>/dev/null | while read -r cached_aapt; do",
                "        cp -f \"\$AAPT_PATH\" \"\$cached_aapt\" 2>/dev/null || true",
                "        chmod +x \"\$cached_aapt\" 2>/dev/null || true",
                "    done",
                "fi",
                if (isFlutter) "echo '===> مشروع Flutter: التحقق من حزم pubspec.yaml وأمر flutter build...'" else "echo ''",
                if (isFlutter) "export PATH=/root/flutter/bin:\$PATH" else "echo ''",
                if (isFlutter) "if [ -f android/local.properties ]; then grep -q '^flutter.sdk=' android/local.properties || echo 'flutter.sdk=/root/flutter' >> android/local.properties; fi" else "echo ''",
                if (isFlutter) "if command -v flutter >/dev/null 2>&1 || [ -f /root/flutter/bin/flutter ]; then echo '===> flutter pub get...'; /root/flutter/bin/flutter pub get 2>&1; echo '===> flutter build apk --debug...'; /root/flutter/bin/flutter build apk --debug -v 2>&1; exit \$?; fi" else "echo ''",
                "TASK_CMD=\":app:assembleDebug\"",
                "if [ ! -d ./app ]; then",
                "    TASK_CMD=\"assembleDebug\"",
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
                onProcessStarted = { proc ->
                    buildMgr.activeProcess = proc
                },
            )
            buildMgr.activeProcess = null

            withContext(Dispatchers.Main) {
                buildMgr.isBuilding = false
                // Mark any last running step
                val runningIdx = buildSteps.indexOfLast { it.status == BuildTaskStepStatus.RUNNING }
                if (runningIdx >= 0) {
                    val prev = buildSteps[runningIdx]
                    buildSteps[runningIdx] = prev.copy(
                        status = if (rc == 0) BuildTaskStepStatus.SUCCESS else BuildTaskStepStatus.FAILED
                    )
                }

                if (rc == 0) {
                    buildMgr.buildSuccess = true
                    buildMgr.currentTaskName = "اكتمل البناء بنجاح ✓"
                    val foundApk = File(projectRoot, "build/app/outputs/flutter-apk/app-debug.apk").takeIf { it.exists() }
                        ?: File(projectRoot, "app/build/outputs/apk/debug/app-debug.apk").takeIf { it.exists() }
                        ?: projectRoot.walkTopDown().filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }.firstOrNull()
                    if (foundApk != null && foundApk.exists()) {
                        buildMgr.apkOutputPath = foundApk.absolutePath
                        appendLog("\n🎉 تم العثور على ملف APK بنجاح:\n${foundApk.absolutePath}")
                        installApk(foundApk.absolutePath)
                    } else {
                        appendLog("\n✓ اكتمل أمر البناء (كود 0)، لكن لم يتم العثور على ملف apk مجمع داخل المشروع.")
                    }
                } else {
                    buildMgr.buildSuccess = false
                    buildMgr.currentTaskName = "فشل البناء (كود الخطأ: $rc)"
                    appendLog("\n✕ فشلت عملية البناء. يمكنك فحص الخطأ أعلاه أو فتح تفاصيل المهمة.")
                }
            }
        }
    }

    LaunchedEffect(project.id) {
        if (!buildMgr.isBuilding && !buildMgr.isInstallingTools) {
            buildMgr.resetForProject(project.id)
            checkToolsEnvironment()
        }
    }

    LaunchedEffect(rawLogOutput) {
        if (!terminalScrollState.isScrollInProgress) {
            terminalScrollState.scrollTo(terminalScrollState.maxValue)
        }
    }

    // -------------------------------------------------------------------------
    // Full Screen Activity Layout
    // -------------------------------------------------------------------------
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Midnight)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepNavy)
                .border(1.dp, GlassStrokeTop)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                IconButton(onClick = { performCloseAndCleanup() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink,
                    )
                }
                Column {
                    Text(
                        text = if (isFlutter) "بناء محلي Flutter (Terminal Engine)" else "بناء محلي Gradle (Terminal Engine)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        ),
                        color = Ink,
                    )
                    Text(
                        text = "${project.name} • ${project.packageName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                        ),
                        color = Cyan,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { copyLogToClipboard(rawLogOutput) },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_content_copy),
                        contentDescription = "نسخ السجل",
                        tint = Cyan,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = { performCloseAndCleanup() }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Muted,
                    )
                }
            }
        }

        // Status Card Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(IndigoDeep)
                .border(1.dp, Cyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (isFlutter) "محرك Flutter SDK + PRoot Ubuntu ARM64" else "محرك Gradle Daemon + كاش دائم /root/.gradle",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                    ),
                    color = Muted,
                )
            }

            if (isBuilding || isInstallingTools) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = Cyan,
                )
            } else if (buildSuccess == true) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        if (isBuilding || isInstallingTools) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = Cyan,
                trackColor = DeepNavy,
            )
        }

        // Missing Tools Notice
        val missingTools = toolStatuses.filter { !it.isInstalled }
        if (!isCheckingEnv && missingTools.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF59E0B).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (isFlutter) "الأدوات المطلوبة لبناء تطبيق Flutter" else "الأدوات المطلوبة لبناء Android و Gradle",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFF59E0B),
                )
                missingTools.forEach { tool ->
                    Text(
                        text = "• ${tool.name}: ${tool.description}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Ink.copy(alpha = 0.85f),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF59E0B))
                        .clickable { installMissingTools() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isFlutter) "تحميل وتثبيت Flutter SDK والأدوات الآن" else "تثبيت أدوات Android SDK و Gradle الآن",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                        ),
                        color = Color.Black,
                    )
                }
            }
        }

        // Main Content Area: Tree / ListView of operations
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            // Switcher Tabs: Tasks Tree vs Raw Terminal Log
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (buildViewTab == 0) Cyan.copy(alpha = 0.18f) else IndigoDeep)
                        .border(
                            1.dp,
                            if (buildViewTab == 0) Cyan else GlassStrokeTop,
                            RoundedCornerShape(8.dp),
                        )
                        .clickable { buildViewTab = 0 },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "شجرة المهام (${buildSteps.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                        ),
                        color = if (buildViewTab == 0) Cyan else Muted,
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (buildViewTab == 1) Cyan.copy(alpha = 0.18f) else IndigoDeep)
                        .border(
                            1.dp,
                            if (buildViewTab == 1) Cyan else GlassStrokeTop,
                            RoundedCornerShape(8.dp),
                        )
                        .clickable { buildViewTab = 1 },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "السجل الطرفي الكامل (Terminal)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                        ),
                        color = if (buildViewTab == 1) Cyan else Muted,
                    )
                }
            }

            if (buildViewTab == 0) {
                if (buildSteps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepNavy)
                            .border(1.dp, GlassStrokeTop, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Cyan.copy(alpha = 0.6f),
                                modifier = Modifier.size(36.dp),
                            )
                            Text(
                                text = "جاهز لتنفيذ عملية البناء المفصلة",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Ink,
                            )
                            Text(
                                text = if (isFlutter) "سيتم تتبع أوامر flutter pub get و flutter build apk -v خطوة بخطوة" else "سيتم استخراج كل مهمة Gradle وعرض تفاصيلها المباشرة",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Muted,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepNavy)
                            .border(1.dp, GlassStrokeTop, RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(buildSteps, key = { it.id }) { step ->
                            val isExpanded = expandedStepIds.contains(step.id)
                            val statusColor = when (step.status) {
                                BuildTaskStepStatus.SUCCESS -> Color(0xFF10B981)
                                BuildTaskStepStatus.RUNNING -> Cyan
                                BuildTaskStepStatus.FAILED -> Color(0xFFEF4444)
                                BuildTaskStepStatus.SKIPPED -> Muted
                                BuildTaskStepStatus.PENDING -> Muted
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IndigoDeep)
                                    .border(
                                        width = 1.dp,
                                        color = if (step.status == BuildTaskStepStatus.RUNNING) Cyan.copy(alpha = 0.5f) else GlassStrokeTop,
                                        shape = RoundedCornerShape(8.dp),
                                    )
                                    .padding(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedSheetStep = step
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(statusColor),
                                        )
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = step.title,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = JetBrainsMono,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp,
                                                ),
                                                color = Ink,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            if (step.subtitle.isNotBlank()) {
                                                Text(
                                                    text = step.subtitle,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.5.sp,
                                                    ),
                                                    color = Muted,
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(statusColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp),
                                        ) {
                                            Text(
                                                text = step.status.name,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = JetBrainsMono,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                                color = statusColor,
                                            )
                                        }
                                        IconButton(
                                            modifier = Modifier.size(28.dp),
                                            onClick = {
                                                expandedStepIds = if (isExpanded) {
                                                    expandedStepIds - step.id
                                                } else {
                                                    expandedStepIds + step.id
                                                }
                                            },
                                        ) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = Muted,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut(),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF030712))
                                            .padding(8.dp),
                                    ) {
                                        Text(
                                            text = step.logs.takeLast(10).joinToString("\n").ifBlank { "لا توجد تفاصيل إضافية مسجلة لهذه المهمة." },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = JetBrainsMono,
                                                fontSize = 9.5.sp,
                                                lineHeight = 14.sp,
                                            ),
                                            color = Color(0xFFE2E8F0),
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "اضغط على المهمة لفتح النافذة السفلية الكاملة",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = Cyan,
                                            modifier = Modifier.clickable { selectedSheetStep = step },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Raw Terminal Output Console (سجل الطرفية الكامل)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF030712))
                            .border(1.dp, GlassStrokeTop, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                    ) {
                        SelectionContainer(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(terminalScrollState)
                                    .horizontalScroll(rememberScrollState()),
                            ) {
                                Text(
                                    text = rawLogOutput.ifBlank { "جاهز لبدء البناء... ستظهر مخرجات الطرفية الكاملة هنا مباشرة." },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 10.sp,
                                        lineHeight = 15.sp,
                                    ),
                                    color = Color(0xFFE2E8F0),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Install APK bar if generated
        if (apkOutputPath != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981))))
                    .clickable { installApk(apkOutputPath!!) },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "تثبيت على الهاتف",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        ),
                        color = Color.White,
                    )
                }
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val btnTitle = when {
                isBuilding -> "جاري البناء..."
                isFlutter && missingTools.isNotEmpty() -> "تحميل Flutter SDK والأدوات وبدء البناء"
                !isFlutter && missingTools.isNotEmpty() -> "تثبيت الأدوات وبدء البناء"
                isFlutter -> "بدء بناء Flutter الآن (flutter build apk -v)"
                else -> "بدء بناء Gradle الآن (assembleDebug)"
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (isBuilding) {
                            Modifier.background(DeepNavy)
                        } else {
                            Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF0284C7), Cyan)))
                        }
                    )
                    .border(1.dp, Cyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable {
                        if (!isBuilding && !isInstallingTools) {
                            startGradleBuild()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = btnTitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                    ),
                    color = if (isBuilding) Muted else Color.Black,
                )
            }

            if (isBuilding || isInstallingTools) {
                Box(
                    modifier = Modifier
                        .height(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.18f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable { abortBuildAndClose() }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "إلغاء البناء",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFEF4444),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .height(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(IndigoDeep)
                    .border(1.dp, GlassStrokeTop, RoundedCornerShape(10.dp))
                    .clickable { performCloseAndCleanup() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isBuilding) "الخلفية (إخفاء)" else "إغلاق",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // Bottom Sheet for Operation Details (نافذة سفلية لعرض التفاصيل)
    // -------------------------------------------------------------------------
    val activeStep = selectedSheetStep
    if (activeStep != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedSheetStep = null },
            sheetState = sheetState,
            containerColor = DeepNavy,
            contentColor = Ink,
            scrimColor = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = activeStep.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            ),
                            color = Ink,
                        )
                        if (activeStep.subtitle.isNotBlank()) {
                            Text(
                                text = activeStep.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Muted,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(IndigoDeep)
                            .border(1.dp, Cyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable {
                                copyLogToClipboard(activeStep.logs.joinToString("\n"))
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = "نسخ التفاصيل",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                            ),
                            color = Cyan,
                        )
                    }
                }

                // Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "الحالة:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Text(
                        text = activeStep.status.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = when (activeStep.status) {
                            BuildTaskStepStatus.SUCCESS -> Color(0xFF10B981)
                            BuildTaskStepStatus.RUNNING -> Cyan
                            BuildTaskStepStatus.FAILED -> Color(0xFFEF4444)
                            else -> Muted
                        },
                    )
                    Text(
                        text = "• عدد السطور: ${activeStep.logs.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }

                // Detailed Log Console
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF030712))
                            .border(1.dp, IndigoDeep, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                    ) {
                        SelectionContainer(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .horizontalScroll(rememberScrollState()),
                            ) {
                                Text(
                                    text = activeStep.logs.joinToString("\n").ifBlank { "لا توجد مخرجات تفصيلية مسجلة لهذه العملية." },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 10.sp,
                                        lineHeight = 15.sp,
                                    ),
                                    color = Color(0xFFE2E8F0),
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(IndigoDeep)
                        .border(1.dp, GlassStrokeTop, RoundedCornerShape(8.dp))
                        .clickable { selectedSheetStep = null },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "إغلاق التفاصيل",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                    )
                }
            }
        }
    }
}
