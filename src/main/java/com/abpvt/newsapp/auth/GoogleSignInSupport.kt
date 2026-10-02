package com.abpvt.newsapp.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.abpvt.newsapp.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/** Uses the web OAuth client generated from google-services.json when no override is supplied. */
internal object GoogleSignInSupport {
    fun webClientId(context: Context): String {
        val override = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        if (override.endsWith(".apps.googleusercontent.com")) return override

        val id = context.resources.getIdentifier(
            "default_web_client_id", "string", context.packageName
        )
        return if (id != 0) context.getString(id).trim() else ""
    }

    suspend fun idToken(context: Context): String {
        val clientId = webClientId(context)
        check(clientId.isNotEmpty()) {
            "Google sign-in is not configured. Add the Firebase google-services.json for this app."
        }
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val credential = CredentialManager.create(context)
            .getCredential(context = context, request = request).credential
        check(credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "Google did not return an ID token. Please try another account." }
        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }
}
