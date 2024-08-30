
package com.varsitycollege.st10043352.opsc_clockit


import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.varsitycollege.st10043352.opsc_clockit.R

// Activity for adding a new category
class Add_Category : AppCompatActivity() {

    private lateinit var categoryNameEditText: EditText
    private lateinit var doneButton: Button
    private lateinit var colorViews: List<ImageView>
    private var selectedColor: Int = Color.RED // Default to Color.RED or any other default color

    // Firebase
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        // Initialize views
        categoryNameEditText = findViewById(R.id.txtCategoryName)
        doneButton = findViewById(R.id.button2)
        colorViews = listOf(
            findViewById(R.id.colorOption1),
            findViewById(R.id.colorOption2),
            findViewById(R.id.colorOption3),
            findViewById(R.id.colorOption4),
            findViewById(R.id.colorOption5)
        )

        // Initialize Firebase
        database = FirebaseDatabase.getInstance("https://clockit-13d02-default-rtdb.europe-west1.firebasedatabase.app").reference.child("categories")

        // Set onClickListener for color options
        for (i in 0 until colorViews.size) {
            colorViews[i].setOnClickListener {
                // Set the selected colour as the background colour of the EditText
                categoryNameEditText.setBackgroundColor(getColorOption(i))
                // Get the selected color
                selectedColor = getColorOption(i)
            }
        }

        // Set onClickListener for the done button
        doneButton.setOnClickListener {
            // Get the category name entered by the user
            val categoryName = categoryNameEditText.text.toString().trim()

            // Save category to Firebase
            saveCategory(categoryName, selectedColor)

            // Navigate back to the main page
            finish()
        }
    }

    // Function to get color option based on index
    private fun getColorOption(index: Int): Int {
        // Define your colour options here
        val colorOptions = listOf(
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.MAGENTA
        )
        return colorOptions[index]
    }

    // Function to save category to Firebase
    private fun saveCategory(categoryName: String, color: Int) {
        // Generate a unique key for the category
        val categoryId = database.push().key

        // Create a HashMap to store category data
        val categoryData = HashMap<String, Any>()
        categoryData["categoryName"] = categoryName
        categoryData["categoryColor"] = color

        // Save category data to Firebase
        categoryId?.let {
            database.child(it).setValue(categoryData)
        }
    }
}
