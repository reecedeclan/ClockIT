
package com.varsitycollege.st10043352.opsc_clockit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class Insights : AppCompatActivity() {

    private lateinit var startDate: TextView
    private lateinit var endDate: TextView
    private lateinit var calendarView: CalendarView
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var btnAddDate: Button
    private var selectedStartDate: Date? = null
    private var selectedEndDate: Date? = null

    // SharedPreferences file name
    private val PREF_NAME = "InsightsPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)

        startDate = findViewById(R.id.StartDate)
        endDate = findViewById(R.id.EndDate)
        calendarView = findViewById(R.id.calendarView)
        btnAddDate = findViewById(R.id.btnAddDate)

        // Reset saved dates to null initially (or on some specific condition)
        selectedStartDate = null
        selectedEndDate = null
        startDate.text = "Start Date: Not selected"
        endDate.text = "End Date: Not selected"

        // Load saved dates from SharedPreferences
        loadSavedDates()

        // Set listener for calendar view
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = getDateFromDatePicker(year, month, dayOfMonth)
            if (selectedStartDate == null || selectedEndDate != null) {
                // Set the selected date as start date if no start date is selected or if end date is already selected
                selectedStartDate = selectedDate
                selectedEndDate = null
                startDate.text = "Start Date: ${dateFormat.format(selectedDate)}"
                endDate.text = ""
            } else if (selectedStartDate != null && selectedEndDate == null) {
                // Set the selected date as end date if start date is already selected but end date is not
                selectedEndDate = selectedDate
                endDate.text = "End Date: ${dateFormat.format(selectedDate)}"
            }

        }

        btnAddDate.setOnClickListener{
            if(selectedStartDate != null && selectedEndDate != null) {

                // Start the StatisticsPeriod activity
                val intent = Intent(this, CategoryActivityInsights::class.java)
                // Pass the selected start and end dates to the next activity
                intent.putExtra("startDate", selectedStartDate?.time ?: -1)
                intent.putExtra("endDate", selectedEndDate?.time ?: -1)
                startActivity(intent)
            }else{
                Toast.makeText(this, "Please enter the date range you would like to view", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to get Date from DatePicker
    private fun getDateFromDatePicker(year: Int, month: Int, day: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return calendar.time
    }

    fun navFun(view: View) {
        startActivity(Intent(this, FunTime::class.java))
    }

    fun navStats(view: View){
        startActivity(Intent(this, stats::class.java))
    }

    fun navGoals(view: View){
        startActivity(Intent(this, goals::class.java))
    }

    fun navHome(view: View){
        startActivity(Intent(this, home_page::class.java))
    }

    // Function to save selected dates to SharedPreferences
    private fun saveSelectedDates() {
        val sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong("startDate", selectedStartDate?.time ?: -1)
            putLong("endDate", selectedEndDate?.time ?: -1)
            apply()
        }
    }

    // Function to load saved dates from SharedPreferences
    private fun loadSavedDates() {
        val sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val startDateMillis = sharedPref.getLong("startDate", -1)
        val endDateMillis = sharedPref.getLong("endDate", -1)

        if (startDateMillis != -1L) {
            selectedStartDate = Date(startDateMillis)
            startDate.text = "Start Date: ${dateFormat.format(selectedStartDate)}"
        }

        if (endDateMillis != -1L) {
            selectedEndDate = Date(endDateMillis)
            endDate.text = "End Date: ${dateFormat.format(selectedEndDate)}"
        }
    }

    override fun onPause() {
        super.onPause()
        // Save selected dates to SharedPreferences when the activity is paused
        saveSelectedDates()
    }
}
