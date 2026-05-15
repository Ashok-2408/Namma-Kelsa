package com.nammakelsa.features.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.auth.FirebaseAuth
import com.nammakelsa.core.datastore.PreferencesManager
import com.nammakelsa.core.ui.theme.NammaKelsaTheme
import com.nammakelsa.core.utils.LocaleHelper
import com.nammakelsa.features.home.MainActivity
import com.nammakelsa.features.onboarding.OnboardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.compose.runtime.*
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    @Inject lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply stored language
        val lang = runBlocking {
            try { prefs.getAppLanguage() } catch (_: Exception) { "en" }
        }
        LocaleHelper.setLocale(this, lang)

        setContent {
            val darkMode by prefs.isDarkMode.collectAsState(initial = androidx.compose.foundation.isSystemInDarkTheme())
            NammaKelsaTheme(darkTheme = darkMode ?: androidx.compose.foundation.isSystemInDarkTheme()) {
                AnimatedSplashScreen(onAnimationFinished = {
                    signInAnonymously()
                })
            }
        }
    }

    private fun signInAnonymously() {
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                CoroutineScope(Dispatchers.IO).launch {
                    prefs.saveUserId(uid)
                    prefs.setLoggedIn(true)
                }
                checkOnboarding()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Sign in failed: ${e.message}", Toast.LENGTH_LONG).show()
                checkOnboarding()
            }
    }

    private fun checkOnboarding() {
        CoroutineScope(Dispatchers.Main).launch {
            val langSelected = prefs.getAppLanguage().isNotEmpty()
            val infoCollected = prefs.userInfoCollected.first()
            val hasOnboarded = prefs.hasOnboarded.first()

            if (infoCollected && hasOnboarded) {
                navigateToMain()
            } else {
                OnboardingActivity.start(this@SplashActivity)
                finish()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        var lang = "en"
        try {
            val p = PreferencesManager(newBase)
            lang = runBlocking { p.getAppLanguage() }
        } catch (_: Exception) {}
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }
}

