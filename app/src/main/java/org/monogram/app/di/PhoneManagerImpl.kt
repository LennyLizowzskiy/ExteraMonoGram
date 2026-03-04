package org.monogram.app.di

import android.telephony.TelephonyManager
import org.monogram.domain.managers.PhoneManager

class PhoneManagerImpl(private val telephonyManager: TelephonyManager?): PhoneManager {
    override fun getSimCountryIso(): String? {
        return telephonyManager?.simCountryIso
    }
}