
package com.varsitycollege.st10043352.opsc_clockit

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class CategoryActivityInsights : AppCompatActivity() {

    private var SessionData: Map<String, Any>? = null
    private lateinit var ActivityData: Map<String, Any>
    private lateinit var sharedPreferences: SharedPreferences
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var startDateMillis by Delegates.notNull<Long>()
    private var endDateMillis by Delegates.notNull<Long>()
    private var currentActivities: MutableList<String> = mutableListOf("")
    private var times: MutableList<String> = mutableListOf()

    private val database = FirebaseDatabase.getInstance("https://clockit-13d02-default-rtdb.europe-west1.firebasedatabase.app")
    private lateinit var allActivities: Map<String, Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_insights)

        // Retrieve the passed start and end dates from the intent
        startDateMillis = intent.getLongExtra("startDate", -1)
        endDateMillis = intent.getLongExtra("endDate", -1)

        // Convert the date millis to Date objects
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        startDate = if (startDateMillis != -1L) Date(startDateMillis) else null
        endDate = if (endDateMillis != -1L) Date(endDateMillis) else null

        // Find TextViews in the layout
        val txtStartDate = findViewById<TextView>(R.id.txtstartdate)
        val txtEndDate = findViewById<TextView>(R.id.txtenddate)

        // Display the selected start and end dates in the TextViews
        txtStartDate.text = "Start Date: ${startDate.let { dateFormat.format(it) } ?: "Not selected"}"
        txtEndDate.text = "End Date: ${endDate?.let { dateFormat.format(it) } ?: "Not selected"}"

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            // Navigate back to the home page
            val intent = Intent(this, Insights::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Clear the back stack
            startActivity(intent)
            finish() // Finish current activity
        }
    }

    override fun onResume() {
        super.onResume()
        fetchActivitiesFromFirebaseDatabase()
        fetchSessionsFromFirebaseDatabase()
    }

    @SuppressLint("RestrictedApi")
    private fun fetchActivitiesFromFirebaseDatabase() {
        val activitiesRef = database.getReference("activities")
        Log.d("CategoryActivityInsights", "Database reference: ${activitiesRef.path}")  // Log the database reference path

        activitiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    ActivityData = snapshot.children.associate { it.key!! to it.value as Any }
                    Log.d("ActivityData", "Activities data retrieved: $ActivityData")
                    updateUI()
                } else {
                    Log.d("CategoryActivityInsights", "No data found at the reference path")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CategoryActivityInsights", "Database error: ${error.message}")
            }
        })
        Log.d("CategoryActivityInsights", "Listener attached to database reference")  // Confirm the listener is attached
    }

    @SuppressLint("RestrictedApi")
    private fun fetchSessionsFromFirebaseDatabase() {
        val activitiesRef = database.getReference("logged_sessions")
        Log.d("CategoryActivityInsights", "Database reference: ${activitiesRef.path}")  // Log the database reference path

        activitiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    SessionData = snapshot.children.associate { it.key!! to it.value as Any }
                    Log.d("ActivityData", "Activities data retrieved: $SessionData")
                    updateUI()
                } else {
                    Log.d("CategoryActivityInsights", "No data found at the reference path")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CategoryActivityInsights", "Database error: ${error.message}")
            }
        })
        Log.d("CategoryActivityInsights", "Listener attached to database reference")  // Confirm the listener is attached
    }

    private fun formatActivityFirebaseData(activityData: Map<String, Any>): CharSequence {
        // Format the activity data as needed
        // Example:
        val name = activityData["activityName"]
        val category = activityData["categoryName"]
        val color = activityData["color"]
        val description = activityData["description"]
        val endTime = activityData["endTime"]
        val startTime = activityData["startTime"]
        val photoUrl = activityData["photoUrl"]
        return "$name,$category,$color,$description,$endTime,$startTime,$photoUrl"
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

    fun formatSharedPref(activity: String?): CharSequence? {
        var activityDetails = ""
        activity?.let {
            val values = activity.split(",")

            val name = values[0]

            activityDetails = "\t\t${name}"
        }
        return activityDetails
    }

    fun formatLogs(log: String?): List<String> {
        var logData: List<String> = emptyList()

        log?.let {
            logData = log.split(",")
        }

        return logData
    }

    fun formatActivity(log: String?): List<String> {
        var logData: List<String> = emptyList()

        log?.let {
            logData = log.split(",")
        }

        return logData
    }

    private fun updateUI() {
        // Remove previously added TextViews
        //findViewById<LinearLayout>(R.id.LinearActivities1).removeAllViews()

        allActivities = ActivityData
        var allSessions = SessionData

        // Remove previously added TextViews
        findViewById<LinearLayout>(R.id.LinearActivities2).removeAllViews()

        // Iterate through all Categories and calculate hours logged for each within the selected period
        val categoryTimeMap = mutableMapOf<String, Pair<Int, Int>>() // Pair of (hours, minutes)
        for ((key, value) in allActivities) {
            Log.d("CategoryActivityInsights", "Key: $key, Value: $value")

            // Check if the value is a Map
            if (value !is Map<*, *>) {
                Log.e("CategoryActivityInsights", "Value is not a Map: $value")
                continue
            }

            val activityData = formatActivityFirebaseData(value as Map<String, Any>)
            val activityInfo = formatActivity(activityData as String)
            val categoryName = activityInfo[1]

            // Initialize total hours and minutes for the category
            var totalHours = 0
            var totalMinutes = 0

            // Iterate through all logs to calculate total hours for the current category within the selected period
            if (allSessions != null) {
                for ((sessionKey, sessionValue) in allSessions) {
                    val logMap = mutableMapOf<String, Any>()
                    logMap[sessionKey] = sessionValue

                    val log = formatSessionFirebaseData(logMap, sessionKey)
                    val logData = formatLogs(log as String)

                    if (logData[4] != "null") {
                        val logDate = logData[3]
                        val logDateWithYear: String? =
                            logDate?.let { "$it/${Calendar.getInstance().get(Calendar.YEAR)}" }

                        if (!logDateWithYear.isNullOrEmpty()) {
                            val logDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val logDateFormatted = logDateFormat.parse(logDateWithYear)

                            // Check if the log date falls within the selected period
                            if (startDate != null && endDate != null && logDateFormatted != null) {
                                if (logDateFormatted >= startDate && logDateFormatted <= endDate) {
                                    val times = logData[5].split(":")

                                    val hours = times[0].toInt()
                                    val minutes = times[1].toInt()
                                    if (logData[1] == categoryName) {
                                        totalMinutes += hours * 60 + minutes
                                    }
                                }
                            }
                        } else {
                            Log.e("CategoryActivityInsights", "Log date is null or empty: $logDate")
                        }
                    }
                }
            }

            // Calculate total hours and remaining minutes for the current category
            totalHours = totalMinutes / 60
            totalMinutes %= 60

            // Add the category and its total hours to the map
            categoryTimeMap[categoryName] = Pair(totalHours, totalMinutes)
        }

        // Iterate through the categories and display only those with logged activities within the selected period
        for ((categoryName, categoryTime) in categoryTimeMap) {
            val hours = categoryTime.first
            val minutes = categoryTime.second

            // Check if hours and minutes are both 0, if so, skip adding the TextView
            if (hours == 0 && minutes == 0) {
                continue
            }

            // Create TextView for the current category
            val CategoryTextView = TextView(this)
            CategoryTextView.text = "$categoryName\t\t\t\t$hours hours $minutes minutes"
            CategoryTextView.setTextColor(Color.WHITE)
            CategoryTextView.setTextSize(20f)
            CategoryTextView.setBackgroundResource(R.drawable.round_buttons)
            CategoryTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            CategoryTextView.visibility = View.VISIBLE
            CategoryTextView.layoutParams = LinearLayout.LayoutParams(
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

            // Add the TextView to the appropriate LinearLayout
            findViewById<LinearLayout>(R.id.LinearActivities2).addView(CategoryTextView)
        }

        // Iterate through all activities and create TextViews
        if (allSessions != null) {
            for ((sessionKey, sessionValue) in allSessions) {
                val logMap = mutableMapOf<String, Any>()
                logMap[sessionKey] = sessionValue

                val log = formatSessionFirebaseData(logMap, sessionKey)
                val logData = formatLogs(log as String)

                if (logData[4] != "null") {
                    var logDate = logData[3]
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val logDateWithYear = "$logDate/$currentYear"
                    val logDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val logDateFormatted = logDateFormat.parse(logDateWithYear)

                    if (startDate != null && endDate != null && logDateFormatted != null) {
                        if (logDateFormatted >= startDate && logDateFormatted <= endDate) {
                            for ((key, value) in allActivities) {
                                Log.d("CategoryActivityInsights", "Key: $key, Value: $value")

                                // Check if the value is a Map
                                if (value !is Map<*, *>) {
                                    Log.e("CategoryActivityInsights", "Value is not a Map: $value")
                                    continue
                                }

                                val activityData = formatActivityFirebaseData(value as Map<String, Any>)
                                val activityInfo = formatActivity(activityData as String)
                                val activityTextView = TextView(this)
                                if (!currentActivities.contains(activityInfo[0])) {
                                    if (logData[0] == activityInfo[0]) {
                                        currentActivities.add(activityInfo[0])

                                        activityTextView.text = formatSharedPref(activityData)
                                        activityTextView.setTextColor(Color.WHITE)
                                        activityTextView.setTextSize(20f)
                                        activityTextView.setBackgroundResource(R.drawable.round_buttons)
                                        activityTextView.textAlignment =
                                            View.TEXT_ALIGNMENT_TEXT_START
                                        activityTextView.visibility = View.VISIBLE
                                        activityTextView.layoutParams =
                                            LinearLayout.LayoutParams(
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
                                        activityTextView.setOnClickListener {
                                            val intent = Intent(this, ActivityInfo1::class.java)
                                            intent.putExtra("activityName", activityInfo[0])
                                            intent.putExtra("description", activityInfo[3])
                                            intent.putExtra("category", activityInfo[1])
                                            intent.putExtra("startTime", activityInfo[5])
                                            intent.putExtra("endTime", activityInfo[4])
                                            if (!activityInfo[6].equals("null")) {
                                                intent.putExtra("photoUri", activityInfo[6])
                                            } else {
                                                intent.putExtra("photoUri", "")
                                            }
                                            startActivity(intent)
                                        }

                                        findViewById<LinearLayout>(R.id.LinearActivities1).addView(
                                            activityTextView
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
