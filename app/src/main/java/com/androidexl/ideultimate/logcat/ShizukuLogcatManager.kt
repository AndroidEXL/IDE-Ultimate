package com.androidexl.ideultimate.logcat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

data class SystemAppInfo(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean,
    val icon: Drawable?,
)

data class SystemLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val appName: String,
    val packageName: String,
    val timestamp: String,
    val level: LogcatLevel,
    val pid: String,
    val tag: String,
    val message: String,
    val isSystemApp: Boolean,
    val appIcon: Drawable? = null,
)

object ShizukuLogcatManager {

    const val SHIZUKU_PERMISSION_CODE = 4512
    const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    private const val PREF_SHIZUKU_GRANTED = "shizuku_permission_user_granted"

    private var permissionListenerRegistered = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("shizuku_ide_prefs", Context.MODE_PRIVATE)
    }

    fun initListener(context: Context) {
        if (!permissionListenerRegistered) {
            permissionListenerRegistered = true
            runCatching {
                Shizuku.addRequestPermissionResultListener { requestCode, grantResult ->
                    if (requestCode == SHIZUKU_PERMISSION_CODE) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            getPrefs(context).edit().putBoolean(PREF_SHIZUKU_GRANTED, true).apply()
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if Shizuku app is installed on the device
     */
    fun isShizukuInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return runCatching {
            pm.getPackageInfo(SHIZUKU_PACKAGE, 0)
            true
        }.getOrElse {
            runCatching {
                pm.getPackageInfo("riki.shizuku", 0)
                true
            }.getOrDefault(false)
        }
    }

    /**
     * Check if the Shizuku server binder is alive and running
     */
    fun isShizukuRunning(): Boolean {
        return runCatching {
            Shizuku.pingBinder()
        }.getOrDefault(false)
    }

    /**
     * Check if Shizuku permission is granted to this app
     */
    fun hasShizukuPermission(context: Context): Boolean {
        initListener(context)
        // 1. Check via official Shizuku API
        if (isShizukuRunning()) {
            val granted = runCatching {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }.getOrDefault(false)
            if (granted) {
                getPrefs(context).edit().putBoolean(PREF_SHIZUKU_GRANTED, true).apply()
                return true
            }
        }

        // 2. Check Android package manager permission API_V23
        val pmGranted = runCatching {
            context.packageManager.checkPermission("moe.shizuku.manager.permission.API_V23", context.packageName) == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)
        if (pmGranted) {
            getPrefs(context).edit().putBoolean(PREF_SHIZUKU_GRANTED, true).apply()
            return true
        }

        // 3. Fallback to persisted preference if user already authorized
        return getPrefs(context).getBoolean(PREF_SHIZUKU_GRANTED, false)
    }

    /**
     * Request Shizuku permission or launch Shizuku manager to start the service
     */
    fun requestShizukuPermissionOrLaunch(context: Context) {
        initListener(context)
        if (!isShizukuInstalled(context)) {
            val installIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/download/")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            runCatching { context.startActivity(installIntent) }
            return
        }

        if (!isShizukuRunning()) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
                ?: context.packageManager.getLaunchIntentForPackage("riki.shizuku")
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
            return
        }

        runCatching {
            Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE)
        }.onFailure {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        }
    }

    /**
     * Manually mark permission as granted
     */
    fun setPermissionGranted(context: Context, granted: Boolean) {
        getPrefs(context).edit().putBoolean(PREF_SHIZUKU_GRANTED, granted).apply()
    }

    /**
     * Get list of all installed applications (system + user)
     */
    fun getInstalledApps(context: Context): List<SystemAppInfo> {
        val pm = context.packageManager
        val installed = runCatching {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        }.getOrDefault(emptyList())

        return installed.map { info ->
            val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val label = runCatching { pm.getApplicationLabel(info).toString() }.getOrDefault(info.packageName)
            val icon = runCatching { pm.getApplicationIcon(info) }.getOrNull()
            SystemAppInfo(
                appName = label,
                packageName = info.packageName,
                isSystemApp = isSystem,
                icon = icon,
            )
        }.sortedBy { it.appName.lowercase() }
    }

    /**
     * Fetch system logcat via Shizuku or runtime process execution
     */
    suspend fun fetchSystemLogcat(
        context: Context,
        levelFilter: LogcatLevel? = null,
        userAppsOnly: Boolean = false,
        systemAppsOnly: Boolean = false,
        targetPackage: String? = null,
        searchQuery: String? = null,
        limit: Int = 1000,
    ): List<SystemLogEntry> = withContext(Dispatchers.IO) {
        val apps = getInstalledApps(context)
        val packageToAppMap = apps.associateBy { it.packageName }
        val entries = mutableListOf<SystemLogEntry>()

        runCatching {
            val cmd = arrayOf("logcat", "-d", "-v", "threadtime", "*:V")
            
            var inputStream: InputStream? = null
            if (isShizukuRunning() && hasShizukuPermission(context)) {
                runCatching {
                    val newProcMethod = Shizuku::class.java.getDeclaredMethod(
                        "newProcess",
                        Array<String>::class.java,
                        Array<String>::class.java,
                        String::class.java,
                    ).apply { isAccessible = true }
                    val proc = newProcMethod.invoke(null, cmd, null, null) as Process
                    inputStream = proc.inputStream
                }
            }

            if (inputStream == null) {
                val proc = Runtime.getRuntime().exec(cmd)
                inputStream = proc.inputStream
            }

            val reader = BufferedReader(InputStreamReader(inputStream!!))
            var line: String? = reader.readLine()

            val pattern = Regex("""^(\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+(\d+)\s+([VDIWEA])\s+([^:]+):\s*(.*)$""")

            while (line != null) {
                val match = pattern.matchEntire(line)
                if (match != null) {
                    val (time, pid, _, lvlCode, tag, msg) = match.destructured
                    val lvl = LogcatLevel.fromCode(lvlCode)
                    val cleanTag = tag.trim()
                    val cleanMsg = msg.trim()

                    val matchedApp = packageToAppMap.values.firstOrNull { app ->
                        cleanTag.contains(app.packageName, ignoreCase = true) ||
                        cleanMsg.contains(app.packageName, ignoreCase = true)
                    }

                    val pkgName = matchedApp?.packageName ?: "system"
                    val appName = matchedApp?.appName ?: if (cleanTag.isNotBlank()) cleanTag else "Android System"
                    val isSystem = matchedApp?.isSystemApp ?: true
                    val icon = matchedApp?.icon

                    val entry = SystemLogEntry(
                        appName = appName,
                        packageName = pkgName,
                        timestamp = time,
                        level = lvl,
                        pid = pid,
                        tag = cleanTag,
                        message = cleanMsg,
                        isSystemApp = isSystem,
                        appIcon = icon,
                    )

                    // Apply filters
                    val matchLevel = levelFilter == null || entry.level.priority >= levelFilter.priority
                    val matchTarget = targetPackage.isNullOrBlank() || entry.packageName.equals(targetPackage, ignoreCase = true)
                    val matchScope = when {
                        userAppsOnly -> !entry.isSystemApp
                        systemAppsOnly -> entry.isSystemApp
                        else -> true
                    }
                    val matchSearch = searchQuery.isNullOrBlank() ||
                            entry.appName.contains(searchQuery, ignoreCase = true) ||
                            entry.packageName.contains(searchQuery, ignoreCase = true) ||
                            entry.message.contains(searchQuery, ignoreCase = true) ||
                            entry.tag.contains(searchQuery, ignoreCase = true)

                    if (matchLevel && matchTarget && matchScope && matchSearch) {
                        entries.add(0, entry)
                        if (entries.size >= limit) break
                    }
                }
                line = reader.readLine()
            }
            reader.close()
        }

        entries
    }
}
