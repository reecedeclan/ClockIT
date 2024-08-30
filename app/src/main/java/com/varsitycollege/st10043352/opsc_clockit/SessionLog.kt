package com.varsitycollege.st10043352.opsc_clockit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.*

class SessionLog : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private val REQUEST_IMAGE_CAPTURE = 2

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var spnrTime: TimePicker
    private lateinit var btnLog: Button
    private lateinit var btnAddPhoto: Button
    private lateinit var btnSave: Button
    private lateinit var txtActivity: TextView
    private lateinit var txtCategory: TextView
    private lateinit var imgPreview: ImageView
    private lateinit var datePicker: DatePicker
    private lateinit var takePhoto: Button
    private var photoUri: Uri? = null

    private lateinit var database: FirebaseDatabase
    private lateinit var activityRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_log)

        sharedPreferences = getSharedPreferences("CategoryPreferences", MODE_PRIVATE)
        database = FirebaseDatabase.getInstance("https://clockit-13d02-default-rtdb.europe-west1.firebasedatabase.app")
        activityRef = database.getReference("logged_sessions")
        storageRef = storage.reference

        val activityData = intent.getStringExtra("activity") ?: ""

        txtActivity = findViewById(R.id.txtActivity1)
        txtCategory = findViewById(R.id.txtCategory1)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        btnSave = findViewById(R.id.btnSave)
        datePicker = findViewById(R.id.DatePicker)
        imgPreview = findViewById(R.id.imgPreview)
        takePhoto = findViewById(R.id.btnTakePhoto)

        spnrTime = findViewById(R.id.spnrTime)
        spnrTime.setIs24HourView(true)

        val details = activityData.split(",")
        if (details.size >= 3) {
            val activityName = details[0]
            val categoryName = details[2]

            txtActivity.text = activityName
            txtCategory.text = categoryName
            txtCategory.setTextColor((details[3]).toInt())
        }

        btnAddPhoto.setOnClickListener {
            openImagePicker()
        }

        takePhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

        btnSave.setOnClickListener {
            btnSave.isClickable = false
            val selectedHour = spnrTime.hour
            val selectedMinute = spnrTime.minute
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)

            val selectedMonth = datePicker.month + 1
            val day = datePicker.dayOfMonth
            val selectedYear = datePicker.year
            val selectedDate = String.format("%02d/%02d", day, selectedMonth, selectedYear)

            if (photoUri != null) {
                val storageRef = storage.reference.child("session_images/${UUID.randomUUID()}.jpg")
                val uploadTask = photoUri?.let { storageRef.putFile(it) }

                uploadTask?.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    storageRef.downloadUrl
                }?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        saveSession(details, selectedTime, selectedDate, downloadUri.toString())
                    } else {
                        Toast.makeText(this, "Failed to upload photo", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                saveSession(details, selectedTime, selectedDate, null)
            }
        }

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, LogHours::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("activityData", activityData)
            startActivity(intent)
            finish()
        }
    }

    private fun saveSession(details: List<String>, time: String, date: String, imageUrl: String?) {
        val logEntry = LoggedSession(
            details[0],
            details[2],
            details[3],
            time,
            date,
            imageUrl ?: ""
        )

        activityRef.push().setValue(logEntry)
            .addOnSuccessListener {
                Toast.makeText(this, "Session logged", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to log session: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_IMAGE_REQUEST -> {
                if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                    // User selected an image from the gallery
                    photoUri = data.data
                    imgPreview.setImageURI(photoUri)
                }
            }
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Display the captured image in the image preview
                    val extras = data.extras
                    if (extras != null && extras.containsKey("data")) {
                        val imageBitmap = extras.get("data") as Bitmap
                        imgPreview.setImageBitmap(imageBitmap)
                        // Convert bitmap to Uri
                        photoUri = getImageUri(this, imageBitmap)
                    }
                }
            }
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }
}
