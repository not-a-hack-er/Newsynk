package com.abpvt.newsapp

import android.app.Application
import com.google.firebase.FirebaseApp

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase SDK
        FirebaseApp.initializeApp(this)
    }
}
