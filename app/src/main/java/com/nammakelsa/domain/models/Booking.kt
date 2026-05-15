package com.nammakelsa.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Booking(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val workerId: String = "",
    val workerName: String = "",
    val workerPhone: String = "",
    val skillType: String = "",
    val scheduledDate: Long = 0,
    val scheduledTime: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val agreedRate: Int = 0,
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis(),
    val acceptedAt: Long? = null,
    val completedAt: Long? = null,
    val cancelledAt: Long? = null,
    val cancellationReason: String? = null
) : Parcelable
