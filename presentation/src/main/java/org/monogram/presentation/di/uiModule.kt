package org.monogram.presentation.di

import org.monogram.presentation.di.coil.coilModule
import org.koin.dsl.module

val uiModule = module {
    includes(coilModule)
}