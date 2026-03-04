package org.monogram.core

interface Logger {
    fun d(tag: String, message: String)
    fun e(tag: String, throwable: Throwable)
}