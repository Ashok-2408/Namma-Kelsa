package com.nammakelsa.features.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.nammakelsa.core.datastore.PreferencesManager
import com.nammakelsa.core.ui.theme.NammaKelsaTheme
import com.nammakelsa.core.utils.Constants
import com.nammakelsa.core.utils.LocaleHelper
import com.nammakelsa.data.repository.WorkerRepository
import com.nammakelsa.domain.models.Worker
import com.nammakelsa.features.home.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingActivity : ComponentActivity() {

    @Inject lateinit var prefs: PreferencesManager
    @Inject lateinit var workerRepo: WorkerRepository

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, OnboardingActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NammaKelsaTheme {
                var currentStep by remember { mutableStateOf("language") }
                var currentLanguage by remember { mutableStateOf("en") }

                when (currentStep) {
                    "language" -> {
                        LanguageSelectionScreen(
                            onLanguageSelected = { lang ->
                                currentLanguage = lang
                                CoroutineScope(Dispatchers.IO).launch {
                                    prefs.setAppLanguage(lang)
                                    prefs.setLanguageSelected(true)
                                }
                                LocaleHelper.setLocale(this@OnboardingActivity, lang)
                                currentStep = "userinfo"
                            }
                        )
                    }
                    "userinfo" -> {
                        UserInfoScreen(
                            currentLanguage = currentLanguage,
                            onSaveAndContinue = { name, phone, email, address, city, isWorker, skillType, dailyRate, experience, bio, galleryUris ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    prefs.saveUserName(name)
                                    prefs.saveUserPhone(phone)
                                    prefs.saveUserEmail(email)
                                    prefs.saveUserAddress(address)
                                    prefs.saveUserCity(city)
                                    if (isWorker) {
                                        prefs.saveUserRole(Constants.ROLE_WORKER)
                                        val userId = prefs.getUserId()
                                        val galleryUrls = mutableListOf<String>()
                                        galleryUris.forEachIndexed { i, uri ->
                                            val url = workerRepo.uploadGalleryPhoto(userId, uri, i)
                                            if (url != null) galleryUrls.add(url)
                                        }
                                        val worker = Worker(
                                            uid = userId,
                                            name = name,
                                            phone = phone,
                                            email = email,
                                            skillType = skillType,
                                            dailyRate = dailyRate,
                                            locationCity = city,
                                            experience = experience,
                                            bio = bio,
                                            galleryPhotos = galleryUrls,
                                            isAvailable = true,
                                            createdAt = System.currentTimeMillis()
                                        )
                                        workerRepo.saveWorkerProfile(worker)
                                    } else {
                                        prefs.saveUserRole(Constants.ROLE_CUSTOMER)
                                    }
                                    prefs.setUserInfoCollected(true)
                                    prefs.setHasOnboarded(true)
                                }
                                val intent = Intent(this@OnboardingActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        var lang = "en"
        try {
            val prefs = PreferencesManager(newBase)
            lang = kotlinx.coroutines.runBlocking { prefs.getAppLanguage() }
        } catch (_: Exception) {}
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }
}
