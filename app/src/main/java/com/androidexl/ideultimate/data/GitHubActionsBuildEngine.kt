package com.androidexl.ideultimate.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

data class WorkflowStepItem(
    val name: String,
    val status: String,
    val conclusion: String?,
    val number: Int,
)

data class WorkflowJobItem(
    val id: Long,
    val name: String,
    val status: String,
    val conclusion: String?,
    val steps: List<WorkflowStepItem>,
    val htmlUrl: String,
)

data class WorkflowRunDetail(
    val id: Long,
    val runNumber: Int,
    val status: String,
    val conclusion: String?,
    val htmlUrl: String,
    val jobs: List<WorkflowJobItem>,
    val fullLogs: String,
    val errorMessage: String?,
    val errorTaskName: String?,
)

class GitHubActionsBuildEngine(
    private val token: String,
    private val owner: String,
    private val repo: String,
) {
    fun fetchLatestWorkflowRun(workflowFileName: String = "build-apk.yml"): Result<WorkflowRunDetail> = runCatching {
        val (code, body) = executeGet("https://api.github.com/repos/$owner/$repo/actions/workflows/$workflowFileName/runs?per_page=1")
        if (code !in 200..299) {
            // Fallback to all runs
            val (allCode, allBody) = executeGet("https://api.github.com/repos/$owner/$repo/actions/runs?per_page=1")
            if (allCode !in 200..299) {
                throw IllegalStateException("تعذر جلب سجلات البناء من GitHub Actions ($code)")
            }
            return@runCatching parseRunDetailFromBody(allBody)
        }
        parseRunDetailFromBody(body)
    }

    fun fetchWorkflowRunById(runId: Long): Result<WorkflowRunDetail> = runCatching {
        val (code, body) = executeGet("https://api.github.com/repos/$owner/$repo/actions/runs/$runId")
        if (code !in 200..299) {
            throw IllegalStateException("تعذر جلب تفاصيل التشغيل $runId ($code)")
        }
        parseRunDetailFromRunJson(JSONObject(body))
    }

    private fun parseRunDetailFromBody(runsBody: String): WorkflowRunDetail {
        val root = JSONObject(runsBody)
        val runs = root.optJSONArray("workflow_runs") ?: JSONArray()
        if (runs.length() == 0) {
            throw IllegalStateException("لا يوجد أي تشغيل سابق لـ GitHub Actions في هذا المستودع")
        }
        val latestRun = runs.getJSONObject(0)
        return parseRunDetailFromRunJson(latestRun)
    }

    private fun parseRunDetailFromRunJson(runJson: JSONObject): WorkflowRunDetail {
        val runId = runJson.getLong("id")
        val runNumber = runJson.optInt("run_number", 1)
        val status = runJson.optString("status", "completed")
        val conclusion = runJson.optString("conclusion").takeIf { it != "null" && it.isNotBlank() }
        val htmlUrl = runJson.optString("html_url", "")

        // Fetch jobs for this run
        val jobs = mutableListOf<WorkflowJobItem>()
        runCatching {
            val (jCode, jBody) = executeGet("https://api.github.com/repos/$owner/$repo/actions/runs/$runId/jobs")
            if (jCode in 200..299) {
                val jRoot = JSONObject(jBody)
                val jArr = jRoot.optJSONArray("jobs") ?: JSONArray()
                for (i in 0 until jArr.length()) {
                    val jObj = jArr.getJSONObject(i)
                    val sArr = jObj.optJSONArray("steps") ?: JSONArray()
                    val steps = mutableListOf<WorkflowStepItem>()
                    for (k in 0 until sArr.length()) {
                        val sObj = sArr.getJSONObject(k)
                        steps.add(
                            WorkflowStepItem(
                                name = sObj.optString("name"),
                                status = sObj.optString("status"),
                                conclusion = sObj.optString("conclusion").takeIf { it != "null" && it.isNotBlank() },
                                number = sObj.optInt("number", k + 1),
                            ),
                        )
                    }
                    jobs.add(
                        WorkflowJobItem(
                            id = jObj.optLong("id"),
                            name = jObj.optString("name"),
                            status = jObj.optString("status"),
                            conclusion = jObj.optString("conclusion").takeIf { it != "null" && it.isNotBlank() },
                            steps = steps,
                            htmlUrl = jObj.optString("html_url", ""),
                        ),
                    )
                }
            }
        }

        // Fetch annotations / errors if any job failed
        var extractedErrorMsg: String? = null
        var extractedErrorTask: String? = null

        val failedStep = jobs.flatMap { it.steps }.firstOrNull { it.conclusion == "failure" }
        if (failedStep != null) {
            extractedErrorTask = failedStep.name
        }

        // Check check-run annotations for compile errors
        for (job in jobs) {
            runCatching {
                val (aCode, aBody) = executeGet("https://api.github.com/repos/$owner/$repo/check-runs/${job.id}/annotations")
                if (aCode in 200..299) {
                    val aArr = JSONArray(aBody)
                    val msgs = mutableListOf<String>()
                    for (m in 0 until aArr.length()) {
                        val an = aArr.getJSONObject(m)
                        val msg = an.optString("message").trim()
                        val title = an.optString("title").trim()
                        val path = an.optString("path").trim()
                        val line = an.optInt("start_line", 0)
                        if (msg.isNotBlank()) {
                            val loc = if (path.isNotBlank()) "$path:$line -> " else ""
                            msgs.add("$loc${title.ifEmpty { msg }}")
                        }
                    }
                    if (msgs.isNotEmpty()) {
                        extractedErrorMsg = msgs.joinToString("\n\n")
                    }
                }
            }
            if (extractedErrorMsg != null) break
        }

        // Download run logs zip if possible
        val logsText = downloadRunLogsText(runId).ifBlank {
            buildFallbackLogs(jobs, conclusion, extractedErrorMsg)
        }

        return WorkflowRunDetail(
            id = runId,
            runNumber = runNumber,
            status = status,
            conclusion = conclusion,
            htmlUrl = htmlUrl,
            jobs = jobs,
            fullLogs = logsText,
            errorMessage = extractedErrorMsg ?: if (conclusion == "failure") "فشل البناء في مهمة: ${extractedErrorTask ?: "Gradle Build"}" else null,
            errorTaskName = extractedErrorTask,
        )
    }

    private fun buildFallbackLogs(
        jobs: List<WorkflowJobItem>,
        conclusion: String?,
        errorMsg: String?,
    ): String = buildString {
        appendLine("=== GitHub Actions Build Log ===")
        jobs.forEach { job ->
            appendLine("Job: ${job.name} [Status: ${job.status}, Conclusion: ${job.conclusion ?: "running"}]")
            job.steps.forEach { step ->
                val icon = when (step.conclusion) {
                    "success" -> "✓"
                    "failure" -> "✗"
                    else -> "•"
                }
                appendLine("  $icon ${step.number}. ${step.name} (${step.status} / ${step.conclusion ?: "in_progress"})")
            }
        }
        if (!errorMsg.isNullOrBlank()) {
            appendLine()
            appendLine("=== BUILD FAILURE DETAILS ===")
            appendLine(errorMsg)
        }
    }

    private fun downloadRunLogsText(runId: Long): String = runCatching {
        val url = "https://api.github.com/repos/$owner/$repo/actions/runs/$runId/logs"
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 20_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/vnd.github+json")
            if (token.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
            }
            setRequestProperty("User-Agent", "IDE-Ultimate-Android")
        }
        var stream: InputStream? = null
        if (conn.responseCode in 300..399) {
            val redirect = conn.getHeaderField("Location")
            if (!redirect.isNullOrBlank()) {
                val redirConn = (URL(redirect).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 20_000
                }
                stream = redirConn.inputStream
            }
        } else if (conn.responseCode in 200..299) {
            stream = conn.inputStream
        }
        val bytes = stream?.use { it.readBytes() } ?: return ""
        val sb = StringBuilder()
        ZipInputStream(bytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".txt")) {
                    val out = ByteArrayOutputStream()
                    val buf = ByteArray(2048)
                    var len: Int
                    while (zis.read(buf).also { len = it } > 0) {
                        out.write(buf, 0, len)
                    }
                    val txt = out.toString("UTF-8")
                    sb.appendLine("----- Log: ${entry.name} -----")
                    sb.appendLine(txt)
                    sb.appendLine()
                }
                entry = zis.nextEntry
            }
        }
        sb.toString()
    }.getOrDefault("")

    private fun executeGet(url: String): Pair<Int, String> {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/vnd.github+json")
            if (token.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
            }
            setRequestProperty("User-Agent", "IDE-Ultimate-Android")
        }
        return try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            code to body
        } finally {
            conn.disconnect()
        }
    }
}
