package com.guahoo.data.network

import android.util.Log

object L {
    fun d(message: String) {
            Log.d(getClassName(), message)

    }

    fun e(message: String) {
            Log.e(getClassName(), message)

    }

    private fun getClassName(): String {
        return Thread.currentThread().stackTrace[3].className.substringAfterLast(".")
    }

    // Add other log methods as needed (e.g., info, warning)
}