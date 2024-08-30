
package com.varsitycollege.st10043352.opsc_clockit

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class ViewAllSessions2 : AppCompatActivity() {
    private var SessionData: Map<String, Any>? = null
    private lateinit var ActivityData: Map<String, Any>
    private val database = FirebaseDatabase.getInstance("https://clockit-13d02-default-rtdb.europe-west1.firebasedatabase.app")
    private lateinit var allActivities: Map<String, Any>
    private lateinit var photos: Array<String>

    private var activityName: String? = null
    private var category: String? = null
    private var photo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_sessions2)

        activityName = intent.getStringExtra("activityName")
        category = intent.getStringExtra("category")
        val color = intent.getStringExtra("color")

        val txtActivity = findViewById<TextView>(R.id.actName)
        val txtCategory = findViewById<TextView>(R.id.Category)

        txtActivity.text = activityName
        txtCategory.text = category

        Log.e("color", "$color")
        if (color != null) {
            txtCategory.setTextColor(color.toInt())
            }
    }

    private fun timeStringToFloat(time: String): Float {
        val (hoursString, minutesString) = time.split(":")
        val hours = hoursString.toFloat()
        val minutes = minutesString.toFloat()
        return hours + minutes / 60
    }

    override fun onResume() {
        super.onResume()
        photos = emptyArray()
        fetchActivitiesFromFirebaseDatabase()
        fetchSessionsFromFirebaseDatabase()
    }

    private fun updateUI() {
        val linearLayout = findViewById<LinearLayout>(R.id.LinearActivities1)
        linearLayout.removeAllViews()

        allActivities = ActivityData
        val allSessions = SessionData

        // Iterate through all activities + sessions and create TextViews
        if (allSessions != null) {
            for ((sessionKey, sessionValue) in allSessions) {
                val logMap = mutableMapOf<String, Any>()
                logMap[sessionKey] = sessionValue

                val log = formatSessionFirebaseData(logMap, sessionKey)
                val logData = formatLogs(log as String)

                if (logData[4] != "null") {
                    val logDate = logData[3]
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val logDateWithYear = "$logDate/$currentYear"
                    val logDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val logDateFormatted = logDateFormat.parse(logDateWithYear)


                            if (logData[0] == intent.getStringExtra("activityName")) {

                                val activityTextView = TextView(this)

                                val time = logData[5]
                                val totalHours = timeStringToFloat(time)
                                val hours = totalHours.toInt()
                                val minutes = ((totalHours - hours) * 60).toInt()
                                val formattedTime = "${hours} Hours ${minutes} Minutes"


                                photo = logData[4]
                                val color = logData[2]
                                photos += ("$photo,$formattedTime,$activityName")

                                activityTextView.text = formatSharedPref(formattedTime)
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
                                activityTextView.setOnClickListener {
                                    val intent = Intent(this, ViewLog::class.java)
                                    intent.putExtra("photos", photos)
                                    intent.putExtra("activityName", activityName)
                                    intent.putExtra("category", category)
                                    intent.putExtra("color", color)
                                    intent.putExtra("time", formattedTime)

                                    startActivity(intent)
                                }

                                linearLayout.addView(activityTextView)
                            }
                        }


            }
        }

    }

    @SuppressLint("RestrictedApi")
    private fun fetchActivitiesFromFirebaseDatabase() {
        val activitiesRef = database.getReference("activities")
        Log.d("CategoryActivityInsights", "Database reference: ${activitiesRef.path}")

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
        Log.d("CategoryActivityInsights", "Listener attached to database reference")
    }

    @SuppressLint("RestrictedApi")
    private fun fetchSessionsFromFirebaseDatabase() {
        val activitiesRef = database.getReference("logged_sessions")
        Log.d("CategoryActivityInsights", "Database reference: ${activitiesRef.path}")

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
        Log.d("CategoryActivityInsights", "Listener attached to database reference")
    }

    private fun formatActivityFirebaseData(activityData: Map<String, Any>): CharSequence {
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
        val nestedMap = sessionData[sessionKey] as? Map<String, Any>

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

    fun back2(view: View) {
        finish()
    }
}
