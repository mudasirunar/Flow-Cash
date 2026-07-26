package com.mudasir.flowcash.data.repository

import com.mudasir.flowcash.data.model.UserProfile
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    fun getCurrentUser(): FirebaseUser?
    fun getCurrentUserProfile(): UserProfile?
    suspend fun signUpWithEmail(firstName: String, lastName: String, email: String, pass: String): Result<UserProfile>
    suspend fun loginWithEmail(email: String, pass: String): Result<UserProfile>
    suspend fun signInWithCredential(credential: AuthCredential): Result<UserProfile>
    fun logout()
}
