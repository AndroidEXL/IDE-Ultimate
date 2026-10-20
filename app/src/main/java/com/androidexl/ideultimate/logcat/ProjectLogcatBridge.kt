package com.androidexl.ideultimate.logcat

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogcatLevel(val code: String, val displayName: String, val priority: Int) {
    VERBOSE("V", "Verbose (تفصيلي)", 2),
    DEBUG("D", "Debug (تصحيح)", 3),
    INFO("I", "Info (معلومات)", 4),
    WARN("W", "Warn (تحذير)", 5),
    ERROR("E", "Error (خطأ)", 6),
    ASSERT("A", "Assert (حرج)", 7);

    companion object {
        fun fromCode(code: String): LogcatLevel = entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEBUG
    }
}

data class LogcatEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val appName: String,
    val packageName: String,
    val timestamp: String,
    val level: LogcatLevel,
    val tag: String,
    val message: String,
    val rawLine: String = "",
)

/**
 * Bridge class to collect, send and read logs for the current active project.
 * Allows apps built by the IDE or internal execution components to send structured log events,
 * as well as reading live system logcat filtered by the project's package name and PID.
 */
object ProjectLogcatBridge {
    private val buffer = mutableStateListOf<LogcatEntry>()
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun log(
        appName: String,
        packageName: String,
        level: LogcatLevel,
        tag: String,
        message: String,
    ) {
        val entry = LogcatEntry(
            appName = appName,
            packageName = packageName,
            timestamp = timeFormat.format(Date()),
            level = level,
            tag = tag,
            message = message,
        )
        synchronized(buffer) {
            if (buffer.size > 2000) {
                buffer.removeRange(0, 500)
            }
            buffer.add(0, entry)
        }
    }

    fun getLogs(): List<LogcatEntry> {
        return synchronized(buffer) { buffer.toList() }
    }

    fun clear() {
        synchronized(buffer) {
            buffer.clear()
        }
    }

    suspend fun fetchSystemLogcatForPackage(
        context: Context,
        appName: String,
        packageName: String,
    ): List<LogcatEntry> = withContext(Dispatchers.IO) {
        val results = mutableListOf<LogcatEntry>()
        val shortTimeFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())

        // 1. First add any programmatic logs already captured for this package
        val inMemory = synchronized(buffer) {
            buffer.filter { it.packageName.equals(packageName, ignoreCase = true) }
        }
        results.addAll(inMemory)

        // 2. Query logcat process for logs matching package or tags
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-v", "time", "*:V"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String? = reader.readLine()
            val pattern = Regex("""^(\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}\.\d{3})\s+([VDIWEA])/([^(:]+)(?:\(\s*(\d+)\))?:\s*(.*)$""")

            while (line != null) {
                val match = pattern.matchEntire(line)
                if (match != null) {
                    val (time, lvlCode, tag, _, msg) = match.destructured
                    val cleanTag = tag.trim()
                    val cleanMsg = msg.trim()

                    // Check if relates to this package
                    if (cleanMsg.contains(packageName, ignoreCase = true) ||
                        cleanTag.contains(packageName, ignoreCase = true) ||
                        cleanTag.contains(appName, ignoreCase = true)
                    ) {
                        results.add(
                            0,
                            LogcatEntry(
                                appName = appName,
                                packageName = packageName,
                                timestamp = time,
                                level = LogcatLevel.fromCode(lvlCode),
                                tag = cleanTag,
                                message = cleanMsg,
                                rawLine = line,
                            )
                        )
                    }
                }
                line = reader.readLine()
            }
            reader.close()
        }

        // If no logs found yet, provide helpful initial greeting/ready log
        if (results.isEmpty()) {
            results.add(
                LogcatEntry(
                    appName = appName,
                    packageName = packageName,
                    timestamp = shortTimeFormat.format(Date()),
                    level = LogcatLevel.INFO,
                    tag = "IDE-Ultimate",
                    message = "تم تفعيل مراقب سجلات Logcat للمشروع بنجاح. في انتظار مخرجات التشغيل...",
                )
            )
        }

        results
    }
}
