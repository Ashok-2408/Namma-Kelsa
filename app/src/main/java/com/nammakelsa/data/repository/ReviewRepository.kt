package com.nammakelsa.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nammakelsa.core.utils.Constants
import com.nammakelsa.core.utils.Resource
import com.nammakelsa.domain.models.Review
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    fun getReviewsFlow(workerId: String): Flow<List<Review>> = callbackFlow {
        val listener = db.collection(Constants.REVIEWS_COLLECTION)
            .whereEqualTo("toUserId", workerId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val reviews = snapshot?.documents?.map { doc ->
                    Review(
                        id = doc.id,
                        bookingId = doc.getString("bookingId") ?: "",
                        fromUserId = doc.getString("fromUserId") ?: "",
                        fromUserName = doc.getString("fromUserName") ?: "",
                        toUserId = doc.getString("toUserId") ?: "",
                        rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                        comment = doc.getString("comment") ?: "",
                        images = (doc.get("images") as? List<String>) ?: emptyList(),
                        createdAt = doc.getLong("createdAt") ?: 0,
                        workerReply = doc.getString("workerReply")
                    )
                } ?: emptyList()
                trySend(reviews)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createReview(review: Review): Resource<String> {
        return try {
            val docRef = db.collection(Constants.REVIEWS_COLLECTION).document()
            val newReview = review.copy(id = docRef.id)
            docRef.set(newReview).await()
            updateWorkerRating(review.toUserId)
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create review")
        }
    }

    private suspend fun updateWorkerRating(workerId: String) {
        try {
            val reviews = db.collection(Constants.REVIEWS_COLLECTION)
                .whereEqualTo("toUserId", workerId).get().await()
            if (reviews.documents.isNotEmpty()) {
                val totalRating = reviews.documents.sumOf { (it.getDouble("rating") ?: 0.0) }
                val avgRating = (totalRating / reviews.documents.size).toFloat()
                val count = reviews.documents.size
                db.collection(Constants.WORKERS_COLLECTION).document(workerId)
                    .update(mapOf("rating" to avgRating, "reviewCount" to count)).await()
            }
        } catch (e: Exception) { }
    }

    suspend fun replyToReview(reviewId: String, reply: String): Resource<Boolean> {
        return try {
            db.collection(Constants.REVIEWS_COLLECTION).document(reviewId)
                .update("workerReply", reply).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to reply")
        }
    }
}