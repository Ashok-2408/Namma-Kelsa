package com.nammakelsa

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class NammaKelsaApp : Application() {
    override fun attachBaseContext(base: Context) {
        val lang = runBlocking {
            try {
                val tempPrefs = com.nammakelsa.core.datastore.PreferencesManager(base)
                tempPrefs.getAppLanguage()
            } catch (_: Exception) { "en" }
        }
        super.attachBaseContext(com.nammakelsa.core.utils.LocaleHelper.setLocale(base, lang))
    }

    override fun onCreate() {
        super.onCreate()
    }
}
