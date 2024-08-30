package com.varsitycollege.st10043352.opsc_clockit

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AddGoal : AppCompatActivity() {

    private lateinit var txtActivity: TextView
    private lateinit var txtCategory: TextView
    private lateinit var btnMin: Button
    private lateinit var btnMax: Button
    private lateinit var btnSetGoals: Button
    private lateinit var txtMin: TextView
    private lateinit var txtMax: TextView
    private lateinit var minPicker: TimePicker
    private val database = FirebaseDatabase.getInstance("https://clockit-13d02-default-rtdb.europe-west1.firebasedatabase.app")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_goal)

        // Initialize all fields
        txtActivity = findViewById(R.id.txtActivity1)
        txtCategory = findViewById(R.id.txtCategory1)
        btnMin = findViewById(R.id.btnAddMin)
        btnMax = findViewById(R.id.btnAddMax)
        btnSetGoals = findViewById(R.id.btnAddAllGoals)
        txtMin = findViewById(R.id.txtMin)
        txtMax = findViewById(R.id.txtMax)
        minPicker = findViewById(R.id.spnrTime)

        minPicker.setIs24HourView(true)

        // Retrieve the information from Intent extras
        val activityData = intent.getStringExtra("activityData")

        // Split the activityData string into an array of relevant fields
        val activityFields = splitActivityData(activityData)

        txtActivity.text = activityFields?.get(0)
        txtCategory.text = activityFields?.get(2)
        txtCategory.setTextColor((activityFields?.get(3))?.toInt() ?: 0)

        btnMin.setOnClickListener {
            val selectedHour = minPicker.hour
            val selectedMinute = minPicker.minute
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            txtMin.text = "Minimum goal: $selectedTime"
        }

        btnMax.setOnClickListener {
            val selectedHour = minPicker.hour
            val selectedMinute = minPicker.minute
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            txtMax.text = "Maximum goal: $selectedTime"
        }

        btnSetGoals.setOnClickListener {
            val minGoal = txtMin.text.toString().replace("Minimum goal: ", "")
            val maxGoal = txtMax.text.toString().replace("Maximum goal: ", "")
            saveGoals(txtActivity.text.toString(), minGoal, maxGoal)
        }
    }

    private fun splitActivityData(activityData: String?): List<String>? {
        return activityData?.split(",")?.map { it.trim() }
    }

    fun back(view: View) {
        finish()
    }

    private fun saveGoals(activity: String, minGoal: String, maxGoal: String) {
        val ref = database.getReference("goals")
        val minGoalKey = "$activity/min_goal"
        val maxGoalKey = "$activity/max_goal"
        ref.child(minGoalKey).setValue(minGoal)
        ref.child(maxGoalKey).setValue(maxGoal)
    }
}
