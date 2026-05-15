package com.nammakelsa.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nammakelsa.core.utils.Constants
import com.nammakelsa.core.utils.Resource
import com.nammakelsa.domain.models.Booking
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    fun getBookingsFlow(userId: String, role: String): Flow<List<Booking>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val field = if (role == Constants.ROLE_WORKER) "workerId" else "customerId"
        val listener = db.collection(Constants.BOOKINGS_COLLECTION)
            .whereEqualTo(field, userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val bookings = snapshot?.documents?.map { doc ->
                    Booking(
                        id = doc.id,
                        customerId = doc.getString("customerId") ?: "",
                        customerName = doc.getString("customerName") ?: "",
                        customerPhone = doc.getString("customerPhone") ?: "",
                        workerId = doc.getString("workerId") ?: "",
                        workerName = doc.getString("workerName") ?: "",
                        workerPhone = doc.getString("workerPhone") ?: "",
                        skillType = doc.getString("skillType") ?: "",
                        scheduledDate = doc.getLong("scheduledDate") ?: 0,
                        scheduledTime = doc.getString("scheduledTime") ?: "",
                        address = doc.getString("address") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        description = doc.getString("description") ?: "",
                        agreedRate = (doc.getLong("agreedRate") ?: 0).toInt(),
                        status = doc.getString("status") ?: "pending",
                        createdAt = doc.getLong("createdAt") ?: 0,
                        acceptedAt = doc.getLong("acceptedAt"),
                        completedAt = doc.getLong("completedAt"),
                        cancelledAt = doc.getLong("cancelledAt"),
                        cancellationReason = doc.getString("cancellationReason")
                    )
                } ?: emptyList()
                trySend(bookings.sortedByDescending { it.createdAt })
            }
        awaitClose { listener.remove() }
    }

    suspend fun createBooking(booking: Booking): Resource<String> {
        return try {
            val docRef = db.collection(Constants.BOOKINGS_COLLECTION).document()
            val newBooking = booking.copy(id = docRef.id)
            docRef.set(newBooking).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create booking")
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Resource<Boolean> {
        return try {
            val updates = mutableMapOf<String, Any>("status" to status)
            when (status) {
                Constants.BOOKING_ACCEPTED -> updates["acceptedAt"] = System.currentTimeMillis()
                Constants.BOOKING_COMPLETED -> updates["completedAt"] = System.currentTimeMillis()
                Constants.BOOKING_CANCELLED -> updates["cancelledAt"] = System.currentTimeMillis()
            }
            db.collection(Constants.BOOKINGS_COLLECTION).document(bookingId).update(updates).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Update failed")
        }
    }
}
