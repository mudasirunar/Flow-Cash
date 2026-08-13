package com.mudasir.flowcash.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mudasir.flowcash.data.model.UserProfile
import com.mudasir.flowcash.util.UserAvatarUtils
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun getCurrentUserProfile(): UserProfile? {
        val fbUser = auth.currentUser ?: return null
        val displayName = fbUser.displayName
        val email = fbUser.email ?: ""
        val resolvedName = if (!displayName.isNullOrBlank()) displayName else {
            if (email.contains("@")) email.substringBefore("@").replaceFirstChar { it.uppercase() } else "User"
        }
        val avatarColorHex = UserAvatarUtils.getAvatarColorHexForUser(email, resolvedName)
        return UserProfile(
            id = fbUser.uid,
            name = resolvedName,
            email = email,
            profilePicUrl = fbUser.photoUrl?.toString(),
            avatarColorHex = avatarColorHex
        )
    }

    private suspend fun syncUserProfileToFirestore(fbUser: FirebaseUser, name: String, email: String): UserProfile {
        val colorHex = UserAvatarUtils.getAvatarColorHexForUser(email, name)
        val firestore = FirebaseFirestore.getInstance()

        var remoteSymbol = "Rs"
        var remoteCode = "PKR"
        try {
            val snapshot = firestore.collection("users").document(fbUser.uid).get().await()
            if (snapshot.exists()) {
                snapshot.getString("currencySymbol")?.let { if (it.isNotBlank()) remoteSymbol = it }
                snapshot.getString("currencyCode")?.let { if (it.isNotBlank()) remoteCode = it }
            }
        } catch (_: Exception) { }

        val userDoc = mapOf(
            "uid" to fbUser.uid,
            "displayName" to name,
            "email" to email,
            "photoUrl" to (fbUser.photoUrl?.toString() ?: ""),
            "avatarColorHex" to colorHex,
            "currencySymbol" to remoteSymbol,
            "currencyCode" to remoteCode,
            "updatedAt" to System.currentTimeMillis()
        )
        try {
            firestore.collection("users").document(fbUser.uid)
                .set(userDoc, SetOptions.merge()).await()
        } catch (_: Exception) {
            // Asynchronous fallback if offline
        }
        return UserProfile(
            id = fbUser.uid,
            name = name,
            email = email,
            profilePicUrl = fbUser.photoUrl?.toString(),
            avatarColorHex = colorHex,
            currencySymbol = remoteSymbol,
            currencyCode = remoteCode
        )
    }

    override suspend fun signUpWithEmail(
        firstName: String,
        lastName: String,
        email: String,
        pass: String
    ): Result<UserProfile> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val fbUser = authResult.user ?: return Result.failure(Exception("Registration failed: Null user"))
            
            val fullName = "${firstName.trim()} ${lastName.trim()}".trim()
            if (fullName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                fbUser.updateProfile(profileUpdates).await()
            }

            val resolvedName = if (fullName.isNotBlank()) fullName else email.substringBefore("@").replaceFirstChar { it.uppercase() }
            val userProfile = syncUserProfileToFirestore(fbUser, resolvedName, email)
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithEmail(email: String, pass: String): Result<UserProfile> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val fbUser = authResult.user ?: return Result.failure(Exception("Sign in failed: Null user"))
            
            val name = fbUser.displayName ?: if (email.contains("@")) email.substringBefore("@").replaceFirstChar { it.uppercase() } else "User"
            val userProfile = syncUserProfileToFirestore(fbUser, name, fbUser.email ?: email)
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithCredential(credential: AuthCredential): Result<UserProfile> {
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            val fbUser = authResult.user ?: return Result.failure(Exception("Google Sign-In failed: Null user"))
            
            val email = fbUser.email ?: ""
            val name = fbUser.displayName ?: if (email.contains("@")) email.substringBefore("@").replaceFirstChar { it.uppercase() } else "User"
            val userProfile = syncUserProfileToFirestore(fbUser, name, email)
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserCurrency(symbol: String, code: String) {
        val fbUser = auth.currentUser ?: return
        val firestore = FirebaseFirestore.getInstance()
        val updates = mutableMapOf<String, Any>(
            "currencySymbol" to symbol,
            "updatedAt" to System.currentTimeMillis()
        )
        if (code.isNotBlank()) {
            updates["currencyCode"] = code
        }
        try {
            firestore.collection("users").document(fbUser.uid)
                .set(updates, SetOptions.merge()).await()
        } catch (_: Exception) { }
    }

    override fun logout() {
        auth.signOut()
    }
}
