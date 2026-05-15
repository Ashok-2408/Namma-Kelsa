package com.nammakelsa.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.nammakelsa.core.utils.Constants
import com.nammakelsa.core.utils.Resource
import com.nammakelsa.domain.models.Worker
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkerRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val hardcodedWorkers = listOf(
        Worker(
            uid = "worker_plumber_1",
            name = "Raju",
            phone = "9876543210",
            email = "raju.plumber@gmail.com",
            skillType = "Plumber",
            skills = listOf("Pipe Repair", "Tap Installation", "Drain Cleaning", "Water Heater Installation"),
            dailyRate = 500,
            experience = 15,
            languages = listOf("Kannada", "Hindi", "English"),
            locationCity = "Bangalore",
            latitude = 12.9716,
            longitude = 77.5946,
            bio = "Master plumber with over 15 years of hands-on experience. I specialize in complex residential and industrial plumbing systems, solar heater installations, and comprehensive drainage solutions.",
            isAvailable = true,
            isVerified = true,
            rating = 4.8f,
            reviewCount = 156,
            profilePhotoUrl = "",
            createdAt = System.currentTimeMillis() - 86400000L * 365 * 10
        ),
        Worker(
            uid = "worker_electrician_1",
            name = "Suresh",
            phone = "9876543211",
            email = "suresh.elec@gmail.com",
            skillType = "Electrician",
            skills = listOf("Wiring", "Fan Installation", "Switch Repair", "AC Wiring"),
            dailyRate = 600,
            experience = 12,
            languages = listOf("Kannada", "Telugu", "English"),
            locationCity = "Bangalore",
            latitude = 12.9345,
            longitude = 77.6100,
            bio = "Highly skilled electrician with 12 years in the field. Expert in smart home automation, industrial wiring, and reliable troubleshooting for all electrical failures.",
            isAvailable = true,
            isVerified = true,
            rating = 4.7f,
            reviewCount = 124,
            profilePhotoUrl = "",
            createdAt = System.currentTimeMillis() - 86400000L * 365 * 8
        ),
        Worker(
            uid = "worker_carpenter_1",
            name = "Venkatesh",
            phone = "9876543212",
            email = "venkatesh.carpenter@gmail.com",
            skillType = "Carpenter",
            skills = listOf("Furniture Making", "Door Repair", "Cabinet Installation", "Wood Polishing"),
            dailyRate = 800,
            experience = 20,
            languages = listOf("Kannada", "Hindi"),
            locationCity = "Mysore",
            latitude = 12.2958,
            longitude = 76.6394,
            bio = "Artisan carpenter with two decades of experience in traditional and modern woodworking. Specialized in custom teakwood furniture and high-end modular kitchen installations.",
            isAvailable = true,
            isVerified = true,
            rating = 4.9f,
            reviewCount = 210,
            profilePhotoUrl = "",
            createdAt = System.currentTimeMillis() - 86400000L * 365 * 15
        ),
        Worker(
            uid = "worker_painter_1",
            name = "Mohan",
            phone = "9876543213",
            email = "mohan.painter@gmail.com",
            skillType = "Painter",
            skills = listOf("Wall Painting", "Texture Finish", "Waterproofing", "Color Mixing"),
            dailyRate = 450,
            experience = 10,
            languages = listOf("Kannada", "Tamil", "English"),
            locationCity = "Bangalore",
            latitude = 12.9500,
            longitude = 77.5800,
            bio = "Professional painting contractor with 10 years of experience. We provide premium texture finishes, waterproofing, and expert color consultation for luxury homes.",
            isAvailable = true,
            isVerified = true,
            rating = 4.5f,
            reviewCount = 89,
            profilePhotoUrl = "",
            createdAt = System.currentTimeMillis() - 86400000L * 365 * 7
        ),
        Worker(
            uid = "worker_mason_1",
            name = "Krishna",
            phone = "9876543214",
            email = "krishna.mason@gmail.com",
            skillType = "Mason",
            skills = listOf("Brick Work", "Plastering", "Flooring", "Tile Installation"),
            dailyRate = 750,
            experience = 25,
            languages = listOf("Kannada", "Telugu"),
            locationCity = "Hubli",
            latitude = 15.3500,
            longitude = 75.1400,
            bio = "Senior civil mason with 25 years in structural construction. Expert in foundation work, detailed plastering, and premium marble/granite flooring.",
            isAvailable = true,
            isVerified = true,
            rating = 4.9f,
            reviewCount = 340,
            profilePhotoUrl = "",
            createdAt = System.currentTimeMillis() - 86400000L * 365 * 20
        ),
        Worker(
            uid = "worker_plumber_2",
            name = "Shankar",
            phone = "9876543215",
            email = "shankar.plumber@gmail.com",
            skillType = "Plumber",
            skills = listOf("Water Tank Installation", "Pipeline Repair", "Bathroom Fitting"),
            dailyRate = 450,
            experience = 9,
            languages = listOf("Kannada", "Hindi"),
            locationCity = "Mangalore",
            latitude = 12.9141,
            longitude = 74.8560,
            bio = "Specialist plumber with 9 years of experience. Expert in multi-story pipeline design and efficient water management systems.",
            isAvailable = true,
            isVerified = true,
            rating = 4.4f,
            reviewCount = 67,
            profilePhotoUrl = "",
            createdAt = System.currentTimeMillis() - 86400000L * 365 * 6
        ),
        Worker(
            uid = "worker_electrician_2",
            name = "Prakash",
            phone = "9876543216",
            email = "prakash.elec@gmail.com",
            skillType = "Electrician",
            skills = listOf("Home Automation", "Security Camera Setup", "Electrical Panel Work"),
            dailyRate = 850,
            experience = 18,
            languages = listOf("Kannada", "English", "Hindi"),
            locationCity = "Bangalore",
            latitude = 12.9800,
            longitude = 77.6200,
            bio = "Senior electrical engineer with 18 years in home automation. Designing secure and energy-efficient electrical systems for modern apartments.",
            isAvailable = true,
            isVerified = true,
            rating = 4.9f,
            reviewCount = 145,
            profilePhotoUrl = "",
            createdAt = System.currentTimeMillis() - 86400000L * 365 * 12
        ),
        Worker(
            uid = "worker_carpenter_2",
            name = "Manjunath",
            phone = "9876543217",
            email = "manjunath.carpenter@gmail.com",
            skillType = "Carpenter",
            skills = listOf("Modular Kitchen", "Wardrobe Design", "Wooden Flooring"),
            dailyRate = 1000,
            experience = 22,
            languages = listOf("Kannada", "English"),
            locationCity = "Bangalore",
            latitude = 12.9400,
            longitude = 77.5600,
            bio = "Master craftsman with 22 years of experience. Specialized in high-end Italian modular kitchens and luxury wardrobe architecture.",
            isAvailable = true,
            isVerified = true,
            rating = 5.0f,
            reviewCount = 289,
            profilePhotoUrl = "",
            createdAt = System.currentTimeMillis() - 86400000L * 365 * 18
        )
    )

    suspend fun saveWorkerProfile(worker: Worker): Resource<Boolean> {
        return try {
            db.collection(Constants.WORKERS_COLLECTION).document(worker.uid)
                .set(worker, SetOptions.merge()).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Save failed")
        }
    }

    suspend fun getWorkerById(uid: String): Worker? {
        return try {
            val doc = db.collection(Constants.WORKERS_COLLECTION).document(uid).get().await()
            if (doc.exists()) {
                Worker(
                    uid = doc.id,
                    name = doc.getString("name") ?: "",
                    phone = doc.getString("phone") ?: "",
                    email = doc.getString("email") ?: "",
                    skillType = doc.getString("skillType") ?: "",
                    skills = (doc.get("skills") as? List<String>) ?: emptyList(),
                    dailyRate = (doc.getLong("dailyRate") ?: 0).toInt(),
                    locationCity = doc.getString("locationCity") ?: "",
                    latitude = doc.getDouble("latitude") ?: 0.0,
                    longitude = doc.getDouble("longitude") ?: 0.0,
                    profilePhotoUrl = doc.getString("profilePhotoUrl") ?: "",
                    coverPhotoUrl = doc.getString("coverPhotoUrl") ?: "",
                    galleryPhotos = (doc.get("galleryPhotos") as? List<String>) ?: emptyList(),
                    bio = doc.getString("bio") ?: "",
                    languages = (doc.get("languages") as? List<String>) ?: emptyList(),
                    experience = (doc.getLong("experience") ?: 0).toInt(),
                    isAvailable = doc.getBoolean("isAvailable") ?: true,
                    isVerified = doc.getBoolean("isVerified") ?: false,
                    rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                    reviewCount = (doc.getLong("reviewCount") ?: 0).toInt(),
                    createdAt = doc.getLong("createdAt") ?: 0
                )
            } else {
                hardcodedWorkers.find { it.uid == uid }
            }
        } catch (e: Exception) {
            hardcodedWorkers.find { it.uid == uid }
        }
    }

    suspend fun getAllAvailableWorkers(): List<Worker> {
        return try {
            val docs = db.collection(Constants.WORKERS_COLLECTION)
                .whereEqualTo("isAvailable", true).get().await()
            if (docs.isEmpty) {
                hardcodedWorkers.filter { it.isAvailable }
            } else {
                docs.map { doc ->
                    Worker(
                        uid = doc.id,
                        name = doc.getString("name") ?: "",
                        phone = doc.getString("phone") ?: "",
                        skillType = doc.getString("skillType") ?: "",
                        dailyRate = (doc.getLong("dailyRate") ?: 0).toInt(),
                        locationCity = doc.getString("locationCity") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        profilePhotoUrl = doc.getString("profilePhotoUrl") ?: "",
                        isAvailable = doc.getBoolean("isAvailable") ?: true,
                        isVerified = doc.getBoolean("isVerified") ?: false,
                        rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                        reviewCount = (doc.getLong("reviewCount") ?: 0).toInt()
                    )
                }
            }
        } catch (e: Exception) {
            hardcodedWorkers.filter { it.isAvailable }
        }
    }

    suspend fun searchWorkers(query: String): List<Worker> {
        val lowerQuery = query.lowercase()
        return hardcodedWorkers.filter { worker ->
            worker.isAvailable && (
                worker.name.lowercase().contains(lowerQuery) ||
                worker.skillType.lowercase().contains(lowerQuery) ||
                worker.locationCity.lowercase().contains(lowerQuery) ||
                worker.skills.any { it.lowercase().contains(lowerQuery) } ||
                worker.bio.lowercase().contains(lowerQuery)
            )
        }
    }

    suspend fun searchWorkersBySkill(skillType: String): List<Worker> {
        return try {
            val docs = db.collection(Constants.WORKERS_COLLECTION)
                .whereEqualTo("skillType", skillType)
                .whereEqualTo("isAvailable", true).get().await()
            if (docs.isEmpty) {
                hardcodedWorkers.filter { it.isAvailable && it.skillType.equals(skillType, ignoreCase = true) }
            } else {
                docs.map { doc ->
                    Worker(
                        uid = doc.id,
                        name = doc.getString("name") ?: "",
                        skillType = doc.getString("skillType") ?: "",
                        dailyRate = (doc.getLong("dailyRate") ?: 0).toInt(),
                        locationCity = doc.getString("locationCity") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        profilePhotoUrl = doc.getString("profilePhotoUrl") ?: "",
                        isAvailable = true,
                        rating = (doc.getDouble("rating") ?: 0.0).toFloat()
                    )
                }
            }
        } catch (e: Exception) {
            hardcodedWorkers.filter { it.isAvailable && it.skillType.equals(skillType, ignoreCase = true) }
        }
    }

    suspend fun updateAvailability(uid: String, isAvailable: Boolean): Resource<Boolean> {
        return try {
            db.collection(Constants.WORKERS_COLLECTION).document(uid)
                .update("isAvailable", isAvailable).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Update failed")
        }
    }

    suspend fun uploadProfilePhoto(uid: String, uri: Uri): String? {
        return try {
            val ref = storage.reference.child("${Constants.PROFILE_PHOTOS_PATH}/$uid/profile.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) { null }
    }

    suspend fun uploadGalleryPhoto(uid: String, uri: Uri, index: Int): String? {
        return try {
            val ref = storage.reference.child("${Constants.WORK_GALLERY_PATH}/$uid/gallery_$index.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) { null }
    }
}
