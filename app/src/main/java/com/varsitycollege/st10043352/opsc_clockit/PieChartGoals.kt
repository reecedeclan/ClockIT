
package com.varsitycollege.st10043352.opsc_clockit

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class PieChartGoals : AppCompatActivity() {
    private var SessionData: Map<String, Any>? = null
    private lateinit var ActivityData: Map<String, Any>
    private val database = FirebaseDatabase.getInstance("https://clockit-13d02-default-rtdb.europe-west1.firebasedatabase.app")
    private lateinit var allActivities: Map<String, Any>
    private var activityList: List<String> = mutableListOf("")
    private lateinit var photos: Array<String>

    private lateinit var sharedPreferences: SharedPreferences
    private var activityName: String? = null
    private var category: String? = null
    private var photo: String? = null
    private var minGoal: Float = 0f
    private var maxGoal: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pie_chart_goals)

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

        sharedPreferences = getSharedPreferences("CategoryPreferences", MODE_PRIVATE)

        // Fetch daily goals
        fetchGoalsForActivity(activityName ?: "", findViewById(R.id.textView11))
    }

    private fun timeStringToFloat(time: String): Float {
        val (hoursString, minutesString) = time.split(":")
        val hours = hoursString.toFloat()
        val minutes = minutesString.toFloat()
        return hours + minutes / 60
    }

    private fun fetchGoalsForActivity(activityName: String, dailyGoalsTextView: TextView) {
        val goalsRef = database.getReference("goals")
        goalsRef.child(activityName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val minGoalStr = snapshot.child("min_goal").getValue(String::class.java)
                val maxGoalStr = snapshot.child("max_goal").getValue(String::class.java)

                if (minGoalStr.isNullOrEmpty() && maxGoalStr.isNullOrEmpty()) {
                    dailyGoalsTextView.text = "No goals have been set"
                } else {
                    val minGoalText = if (!minGoalStr.isNullOrEmpty()) {
                        "Min goal: $minGoalStr\t\t\t\t\t"
                    } else {
                        ""
                    }

                    val maxGoalText = if (!maxGoalStr.isNullOrEmpty()) {
                        "Max goal: $maxGoalStr"
                    } else {
                        ""
                    }

                    val spannableString = SpannableString("$minGoalText$maxGoalText")

                    if (!minGoalStr.isNullOrEmpty()) {
                        val minGoalStart = 0
                        val minGoalEnd = minGoalText.length
                        spannableString.setSpan(ForegroundColorSpan(Color.WHITE), minGoalStart, minGoalEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        minGoal = timeStringToFloat(minGoalStr)
                    } else {
                        minGoal = 0f
                    }

                    if (!maxGoalStr.isNullOrEmpty()) {
                        val maxGoalStart = spannableString.indexOf(maxGoalText)
                        val maxGoalEnd = maxGoalStart + maxGoalText.length
                        spannableString.setSpan(ForegroundColorSpan(Color.WHITE), maxGoalStart, maxGoalEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        maxGoal = timeStringToFloat(maxGoalStr)
                    } else {
                        maxGoal = 0f
                    }

                    dailyGoalsTextView.text = spannableString

                    // Fetch logs and calculate percentages
                    fetchLogsAndCalculatePercentages()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchGoals", "Error fetching goals: ${error.message}")
            }
        })
    }

    private fun fetchLogsAndCalculatePercentages() {
        val sessionsRef = database.getReference("logged_sessions")
        sessionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val logsData = mutableMapOf<String, Float>()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                // Get the current date and set the start and end dates to the first and last days of the current month
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startOfMonth = calendar.time

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val endOfMonth = calendar.time

                for (session in snapshot.children) {
                    val sessionMap = session.value as? Map<String, Any> ?: continue
                    val activityName = sessionMap["activityName"] as? String ?: continue
                    val date = sessionMap["date"] as? String ?: continue
                    val time = sessionMap["time"] as? String ?: continue

                    if (activityName == this@PieChartGoals.activityName) {
                        try {
                            val logDate = dateFormat.parse("$date/${Calendar.getInstance().get(Calendar.YEAR)}")

                            if (logDate != null && logDate in startOfMonth..endOfMonth) {
                                val totalHours = timeStringToFloat(time)
                                logsData[date] = logsData.getOrDefault(date, 0f) + totalHours
                            }
                        } catch (e: ParseException) {
                            Log.e("DateParse", "Error parsing date: $date", e)
                        }
                    }
                }

                calculatePercentagesAndDisplay(logsData)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchLogs", "Error fetching logs: ${error.message}")
            }
        })
    }

    private fun calculatePercentagesAndDisplay(logsData: Map<String, Float>) {
        var withinGoals = 0
        var overGoals = 0
        var underGoals = 0

        logsData.values.forEach { hours ->
            when {
                hours < minGoal -> underGoals++
                hours > maxGoal -> overGoals++
                else -> withinGoals++
            }
        }

        val totalLogs = logsData.size
        if (totalLogs == 0) {
            Log.e("NoData", "No logs found within the specified date range")
            return
        }

        val withinGoalsPercentage = (withinGoals.toFloat() / totalLogs) * 100
        val overGoalsPercentage = (overGoals.toFloat() / totalLogs) * 100
        val underGoalsPercentage = (underGoals.toFloat() / totalLogs) * 100

        Log.d("Percentage", "Within Goals: $withinGoalsPercentage%, Over Goals: $overGoalsPercentage%, Under Goals: $underGoalsPercentage%")

        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val entries = listOf(
            PieEntry(withinGoalsPercentage, "Within goal"),
            PieEntry(overGoalsPercentage, "Over maximum goal"),
            PieEntry(underGoalsPercentage, "Under minimum goal")
        )
        val dataSet = PieDataSet(entries, "")
        dataSet.setColors(Color.GREEN, Color.RED, Color.rgb(255, 165, 0)) // Orange color
        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueFormatter = PercentFormatter(pieChart)

        val pieData = PieData(dataSet)

        pieChart.data = pieData
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)
        pieChart.setCenterTextSize(20f)
        pieChart.setCenterTextColor(Color.WHITE)
        pieChart.setCenterText("Goal Adherence")
        pieChart.description.isEnabled = false
        val legend = pieChart.legend
        legend.textColor = Color.WHITE

        pieChart.invalidate() // refresh the chart
    }

    fun back2(view: View) {
        finish()
    }
}
