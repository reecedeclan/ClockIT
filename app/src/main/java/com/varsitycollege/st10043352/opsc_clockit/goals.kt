
package com.varsitycollege.st10043352.opsc_clockit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class goals : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance("https://clockit-13d02-default-rtdb.europe-west1.firebasedatabase.app")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)
        fetchActivitiesFromFirebaseDatabase()
    }

    private fun fetchActivitiesFromFirebaseDatabase() {
        val activitiesRef = database.getReference("activities")

        activitiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Remove previously added TextViews
                findViewById<LinearLayout>(R.id.LinearActivities1).removeAllViews()

                for (activitySnapshot in snapshot.children) {
                    val activityData = activitySnapshot.value as Map<String, Any>
                    val activityTextView = TextView(this@goals)

                    activityTextView.text = formatFirebaseData(activityData)
                    activityTextView.setTextColor(Color.WHITE)
                    activityTextView.setTextSize(20f)
                    activityTextView.setBackgroundResource(R.drawable.round_buttons)
                    activityTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    activityTextView.visibility = View.VISIBLE // Make the TextView visible
                    activityTextView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT // Change to WRAP_CONTENT
                    ).apply {
                        setMargins(
                            resources.getDimensionPixelSize(R.dimen.activity_box_margin_start),
                            resources.getDimensionPixelSize(R.dimen.activity_box_margin_top),
                            resources.getDimensionPixelSize(R.dimen.activity_box_margin_end),
                            resources.getDimensionPixelSize(R.dimen.activity_box_margin_bottom)
                        )
                    }
                    activityTextView.setOnClickListener {
                        val intent = Intent(this@goals, AddGoal::class.java)
                        intent.putExtra("activityData", formatFirebaseData1(activityData)) // Pass activityData directly
                        startActivity(intent)
                    }

                    findViewById<LinearLayout>(R.id.LinearActivities1).addView(activityTextView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Log.e("goals", "Database error: ${error.message}")
            }
        })
    }

    private fun formatFirebaseData(activityData: Map<String, Any>): CharSequence {
        // Format the activity data as needed
        // Example:
        val name = activityData["activityName"]
        val category = activityData["categoryName"]
        val startTime = activityData["startTime"]
        val endTime = activityData["endTime"]

        return "$name\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t$category\n$startTime\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t$endTime"
    }

    private fun formatFirebaseData1(activityData: Map<String, Any>): String {
        // Format the activity data for passing to the next activity
        val name = activityData["activityName"]
        val category = activityData["categoryName"]
        val color = activityData["color"]

        return "$name, ,$category,$color"
    }

    // Functions for navigation
    fun navInsights(view: View) {
        startActivity(Intent(this, Insights::class.java))
    }

    fun navStats(view: View) {
        startActivity(Intent(this, stats::class.java))
    }

    fun navFun(view: View) {
        startActivity(Intent(this, FunTime::class.java))
    }

    fun navHome(view: View) {
        startActivity(Intent(this, home_page::class.java))
    }
}
