package com.example.pantrypal.ui.additem

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pantrypal.PantryPalApplication
import com.example.pantrypal.data.model.Category
import com.example.pantrypal.data.model.PantryItem
import com.example.pantrypal.data.repository.PantryRepository
import com.example.pantrypal.databinding.ActivityAddEditItemBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddEditItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditItemBinding
    private lateinit var repository: PantryRepository

    private var itemId: Int = -1
    private var isEditMode: Boolean = false
    private var selectedExpiryDate: Long = 0
    private var selectedPurchaseDate: Long? = null

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository
        val database = (application as PantryPalApplication).database
        repository = PantryRepository(database.pantryDao())

        // Check if edit mode
        itemId = intent.getIntExtra(EXTRA_ITEM_ID, -1)
        isEditMode = itemId != -1

        setupToolbar()
        setupCategorySpinner()
        setupUnitSpinner()
        setupDatePickers()
        setupSaveButton()

        if (isEditMode) {
            loadItemData()
        } else {
            // Set default expiry date to 7 days from now
            calendar.add(Calendar.DAY_OF_MONTH, 7)
            selectedExpiryDate = calendar.timeInMillis
            binding.etExpiryDate.setText(dateFormatter.format(calendar.time))
            calendar.timeInMillis = System.currentTimeMillis()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (isEditMode) "Edit Item" else "Add Item"
        }
    }

    private fun setupCategorySpinner() {
        val categories = Category.getCategoryNames()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.spinnerCategory.setAdapter(adapter)
    }

    private fun setupUnitSpinner() {
        val units = listOf("pieces", "kg", "g", "liter", "ml", "pack", "bottle", "box", "can", "bag")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, units)
        binding.spinnerUnit.setAdapter(adapter)
    }

    private fun setupDatePickers() {
        // Expiry date picker
        binding.etExpiryDate.setOnClickListener {
            showDatePicker { year, month, day ->
                calendar.set(year, month, day, 23, 59, 59)
                selectedExpiryDate = calendar.timeInMillis
                binding.etExpiryDate.setText(dateFormatter.format(calendar.time))
            }
        }

        // Purchase date picker
        binding.etPurchaseDate.setOnClickListener {
            showDatePicker { year, month, day ->
                calendar.set(year, month, day, 0, 0, 0)
                selectedPurchaseDate = calendar.timeInMillis
                binding.etPurchaseDate.setText(dateFormatter.format(calendar.time))
            }
        }
    }

    private fun showDatePicker(onDateSelected: (year: Int, month: Int, day: Int) -> Unit) {
        val currentCalendar = Calendar.getInstance()
        
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                onDateSelected(year, month, dayOfMonth)
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                saveItem()
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        // Validate name
        val name = binding.etItemName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilItemName.error = "Item name is required"
            isValid = false
        } else {
            binding.tilItemName.error = null
        }

        // Validate category
        val category = binding.spinnerCategory.text.toString().trim()
        if (category.isEmpty()) {
            binding.tilCategory.error = "Please select a category"
            isValid = false
        } else {
            binding.tilCategory.error = null
        }

        // Validate expiry date
        if (selectedExpiryDate == 0L) {
            binding.tilExpiryDate.error = "Please select expiry date"
            isValid = false
        } else {
            binding.tilExpiryDate.error = null
        }

        return isValid
    }

    private fun saveItem() {
        val name = binding.etItemName.text.toString().trim()
        val category = binding.spinnerCategory.text.toString().trim()
        val quantity = binding.etQuantity.text.toString().trim()
        val unit = binding.spinnerUnit.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        val pantryItem = PantryItem(
            id = if (isEditMode) itemId else 0,
            name = name,
            category = category,
            quantity = quantity,
            unit = unit,
            purchaseDate = selectedPurchaseDate,
            expiryDate = selectedExpiryDate,
            notes = notes
        )

        lifecycleScope.launch {
            try {
                if (isEditMode) {
                    repository.update(pantryItem)
                    Toast.makeText(this@AddEditItemActivity, "Item updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    repository.insert(pantryItem)
                    Toast.makeText(this@AddEditItemActivity, "Item added successfully", Toast.LENGTH_SHORT).show()
                }
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddEditItemActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadItemData() {
        lifecycleScope.launch {
            val item = repository.getItemById(itemId)
            item?.let { populateForm(it) }
        }
    }

    private fun populateForm(item: PantryItem) {
        binding.etItemName.setText(item.name)
        binding.spinnerCategory.setText(item.category, false)
        binding.etQuantity.setText(item.quantity)
        binding.spinnerUnit.setText(item.unit, false)
        binding.etNotes.setText(item.notes)

        // Set expiry date
        selectedExpiryDate = item.expiryDate
        binding.etExpiryDate.setText(dateFormatter.format(Date(item.expiryDate)))

        // Set purchase date if available
        item.purchaseDate?.let {
            selectedPurchaseDate = it
            binding.etPurchaseDate.setText(dateFormatter.format(Date(it)))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
