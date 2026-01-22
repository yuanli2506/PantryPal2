package com.example.pantrypal.ui.detail

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pantrypal.PantryPalApplication
import com.example.pantrypal.R
import com.example.pantrypal.data.model.ExpiryStatus
import com.example.pantrypal.data.model.PantryItem
import com.example.pantrypal.data.repository.PantryRepository
import com.example.pantrypal.databinding.ActivityItemDetailBinding
import com.example.pantrypal.ui.additem.AddEditItemActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var repository: PantryRepository

    private var itemId: Int = -1
    private var currentItem: PantryItem? = null

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get item ID from intent
        itemId = intent.getIntExtra(EXTRA_ITEM_ID, -1)
        if (itemId == -1) {
            Toast.makeText(this, "Invalid item", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize repository
        val database = (application as PantryPalApplication).database
        repository = PantryRepository(database.pantryDao())

        setupToolbar()
        setupButtons()
        loadItemData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Item Details"
        }

        // Get the colorOnSurface color from the theme
        val typedValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
        val colorOnSurface = typedValue.data

        // Apply the color to the navigation icon
        binding.toolbar.navigationIcon?.colorFilter = PorterDuffColorFilter(colorOnSurface, PorterDuff.Mode.SRC_ATOP)
    }

    private fun setupButtons() {
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, AddEditItemActivity::class.java)
            intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, itemId)
            startActivity(intent)
        }

        binding.btnMarkConsumed.setOnClickListener {
            showConsumedConfirmation()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadItemData() {
        lifecycleScope.launch {
            repository.getItemByIdFlow(itemId).collectLatest { item ->
                if (item != null) {
                    currentItem = item
                    displayItem(item)
                } else {
                    // Item was deleted
                    finish()
                }
            }
        }
    }

    private fun displayItem(item: PantryItem) {
        binding.apply {
            // Basic info
            tvItemName.text = item.name
            tvCategory.text = "${item.getCategoryEmoji()} ${item.category}"
            tvQuantity.text = item.getQuantityWithUnit()

            // Dates
            tvExpiryDate.text = item.getFormattedExpiryDate()
            tvPurchaseDate.text = item.getFormattedPurchaseDate()

            // Days until expiry
            val daysLeft = item.getDaysUntilExpiry()
            tvDaysLeft.text = when {
                daysLeft < 0 -> "Expired ${-daysLeft} day(s) ago"
                daysLeft == 0 -> "Expires today!"
                daysLeft == 1 -> "Expires tomorrow"
                else -> "Expires in $daysLeft days"
            }

            // Status indicator
            val (statusText, statusColor) = when (item.getExpiryStatus()) {
                ExpiryStatus.EXPIRED -> "EXPIRED" to "#F44336"
                ExpiryStatus.EXPIRES_TODAY -> "EXPIRES TODAY" to "#FF5722"
                ExpiryStatus.CRITICAL -> "EXPIRING SOON" to "#FF9800"
                ExpiryStatus.WARNING -> "USE SOON" to "#FFC107"
                ExpiryStatus.FRESH -> "FRESH" to "#4CAF50"
            }
            tvStatus.text = statusText
            tvStatus.setTextColor(Color.parseColor(statusColor))
            statusIndicator.setBackgroundColor(Color.parseColor(statusColor))

            // Notes
            if (item.notes.isNotEmpty()) {
                tvNotes.text = item.notes
                layoutNotes.visibility = View.VISIBLE
            } else {
                layoutNotes.visibility = View.GONE
            }

            // Consumed button visibility
            if (item.isConsumed) {
                btnMarkConsumed.visibility = View.GONE
                tvConsumedBadge.visibility = View.VISIBLE
            } else {
                btnMarkConsumed.visibility = View.VISIBLE
                tvConsumedBadge.visibility = View.GONE
            }
        }
    }

    private fun showConsumedConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Mark as Consumed")
            .setMessage("Mark \"${currentItem?.name}\" as consumed? This will remove it from your active pantry list.")
            .setPositiveButton("Yes, I consumed it") { _, _ ->
                markAsConsumed()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun markAsConsumed() {
        lifecycleScope.launch {
            repository.markAsConsumed(itemId)
            Toast.makeText(this@ItemDetailActivity, "Item marked as consumed!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete \"${currentItem?.name}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteItem()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteItem() {
        lifecycleScope.launch {
            repository.deleteById(itemId)
            Toast.makeText(this@ItemDetailActivity, "Item deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_item_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_edit -> {
                val intent = Intent(this, AddEditItemActivity::class.java)
                intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, itemId)
                startActivity(intent)
                true
            }
            R.id.action_delete -> {
                showDeleteConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
