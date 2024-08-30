
package com.varsitycollege.st10043352.opsc_clockit

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)

        val usernameEditText = findViewById<EditText>(R.id.txtUsername)
        val passwordEditText = findViewById<EditText>(R.id.txtpassword)
        val loginButton = findViewById<Button>(R.id.btnRegister)
        val createAccountTextView = findViewById<TextView>(R.id.create)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username or password cannot be blank", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(username, password)
            }
        }

        createAccountTextView.setOnClickListener {
            navigateToRegisterPage()
        }
    }

    private fun loginUser(username: String, password: String) {
        val database = FirebaseDatabase.getInstance("https://clockit-13d02-default-rtdb.europe-west1.firebasedatabase.app/")
        val usernameRef = database.getReference("usernames").child(username)

        usernameRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = task.result.getValue(String::class.java)
                if (userId != null) {
                    val userRef = database.getReference("users").child(userId)
                    userRef.child("email").get().addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            val email = emailTask.result.getValue(String::class.java)
                            if (email != null) {
                                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { signInTask ->
                                    if (signInTask.isSuccessful) {
                                        Log.d("MainActivity", "Login successful")
                                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, home_page::class.java))
                                        finish()
                                    } else {
                                        Log.e("MainActivity", "Login failed", signInTask.exception)
                                        Toast.makeText(this, "Invalid username or password!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Failed to retrieve email for the username", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.e("MainActivity", "Failed to retrieve email", emailTask.exception)
                            Toast.makeText(this, "Failed to retrieve email: ${emailTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Invalid username", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("MainActivity", "Failed to retrieve username mapping", task.exception)
                Toast.makeText(this, "Failed to retrieve username mapping: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToRegisterPage() {
        startActivity(Intent(this, register_page::class.java))
    }
}
