/*
References:

1. Title: Authenticate with Firebase using Password-Based Accounts on Android
   Author: Firebase
   Date: May 23, 2023
   URL: https://firebase.google.com/docs/auth/android/password-auth

2. Title: FirebaseAuth reference
   Author: Firebase
   Date: April 30, 2024
   URL: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth

3. Title: Toasts overview
   Author: Android Developers
   Date: January 3, 2024
   URL: https://developer.android.com/guide/topics/ui/notifiers/toasts

4. Title: Intents and intent filters
   Author: Android Developers
   Date: May 20, 2025
   URL: https://developer.android.com/guide/components/intents-filters
*/


package com.example.badbudget

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var checkBoxRememberMe: CheckBox
    private lateinit var buttonLogin: Button
    private lateinit var textViewForgotPassword: TextView
    private lateinit var textViewCreateAccount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase Auth instance
        auth = FirebaseAuth.getInstance()
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        checkBoxRememberMe = findViewById(R.id.checkboxRememberMe)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword)
        textViewCreateAccount = findViewById(R.id.textViewCreateAccount)

        // login button click
        buttonLogin.setOnClickListener {
            val email = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            buttonLogin.isEnabled = false
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    buttonLogin.isEnabled = true
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        val uid = auth.currentUser?.uid ?: ""
                        UserSession.init(this, uid)
                        GamificationManager.recordLogin(this)
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Invalid credentials.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // forgot password link
        textViewForgotPassword.setOnClickListener {
            val email = editTextUsername.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email to reset password.", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Reset link sent to $email", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // create new account link
        textViewCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
