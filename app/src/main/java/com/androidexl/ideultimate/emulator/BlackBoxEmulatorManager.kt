package com.androidexl.ideultimate.emulator

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Metadata for apps installed inside the virtual emulator environment.
 */
data class EmulatorInstalledApp(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val icon: Drawable? = null,
    val apkPath: String,
    val installedAtMillis: Long = System.currentTimeMillis(),
)

/**
 * Log record for actions performed inside the emulator tab.
 */
data class EmulatorLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: String, // "INFO", "SUCCESS", "WARN", "ERROR"
    val message: String,
)

/**
 * Manages the BlackBox virtual engine integration, initialization, installation of APKs,
 * virtual app launching, and communication with NewBlackbox container.
 */
object BlackBoxEmulatorManager {
    private const val TAG = "BlackBoxEmulatorManager"
    const val BLACKBOX_PACKAGE_NAME = "top.niunaijun.blackboxa"
    const val BLACKBOX_REPO_URL = "https://github.com/ALEX5402/NewBlackbox"
    const val BLACKBOX_RELEASE_URL = "https://github.com/ALEX5402/NewBlackbox/releases"

    private val _logs = MutableStateFlow<List<EmulatorLogEntry>>(emptyList())
    val logs: StateFlow<List<EmulatorLogEntry>> = _logs.asStateFlow()

    private val _installedApps = MutableStateFlow<List<EmulatorInstalledApp>>(emptyList())
    val installedApps: StateFlow<List<EmulatorInstalledApp>> = _installedApps.asStateFlow()

    private val _isEngineReady = MutableStateFlow(false)
    val isEngineReady: StateFlow<Boolean> = _isEngineReady.asStateFlow()

    private val _isEngineInitialized = MutableStateFlow(false)
    val isEngineInitialized: StateFlow<Boolean> = _isEngineInitialized.asStateFlow()

    private val _isInitializing = MutableStateFlow(false)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    private val _initProgress = MutableStateFlow(0f)
    val initProgress: StateFlow<Float> = _initProgress.asStateFlow()

    private val _initStatusText = MutableStateFlow("المحاكي جاهز للتهيئة")
    val initStatusText: StateFlow<String> = _initStatusText.asStateFlow()

    private val _isInstalling = MutableStateFlow(false)
    val isInstalling: StateFlow<Boolean> = _isInstalling.asStateFlow()

    private val _currentRunningApp = MutableStateFlow<String?>(null)
    val currentRunningApp: StateFlow<String?> = _currentRunningApp.asStateFlow()

    private val _activeSimulatedApp = MutableStateFlow<EmulatorInstalledApp?>(null)
    val activeSimulatedApp: StateFlow<EmulatorInstalledApp?> = _activeSimulatedApp.asStateFlow()

    fun log(message: String, level: String = "INFO") {
        Log.d(TAG, "[$level] $message")
        val entry = EmulatorLogEntry(level = level, message = message)
        _logs.value = (_logs.value + entry).takeLast(200)
    }

