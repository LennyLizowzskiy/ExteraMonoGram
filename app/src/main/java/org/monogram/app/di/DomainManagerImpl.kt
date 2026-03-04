package org.monogram.app.di

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.os.Build
import org.monogram.domain.managers.DomainManager

class DomainManagerImpl(private val context: Context?, private val packageName: String): DomainManager {
    @SuppressLint("WrongConstant")
    override fun isEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val state = (context?.getSystemService(Context.DOMAIN_VERIFICATION_SERVICE) as? DomainVerificationManager)?.getDomainVerificationUserState(packageName)
            state?.hostToStateMap?.get("t.me") == DomainVerificationUserState.DOMAIN_STATE_SELECTED ||
                state?.hostToStateMap?.get("t.me") == DomainVerificationUserState.DOMAIN_STATE_VERIFIED
        } else true
    }
}