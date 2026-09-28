package com.abpvt.newsapp.data.repository


import com.abpvt.newsapp.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /** Sign up a new user with email and password. */
    suspend fun signUp(email: String, password: String): Resource<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Resource.Success(user.uid)
            } else {
                Resource.Error("Sign up failed: user is null")
            }
        } catch (e: FirebaseAuthException) {
            // FirebaseAuthException contains the error code/message.
            Resource.Error(e.message ?: "Sign up failed")
        } catch (e: Exception) {
            // Catch any other exception (e.g., network errors).
            Resource.Error(e.message ?: "Sign up failed")
        }
    }

    /** Sign in an existing user with email and password. */
    suspend fun signIn(email: String, password: String): Resource<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Resource.Success(user.uid)
            } else {
                Resource.Error("Sign in failed: user is null")
            }
        } catch (e: FirebaseAuthException) {
            Resource.Error(e.message ?: "Sign in failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign in failed")
        }
    }

    /** Returns the currently signed-in user’s UID, or null if none. */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /** Sign out the current user. */
    suspend fun signOut(): Resource<Boolean> {
        return try {
            auth.signOut()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign out failed")
        }
    }
}
