package com.example.badbudget

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.badbudget.models.Expense
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var editTextAmount: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var buttonSelectDate: MaterialButton
    private lateinit var buttonSelectStartTime: MaterialButton
    private lateinit var buttonSelectEndTime: MaterialButton
    private lateinit var textViewSelectedDate: TextView
    private lateinit var textViewSelectedStartTime: TextView
    private lateinit var textViewSelectedEndTime: TextView
    private lateinit var editTextDescription: TextInputEditText
    private lateinit var buttonUploadReceipt: MaterialButton
    private lateinit var imageViewReceiptPreview: ImageView
    private lateinit var buttonLogExpense: MaterialButton
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private var selectedDate = ""
    private var selectedStartTime = ""
    private var selectedEndTime = ""
    private var receiptUriString: String? = null

    @Throws(IOException::class)
    private fun saveReceiptToInternalStorage(bitmap: Bitmap): String {
        val filename = "receipt_${System.currentTimeMillis()}.png"
        openFileOutput(filename, Context.MODE_PRIVATE).use { out ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                throw IOException("Couldn't save receipt bitmap")
            }
        }
        return File(filesDir, filename).absolutePath
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // back button
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, ExpenseHistoryActivity::class.java))
            finish()
        }

        // form views
        editTextAmount           = findViewById(R.id.editTextAmount)
        spinnerCategory          = findViewById(R.id.spinnerCategory)
        buttonSelectDate         = findViewById(R.id.buttonSelectDate)
        buttonSelectStartTime    = findViewById(R.id.buttonSelectStartTime)
        buttonSelectEndTime      = findViewById(R.id.buttonSelectEndTime)
        textViewSelectedDate     = findViewById(R.id.textViewSelectedDate)
        textViewSelectedStartTime= findViewById(R.id.textViewSelectedStartTime)
        textViewSelectedEndTime  = findViewById(R.id.textViewSelectedEndTime)
        editTextDescription      = findViewById(R.id.editTextDescription)
        buttonUploadReceipt      = findViewById(R.id.buttonUploadReceipt)
        imageViewReceiptPreview  = findViewById(R.id.imageViewReceiptPreview)
        buttonLogExpense         = findViewById(R.id.buttonLogExpense)

        // populate spinner
        FirestoreService.getBudgets(UserSession.id(this)) { budgets ->
            val cats = budgets.map { it.category }
                .distinct()
                .ifEmpty { listOf("General", "Food", "Transport", "Fun") }
            spinnerCategory.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                cats
            )
        }

        // image picker
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                receiptUriString = it.toString()
                imageViewReceiptPreview.setImageURI(it)
                imageViewReceiptPreview.visibility = View.VISIBLE
            }
        }
        buttonUploadReceipt.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // date picker
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        buttonSelectDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = dateFormat.format(calendar.time)
                    textViewSelectedDate.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // time pickers
        buttonSelectStartTime.setOnClickListener {
            val tCal = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, h, m ->
                    selectedStartTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                    textViewSelectedStartTime.text = selectedStartTime
                },
                tCal.get(Calendar.HOUR_OF_DAY),
                tCal.get(Calendar.MINUTE),
                true
            ).show()
        }
        buttonSelectEndTime.setOnClickListener {
            val tCal = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, h, m ->
                    selectedEndTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                    textViewSelectedEndTime.text = selectedEndTime
                },
                tCal.get(Calendar.HOUR_OF_DAY),
                tCal.get(Calendar.MINUTE),
                true
            ).show()
        }

        // add expense to firestore
        buttonLogExpense.setOnClickListener {
            val category = spinnerCategory.selectedItem?.toString()
                ?: return@setOnClickListener toast("Pick a category first")
            val amountTxt = editTextAmount.text.toString().trim()
            if (amountTxt.isEmpty()) return@setOnClickListener toast("Amount is required")
            val amount = amountTxt.toDouble()

            // save receipt
            var savedPath: String? = null
            (imageViewReceiptPreview.drawable as? BitmapDrawable)?.bitmap?.let { bmp ->
                savedPath = try {
                    saveReceiptToInternalStorage(bmp)
                } catch (e: IOException) {
                    toast("Couldn't save receipt image")
                    null
                }
            }

            // Build Expense data class
            val expense = Expense(
                amount = amount,
                category = category,
                date = selectedDate,
                startTime = selectedStartTime,
                endTime = selectedEndTime,
                description = editTextDescription.text.toString().trim(),
                receiptPath = savedPath,
                userId = UserSession.id(this)
            )

            FirestoreService.addExpense(expense) { success ->
                if (success) {
                    Toast.makeText(this, "Expense added!", Toast.LENGTH_SHORT).show()
                    GamificationManager.onExpenseLogged(this, savedPath != null)
                    GamificationManager.onExpenseLogged(this)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
