package org.monogram.domain.managers

interface DistrManager {
    fun isGmsAvailable(): Boolean
    fun isInstalledFromGooglePlay(): Boolean
}