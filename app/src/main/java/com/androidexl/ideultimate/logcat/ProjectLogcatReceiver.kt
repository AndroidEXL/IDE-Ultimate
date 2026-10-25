package com.androidexl.ideultimate.logcat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver in IDE Ultimate that receives real-time log broadcasts
 * emitted by projects using AppLogger (ACTION_LOG_EVENT).
 */
class ProjectLogcatReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (action == ACTION_LOG_EVENT) {
            val pkg = intent.getStringExtra(EXTRA_PACKAGE) ?: "unknown.package"
            val tag = intent.getStringExtra(EXTRA_TAG) ?: "App"
            val lvlStr = intent.getStringExtra(EXTRA_LEVEL) ?: "D"
            val msg = intent.getStringExtra(EXTRA_MESSAGE) ?: ""
            val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: pkg.substringAfterLast('.')

            ProjectLogcatBridge.log(
                appName = appName,
                packageName = pkg,
                level = LogcatLevel.fromCode(lvlStr),
                tag = tag,
                message = msg,
            )
        }
    }

    companion object {
        const val ACTION_LOG_EVENT = "com.androidexl.ideultimate.ACTION_LOG_EVENT"
        const val EXTRA_PACKAGE = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_TAG = "extra_tag"
        const val EXTRA_LEVEL = "extra_level"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_TIMESTAMP = "extra_timestamp"
    }
}
