package com.varsitycollege.st10043352.opsc_clockit

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.*

class LeaderBoard : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var activityRef: DatabaseReference
    private lateinit var leaderboardContainer: LinearLayout
    private lateinit var firstPlaceName: TextView
    private lateinit var secondPlaceName: TextView
    private lateinit var thirdPlaceName: TextView
    private lateinit var firstPlacePoints: TextView
    private lateinit var secondPlacePoints: TextView
    private lateinit var thirdPlacePoints: TextView
    private val botUsers = listOf("bot1", "bot2", "bot3", "bot4", "bot5")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leader_board)

        database = FirebaseDatabase.getInstance("https://clockit-13d02-default-rtdb.europe-west1.firebasedatabase.app")
        activityRef = database.getReference("logged_sessions")

        leaderboardContainer = findViewById(R.id.leaderboardContainer)
        firstPlaceName = findViewById(R.id.firstPlaceName)
        secondPlaceName = findViewById(R.id.secondPlaceName)
        thirdPlaceName = findViewById(R.id.thirdPlaceName)
        firstPlacePoints = findViewById(R.id.firstPlacePoints)
        secondPlacePoints = findViewById(R.id.secondPlacePoints)
        thirdPlacePoints = findViewById(R.id.thirdPlacePoints)

        fetchLeaderboardData()
    }

    private fun fetchLeaderboardData() {
        activityRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var totalUserPoints = 0
                val botPointsMap: MutableMap<String, Int> = mutableMapOf()

                for (snapshot in dataSnapshot.children) {
                    val session = snapshot.getValue(LoggedSession::class.java)
                    session?.let {
                        val totalMinutes = convertToMinutes(it.time)
                        if (!botUsers.contains(it.activityName.toLowerCase())) {
                            totalUserPoints += totalMinutes
                        } else {
                            botPointsMap[it.activityName] = botPointsMap.getOrDefault(it.activityName, 0) + totalMinutes
                        }
                    }
                }

                // Adding bot users with fixed points for illustration
                botPointsMap["Tyler"] = 2019
                botPointsMap["Emily"] = 1731
                botPointsMap["Shira"] = 1241
                botPointsMap["Sarah"] = 1051
                botPointsMap["Reece"] = 432
                botPointsMap["Nic"] = 2014
                botPointsMap["David"] = 1431
                botPointsMap["James"] = 1642
                botPointsMap["Daniel"] = 1061
                botPointsMap["Matteo"] = 846

                val userPointsMap = mutableMapOf("Me" to totalUserPoints)
                userPointsMap.putAll(botPointsMap)

                val sortedUserPoints = userPointsMap.entries.sortedByDescending { it.value }
                displayLeaderboard(sortedUserPoints)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@LeaderBoard, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun convertToMinutes(time: String): Int {
        val parts = time.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        return hours * 60 + minutes
    }

    private fun displayLeaderboard(userPointsList: List<Map.Entry<String, Int>>) {
        leaderboardContainer.removeAllViews() // Clear existing views if any

        if (userPointsList.isNotEmpty()) {
            firstPlaceName.text = userPointsList[0].key
            firstPlacePoints.text = "${userPointsList[0].value} pts"
        }
        if (userPointsList.size > 1) {
            secondPlaceName.text = userPointsList[1].key
            secondPlacePoints.text = "${userPointsList[1].value} pts"
        }
        if (userPointsList.size > 2) {
            thirdPlaceName.text = userPointsList[2].key
            thirdPlacePoints.text = "${userPointsList[2].value} pts"
        }

        for ((index, entry) in userPointsList.withIndex()) {
            val userLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                background = when (index) {
                    0 -> ContextCompat.getDrawable(this@LeaderBoard, R.drawable.rank_first)
                    1 -> ContextCompat.getDrawable(this@LeaderBoard, R.drawable.rank_second)
                    2 -> ContextCompat.getDrawable(this@LeaderBoard, R.drawable.rank_third)
                    else -> ContextCompat.getDrawable(this@LeaderBoard, R.drawable.ranked_background)
                }
            }

            val userRankTextView = TextView(this).apply {
                text = "${index + 1}"
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.2f)
            }

            val userNameTextView = TextView(this).apply {
                text = entry.key
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f)
            }

            val userPointsTextView = TextView(this).apply {
                text = "${entry.value} pts"
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f)
            }

            userLayout.addView(userRankTextView)
            userLayout.addView(userNameTextView)
            userLayout.addView(userPointsTextView)

            leaderboardContainer.addView(userLayout)
        }
    }

    fun back(view: View) {
        finish()
    }
}

data class LoggedSession(
    val activityName: String = "",
    val categoryName: String = "",
    val categoryColor: String = "",
    val time: String = "",
    val date: String = "",
    val imageUrl: String = ""
)
