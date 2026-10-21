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

    var activeStepId: String? = null

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
                    activeStepId = stepId
                }
            }
            matcher.find() -> {
                val taskName = matcher.group(1) ?: trimmed
                val tag = matcher.group(2) ?: "EXECUTED"
                val isFailed = tag.equals("FAILED", ignoreCase = true) || trimmed.endsWith("FAILED", ignoreCase = true)
                val isSkipped = tag in listOf("UP-TO-DATE", "SKIPPED", "FROM-CACHE")
                val stepStatus = when {
                    isFailed -> BuildTaskStepStatus.FAILED
                    isSkipped -> BuildTaskStepStatus.SKIPPED
                    else -> BuildTaskStepStatus.RUNNING
                }

                val existingIdx = buildSteps.indexOfFirst { it.title == taskName }
                if (existingIdx >= 0) {
                    val item = buildSteps[existingIdx]
                    val updatedStatus = if (isFailed) {
                        BuildTaskStepStatus.FAILED
                    } else if (item.status == BuildTaskStepStatus.FAILED) {
                        BuildTaskStepStatus.FAILED
                    } else {
                        stepStatus
                    }
                    buildSteps[existingIdx] = item.copy(
                        status = updatedStatus,
                        logs = item.logs + line,
                    )
                    activeStepId = item.id
                } else {
                    val lastRunning = buildSteps.indexOfLast { it.status == BuildTaskStepStatus.RUNNING }
                    if (lastRunning >= 0 && !isFailed) {
                        val prev = buildSteps[lastRunning]
                        buildSteps[lastRunning] = prev.copy(status = BuildTaskStepStatus.SUCCESS)
                    }
                    val newId = "task_${taskName.replace(':', '_')}"
                    buildSteps.add(
                        BuildTaskStep(
                            id = newId,
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
                    activeStepId = newId
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
                activeStepId = stepId
            }
            else -> {
                val stepIdx = if (activeStepId != null) {
                    buildSteps.indexOfFirst { it.id == activeStepId }
                } else {
                    -1
                }
                val targetIdx = if (stepIdx >= 0) {
                    stepIdx
                } else {
                    buildSteps.indexOfLast { it.status == BuildTaskStepStatus.RUNNING }
                }

                if (targetIdx >= 0) {
                    val currentStep = buildSteps[targetIdx]
                    val markFailed = trimmed.contains("FAILED", ignoreCase = true) ||
                                     trimmed.contains("error:", ignoreCase = true) ||
                                     trimmed.contains("Android resource linking failed", ignoreCase = true)
                    val newStatus = if (markFailed) BuildTaskStepStatus.FAILED else currentStep.status
                    buildSteps[targetIdx] = currentStep.copy(
                        status = newStatus,
                        logs = currentStep.logs + line,
                    )
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
