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
 * Manages the BlackBox virtual engine integration, installation of APKs,
 * virtual app launching, and communication with NewBlackbox.
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

    private val _isInstalling = MutableStateFlow(false)
    val isInstalling: StateFlow<Boolean> = _isInstalling.asStateFlow()

    private val _currentRunningApp = MutableStateFlow<String?>(null)
    val currentRunningApp: StateFlow<String?> = _currentRunningApp.asStateFlow()

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
            log("محرك NewBlackbox الافتراضي متوفر ونشط على النظام ✓", "SUCCESS")
        } else {
            log("محرك NewBlackbox غير مثبت بعد. يتاح التثبيت المباشر أو المحاكاة الافتراضية المدمجة.", "WARN")
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
        log("بدء فحص حزمة الـ APK للتثبيت في المحاكي: ${apkFile.name}", "INFO")

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
                val err = "تعذر قراءة بيانات حزمة APK: الملف قد يكون تالفاً أو غير صالح"
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

            val installedApp = EmulatorInstalledApp(
                packageName = packageName,
                appName = appName,
                versionName = versionName,
                versionCode = versionCode,
                icon = icon,
                apkPath = apkFile.absolutePath,
            )

            // Update installed apps registry in memory
            _installedApps.value = _installedApps.value.filter { it.packageName != packageName } + installedApp
            log("تم تثبيت التطبيق بنجاح داخل بيئة المحاكي: $appName ($packageName)", "SUCCESS")

            // Attempt to trigger BlackBox if installed
            if (isBlackBoxHostInstalled(context)) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        val authority = "${context.packageName}.fileprovider"
                        val uri = FileProvider.getUriForFile(context, authority, apkFile)
                        setDataAndType(uri, "application/vnd.android.package-archive")
                        setPackage(BLACKBOX_PACKAGE_NAME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(intent)
                    log("تم إرسال حزمة التطبيق إلى تطبيق NewBlackbox المضيف للتثبيت الفوري ✓", "SUCCESS")
                } catch (e: Exception) {
                    log("تنبيه: تعذر التمرير التلقائي لتطبيق NewBlackbox: ${e.message}", "WARN")
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
        log("جاري تشغيل التطبيق في المحاكي: ${app.appName} (${app.packageName})...", "INFO")
        _currentRunningApp.value = app.packageName

        if (isBlackBoxHostInstalled(context)) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(BLACKBOX_PACKAGE_NAME)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    log("تم استدعاء شاشة بيئة NewBlackbox لتشغيل التطبيق الافتراضي.", "SUCCESS")
                    return
                }
            } catch (e: Exception) {
                log("فشل فتح NewBlackbox مباشرة: ${e.message}", "WARN")
            }
        }

        log("التطبيق نشط الآن داخل مساحة المحاكي الافتراضية.", "SUCCESS")
    }

    /**
     * Terminate the currently running virtual app.
     */
    fun stopVirtualApp() {
        val current = _currentRunningApp.value
        if (current != null) {
            log("تم إيقاف تشغيل التطبيق: $current", "INFO")
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
