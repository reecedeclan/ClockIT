
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

class home_page : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance("https://clockit-13d02-default-rtdb.europe-west1.firebasedatabase.app")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
    }

    override fun onResume() {
        super.onResume()
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
                    val activityTextView = TextView(this@home_page)

                    val activityData1 = formatFirebaseData1(activityData)

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
                        val intent = Intent(this@home_page, LogHours::class.java)
                        intent.putExtra("activityData", activityData1) // Convert to String if needed
                        startActivity(intent)
                    }

                    findViewById<LinearLayout>(R.id.LinearActivities1).addView(activityTextView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Log.e("home_page", "Database error: ${error.message}")
            }
        })
    }

    private fun formatFirebaseData1(activityData: Map<String, Any>): CharSequence {
        // Format the activity data as needed
        // Example:
        val name = activityData["activityName"]
        val category = activityData["categoryName"]
        val color = activityData["color"]

        return "$name, ,$category,$color"
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

    fun navViewAllSessions(view: View){
        startActivity(Intent(this, ViewAllSessions::class.java))
    }

    // Functions for navigation
    fun navAddCategory(view: View) {
        startActivity(Intent(this, Add_Category::class.java))
    }

    fun navAddActivity(view: View) {
        startActivity(Intent(this, AddActivity::class.java))
    }

    fun navInsights(view: View) {
        startActivity(Intent(this, Insights::class.java))
    }

    fun navStats(view: View) {
        startActivity(Intent(this, stats::class.java))
    }

    fun navGoals(view: View) {
        startActivity(Intent(this, goals::class.java))
    }

    fun navFun(view: View) {
        startActivity(Intent(this, FunTime::class.java))
    }

    fun navHome(view: View) {}
}
