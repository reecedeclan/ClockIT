package com.varsitycollege.st10043352.opsc_clockit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ViewLog : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var txtName: TextView
    private lateinit var txtCategory: TextView
    private lateinit var txtTime: TextView
    private lateinit var LogPhoto: ImageView
    private lateinit var txt404: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_log)

        backButton = findViewById(R.id.back_button)
        txtName = findViewById(R.id.ActivityName)
        txtCategory = findViewById(R.id.CategoryName)
        txtTime = findViewById(R.id.TimeOnTask)
        LogPhoto = findViewById(R.id.LogPhoto)
        txt404 = findViewById(R.id.txt404)

        val ActivityName = intent.getStringExtra("activityName")
        val Category = intent.getStringExtra("category")
        val time = intent.getStringExtra("time")
        val photoUrls = intent.getStringArrayExtra("photos")
        val color = intent.getStringExtra("color")


        txtName.text = ActivityName
        txtCategory.text = Category
        Log.e("color", "$color")
        if (color != null) {
            txtCategory.setTextColor(color.toInt())
        }
        txtTime.text = time

        if (photoUrls != null && photoUrls.isNotEmpty()) {
            var photoFound = false
            for (key in photoUrls) {
                val id = key.split(",")
                val photoUrl = id[0]
                val time1 = id[1]
                val act = id[2]

                if (time1 == time && act == ActivityName) {
                    if (photoUrl.isNotEmpty()) {
                        loadLogImage(photoUrl)
                        photoFound = true
                        break
                    }
                }
            }
            if (!photoFound) {
                txt404.text = "No photo was added"
                txt404.isVisible = true
            }
        } else {
            txt404.text = "No photo was added"
            txt404.isVisible = true
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadLogImage(photoUrl: String) {
        val logImageUri = Uri.parse(photoUrl)
        if (logImageUri.scheme != null && logImageUri.host != null) {
            // Load image from Firebase Storage using Picasso
            FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl).downloadUrl
                .addOnSuccessListener { uri ->
                    Picasso.get().load(uri).into(LogPhoto)
                }
                .addOnFailureListener { exception ->
                    Log.e("ViewLog", "Failed to load image: $exception")
                }
        } else {
            Log.e("ViewLog", "Invalid URI: $logImageUri")
        }
    }

    companion object {
        fun formatLogs(log: String?): List<String> {
            return log?.split(",") ?: emptyList()
        }

        fun formatActivities(log: String?): List<String> {
            return log?.split(",") ?: emptyList()
        }

        fun formatSharedPref(activity: String?): CharSequence? {
            return activity?.let {
                val values = activity.split(",")
                val name = values[3]
                val times = name.split(":")
                "\t${times[0]} hours ${times[1]} minutes"
            } ?: ""
        }
    }
}