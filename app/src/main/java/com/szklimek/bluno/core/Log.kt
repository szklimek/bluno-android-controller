package com.szklimek.bluno.core

object Log {

    private const val TAG = "DietApp"
    var remoteLogger: RemoteLogger? = null

    private val lineNumber: Int
        get() = runCatching { Thread.currentThread().stackTrace[6].lineNumber }.getOrDefault(-1)

    private val fileName: String?
        get() = runCatching { Thread.currentThread().stackTrace[6].fileName }.getOrNull()

    private val methodName: String
        get() = runCatching { Thread.currentThread().stackTrace[6].methodName }.getOrDefault("InvalidMethodName")

    private val currentThread: String
        get() = Thread.currentThread().name

    fun i(message: String, remote: Boolean = false) {
        val logMessage = getLogMessage(message)
        android.util.Log.i(TAG, logMessage)
        if (remote) remoteLogger?.logMessage("i", logMessage)
    }

    fun v(message: String, remote: Boolean = false) {
        val logMessage = getLogMessage(message)
        android.util.Log.v(TAG, logMessage)
        if (remote) remoteLogger?.logMessage("v", logMessage)
    }

    fun d(message: String, remote: Boolean = false) {
        val logMessage = getLogMessage(message)
        android.util.Log.d(TAG, logMessage)
        if (remote) remoteLogger?.logMessage("d", logMessage)
    }

    fun w(message: String, remote: Boolean = false) {
        val logMessage = getLogMessage(message)
        android.util.Log.w(TAG, logMessage)
        if (remote) remoteLogger?.logMessage("w", logMessage)
    }

    fun e(message: String, remote: Boolean = false) {
        val logMessage = getLogMessage(message)
        android.util.Log.e(TAG, logMessage)
        if (remote) remoteLogger?.logMessage("e", logMessage)
    }

    private fun getLogMessage(message: String): String {
        return "($fileName:$lineNumber) $methodName @$currentThread $message"
    }
}

interface RemoteLogger {
    fun logMessage(level: String, message: String)
}
