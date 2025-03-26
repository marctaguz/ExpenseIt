package com.example.expenseit.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlin.coroutines.resume

object FirebaseUtils {
    suspend fun uploadImageToFirebase(imageUri: Uri): String? {
        return suspendCancellableCoroutine { continuation ->
            val storageRef = Firebase.storage.reference.child("receipts/${UUID.randomUUID()}")
            Log.d("uploadImageToFirebase", "Uploading image to Firebase")
            val uploadTask = storageRef.putFile(imageUri)

            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d("uploadImageToFirebase", "Download URL: $downloadUri")
                    continuation.resume(downloadUri.toString())
                }.addOnFailureListener { exception ->
                    Log.e("uploadImageToFirebase", "Failed to get download URL", exception)
                    continuation.resume(null)
                }
            }.addOnFailureListener { exception ->
                Log.e("uploadImageToFirebase", "Upload failed", exception)
                continuation.resume(null)
            }
            continuation.invokeOnCancellation {
                uploadTask.cancel()
            }
        }
    }
}