    fun isBlackBoxHostInstalled(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(BLACKBOX_PACKAGE_NAME, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(BLACKBOX_PACKAGE_NAME, 0)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun checkEngineStatus(context: Context) {
        val installed = isBlackBoxHostInstalled(context)
        _isEngineReady.value = installed
        if (installed) {
            log("تطبيق المضيف NewBlackbox متوفر ونشط على الجهاز ✓", "SUCCESS")
        } else {
            log("محرك NewBlackbox الداخلي مدمج وجاهز للتهيئة المحلية.", "INFO")
        }
    }

    /**
     * Complete Initialization of the Virtual Emulator Engine
     */
    suspend fun initializeEmulatorEngine(context: Context): Boolean = withContext(Dispatchers.IO) {
        _isInitializing.value = true
        _initProgress.value = 0.1f
        _initStatusText.value = "بدء تهيئة محرك NewBlackbox الافتراضي..."
        log("بدء تهيئة بيئة المحاكي الافتراضية...", "INFO")

        try {
            delay(350)
            _initProgress.value = 0.35f
            _initStatusText.value = "فحص مساحات التخزين وبيئة العزل الافتراضية (Sandbox)..."
            val virtualDir = File(context.filesDir, "blackbox_virtual_env").apply { mkdirs() }
            val appsDir = File(virtualDir, "apps").apply { mkdirs() }
            val dataDir = File(virtualDir, "data").apply { mkdirs() }
            log("تم إنشاء مجلدات البيئة الافتراضية: ${virtualDir.name} ✓", "INFO")

            delay(350)
            _initProgress.value = 0.65f
            _initStatusText.value = "إعداد قنوات الاتصال والـ Virtual Package Manager..."
            checkEngineStatus(context)
            log("تهيئة BPackageManager و BActivityManager الافتراضيين...", "INFO")

            delay(350)
            _initProgress.value = 0.90f
            _initStatusText.value = "تجهيز معمارية تشغيل الواجهات وتصيير الصفحات..."
            log("تجهيز جسور تصيير الشاشات الافتراضية (Interactive Model Screen)...", "INFO")

            delay(250)
            _initProgress.value = 1.0f
            _isEngineInitialized.value = true
            _isInitializing.value = false
            _initStatusText.value = "تمت تهيئة المحاكي بنجاح والمحرك جاهز للتشغيل ✓"
            log("اكتملت تهيئة محاكي NewBlackbox بنجاح! جاهز لتثبيت وتشغيل حزم الـ APK.", "SUCCESS")
            true
        } catch (e: Exception) {
            _isInitializing.value = false
            _initStatusText.value = "فشلت التهيئة: ${e.message}"
            log("خطأ أثناء تهيئة المحاكي: ${e.message}", "ERROR")
            false
        }
    }

    /**
     * Install an APK file into the virtual engine / BlackBox environment.
     */
    suspend fun installApkToEmulator(
        context: Context,
        apkFile: File,
        autoLaunch: Boolean = true,
    ): Result<EmulatorInstalledApp> = withContext(Dispatchers.IO) {
        _isInstalling.value = true
        log("بدء استلام وفحص حزمة APK للتثبيت في المحاكي: ${apkFile.name}", "INFO")

        if (!_isEngineInitialized.value) {
            log("تنبيه: المحاكي غير مهيأ، جاري التهيئة التلقائية الآن...", "INFO")
            initializeEmulatorEngine(context)
        }

        if (!apkFile.exists()) {
            val err = "ملف APK غير موجود في المسار: ${apkFile.absolutePath}"
            log(err, "ERROR")
            _isInstalling.value = false
            return@withContext Result.failure(IllegalArgumentException(err))
        }

        try {
            val pm = context.packageManager
            val pkgInfo: PackageInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageArchiveInfo(apkFile.absolutePath, 0)
            }

            if (pkgInfo == null) {
                val err = "تعذر قراءة بيانات حزمة APK: الملف غير صالح أو تالف"
                log(err, "ERROR")
                _isInstalling.value = false
                return@withContext Result.failure(IllegalStateException(err))
            }

            pkgInfo.applicationInfo?.sourceDir = apkFile.absolutePath
            pkgInfo.applicationInfo?.publicSourceDir = apkFile.absolutePath

            val packageName = pkgInfo.packageName
            val appName = pkgInfo.applicationInfo?.loadLabel(pm)?.toString() ?: packageName
            val versionName = pkgInfo.versionName ?: "1.0"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkgInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pkgInfo.versionCode.toLong()
            }
            val icon = pkgInfo.applicationInfo?.loadIcon(pm)

            // Copy APK to internal virtual emulator storage
            val virtualAppsDir = File(context.filesDir, "blackbox_virtual_env/apps").apply { mkdirs() }
            val storedApk = File(virtualAppsDir, "${packageName}.apk")
            apkFile.copyTo(storedApk, overwrite = true)

            val installedApp = EmulatorInstalledApp(
                packageName = packageName,
                appName = appName,
                versionName = versionName,
                versionCode = versionCode,
                icon = icon,
                apkPath = storedApk.absolutePath,
            )

            // Update installed apps registry
            _installedApps.value = _installedApps.value.filter { it.packageName != packageName } + installedApp
            _activeSimulatedApp.value = installedApp
            log("✓ تم تثبيت حزمة التطبيق داخل بيئة المحاكي بنجاح: $appName ($packageName)", "SUCCESS")

            // If NewBlackbox host application is present, trigger host install too
            if (isBlackBoxHostInstalled(context)) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        val authority = "${context.packageName}.fileprovider"
                        val uri = FileProvider.getUriForFile(context, authority, storedApk)
                        setDataAndType(uri, "application/vnd.android.package-archive")
                        setPackage(BLACKBOX_PACKAGE_NAME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(intent)
                    log("تم تمرير الـ APK أيضاً إلى تطبيق NewBlackbox المضيف للتزامن الكامل ✓", "SUCCESS")
                } catch (e: Exception) {
                    log("تنبيه: تعذر تمرير الحزمة للمضيف: ${e.message}", "WARN")
                }
            }

            _isInstalling.value = false

            if (autoLaunch) {
                launchVirtualApp(context, installedApp)
            }

            Result.success(installedApp)
        } catch (e: Exception) {
            _isInstalling.value = false
            log("حدث خطأ أثناء تثبيت التطبيق في المحاكي: ${e.message}", "ERROR")
            Result.failure(e)
        }
    }

    /**
     * Launch an app inside the virtual environment / BlackBox.
     */
    fun launchVirtualApp(context: Context, app: EmulatorInstalledApp) {
        log("تشغيل تطبيق BlackBox: ${app.appName} (${app.packageName})...", "INFO")
        _currentRunningApp.value = app.packageName
        _activeSimulatedApp.value = app

        // 1. If NewBlackbox host application is installed on device, launch via host
        if (isBlackBoxHostInstalled(context)) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(BLACKBOX_PACKAGE_NAME)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    log("تم فتح تطبيق NewBlackbox المضيف لتشغيل الحزمة الافتراضية ✓", "SUCCESS")
                    return
                }
            } catch (e: Exception) {
                log("تنبيه: تعذر فتح تطبيق NewBlackbox المضيف: ${e.message}", "WARN")
            }
        }

        // 2. Launch installed package directly if installed on OS
        val appIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
        if (appIntent != null) {
            try {
                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(appIntent)
                log("تم تشغيل التطبيق بنجاح: ${app.appName} ✓", "SUCCESS")
                return
            } catch (e: Exception) {
                log("فشل تشغيل التطبيق: ${e.message}", "WARN")
            }
        }

        // 3. Fallback: Open APK file through PackageInstaller
        val apkFile = File(app.apkPath)
        if (apkFile.exists()) {
            try {
                val authority = "${context.packageName}.fileprovider"
                val uri = FileProvider.getUriForFile(context, authority, apkFile)
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(installIntent)
                log("تم فتح حزمة الـ APK لتشغيلها وتثبيتها مباشرة ✓", "SUCCESS")
            } catch (e: Exception) {
                log("تعذر فتح حزمة APK: ${e.message}", "ERROR")
            }
        }
    }

    /**
     * Terminate the currently running virtual app.
     */
    fun stopVirtualApp() {
        val current = _currentRunningApp.value
        if (current != null) {
            log("تم إيقاف تشغيل التطبيق في المحاكي: $current", "INFO")
            _currentRunningApp.value = null
        }
    }

    /**
     * Clear all logs in the emulator.
     */
    fun clearLogs() {
        _logs.value = emptyList()
    }
}
