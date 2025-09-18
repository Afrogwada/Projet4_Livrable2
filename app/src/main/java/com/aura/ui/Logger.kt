package com.aura.ui

import android.util.Log



object Logger {

    private fun buildMessage(message: String): String {
        val element = Throwable().stackTrace[2] // [0] = ici, [1] = log(), [2] = appelant réel
        val fileName = element.fileName
        val lineNumber = element.lineNumber
        val methodName = element.methodName

        return "($fileName:$lineNumber) -> $methodName : $message"

    }
    fun d( message: String) {
        Log.d("LogPerso", buildMessage(message))
    }
}