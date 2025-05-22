package com.example.badbudget

import android.app.Application
import com.google.firebase.FirebaseApp

class BadBudgetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
