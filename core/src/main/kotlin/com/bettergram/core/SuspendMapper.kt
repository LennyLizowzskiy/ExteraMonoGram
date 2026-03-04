package com.bettergram.core

interface SuspendMapper<in I, out O> {
    suspend fun map(input: I): O
}