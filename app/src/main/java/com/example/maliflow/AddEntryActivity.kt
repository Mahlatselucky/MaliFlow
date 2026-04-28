package com.example.maliflow

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.maliflow.data.database.AppDatabase
import com.example.maliflow.data.model.Category
import com.example.maliflow.data.model.ExpenseEntry
import com.example.maliflow.databinding.ActivityAddEntryBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEntryActivity : AppCompatActivity() {
//This is where you can add expense entries
    private lateinit var binding: ActivityAddEntryBinding
    private var userId: Int = -1
    private var photoPath: String? = null
    private var categories: List<Category> = emptyList()
    private lateinit var photoFile: File

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoPath = photoFile.absolutePath
            binding.ivPhoto.setImageURI(Uri.fromFile(photoFile))
            binding.ivPhoto.visibility = android.view.View.VISIBLE
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoPath = it.toString()
            binding.ivPhoto.setImageURI(it)
            binding.ivPhoto.visibility = android.view.View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", -1)
        val db = AppDatabase.getDatabase(this)

        db.categoryDao().getCategoriesByUser(userId).observe(this) { list ->
            categories = list
            val names = list.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }

        binding.btnAddPhoto.setOnClickListener {
            showPhotoOptions()
        }

        binding.btnSaveEntry.setOnClickListener {
            saveEntry(db)
        }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        android.app.AlertDialog.Builder(this)
            .setTitle("Add Photo")
            .setItems(options) { _, which ->
                if (which == 0) takePhoto() else pickImageLauncher.launch("image/*")
            }.show()
    }

    private fun takePhoto() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        photoFile = File(getExternalFilesDir(null), "IMG_$timeStamp.jpg")
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(uri)
    }

    private fun saveEntry(db: AppDatabase) {
        val date = binding.etDate.text.toString().trim()
        val startTime = binding.etStartTime.text.toString().trim()
        val endTime = binding.etEndTime.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()

        if (date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() ||
            description.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (categories.isEmpty()) {
            Toast.makeText(this, "Please create a category first", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: run {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategory = categories[binding.spinnerCategory.selectedItemPosition]

        val entry = ExpenseEntry(
            date = date,
            startTime = startTime,
            endTime = endTime,
            description = description,
            amount = amount,
            categoryId = selectedCategory.id,
            userId = userId,
            photoPath = photoPath
        )

        lifecycleScope.launch {
            db.expenseEntryDao().insert(entry)
            runOnUiThread {
                Toast.makeText(this@AddEntryActivity, "Entry saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
