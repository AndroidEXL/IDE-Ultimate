package com.androidexl.ideultimate.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

/**
 * Centralised Firebase Realtime Database & Storage configuration initialized
 * from `google-services.json` (`sketchware-f2a24`).
 */
object FirebaseConfig {
    const val PROJECT_NUMBER = "481936198924"
    const val FIREBASE_URL = "https://sketchware-f2a24-default-rtdb.firebaseio.com"
    const val PROJECT_ID = "sketchware-f2a24"
    const val STORAGE_BUCKET = "sketchware-f2a24.firebasestorage.app"
    const val MOBILESDK_APP_ID = "1:481936198924:android:506fe0f2635857d461cd29"
    const val CLIENT_PACKAGE_NAME = "pro.sketchware"
    const val API_KEY = "AIzaSyABSZalOZMBGQR6z9NKm-tEVmY6ZfDqhLA"

    const val NODE_ROOT = "ide_ultimate"
    const val NODE_STORE_ITEMS = "$NODE_ROOT/store_items"
    const val NODE_ACCOUNTS = "$NODE_ROOT/accounts"
    const val NODE_USERS = "$NODE_ROOT/users"
    const val NODE_COLLECTIONS = "$NODE_ROOT/collections"
    const val NODE_REACTIONS = "$NODE_ROOT/reactions"

    // Legacy demo nodes to clean up automatically so no demo items remain
    val LEGACY_DEMO_NODES = listOf(
        "$NODE_ROOT/store_projects/proj_ide_ultimate_studio",
        "$NODE_ROOT/store_projects/proj_flutter_commerce_pro",
        "$NODE_ROOT/store_projects/proj_java_file_explorer",
        "$NODE_ROOT/code_snippets",
        "$NODE_ROOT/custom_libraries",
    )

    @Volatile
    private var persistenceConfigured = false

    @Synchronized
    fun ensureInitialized(context: Context): FirebaseDatabase? {
        return runCatching {
            val appContext = context.applicationContext
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(MOBILESDK_APP_ID)
                    .setApiKey(API_KEY)
                    .setDatabaseUrl(FIREBASE_URL)
                    .setProjectId(PROJECT_ID)
                    .setStorageBucket(STORAGE_BUCKET)
                    .setGcmSenderId(PROJECT_NUMBER)
                    .build()
                FirebaseApp.initializeApp(appContext, options)
            }
            val database = FirebaseDatabase.getInstance(FIREBASE_URL)
            if (!persistenceConfigured) {
                runCatching {
                    database.setPersistenceEnabled(true)
                }
                persistenceConfigured = true
            }
            database
        }.getOrNull()
    }

    fun databaseReference(context: Context, path: String): DatabaseReference? =
        ensureInitialized(context)?.getReference(path)

    fun storageInstance(context: Context): FirebaseStorage? =
        runCatching {
            ensureInitialized(context)
            FirebaseStorage.getInstance("gs://$STORAGE_BUCKET")
        }.getOrNull()
}
