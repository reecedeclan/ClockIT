
package com.varsitycollege.st10043352.opsc_clockit
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddActivity : AppCompatActivity() {

    private lateinit var colorBox: View
    private lateinit var spinner: Spinner
    private lateinit var doneButton: Button
    private lateinit var categoryColors: MutableList<Int>
    private lateinit var txtActivityName: EditText
    private lateinit var txtDescription: EditText
    private lateinit var txtStartTime: EditText
    private lateinit var txtEndTime: EditText
    private lateinit var imgPreview: ImageView
    private var photoUri: Uri? = null

    // Initialize FirebaseStorage instance
    private val storage = FirebaseStorage.getInstance()
    private val database = FirebaseDatabase.getInstance("https://clockit-13d02-default-rtdb.europe-west1.firebasedatabase.app")

    // Constants for image selection
    private val PICK_IMAGE_REQUEST = 1
    private val REQUEST_IMAGE_CAPTURE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        // Initialize elements
        colorBox = findViewById(R.id.colorBox)
        spinner = findViewById(R.id.spnrMinMinutes)
        doneButton = findViewById(R.id.btnAddMin)
        txtActivityName = findViewById(R.id.txtActivityName)
        txtDescription = findViewById(R.id.txtDescription)
        txtStartTime = findViewById(R.id.txtStartTime)
        txtEndTime = findViewById(R.id.txtEndTime)
        imgPreview = findViewById(R.id.imgPreview)

        // Retrieve data from Firebase
        populateSpinner()

        // Set onClickListener for the done button to finish activity
        doneButton.setOnClickListener {
            saveActivity()
            doneButton.isClickable = false
        }

        // Set up the spinner listener
        setupSpinnerListener()

        // Set up click listener for the add photo button
        val btnAddPhoto = findViewById<Button>(R.id.btnLog)
        btnAddPhoto.setOnClickListener {
            openGallery()
        }

        // Set up click listener for the take photo button
        val btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)
        btnTakePhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

        // Set up click listener for the start time EditText
        txtStartTime.setOnClickListener {
            showTimePickerDialog(txtStartTime)
        }

        // Set up click listener for the end time EditText
        txtEndTime.setOnClickListener {
            showTimePickerDialog(txtEndTime)
        }
    }

    // Function to populate spinner with category names from Firebase
    private fun populateSpinner() {
        val categoriesReference = database.getReference("categories")

        categoriesReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryNames = mutableListOf<String>()
                categoryColors = mutableListOf()

                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.child("categoryName").getValue(String::class.java)
                    val categoryColor = categorySnapshot.child("categoryColor").getValue(Int::class.java)

                    if (categoryName != null && categoryColor != null) {
                        categoryNames.add(categoryName)
                        categoryColors.add(categoryColor)
                    }
                }

                // Create adapter for spinner
                val adapter = object : ArrayAdapter<String>(this@AddActivity, android.R.layout.simple_spinner_dropdown_item, categoryNames) {
                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        if (view is TextView) {
                            view.setTextColor(Color.WHITE) // Set text color for dropdown items
                            view.setBackgroundColor(Color.parseColor("#1B232E")) // Set background color for dropdown items
                        }
                        return view
                    }

                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent)
                        if (view is TextView) {
                            view.setTextColor(Color.WHITE) // Set text color for selected item
                            view.setBackgroundColor(Color.TRANSPARENT) // Set background color for selected item
                        }
                        return view
                    }
                }

                // Set adapter for spinner
                spinner.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Log.e("AddActivity", "Database error: ${error.message}")
                Toast.makeText(this@AddActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to set up listener for spinner
    private fun setupSpinnerListener() {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Set color for color box based on selected category
                colorBox.setBackgroundColor(categoryColors[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    // Function to open image picker
    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    // Function to handle result of image picker or camera capture
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
                    val imageBitmap = extras?.get("data") as Bitmap
                    imgPreview.setImageBitmap(imageBitmap)

                    photoUri = getImageURI(this, imageBitmap)
                }
            }
        }
    }

    private fun getImageURI(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    // Function to save activity details
    private fun saveActivity() {
        val activityName = txtActivityName.text.toString()
        val description = txtDescription.text.toString()
        val categoryName = spinner.selectedItem.toString()
        val color = categoryColors[spinner.selectedItemPosition]
        val startTime = txtStartTime.text.toString()
        val endTime = txtEndTime.text.toString()

        if (activityName.isEmpty()) {
            // Handle empty activity name
            return
        }

        // Generate a unique ID for the image file
        val imageFileName = "${UUID.randomUUID()}.jpg"

        // Get a reference to the image file in Firebase Storage
        val storageRef = storage.reference.child("images/$imageFileName")
        val uploadTask = photoUri?.let { storageRef.putFile(it) }

        if (photoUri == null) {
            // If there's no photo, directly save activity details
            saveActivityDetails(
                activityName,
                description,
                categoryName,
                color,
                startTime,
                endTime,
                null
            )
            Toast.makeText(this, "Activity saved", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageRef.downloadUrl
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Get the download URL for the image
                val downloadUri = task.result

                // Save activity details along with the download URL
                saveActivityDetails(
                    activityName,
                    description,
                    categoryName,
                    color,
                    startTime,
                    endTime,
                    downloadUri
                )
                Toast.makeText(this, "Activity saved", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // Log the error
                Log.e("AddActivity", "Upload failed: ${task.exception}")
                // Display a toast to the user
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveActivityDetails(activityName: String, description: String, categoryName: String, color: Int, startTime: String, endTime: String, photoUri: Uri?) {
        // Get a reference to the 'activities' node in the database
        val activitiesRef = database.getReference("activities")

        // Create a unique key for the new activity
        val activityKey = activitiesRef.push().key

        // Create a HashMap to hold the activity data
        val activityData = HashMap<String, Any?>()
        activityData["activityName"] = activityName
        activityData["description"] = description
        activityData["categoryName"] = categoryName
        activityData["color"] = color
        activityData["startTime"] = startTime
        activityData["endTime"] = endTime
        activityData["photoUrl"] = photoUri?.toString()

        // Save the activity data to the database
        if (activityKey != null) {
            activitiesRef.child(activityKey).setValue(activityData)
        }
    }

    // Function to show time picker dialog
    private fun showTimePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                // Update EditText with selected time
                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                editText.setText(selectedTime)
            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()
    }

    // Function to dispatch intent for taking a photo with the camera
    private fun dispatchTakePictureIntent() {
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    // Function to create an image file
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            photoUri = absoluteFile.toUri()
        }
    }
}
