package com.androidexl.ideultimate.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class GitHubAccount(
    val login: String,
    val name: String,
    val avatarUrl: String,
    val htmlUrl: String,
    val publicRepos: Int,
    val token: String,
)

data class GitHubRepoItem(
    val id: Long,
    val name: String,
    val fullName: String,
    val ownerLogin: String,
    val description: String,
    val isPrivate: Boolean,
    val defaultBranch: String,
    val htmlUrl: String,
    val updatedAt: String,
)

data class GitHubContentEntry(
    val name: String,
    val path: String,
    val sha: String,
    val size: Long,
    val type: String, // "dir" or "file"
    val downloadUrl: String,
) {
    val isDirectory: Boolean
        get() = type.equals("dir", ignoreCase = true)
}

data class GitHubFileDetail(
    val name: String,
    val path: String,
    val sha: String,
    val textContent: String,
    val rawBytes: ByteArray,
)

class GitHubToolsRepository(
    private val context: Context,
) {
    private val prefs = context.applicationContext.getSharedPreferences("ide_github_tools_prefs", Context.MODE_PRIVATE)

    fun getSavedAccount(): GitHubAccount? {
        val token = prefs.getString("gh_token", null)?.trim().orEmpty()
        val login = prefs.getString("gh_login", null)?.trim().orEmpty()
        if (token.isEmpty() || login.isEmpty()) return null
        return GitHubAccount(
            login = login,
            name = prefs.getString("gh_name", login).orEmpty().ifBlank { login },
            avatarUrl = prefs.getString("gh_avatar", "").orEmpty(),
            htmlUrl = prefs.getString("gh_url", "https://github.com/$login").orEmpty(),
            publicRepos = prefs.getInt("gh_repos", 0),
            token = token,
        )
    }

    fun logout() {
        prefs.edit()
            .remove("gh_token")
            .remove("gh_login")
            .remove("gh_name")
            .remove("gh_avatar")
            .remove("gh_url")
            .remove("gh_repos")
            .apply()
    }

    fun verifyAndSaveToken(rawToken: String): Result<GitHubAccount> = runCatching {
        val cleanToken = rawToken.trim()
        require(cleanToken.isNotEmpty()) { "يرجى إدخال رمز الدخول (GitHub Personal Access Token)" }

        val (code, body) = executeGitHubRequest(
            method = "GET",
            url = "https://api.github.com/user",
            token = cleanToken,
        )
        if (code !in 200..299) {
            val msg = runCatching { JSONObject(body).optString("message") }.getOrNull().orEmpty()
            throw IllegalStateException("فشل تسجيل الدخول في GitHub ($code): ${msg.ifBlank { "تأكد من صحة الـ Token وصلاحية repo" }}")
        }

        val obj = JSONObject(body)
        val login = obj.optString("login").trim()
        val name = obj.optString("name", login).takeIf { it != "null" && it.isNotBlank() } ?: login
        val avatar = obj.optString("avatar_url", "")
        val htmlUrl = obj.optString("html_url", "https://github.com/$login")
        val pubRepos = obj.optInt("public_repos", 0)

        val account = GitHubAccount(
            login = login,
            name = name,
            avatarUrl = avatar,
            htmlUrl = htmlUrl,
            publicRepos = pubRepos,
            token = cleanToken,
        )

        prefs.edit()
            .putString("gh_token", cleanToken)
            .putString("gh_login", login)
            .putString("gh_name", name)
            .putString("gh_avatar", avatar)
            .putString("gh_url", htmlUrl)
            .putInt("gh_repos", pubRepos)
            .apply()

        account
    }

    fun getLinkedRepoForProject(projectId: String): Pair<String, String>? {
        val owner = prefs.getString("gh_linked_owner_$projectId", null)?.trim().orEmpty()
        val repo = prefs.getString("gh_linked_repo_$projectId", null)?.trim().orEmpty()
        if (owner.isNotEmpty() && repo.isNotEmpty()) {
            return owner to repo
        }
        return null
    }

    fun setLinkedRepoForProject(projectId: String, owner: String, repo: String) {
        prefs.edit()
            .putString("gh_linked_owner_$projectId", owner.trim())
            .putString("gh_linked_repo_$projectId", repo.trim())
            .apply()
    }

    fun listUserRepositories(): Result<List<GitHubRepoItem>> = runCatching {
        val account = getSavedAccount() ?: throw IllegalStateException("غير مسجل الدخول")
        val (code, body) = executeGitHubRequest(
            method = "GET",
            url = "https://api.github.com/user/repos?per_page=100&sort=updated&affiliation=owner,collaborator",
            token = account.token,
        )
        if (code !in 200..299) {
            throw IllegalStateException("تعذر جلب المستودعات ($code)")
        }
        val arr = JSONArray(body)
        val list = ArrayList<GitHubRepoItem>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(parseRepoJson(obj, account.login))
        }
        saveCachedRepos(list)
        list
    }.recoverCatching { err ->
        val cached = getCachedRepos()
        if (cached.isNotEmpty()) cached else throw err
    }

    fun createRepository(
        rawName: String,
        description: String,
        isPrivate: Boolean,
        autoInit: Boolean = true,
    ): Result<GitHubRepoItem> = runCatching {
        val account = getSavedAccount() ?: throw IllegalStateException("غير مسجل الدخول")
        val cleanName = sanitizeRepoName(rawName)
        require(cleanName.isNotEmpty()) { "يرجى إدخال اسم صالح للمستودع" }

        val payload = JSONObject().apply {
            put("name", cleanName)
            put("description", description.trim())
            put("private", isPrivate)
            put("auto_init", autoInit)
        }
        val (code, body) = executeGitHubRequest(
            method = "POST",
            url = "https://api.github.com/user/repos",
            token = account.token,
            jsonBody = payload.toString(),
        )
        if (code !in 200..299) {
            // If repo already exists, fetch it directly
            val (getCode, getBody) = executeGitHubRequest(
                method = "GET",
                url = "https://api.github.com/repos/${account.login}/$cleanName",
                token = account.token,
            )
            if (getCode in 200..299) {
                return@runCatching parseRepoJson(JSONObject(getBody), account.login)
            }
            val msg = runCatching { JSONObject(body).optString("message") }.getOrNull().orEmpty()
            throw IllegalStateException("تعذر إنشاء المستودع ($code): $msg")
        }
        parseRepoJson(JSONObject(body), account.login)
    }

    fun renameRepository(
        owner: String,
        oldName: String,
        rawNewName: String,
    ): Result<GitHubRepoItem> = runCatching {
        val account = getSavedAccount() ?: throw IllegalStateException("غير مسجل الدخول")
        val cleanNew = sanitizeRepoName(rawNewName)
        require(cleanNew.isNotEmpty()) { "الاسم الجديد غير صالح" }

        val payload = JSONObject().apply {
            put("name", cleanNew)
        }
        val (code, body) = executeGitHubRequest(
            method = "PATCH",
            url = "https://api.github.com/repos/$owner/$oldName",
            token = account.token,
            jsonBody = payload.toString(),
        )
        if (code !in 200..299) {
            val msg = runCatching { JSONObject(body).optString("message") }.getOrNull().orEmpty()
            throw IllegalStateException("تعذر تعديل اسم المستودع ($code): $msg")
        }
        parseRepoJson(JSONObject(body), owner)
    }

    fun deleteRepository(
        owner: String,
        repoName: String,
    ): Result<Unit> = runCatching {
        val account = getSavedAccount() ?: throw IllegalStateException("غير مسجل الدخول")
        val (code, body) = executeGitHubRequest(
            method = "DELETE",
            url = "https://api.github.com/repos/$owner/$repoName",
            token = account.token,
        )
        if (code !in 200..299 && code != 404) {
            val msg = runCatching { JSONObject(body).optString("message") }.getOrNull().orEmpty()
            throw IllegalStateException("تعذر حذف المستودع ($code — تأكد من تفعيل صلاحية delete_repo في الـ Token): $msg")
        }
    }

    fun uploadCurrentProjectToGitHub(
        rawRepoName: String,
        description: String,
        isPrivate: Boolean,
        projectFiles: List<Pair<String, ByteArray>>,
        onProgress: (Int, Int, String) -> Unit = { _, _, _ -> },
    ): Result<GitHubRepoItem> = runCatching {
        val repo = createRepository(
            rawName = rawRepoName,
            description = description,
            isPrivate = isPrivate,
            autoInit = true,
        ).getOrThrow()

        val total = projectFiles.size.coerceAtLeast(1)
        projectFiles.forEachIndexed { idx, (relPath, bytes) ->
            onProgress(idx + 1, total, relPath)
            uploadOrUpdateFile(
                owner = repo.ownerLogin,
                repo = repo.name,
                path = relPath,
                bytes = bytes,
                commitMessage = "Upload $relPath from IDE Ultimate",
            ).getOrThrow()
        }
        repo
    }

    fun listRepoContents(
        owner: String,
        repo: String,
        path: String,
    ): Result<List<GitHubContentEntry>> = runCatching {
        val account = getSavedAccount() ?: throw IllegalStateException("غير مسجل الدخول")
        val cleanPath = path.trim('/')
        val encodedPath = encodeGitHubPath(cleanPath)
        val url = if (encodedPath.isEmpty()) {
            "https://api.github.com/repos/$owner/$repo/contents"
        } else {
            "https://api.github.com/repos/$owner/$repo/contents/$encodedPath"
        }
        val (code, body) = executeGitHubRequest(
            method = "GET",
            url = url,
            token = account.token,
        )
        if (code == 404) return@runCatching emptyList()
        if (code !in 200..299) {
            val msg = runCatching { JSONObject(body).optString("message") }.getOrNull().orEmpty()
            throw IllegalStateException("تعذر قراءة محتويات المجلد ($code): $msg")
        }
        val trimmed = body.trim()
        if (!trimmed.startsWith("[")) return@runCatching emptyList()
        val arr = JSONArray(trimmed)
        val entries = ArrayList<GitHubContentEntry>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            entries.add(
                GitHubContentEntry(
                    name = obj.optString("name"),
                    path = obj.optString("path"),
                    sha = obj.optString("sha"),
                    size = obj.optLong("size", 0L),
                    type = obj.optString("type", "file"),
                    downloadUrl = obj.optString("download_url", ""),
                ),
            )
        }
        entries.sortedWith(compareBy<GitHubContentEntry>({ !it.isDirectory }, { it.name.lowercase() }))
    }

    fun readFileContent(
        owner: String,
        repo: String,
        path: String,
    ): Result<GitHubFileDetail> = runCatching {
        val account = getSavedAccount() ?: throw IllegalStateException("غير مسجل الدخول")
        val cleanPath = path.trim('/')
        val encodedPath = encodeGitHubPath(cleanPath)
        val (code, body) = executeGitHubRequest(
            method = "GET",
            url = "https://api.github.com/repos/$owner/$repo/contents/$encodedPath",
            token = account.token,
        )
        if (code !in 200..299) {
            throw IllegalStateException("تعذر فتح الملف ($code)")
        }
        val obj = JSONObject(body)
        val sha = obj.optString("sha")
        val name = obj.optString("name", cleanPath.substringAfterLast('/'))
        val base64Content = obj.optString("content", "").replace("\n", "").replace("\r", "")
        val rawBytes = if (base64Content.isNotEmpty()) {
            Base64.decode(base64Content, Base64.DEFAULT)
        } else {
            ByteArray(0)
        }
        val text = runCatching { String(rawBytes, Charsets.UTF_8) }.getOrDefault("")
        GitHubFileDetail(
            name = name,
            path = cleanPath,
            sha = sha,
            textContent = text,
            rawBytes = rawBytes,
        )
    }

    fun uploadOrUpdateFile(
        owner: String,
        repo: String,
        path: String,
        bytes: ByteArray,
        commitMessage: String,
        existingSha: String? = null,
    ): Result<Unit> = runCatching {
        val account = getSavedAccount() ?: throw IllegalStateException("غير مسجل الدخول")
        val cleanPath = path.trim('/').replace('\\', '/')
        val encodedPath = encodeGitHubPath(cleanPath)
        val apiUrl = "https://api.github.com/repos/$owner/$repo/contents/$encodedPath"

        // Check existing SHA if not provided
        val resolvedSha = existingSha ?: runCatching {
            val (getCode, getBody) = executeGitHubRequest(
                method = "GET",
                url = apiUrl,
                token = account.token,
            )
            if (getCode in 200..299) {
                JSONObject(getBody).optString("sha").takeIf { it.isNotBlank() }
            } else {
                null
            }
        }.getOrNull()

        val payload = JSONObject().apply {
            put("message", commitMessage)
            put("content", Base64.encodeToString(bytes, Base64.NO_WRAP))
            if (!resolvedSha.isNullOrBlank()) {
                put("sha", resolvedSha)
            }
        }
        val (code, body) = executeGitHubRequest(
            method = "PUT",
            url = apiUrl,
            token = account.token,
            jsonBody = payload.toString(),
        )
        if (code !in 200..299) {
            val msg = runCatching { JSONObject(body).optString("message") }.getOrNull().orEmpty()
            throw IllegalStateException("تعذر رفع الملف $cleanPath ($code): $msg")
        }
    }

    fun createFolderInRepo(
        owner: String,
        repo: String,
        parentPath: String,
        rawFolderName: String,
    ): Result<Unit> = runCatching {
        val cleanFolder = rawFolderName.trim().trim('/').replace(Regex("[\\\\:*?\"<>|]"), "_")
        require(cleanFolder.isNotEmpty()) { "يرجى إدخال اسم الفولدر" }
        val base = parentPath.trim('/')
        val gitkeepPath = if (base.isEmpty()) {
            "$cleanFolder/.gitkeep"
        } else {
            "$base/$cleanFolder/.gitkeep"
        }
        uploadOrUpdateFile(
            owner = owner,
            repo = repo,
            path = gitkeepPath,
            bytes = "".toByteArray(Charsets.UTF_8),
            commitMessage = "Create folder $cleanFolder",
        ).getOrThrow()
    }

    fun deleteFileOrFolderRecursive(
        owner: String,
        repo: String,
        entry: GitHubContentEntry,
    ): Result<Int> = runCatching {
        val account = getSavedAccount() ?: throw IllegalStateException("غير مسجل الدخول")
        if (!entry.isDirectory) {
            deleteSingleFile(account.token, owner, repo, entry.path, entry.sha)
            1
        } else {
            var deletedCount = 0
            val children = listRepoContents(owner, repo, entry.path).getOrDefault(emptyList())
            children.forEach { child ->
                deletedCount += deleteFileOrFolderRecursive(owner, repo, child).getOrDefault(0)
            }
            deletedCount
        }
    }

    private fun deleteSingleFile(
        token: String,
        owner: String,
        repo: String,
        path: String,
        sha: String,
    ) {
        val cleanPath = path.trim('/')
        val encodedPath = encodeGitHubPath(cleanPath)
        val payload = JSONObject().apply {
            put("message", "Delete $cleanPath from IDE Ultimate")
            put("sha", sha)
        }
        val (code, body) = executeGitHubRequest(
            method = "DELETE",
            url = "https://api.github.com/repos/$owner/$repo/contents/$encodedPath",
            token = token,
            jsonBody = payload.toString(),
        )
        if (code !in 200..299 && code != 404) {
            val msg = runCatching { JSONObject(body).optString("message") }.getOrNull().orEmpty()
            throw IllegalStateException("تعذر حذف $cleanPath ($code): $msg")
        }
    }

    fun downloadFileOrFolderRecursive(
        owner: String,
        repo: String,
        entry: GitHubContentEntry,
        targetBaseDir: File? = null,
    ): Result<File> = runCatching {
        val downloadsRoot = targetBaseDir ?: File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "IDE-Ultimate-GitHub/$repo",
        ).apply { mkdirs() }

        downloadEntryIntoDir(owner, repo, entry, downloadsRoot)
        File(downloadsRoot, entry.path)
    }

    private fun downloadEntryIntoDir(
        owner: String,
        repo: String,
        entry: GitHubContentEntry,
        rootDir: File,
    ) {
        if (!entry.isDirectory) {
            val detail = readFileContent(owner, repo, entry.path).getOrThrow()
            val outFile = File(rootDir, entry.path)
            outFile.parentFile?.mkdirs()
            outFile.writeBytes(detail.rawBytes)
        } else {
            val dirFile = File(rootDir, entry.path).apply { mkdirs() }
            val children = listRepoContents(owner, repo, entry.path).getOrDefault(emptyList())
            children.forEach { child ->
                downloadEntryIntoDir(owner, repo, child, rootDir)
            }
        }
    }

    fun importPhoneFilesToRepo(
        owner: String,
        repo: String,
        currentFolderPath: String,
        uris: List<Uri>,
        onProgress: (Int, Int, String) -> Unit = { _, _, _ -> },
    ): Result<Int> = runCatching {
        var uploaded = 0
        val base = currentFolderPath.trim('/')
        uris.forEachIndexed { idx, uri ->
            val fileName = resolveUriDisplayName(uri) ?: "file_${idx + 1}"
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@forEachIndexed
            val destPath = if (base.isEmpty()) fileName else "$base/$fileName"
            onProgress(idx + 1, uris.size, destPath)
            uploadOrUpdateFile(
                owner = owner,
                repo = repo,
                path = destPath,
                bytes = bytes,
                commitMessage = "Import $fileName to ${base.ifEmpty { "root" }}",
            ).getOrThrow()
            uploaded++
        }
        uploaded
    }

    fun importDeviceFolderTreeToRepo(
        owner: String,
        repo: String,
        currentFolderPath: String,
        treeUri: Uri,
        onProgress: (Int, String) -> Unit = { _, _ -> },
    ): Result<Int> = runCatching {
        val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
        val rootFolderName = treeDocId.substringAfterLast(':').substringAfterLast('/').ifBlank { "imported_folder" }
        val base = currentFolderPath.trim('/')
        val targetPrefix = if (base.isEmpty()) rootFolderName else "$base/$rootFolderName"

        val collectedFiles = mutableListOf<Pair<String, Uri>>()
        collectDocumentTreeFiles(treeUri, treeDocId, targetPrefix, collectedFiles)

        var count = 0
        if (collectedFiles.isEmpty()) {
            createFolderInRepo(owner, repo, base, rootFolderName).getOrThrow()
            return@runCatching 1
        }
        collectedFiles.forEachIndexed { idx, (repoRelPath, docUri) ->
            val bytes = context.contentResolver.openInputStream(docUri)?.use { it.readBytes() } ?: return@forEachIndexed
            onProgress(idx + 1, repoRelPath)
            uploadOrUpdateFile(
                owner = owner,
                repo = repo,
                path = repoRelPath,
                bytes = bytes,
                commitMessage = "Import $repoRelPath from device folder",
            ).getOrThrow()
            count++
        }
        count
    }

    private fun collectDocumentTreeFiles(
        treeUri: Uri,
        parentDocId: String,
        currentRepoPrefix: String,
        outList: MutableList<Pair<String, Uri>>,
    ) {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )
        runCatching {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                while (cursor.moveToNext()) {
                    val docId = if (idIdx >= 0) cursor.getString(idIdx) else continue
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx) else docId.substringAfterLast('/')
                    val mime = if (mimeIdx >= 0) cursor.getString(mimeIdx) else ""
                    val nextPath = "$currentRepoPrefix/$name"
                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                        collectDocumentTreeFiles(treeUri, docId, nextPath, outList)
                    } else {
                        val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        outList.add(nextPath to fileUri)
                    }
                }
            }
        }
    }

    private fun resolveUriDisplayName(uri: Uri): String? {
        runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) {
                    val n = cursor.getString(idx)
                    if (!n.isNullOrBlank()) return n
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':')
    }

    private fun sanitizeRepoName(raw: String): String =
        raw.trim().replace(Regex("[^A-Za-z0-9._-]+"), "-").trim('-')

    private fun encodeGitHubPath(path: String): String =
        path.split('/')
            .filter { it.isNotEmpty() }
            .joinToString("/") { segment ->
                URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
            }

    private fun parseRepoJson(obj: JSONObject, fallbackOwner: String): GitHubRepoItem {
        val ownerObj = obj.optJSONObject("owner")
        val ownerLogin = ownerObj?.optString("login")?.ifBlank { fallbackOwner } ?: fallbackOwner
        val name = obj.optString("name")
        return GitHubRepoItem(
            id = obj.optLong("id", System.currentTimeMillis()),
            name = name,
            fullName = obj.optString("full_name", "$ownerLogin/$name"),
            ownerLogin = ownerLogin,
            description = obj.optString("description", "").takeIf { it != "null" }.orEmpty(),
            isPrivate = obj.optBoolean("private", false),
            defaultBranch = obj.optString("default_branch", "main"),
            htmlUrl = obj.optString("html_url", "https://github.com/$ownerLogin/$name"),
            updatedAt = obj.optString("updated_at", ""),
        )
    }

    private fun saveCachedRepos(repos: List<GitHubRepoItem>) {
        val arr = JSONArray()
        repos.forEach { r ->
            arr.put(
                JSONObject().apply {
                    put("id", r.id)
                    put("name", r.name)
                    put("full_name", r.fullName)
                    put("ownerLogin", r.ownerLogin)
                    put("description", r.description)
                    put("private", r.isPrivate)
                    put("default_branch", r.defaultBranch)
                    put("html_url", r.htmlUrl)
                    put("updated_at", r.updatedAt)
                },
            )
        }
        prefs.edit().putString("gh_cached_repos", arr.toString()).apply()
    }

    private fun getCachedRepos(): List<GitHubRepoItem> = runCatching {
        val raw = prefs.getString("gh_cached_repos", null) ?: return emptyList()
        val arr = JSONArray(raw)
        val list = ArrayList<GitHubRepoItem>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                GitHubRepoItem(
                    id = o.optLong("id"),
                    name = o.optString("name"),
                    fullName = o.optString("full_name"),
                    ownerLogin = o.optString("ownerLogin"),
                    description = o.optString("description"),
                    isPrivate = o.optBoolean("private"),
                    defaultBranch = o.optString("default_branch", "main"),
                    htmlUrl = o.optString("html_url"),
                    updatedAt = o.optString("updated_at"),
                ),
            )
        }
        list
    }.getOrDefault(emptyList())

    private fun executeGitHubRequest(
        method: String,
        url: String,
        token: String,
        jsonBody: String? = null,
    ): Pair<Int, String> {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 20_000
            readTimeout = 25_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "IDE-Ultimate-Android")
            if (jsonBody != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
        }
        return try {
            if (jsonBody != null) {
                conn.outputStream.use { os ->
                    os.write(jsonBody.toByteArray(Charsets.UTF_8))
                }
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
