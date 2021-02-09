package com.szklimek.bluno.core

/**
 * Helper class to provide more information in logs
 */
object Log {

    private const val TAG = "BlunoApp"

    private val lineNumber: Int
        get() = runCatching { Thread.currentThread().stackTrace[6].lineNumber }.getOrDefault(-1)

    private val fileName: String?
        get() = runCatching { Thread.currentThread().stackTrace[6].fileName }.getOrNull()

    private val methodName: String
        get() = runCatching { Thread.currentThread().stackTrace[6].methodName }.getOrDefault("InvalidMethodName")

    private val currentThread: String
        get() = Thread.currentThread().name

    fun i(message: String) {
        val logMessage = getLogMessage(message)
        android.util.Log.i(TAG, logMessage)
    }

    fun v(message: String) {
        val logMessage = getLogMessage(message)
        android.util.Log.v(TAG, logMessage)
    }

    fun d(message: String) {
        val logMessage = getLogMessage(message)
        android.util.Log.d(TAG, logMessage)
    }

    fun w(message: String) {
        val logMessage = getLogMessage(message)
        android.util.Log.w(TAG, logMessage)
    }

    fun e(message: String) {
        val logMessage = getLogMessage(message)
        android.util.Log.e(TAG, logMessage)
    }

    private fun getLogMessage(message: String): String {
        return "($fileName:$lineNumber) $methodName @$currentThread $message"
    }
}
