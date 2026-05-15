package com.nammakelsa.core.utils

object Constants {
    const val USERS_COLLECTION = "users"
    const val WORKERS_COLLECTION = "workers"
    const val BOOKINGS_COLLECTION = "bookings"
    const val REVIEWS_COLLECTION = "reviews"
    const val CHATS_COLLECTION = "chats"
    const val MESSAGES_COLLECTION = "messages"
    const val NOTIFICATIONS_COLLECTION = "notifications"

    const val PROFILE_PHOTOS_PATH = "profile_photos"
    const val WORK_GALLERY_PATH = "work_gallery"
    const val CHAT_IMAGES_PATH = "chat_images"
    const val VERIFICATION_DOCS_PATH = "verification_docs"

    const val ROLE_WORKER = "worker"
    const val ROLE_CUSTOMER = "customer"
    const val ROLE_ADMIN = "admin"

    const val BOOKING_PENDING = "pending"
    const val BOOKING_ACCEPTED = "accepted"
    const val BOOKING_REJECTED = "rejected"
    const val BOOKING_COMPLETED = "completed"
    const val BOOKING_CANCELLED = "cancelled"

    val SKILL_TYPES = arrayOf(
        "Painter", "Tiler", "Gardener", "Plumber", 
        "Electrician", "Carpenter", "Mechanic", "Driver", "Cleaner"
    )
}
