
package com.varsitycollege.st10043352.opsc_clockit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LogHours : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var activityRef: DatabaseReference

    private var details: List<String> = listOf("")
    private var activityData: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_hours)

        database = FirebaseDatabase.getInstance("https://clockit-13d02-default-rtdb.europe-west1.firebasedatabase.app")
        activityRef = database.getReference("activities")

        activityData = intent.getStringExtra("activityData") ?: ""
        val activityTextView = findViewById<TextView>(R.id.txtActivity1)
        val categoryTextView = findViewById<TextView>(R.id.txtCategory1)
        val dailyGoalsTextView = findViewById<TextView>(R.id.textView11) // TextView for daily goals
        val button = findViewById<Button>(R.id.btnLogHours)

        // Split the activity details
        details = activityData.split(",")
        if (details.size >= 3) { // Ensure we have at least three elements (name, description, category)
            val activityName = details[0] // Activity Name
            val categoryName = details[2] // Category Name

            activityTextView.text = activityName
            categoryTextView.text = categoryName
            categoryTextView.setTextColor((details.get(3)).toInt())

            // Fetch and display goals for the activity
            fetchGoalsForActivity(activityName, dailyGoalsTextView)

            button.setOnClickListener{
                val intent = Intent(this, SessionLog::class.java)
                intent.putExtra("activity", activityData)
                startActivity(intent)
            }
        }

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            // Navigate back to the home page
            val intent = Intent(this, home_page::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Clear the back stack
            startActivity(intent)
            finish() // Finish current activity
        }
    }

    fun DeleteOnClick(view: View) {
        val activityName = details[0]

        activityRef.orderByChild("activityName").equalTo(activityName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (activitySnapshot in snapshot.children) {
                        activitySnapshot.ref.removeValue()
                        Log.d("Delete", "activity deleted")
                        Toast.makeText(this@LogHours, "Activity Deleted", Toast.LENGTH_SHORT).show()
                        finish()
                        break
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Delete", "Error deleting activity: ${error.message}")
                }
            })
    }

    private fun fetchGoalsForActivity(activityName: String, dailyGoalsTextView: TextView) {
        val goalsRef = database.getReference("goals")
        goalsRef.child(activityName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val minGoal = snapshot.child("min_goal").getValue(String::class.java)
                val maxGoal = snapshot.child("max_goal").getValue(String::class.java)

                val dailyGoalsText = StringBuilder()
                if (!minGoal.isNullOrEmpty()) {
                    dailyGoalsText.append("Minimum goal: $minGoal\n")
                }

                if (!maxGoal.isNullOrEmpty()) {
                    dailyGoalsText.append("Maximum goal: $maxGoal")
                }

                dailyGoalsTextView.text = dailyGoalsText.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchGoals", "Error fetching goals: ${error.message}")
            }
        })
    }
}
