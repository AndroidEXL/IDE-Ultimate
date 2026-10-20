package com.androidexl.ideultimate.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.androidexl.ideultimate.ui.theme.Midnight
import com.androidexl.ideultimate.ui.theme.IndigoDeep
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
    var isBuilding by remember { mutableStateOf(false) }
    var isInstallingTools by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (!isBuilding && !isInstallingTools) {
            onDismiss()
        } else {
            Toast.makeText(context, "يرجى الانتظار حتى اكتمال العملية الحالية...", Toast.LENGTH_SHORT).show()
        }
    }

    var isCheckingEnv by remember { mutableStateOf(true) }
    var isUbuntuReady by remember { mutableStateOf(false) }
    var toolStatuses by remember { mutableStateOf<List<AndroidToolStatus>>(emptyList()) }

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
                "OpenJDK 17" to "java -version",
                "Android SDK (Platform 34/35)" to "[ -d /root/android-sdk/platforms ] || [ -d /root/Android/Sdk/platforms ]",
                "Android Build Tools (aapt2/zipalign)" to "[ -d /root/android-sdk/build-tools ] || [ -d /root/Android/Sdk/build-tools ] || command -v aapt2",
                "Commandline Tools & sdkmanager" to "command -v sdkmanager || [ -f /root/android-sdk/cmdline-tools/latest/bin/sdkmanager ] || [ -f /root/Android/Sdk/cmdline-tools/latest/bin/sdkmanager ]",
                "Gradle Engine & Wrapper" to "command -v gradle || [ -f /root/.gradle/wrapper/dists ] || [ -f /root/project_build/gradlew ]",
            )

            val results = mutableListOf<AndroidToolStatus>()
            checks.forEach { (name, cmd) ->
                val (rc, _) = TerminalManager.execInUbuntu(context, cmd, timeoutSec = 15)
                val ok = (rc == 0)
                val desc = when (name) {
                    "OpenJDK 17" -> "محرك جافا الرسمي لتشغيل Gradle وتجميع الأكواد"
                    "Android SDK (Platform 34/35)" -> "منصات ومكتبات أندرويد الرسمية لبناء الحزم"
                    "Android Build Tools (aapt2/zipalign)" -> "أدوات بناء وضغط وتوقيع ملفات الـ APK"
                    "Commandline Tools & sdkmanager" -> "أدوات Google الرسمية لإدارة وتنزيل الحزم"
                    else -> "بيئة تشغيل ومحرك Gradle Wrapper"
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
            ).joinToString("\n")

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
                "export PATH=/root/android-sdk/cmdline-tools/latest/bin:/root/android-sdk/platform-tools:/root/android-sdk/build-tools/34.0.0:\$PATH",
                "cd /root/project_build",
                "echo \"===> مسار مجلد المشروع: \$(pwd)\"",
                "echo \"===> ذاكرة التخزين المؤقت الدائمة: /root/.gradle\"",
                "echo \"===> فحص ملفات وأدوات Gradle...\"",
                "echo \"===> التحقق من بنية مشروع Android و Gradle...\"",
                "if [ ! -d ./app ]; then",
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
                "if [ ! -f settings.gradle.kts ] && [ ! -f settings.gradle ]; then",
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

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD903060F)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(DeepNavy)
                    .border(1.dp, Color(0x2B00E5FF), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
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
                    .background(IndigoDeep)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
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
}
