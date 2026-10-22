package com.androidexl.ideultimate.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class RtdbHttpResponse(
    val isSuccess: Boolean,
    val statusCode: Int,
    val body: String,
)

data class AuthToolkitResponse(
    val isSuccess: Boolean,
    val localId: String = "",
    val idToken: String = "",
    val errorMessage: String? = null,
)

/**
 * Centralised Firebase Auth, Realtime Database & Direct REST client for
 * `https://sketchware-f2a24-default-rtdb.firebaseio.com` (`sketchware-f2a24`).
 */
object FirebaseConfig {
    const val APP_INSTANCE_NAME = "sketchware_rtdb_app"
    const val PROJECT_NUMBER = "481936198924"
    const val FIREBASE_URL = "https://sketchware-f2a24-default-rtdb.firebaseio.com"
    const val PROJECT_ID = "sketchware-f2a24"
    const val STORAGE_BUCKET = "sketchware-f2a24.firebasestorage.app"
    const val MOBILESDK_APP_ID = "1:481936198924:android:506fe0f2635857d461cd29"
    const val CLIENT_PACKAGE_NAME = "pro.sketchware"
    const val API_KEY = "AIzaSyABSZalOZMBGQR6z9NKm-tEVmY6ZfDqhLA"

    // Top-level nodes in https://sketchware-f2a24-default-rtdb.firebaseio.com
    const val NODE_USERS = "users"
    const val NODE_STORE_ITEMS = "store_items"
    const val NODE_PROJECTS = "projects"
    const val NODE_CODES = "codes"
    const val NODE_LIBRARIES = "libraries"
    const val NODE_COLLECTIONS = "collections"
    const val NODE_REACTIONS = "reactions"

    @Volatile
    var currentIdToken: String? = null

    private fun buildFirebaseOptions(): FirebaseOptions =
        FirebaseOptions.Builder()
            .setApplicationId(MOBILESDK_APP_ID)
            .setApiKey(API_KEY)
            .setDatabaseUrl(FIREBASE_URL)
            .setProjectId(PROJECT_ID)
            .setStorageBucket(STORAGE_BUCKET)
            .setGcmSenderId(PROJECT_NUMBER)
            .build()

