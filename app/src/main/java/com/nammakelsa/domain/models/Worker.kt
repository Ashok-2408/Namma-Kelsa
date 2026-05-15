package com.nammakelsa.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Worker(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val skillType: String = "",
    val skills: List<String> = emptyList(),
    val dailyRate: Int = 0,
    val locationCity: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val profilePhotoUrl: String = "",
    val coverPhotoUrl: String = "",
    val galleryPhotos: List<String> = emptyList(),
    val bio: String = "",
    val languages: List<String> = emptyList(),
    val experience: Int = 0,
    val isAvailable: Boolean = true,
    val isVerified: Boolean = false,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val aadhaarPhotoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
