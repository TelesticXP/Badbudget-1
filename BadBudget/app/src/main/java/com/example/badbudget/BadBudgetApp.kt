/*
References:

1. Title: Get started with Cloud Firestore
   Author: Firebase
   Date: May 23, 2023
   URL: https://firebase.google.com/docs/firestore/quickstart
*/
package com.example.badbudget

import android.app.Application
import com.google.firebase.FirebaseApp
// application class to initialize firebase
class BadBudgetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
