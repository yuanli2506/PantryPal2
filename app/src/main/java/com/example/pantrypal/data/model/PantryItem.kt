package com.example.pantrypal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Entity(tableName = "pantry_items")
data class PantryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "quantity")
    val quantity: String = "",

    @ColumnInfo(name = "unit")
    val unit: String = "",

    @ColumnInfo(name = "purchase_date")
    val purchaseDate: Long? = null,

    @ColumnInfo(name = "expiry_date")
    val expiryDate: Long,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "is_consumed")
    val isConsumed: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Calculate days until expiry
    fun getDaysUntilExpiry(): Int {
        val today = System.currentTimeMillis()
        val diff = expiryDate - today
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    // Get expiry status
    fun getExpiryStatus(): ExpiryStatus {
        val days = getDaysUntilExpiry()
        return when {
            days < 0 -> ExpiryStatus.EXPIRED
            days == 0 -> ExpiryStatus.EXPIRES_TODAY
            days <= 3 -> ExpiryStatus.CRITICAL
            days <= 7 -> ExpiryStatus.WARNING
            else -> ExpiryStatus.FRESH
        }
    }

    // Format date for display
    fun getFormattedExpiryDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(expiryDate))
    }

    fun getFormattedPurchaseDate(): String {
        return purchaseDate?.let {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(it))
        } ?: "Not set"
    }

    // Get quantity with unit
    fun getQuantityWithUnit(): String {
        return if (quantity.isNotEmpty() && unit.isNotEmpty()) {
            "$quantity $unit"
        } else if (quantity.isNotEmpty()) {
            quantity
        } else {
            "Not specified"
        }
    }

    // Get category emoji
    fun getCategoryEmoji(): String {
        return when (category.lowercase()) {
            "dairy" -> "🥛"
            "produce", "vegetables", "fruits" -> "🥬"
            "meat", "poultry" -> "🍗"
            "seafood", "fish" -> "🐟"
            "bakery", "bread" -> "🍞"
            "frozen" -> "🧊"
            "beverages", "drinks" -> "🥤"
            "snacks" -> "🍿"
            "condiments", "sauces" -> "🧴"
            "canned", "canned goods" -> "🥫"
            "grains", "pasta", "rice" -> "🍚"
            "eggs" -> "🥚"
            else -> "📦"
        }
    }
}

enum class ExpiryStatus {
    FRESH,      // More than 7 days
    WARNING,    // 4-7 days
    CRITICAL,   // 1-3 days
    EXPIRES_TODAY, // Today
    EXPIRED     // Past expiry
}
