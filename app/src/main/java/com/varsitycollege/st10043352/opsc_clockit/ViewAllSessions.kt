
package com.varsitycollege.st10043352.opsc_clockit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ViewAllSessions : AppCompatActivity() {
    private var SessionData: Map<String, Any>? = null
    private lateinit var ActivityData: Map<String, Any>
    private lateinit var logData: List<String>
    private var times: MutableList<String> = mutableListOf()

    private val database = FirebaseDatabase.getInstance("https://clockit-13d02-default-rtdb.europe-west1.firebasedatabase.app")
    private lateinit var allActivities: Map<String, Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_sessions)


        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            // Navigate back to the home page
            val intent = Intent(this, home_page::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Clear the back stack
            startActivity(intent)
            finish() // Finish current activity
        }
        fetchActivitiesFromFirebaseDatabase()
        fetchSessionsFromFirebaseDatabase()
    }

    override fun onResume() {
        super.onResume()
        fetchActivitiesFromFirebaseDatabase()
        fetchSessionsFromFirebaseDatabase()
    }

    private fun fetchActivitiesFromFirebaseDatabase() {
        val activitiesRef = database.getReference("activities")

        activitiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    allActivities = snapshot.children.associate { it.key!! to it.value as Any }
                    Log.d("ActivityData", "Activities data retrieved: $allActivities")
                    // Now that allActivities is populated, call updateUI
                    updateUI()
                } else {
                    Log.d("staticsticsPeriod", "No data found at the reference path")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("staticsticsPeriod", "Database error: ${error.message}")
            }
        })
    }

    private fun fetchSessionsFromFirebaseDatabase() {
        val activitiesRef = database.getReference("logged_sessions")

        activitiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    SessionData = snapshot.children.associate { it.key!! to it.value as Any }
                    Log.d("SessionData", "Sessions data retrieved: $SessionData")
                    updateUI()
                } else {
                    Log.d("staticsticsPeriod", "No data found at the reference path")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("staticsticsPeriod", "Database error: ${error.message}")
            }
        })
    }

    private fun formatSessionFirebaseData(sessionData: Map<String, Any>, sessionKey: String): CharSequence {
        // Access the nested Map using the session key
        val nestedMap = sessionData[sessionKey] as? Map<String, Any>

        // Access values using safe casting
        val name = nestedMap?.get("activityName") as? String ?: ""
        val category = nestedMap?.get("categoryName") as? String ?: ""
        val color = nestedMap?.get("categoryColor") as? String ?: ""
        val date = nestedMap?.get("date") as? String ?: ""
        val photoUri = nestedMap?.get("imageUrl") as? String ?: ""
        val time = nestedMap?.get("time") as? String ?: ""

        return "$name,$category,$color,$date,$photoUri,$time"
    }

    fun formatLogs(log: String?): List<String> {
        var logData: List<String> = emptyList()
        log?.let {
            logData = log.split(",")
        }
        return logData
    }

    private fun updateUI() {
        // Remove previously added TextViews
        findViewById<LinearLayout>(R.id.LinearActivities1).removeAllViews()

        ActivityData = allActivities

        // Keep track of already displayed activities to avoid duplicates
        val displayedActivities = mutableSetOf<String>()

        // Iterate through all sessions and calculate statistics
        SessionData?.forEach { (sessionKey, sessionValue) ->
            val logMap = mutableMapOf<String, Any>()
            logMap[sessionKey] = sessionValue

            val log = formatSessionFirebaseData(logMap, sessionKey)
            logData = formatLogs(log as String)

            // Check if the session falls within the selected date range

                    val activityName = logData[0]

                    // Check if the activity has already been displayed
                    if (!displayedActivities.contains(activityName)) {
                        displayedActivities.add(activityName)

                        val activityCategory = logData[1]
                        val activityColor = logData[2]

                        // Update statistics for the activity
                        val time = logData[5]
                        val (hoursString, minutesString) = time.split(":")
                        val hours = hoursString.toInt()
                        val minutes = minutesString.toInt()
                        val formattedTime = "${hours} Hours ${minutes} Minutes"

                        activityStatistics(activityName, formattedTime, activityCategory, activityColor)
                    }


        }
    }        // Display statistics for each activity
    private fun activityStatistics(activityName : String, activityTime: String, activityCategory:String, activityColor : String) {

        // Create TextView for the activity with its statistics
        val activityTextView = TextView(this)
        activityTextView.text = activityName
        activityTextView.setTextColor(Color.WHITE)
        activityTextView.setTextSize(20f)
        activityTextView.setBackgroundResource(R.drawable.round_buttons)
        activityTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        activityTextView.visibility = View.VISIBLE
        activityTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.activity_box_height)
        ).apply {
            setMargins(
                resources.getDimensionPixelSize(R.dimen.activity_box_margin_start),
                resources.getDimensionPixelSize(R.dimen.activity_box_margin_top),
                resources.getDimensionPixelSize(R.dimen.activity_box_margin_end),
                resources.getDimensionPixelSize(R.dimen.activity_box_margin_bottom)
            )
        }

        // Set click listener to view more details about the activity
        activityTextView.setOnClickListener {
            val intent = Intent(this, ViewAllSessions2::class.java)
            intent.putExtra("activityName", activityName)
            intent.putExtra("category", activityCategory)
            intent.putExtra("color", activityColor)
            intent.putExtra("time", activityTime)

            startActivity(intent)
        }

        // Add the TextView to the layout
        findViewById<LinearLayout>(R.id.LinearActivities1).addView(activityTextView)
    }
}