    @Synchronized
    fun ensureFirebaseApp(context: Context): FirebaseApp? {
        return runCatching {
            val appContext = context.applicationContext
            val options = buildFirebaseOptions()
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                FirebaseApp.initializeApp(appContext, options)
            }
            val existingNamed = FirebaseApp.getApps(appContext)
                .firstOrNull { it.name == APP_INSTANCE_NAME }
            existingNamed ?: FirebaseApp.initializeApp(appContext, options, APP_INSTANCE_NAME)
        }.getOrElse {
            runCatching { FirebaseApp.getInstance() }.getOrNull()
        }
    }

    @Synchronized
    fun ensureInitialized(context: Context): FirebaseDatabase? {
        return runCatching {
            val app = ensureFirebaseApp(context)
            if (app != null) {
                FirebaseDatabase.getInstance(app, FIREBASE_URL)
            } else {
                FirebaseDatabase.getInstance(FIREBASE_URL)
            }
        }.getOrNull()
    }

    fun authInstance(context: Context): FirebaseAuth? =
        runCatching {
            val app = ensureFirebaseApp(context)
            if (app != null) FirebaseAuth.getInstance(app) else FirebaseAuth.getInstance()
        }.getOrNull()

    fun databaseReference(context: Context, path: String): DatabaseReference? =
        ensureInitialized(context)?.getReference(path)

    fun storageInstance(context: Context): FirebaseStorage? =
        runCatching {
            val app = ensureFirebaseApp(context)
            if (app != null) {
                FirebaseStorage.getInstance(app, "gs://$STORAGE_BUCKET")
            } else {
                FirebaseStorage.getInstance("gs://$STORAGE_BUCKET")
            }
        }.getOrNull()

    /**
     * Registers a user via Firebase Auth IdentityToolkit REST API (`accounts:signUp`).
     */
    fun signUpWithIdentityToolkit(email: String, password: String): AuthToolkitResponse {
        val url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$API_KEY"
        val payload = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("returnSecureToken", true)
        }
        return executeIdentityToolkitPost(url, payload)
    }

    /**
     * Signs in a user via Firebase Auth IdentityToolkit REST API (`accounts:signInWithPassword`).
     */
    fun signInWithIdentityToolkit(email: String, password: String): AuthToolkitResponse {
        val url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$API_KEY"
        val payload = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("returnSecureToken", true)
        }
        return executeIdentityToolkitPost(url, payload)
    }

    /**
     * Obtains an anonymous Firebase Auth token if Email/Password provider is not active.
     */
    fun fetchAnonymousToken(): String? {
        val url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$API_KEY"
        val payload = JSONObject().apply {
            put("returnSecureToken", true)
        }
        val res = executeIdentityToolkitPost(url, payload)
        if (res.isSuccess && res.idToken.isNotBlank()) {
            currentIdToken = res.idToken
            return res.idToken
        }
        return null
    }

    private fun executeIdentityToolkitPost(urlStr: String, bodyJson: JSONObject): AuthToolkitResponse {
        return runCatching {
            val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12_000
                readTimeout = 12_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(bodyJson.toString())
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            conn.disconnect()
            if (code in 200..299) {
                val json = JSONObject(text)
                val token = json.optString("idToken", "")
                val uid = json.optString("localId", "")
                if (token.isNotBlank()) currentIdToken = token
                AuthToolkitResponse(isSuccess = true, localId = uid, idToken = token)
            } else {
                val errMsg = runCatching {
                    JSONObject(text).optJSONObject("error")?.optString("message")
                }.getOrNull() ?: "HTTP_$code"
                AuthToolkitResponse(isSuccess = false, errorMessage = errMsg)
            }
        }.getOrElse { e ->
            AuthToolkitResponse(isSuccess = false, errorMessage = e.message)
        }
    }

    /**
     * Writes JSON directly to `https://sketchware-f2a24-default-rtdb.firebaseio.com/<path>.json`
     * using HTTP PUT. Retries automatically with/without auth token if needed.
     */
    fun putRtdbJson(path: String, jsonBody: String): RtdbHttpResponse {
        val cleanPath = path.trimStart('/')
        val token = currentIdToken
        // 1. Try direct public or current token write
        val firstUrl = if (!token.isNullOrBlank()) {
            "$FIREBASE_URL/$cleanPath.json?auth=$token"
        } else {
            "$FIREBASE_URL/$cleanPath.json"
        }
        val firstAttempt = executeRtdbRequest("PUT", firstUrl, jsonBody)
        if (firstAttempt.isSuccess) return firstAttempt

        // 2. If token was used and rejected, try without token; if no token was used and 401, try anonymous token
        return if (!token.isNullOrBlank()) {
            executeRtdbRequest("PUT", "$FIREBASE_URL/$cleanPath.json", jsonBody)
        } else {
            val anon = fetchAnonymousToken()
            if (!anon.isNullOrBlank()) {
                executeRtdbRequest("PUT", "$FIREBASE_URL/$cleanPath.json?auth=$anon", jsonBody)
            } else {
                firstAttempt
            }
        }
    }

    /**
     * Reads JSON directly from `https://sketchware-f2a24-default-rtdb.firebaseio.com/<path>.json`.
     */
    fun getRtdbJson(path: String): RtdbHttpResponse {
        val cleanPath = path.trimStart('/')
        val token = currentIdToken
        val firstUrl = if (!token.isNullOrBlank()) {
            "$FIREBASE_URL/$cleanPath.json?auth=$token"
        } else {
            "$FIREBASE_URL/$cleanPath.json"
        }
        val firstAttempt = executeRtdbRequest("GET", firstUrl, null)
        if (firstAttempt.isSuccess) return firstAttempt

        return if (!token.isNullOrBlank()) {
            executeRtdbRequest("GET", "$FIREBASE_URL/$cleanPath.json", null)
        } else {
            val anon = fetchAnonymousToken()
            if (!anon.isNullOrBlank()) {
                executeRtdbRequest("GET", "$FIREBASE_URL/$cleanPath.json?auth=$anon", null)
            } else {
                firstAttempt
            }
        }
    }

    /**
     * Deletes a node from `https://sketchware-f2a24-default-rtdb.firebaseio.com/<path>.json`.
     */
    fun deleteRtdbJson(path: String): RtdbHttpResponse {
        val cleanPath = path.trimStart('/')
        val token = currentIdToken
        val url = if (!token.isNullOrBlank()) {
            "$FIREBASE_URL/$cleanPath.json?auth=$token"
        } else {
            "$FIREBASE_URL/$cleanPath.json"
        }
        val first = executeRtdbRequest("DELETE", url, null)
        if (first.isSuccess) return first
        return executeRtdbRequest("DELETE", "$FIREBASE_URL/$cleanPath.json", null)
    }

    private fun executeRtdbRequest(
        method: String,
        urlStr: String,
        jsonBody: String?,
    ): RtdbHttpResponse {
        return runCatching {
            val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 12_000
                readTimeout = 12_000
                setRequestProperty("Accept", "application/json")
                if (jsonBody != null) {
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                }
            }
            if (jsonBody != null) {
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonBody)
                }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            conn.disconnect()
            RtdbHttpResponse(
                isSuccess = code in 200..299,
                statusCode = code,
                body = body,
            )
        }.getOrElse { e ->
            RtdbHttpResponse(
                isSuccess = false,
                statusCode = -1,
                body = e.message ?: "Network error",
            )
        }
    }
}
