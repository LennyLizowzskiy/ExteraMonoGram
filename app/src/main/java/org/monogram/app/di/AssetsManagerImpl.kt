package org.monogram.app.di

import android.content.Context
import org.monogram.domain.managers.AssetsManager
import java.io.InputStream

class AssetsManagerImpl(private val context: Context) : AssetsManager {
    override fun getAssets(path: String): InputStream {
        return context.assets.open(path)
    }
}