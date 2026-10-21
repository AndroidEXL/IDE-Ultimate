package com.androidexl.ideultimate.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.androidexl.ideultimate.ui.screens.AndroidToolStatus
import com.androidexl.ideultimate.ui.screens.BuildTaskStep
import com.androidexl.ideultimate.ui.screens.BuildTaskStepStatus
import kotlinx.coroutines.Job
import java.util.regex.Pattern

/**
 * Singleton state manager for Android & Flutter terminal builds.
 * Preserves process, log stream, task tree, and build status across navigation
 * so that when user navigates back to other screens, the build continues running in background.
 */
object TerminalBuildManager {
    var activeProjectId by mutableStateOf<String?>(null)
    var isBuilding by mutableStateOf(false)
    var isInstallingTools by mutableStateOf(false)
    var isCheckingEnv by mutableStateOf(false)
    var isUbuntuReady by mutableStateOf(false)

    var currentTaskName by mutableStateOf("بيئة البناء غير نشطة")
    var rawLogOutput by mutableStateOf("")
    var buildSuccess by mutableStateOf<Boolean?>(null)
    var apkOutputPath by mutableStateOf<String?>(null)

    val buildSteps = mutableStateListOf<BuildTaskStep>()
    var toolStatuses by mutableStateOf<List<AndroidToolStatus>>(emptyList())

    @Volatile
    var activeJob: Job? = null

    @Volatile
    var activeProcess: Process? = null

    fun resetForProject(projectId: String) {
        if (activeProjectId != projectId || (!isBuilding && !isInstallingTools)) {
            activeProjectId = projectId
            buildSteps.clear()
            rawLogOutput = ""
            buildSuccess = null
            apkOutputPath = null
            currentTaskName = "جاري فحص بيئة البناء..."
            isCheckingEnv = true
        }
    }

    fun appendLog(line: String, isFlutter: Boolean) {
        rawLogOutput = if (rawLogOutput.isBlank()) line else "$rawLogOutput\n$line"

        val trimmed = line.trim()
        val gradleTaskPattern = Pattern.compile("^>\\s*Task\\s+([:\\w\\-\\.]+)(?:\\s+([\\w\\-]+))?")
        val matcher = gradleTaskPattern.matcher(trimmed)

        when {
            trimmed.startsWith("===>") -> {
                val stepTitle = trimmed.removePrefix("===>").trim()
                if (stepTitle.isNotBlank()) {
                    val stepId = "step_${System.currentTimeMillis()}_${buildSteps.size}"
                    val lastIdx = buildSteps.indexOfLast { it.status == BuildTaskStepStatus.RUNNING }
                    if (lastIdx >= 0) {
                        val prev = buildSteps[lastIdx]
                        buildSteps[lastIdx] = prev.copy(status = BuildTaskStepStatus.SUCCESS)
                    }
                    buildSteps.add(
                        BuildTaskStep(
                            id = stepId,
                            title = stepTitle,
                            subtitle = if (isFlutter) "عملية إعداد محرك فلاتر" else "خطوة إعداد البيئة ومسارات البناء",
                            status = BuildTaskStepStatus.RUNNING,
                            logs = listOf(line),
                        )
                    )
                }
            }
            matcher.find() -> {
                val taskName = matcher.group(1) ?: trimmed
                val tag = matcher.group(2) ?: "EXECUTED"
                val stepStatus = when (tag) {
                    "UP-TO-DATE", "SKIPPED", "FROM-CACHE" -> BuildTaskStepStatus.SKIPPED
                    "FAILED" -> BuildTaskStepStatus.FAILED
                    else -> BuildTaskStepStatus.RUNNING
                }
                val lastRunning = buildSteps.indexOfLast { it.status == BuildTaskStepStatus.RUNNING }
                if (lastRunning >= 0) {
                    val prev = buildSteps[lastRunning]
                    buildSteps[lastRunning] = prev.copy(status = BuildTaskStepStatus.SUCCESS)
                }
                val existingIdx = buildSteps.indexOfFirst { it.title == taskName }
                if (existingIdx >= 0) {
                    val item = buildSteps[existingIdx]
                    buildSteps[existingIdx] = item.copy(
                        status = stepStatus,
                        logs = item.logs + line,
                    )
                } else {
                    buildSteps.add(
                        BuildTaskStep(
                            id = "task_${taskName.replace(':', '_')}",
                            title = taskName,
                            subtitle = when {
                                taskName.contains("Kotlin", true) -> "مترجم كود Kotlin (kotlinc)"
                                taskName.contains("Java", true) -> "مترجم كود جافا (javac)"
                                taskName.contains("Dex", true) || taskName.contains("D8", true) -> "تحويل البايتكود إلى DEX (d8/r8)"
                                taskName.contains("Resource", true) || taskName.contains("Aapt", true) -> "تجميع ملفات الموارد والتصميم (AAPT2)"
                                taskName.contains("Manifest", true) -> "دمج ملفات AndroidManifest.xml"
                                taskName.contains("Package", true) -> "حزم وتوليد ملف APK النهائي"
                                taskName.contains("Sign", true) -> "توقيع الحزمة الرقمي (v1+v2 signing)"
                                else -> "مهمة تنفيذ في دورة حياة Gradle"
                            },
                            status = stepStatus,
                            logs = listOf(line),
                        )
                    )
                }
            }
            trimmed.startsWith("Running Gradle task", ignoreCase = true) ||
            trimmed.startsWith("Resolving dependencies", ignoreCase = true) ||
            trimmed.startsWith("Building APK", ignoreCase = true) ||
            trimmed.startsWith("Running 'gradlew", ignoreCase = true) -> {
                val stepId = "fl_${System.currentTimeMillis()}_${buildSteps.size}"
                val lastIdx = buildSteps.indexOfLast { it.status == BuildTaskStepStatus.RUNNING }
                if (lastIdx >= 0) {
                    val prev = buildSteps[lastIdx]
                    buildSteps[lastIdx] = prev.copy(status = BuildTaskStepStatus.SUCCESS)
                }
                buildSteps.add(
                    BuildTaskStep(
                        id = stepId,
                        title = trimmed,
                        subtitle = "مهمة تشغيل وتجميع في فلاتر",
                        status = BuildTaskStepStatus.RUNNING,
                        logs = listOf(line),
                    )
                )
            }
            else -> {
                val lastIdx = buildSteps.indexOfLast { it.status == BuildTaskStepStatus.RUNNING }
                if (lastIdx >= 0) {
                    val currentStep = buildSteps[lastIdx]
                    buildSteps[lastIdx] = currentStep.copy(logs = currentStep.logs + line)
                }
            }
        }
    }

    fun abortBuild() {
        runCatching {
            activeProcess?.destroyForcibly()
        }
        activeProcess = null
        activeJob?.cancel()
        activeJob = null
        isBuilding = false
        isInstallingTools = false
    }
}
