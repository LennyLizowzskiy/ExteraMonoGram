package org.monogram.domain.managers

import java.io.InputStream

interface AssetsManager {
    fun getAssets(path: String): InputStream
}