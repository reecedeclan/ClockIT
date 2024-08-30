package com.varsitycollege.st10043352.opsc_clockit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FunTime : AppCompatActivity() {
    private lateinit var btnLeaderBoard: TextView
    private lateinit var btnchallenges: TextView
    private lateinit var btnAwards: TextView
    private lateinit var btnHowdoesfuntimework: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun_time)

        btnLeaderBoard = findViewById(R.id.btnLeaderBoard)
        btnLeaderBoard.setOnClickListener {
            val intent = Intent(this, LeaderBoard::class.java)
            startActivity(intent)
        }

        btnHowdoesfuntimework = findViewById(R.id.btnHowdoesfuntimework)
        btnHowdoesfuntimework.setOnClickListener {
            val intent = Intent(this, Howdoesfuntimework::class.java)
            startActivity(intent)
        }
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

    fun navHome(view: View) {
        startActivity(Intent(this, home_page ::class.java))
    }

    fun navChallenges(view: View) {
        startActivity(Intent(this, Challenges::class.java))
    }
}
