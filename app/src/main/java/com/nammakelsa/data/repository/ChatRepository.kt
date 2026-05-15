package com.nammakelsa.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.nammakelsa.core.utils.Constants
import com.nammakelsa.core.utils.Resource
import com.nammakelsa.domain.models.Chat
import com.nammakelsa.domain.models.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }

    fun getChatsFlow(userId: String): Flow<List<Chat>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection(Constants.CHATS_COLLECTION)
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val chats = snapshot?.documents?.map { doc ->
                    Chat(
                        id = doc.id,
                        participants = (doc.get("participants") as? List<String>) ?: emptyList(),
                        lastMessage = doc.getString("lastMessage") ?: "",
                        lastMessageTime = doc.getLong("lastMessageTime") ?: 0,
                        unreadCount = (doc.getLong("unreadCount") ?: 0).toInt()
                    )
                }?.sortedByDescending { it.lastMessageTime } ?: emptyList()
                trySend(chats)
            }
        awaitClose { listener.remove() }
    }

    fun getMessagesFlow(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = db.collection(Constants.CHATS_COLLECTION).document(chatId)
            .collection(Constants.MESSAGES_COLLECTION)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val messages = snapshot?.documents?.map { doc ->
                    Message(
                        id = doc.id,
                        chatId = chatId,
                        senderId = doc.getString("senderId") ?: "",
                        receiverId = doc.getString("receiverId") ?: "",
                        text = doc.getString("text") ?: "",
                        imageUrl = doc.getString("imageUrl"),
                        timestamp = doc.getLong("timestamp") ?: 0,
                        isRead = doc.getBoolean("isRead") ?: false
                    )
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(senderId: String, receiverId: String, text: String, imageUrl: String? = null): Resource<Message> {
        return try {
            val chatId = getChatId(senderId, receiverId)
            val chatRef = db.collection(Constants.CHATS_COLLECTION).document(chatId)
            val messagesRef = chatRef.collection(Constants.MESSAGES_COLLECTION).document()
            val message = Message(
                id = messagesRef.id,
                chatId = chatId,
                senderId = senderId,
                receiverId = receiverId,
                text = text,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis()
            )
            messagesRef.set(message).await()
            chatRef.set(mapOf(
                "participants" to listOf(senderId, receiverId),
                "lastMessage" to text,
                "lastMessageTime" to System.currentTimeMillis()
            ), com.google.firebase.firestore.SetOptions.merge()).await()
            Resource.Success(message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send message")
        }
    }

    suspend fun markAsRead(chatId: String, userId: String) {
        try {
            db.collection(Constants.CHATS_COLLECTION).document(chatId)
                .update("unreadCount", 0).await()
        } catch (e: Exception) { }
    }

    suspend fun uploadChatImage(chatId: String, uri: Uri): String? {
        return try {
            val ref = storage.reference.child("${Constants.CHAT_IMAGES_PATH}/$chatId/${System.currentTimeMillis()}.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) { null }
    }
}
