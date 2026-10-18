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

data class WorkflowArtifactItem(
    val id: Long,
    val name: String,
    val sizeInBytes: Long,
    val archiveDownloadUrl: String,
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
    val artifacts: List<WorkflowArtifactItem> = emptyList(),
)

class GitHubActionsBuildEngine(
    private val token: String,
    private val owner: String,
    private val repo: String,
) {
    fun fetchLatestWorkflowRun(workflowFileName: String = "build-apk.yml"): Result<WorkflowRunDetail> = runCatching {
        val (code, body) = executeGet("https://api.github.com/repos/$owner/$repo/actions/workflows/$workflowFileName/runs?per_page=1")
        if (code in 200..299) {
            val root = JSONObject(body)
            val runs = root.optJSONArray("workflow_runs") ?: JSONArray()
            if (runs.length() > 0) {
                return@runCatching parseRunDetailFromRunJson(runs.getJSONObject(0))
            }
        }
        // Fallback to all runs in the repo
        val (allCode, allBody) = executeGet("https://api.github.com/repos/$owner/$repo/actions/runs?per_page=1")
        if (allCode == 404) {
            throw IllegalStateException("المستودع غير موجود أو لا يمكن الوصول إليه (404 Not Found). يرجى التأكد من ربط مستودع GitHub الخاص بك أو إنشائه ورفع سير العمل.")
        }
        if (allCode !in 200..299) {
            val msg = runCatching { JSONObject(allBody).optString("message") }.getOrNull().orEmpty()
            throw IllegalStateException("تعذر جلب تفاصيل البناء من GitHub ($allCode): ${msg.ifBlank { "تأكد من صلاحيات الـ Token" }}")
        }
        val rootAll = JSONObject(allBody)
        val allRuns = rootAll.optJSONArray("workflow_runs") ?: JSONArray()
        if (allRuns.length() == 0) {
            throw IllegalStateException("لا يوجد تشغيل سابق للكود. يمكنك بدء بناء جديد الآن عبر تشغيل Workflow Dispatch.")
        }
        parseRunDetailFromRunJson(allRuns.getJSONObject(0))
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

        // Download run logs zip or live job logs
        var logsText = downloadRunLogsText(runId)
        if (logsText.isBlank()) {
            // Fetch live job logs while in-progress
            for (job in jobs) {
                val liveJobLog = downloadJobLogText(job.id)
                if (liveJobLog.isNotBlank()) {
                    logsText = (if (logsText.isBlank()) "" else logsText + "\n\n") +
                        "=== Job: ${job.name} (Live Log) ===\n" + liveJobLog
                }
            }
        }
        if (logsText.isBlank()) {
            logsText = buildFallbackLogs(jobs, conclusion, extractedErrorMsg)
        }

        // Fetch artifacts for this run (e.g. built APKs)
        val artifactsList = mutableListOf<WorkflowArtifactItem>()
        runCatching {
            val (aCode, aBody) = executeGet("https://api.github.com/repos/$owner/$repo/actions/runs/$runId/artifacts")
            if (aCode in 200..299) {
                val aRoot = JSONObject(aBody)
                val aArr = aRoot.optJSONArray("artifacts") ?: JSONArray()
                for (i in 0 until aArr.length()) {
                    val aObj = aArr.getJSONObject(i)
                    artifactsList.add(
                        WorkflowArtifactItem(
                            id = aObj.optLong("id"),
                            name = aObj.optString("name"),
                            sizeInBytes = aObj.optLong("size_in_bytes", 0L),
                            archiveDownloadUrl = aObj.optString("archive_download_url", ""),
                        )
                    )
                }
            }
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
            artifacts = artifactsList,
        )
    }

    private fun buildFallbackLogs(
        jobs: List<WorkflowJobItem>,
        conclusion: String?,
        errorMsg: String?,
    ): String = buildString {
        appendLine("=== GitHub Actions Live Console & Build Stream ===")
        appendLine("Time: " + java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date()))
        appendLine("Repository: $owner/$repo")
        appendLine()
        jobs.forEach { job ->
            appendLine("--------------------------------------------------------------------------------")
            appendLine("Job: ${job.name} [Status: ${job.status.uppercase()}, Conclusion: ${job.conclusion?.uppercase() ?: "RUNNING"}]")
            appendLine("--------------------------------------------------------------------------------")
            job.steps.forEach { step ->
                val icon = when (step.conclusion) {
                    "success" -> "✓"
                    "failure" -> "✕"
                    else -> if (step.status == "in_progress") "►" else "○"
                }
                val stepStatus = when {
                    step.conclusion == "success" -> "SUCCESS"
                    step.conclusion == "failure" -> "FAILED"
                    step.status == "in_progress" -> "IN_PROGRESS (جاري التنفيذ الآن)"
                    else -> "QUEUED (في الانتظار)"
                }
                appendLine("  $icon Step ${step.number}: ${step.name}")
                appendLine("     -> State: $stepStatus")
            }
            appendLine()
        }
        if (!errorMsg.isNullOrBlank()) {
            appendLine("================================================================================")
            appendLine("❌ BUILD FAILURE / COMPILATION DETAILS:")
            appendLine("================================================================================")
            appendLine(errorMsg)
        } else if (conclusion == null || conclusion == "in_progress") {
            appendLine("⏳ عملية البناء قيد التنفيذ السحابي الآن على خوادم GitHub Actions...")
            appendLine("يتم تحديث الخطوات والسجلات تلقائياً كل بضع ثوانٍ مباشرة على الشاشة.")
        }
    }

        fun downloadJobLogText(jobId: Long): String = runCatching {
        val url = "https://api.github.com/repos/$owner/$repo/actions/jobs/$jobId/logs"
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
        stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
    }.getOrDefault("")

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

    fun downloadArtifactZip(archiveUrl: String, targetFile: java.io.File, onProgress: (Float) -> Unit = {}): Result<Unit> = runCatching {
        val conn = (URL(archiveUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 20_000
            readTimeout = 60_000
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
                    connectTimeout = 20_000
                    readTimeout = 60_000
                }
                stream = redirConn.inputStream
            }
        } else if (conn.responseCode in 200..299) {
            stream = conn.inputStream
        } else {
            throw IllegalStateException("فشل الاتصال لتحميل الملف (${conn.responseCode})")
        }

        val totalLen = conn.contentLength.takeIf { it > 0 } ?: 15_000_000
        var downloaded = 0L
        targetFile.parentFile?.mkdirs()
        stream?.use { input ->
            java.io.FileOutputStream(targetFile).use { output ->
                val buf = ByteArray(16384)
                var read: Int
                while (input.read(buf).also { read = it } != -1) {
                    output.write(buf, 0, read)
                    downloaded += read
                    onProgress((downloaded.toFloat() / totalLen.toFloat()).coerceIn(0.01f, 0.99f))
                }
            }
        } ?: throw IllegalStateException("لم يتم استلام أي بيانات")
        onProgress(1.0f)
    }

    fun downloadLatestReleaseApk(targetFile: java.io.File, onProgress: (Float) -> Unit = {}): Result<java.io.File> = runCatching {
        val (code, body) = executeGet("https://api.github.com/repos/$owner/$repo/releases/latest")
        if (code !in 200..299) {
            throw IllegalStateException("لم يتم العثور على Release متاح ($code)")
        }
        val root = JSONObject(body)
        val assets = root.optJSONArray("assets") ?: JSONArray()
        var apkUrl = ""
        for (i in 0 until assets.length()) {
            val ast = assets.getJSONObject(i)
            val name = ast.optString("name", "")
            if (name.endsWith(".apk", ignoreCase = true)) {
                apkUrl = ast.optString("browser_download_url", "")
                if (name.contains("release", ignoreCase = true) || name.contains("debug", ignoreCase = true)) {
                    break
                }
            }
        }
        if (apkUrl.isBlank()) {
            throw IllegalStateException("لا يوجد ملف APK مرفوع في الـ Release الأخير")
        }

        val conn = (URL(apkUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 60_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "IDE-Ultimate-Android")
        }
        val totalLen = conn.contentLength.takeIf { it > 0 } ?: 20_000_000
        var downloaded = 0L
        targetFile.parentFile?.mkdirs()
        val inStream = if (conn.responseCode in 300..399) {
            val redir = conn.getHeaderField("Location")
            (URL(redir).openConnection() as HttpURLConnection).inputStream
        } else {
            conn.inputStream
        }
        inStream.use { input ->
            java.io.FileOutputStream(targetFile).use { output ->
                val buf = ByteArray(16384)
                var read: Int
                while (input.read(buf).also { read = it } != -1) {
                    output.write(buf, 0, read)
                    downloaded += read
                    onProgress((downloaded.toFloat() / totalLen.toFloat()).coerceIn(0.01f, 0.99f))
                }
            }
        }
        onProgress(1.0f)
        targetFile
    }

    fun triggerWorkflowDispatch(
        workflowFileName: String = "build-apk.yml",
        ref: String = "main",
        inputs: Map<String, String> = emptyMap(),
    ): Result<Unit> = runCatching {
        val url = "https://api.github.com/repos/$owner/$repo/actions/workflows/$workflowFileName/dispatches"
        val payload = JSONObject().apply {
            put("ref", ref)
            if (inputs.isNotEmpty()) {
                val inpObj = JSONObject()
                inputs.forEach { (k, v) -> inpObj.put(k, v) }
                put("inputs", inpObj)
            }
        }
        val (code, body) = executePost(url, payload.toString())
        if (code !in 200..299 && code != 204) {
            val msg = runCatching { JSONObject(body).optString("message") }.getOrNull().orEmpty()
            throw IllegalStateException("تعذر بدء تشغيل GitHub Actions ($code): ${msg.ifBlank { "تأكد من وجود ملف $workflowFileName وصلاحيات الـ Token" }}")
        }
    }

    fun ensureWorkflowFileExists(
        workflowFileName: String = "build-apk.yml",
        content: String,
        branch: String = "main",
    ): Result<Unit> = runCatching {
        val path = ".github/workflows/$workflowFileName"
        val apiUrl = "https://api.github.com/repos/$owner/$repo/contents/$path?ref=$branch"
        val (getCode, getBody) = executeGet(apiUrl)
        val existingSha = if (getCode in 200..299) {
            JSONObject(getBody).optString("sha").takeIf { it.isNotBlank() }
        } else {
            null
        }

        val putUrl = "https://api.github.com/repos/$owner/$repo/contents/$path"
        val payload = JSONObject().apply {
            put("message", "Add GitHub Actions workflow $workflowFileName from IDE Ultimate")
            put("content", android.util.Base64.encodeToString(content.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP))
            put("branch", branch)
            if (!existingSha.isNullOrBlank()) {
                put("sha", existingSha)
            }
        }
        val (putCode, putBody) = executePut(putUrl, payload.toString())
        if (putCode !in 200..299 && putCode != 201) {
            val msg = runCatching { JSONObject(putBody).optString("message") }.getOrNull().orEmpty()
            throw IllegalStateException("تعذر رفع ملف سير العمل $workflowFileName ($putCode): $msg")
        }
    }

    private fun executePost(url: String, jsonBody: String): Pair<Int, String> =
        executeWrite("POST", url, jsonBody)

    private fun executePut(url: String, jsonBody: String): Pair<Int, String> =
        executeWrite("PUT", url, jsonBody)

    private fun executeWrite(method: String, url: String, jsonBody: String): Pair<Int, String> {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 25_000
            doOutput = true
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            if (token.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
            }
            setRequestProperty("User-Agent", "IDE-Ultimate-Android")
        }
        return try {
            conn.outputStream.use { os ->
                os.write(jsonBody.toByteArray(Charsets.UTF_8))
                os.flush()
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            code to body
        } finally {
            conn.disconnect()
        }
    }
}
