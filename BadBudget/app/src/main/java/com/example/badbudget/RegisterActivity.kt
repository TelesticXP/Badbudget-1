package com.example.badbudget

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var backButton: ImageView
    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonCreateAccount: Button
    private lateinit var textViewBackToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        backButton = findViewById(R.id.backButton)
        editTextUsername        = findViewById(R.id.editTextUsernameRegister)
        editTextEmail           = findViewById(R.id.editTextEmailRegister)
        editTextPassword        = findViewById(R.id.editTextPasswordRegister)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPasswordRegister)
        buttonCreateAccount     = findViewById(R.id.buttonCreateAccount)
        textViewBackToLogin     = findViewById(R.id.textViewBackToLogin)

        // Back arrow
        backButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Create Account
        buttonCreateAccount.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val email    = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirm  = editTextConfirmPassword.text.toString().trim()

            when {
                username.isEmpty() ||
                        email.isEmpty() ||
                        password.isEmpty() ||
                        confirm.isEmpty() -> {
                    Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                }
                password != confirm -> {
                    Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    buttonCreateAccount.isEnabled = false
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            buttonCreateAccount.isEnabled = true
                            if (task.isSuccessful) {
                                // Display name
                                auth.currentUser?.updateProfile(
                                    UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build()
                                )
                                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Registration failed: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
        }

        // Back to login screen
        textViewBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
