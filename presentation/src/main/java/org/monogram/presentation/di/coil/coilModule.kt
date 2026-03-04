package org.monogram.presentation.di.coil

import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import org.koin.dsl.module

val coilModule = module {
    single {
        ImageLoader.Builder(get())
            .components {
                add(LottieDecoder.Factory())
                add(SvgDecoder.Factory())
                add(OkHttpNetworkFetcherFactory())
            }
            .build()
    }
}
