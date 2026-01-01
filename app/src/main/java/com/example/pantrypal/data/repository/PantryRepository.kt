package com.example.pantrypal.data.repository

import com.example.pantrypal.data.local.PantryDao
import com.example.pantrypal.data.model.PantryItem
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class PantryRepository(private val pantryDao: PantryDao) {

    // Get all items
    val allItems: Flow<List<PantryItem>> = pantryDao.getAllItems()

    // Get total count
    val totalItemCount: Flow<Int> = pantryDao.getTotalItemCount()

    // Get expiring soon count (within 7 days)
    fun getExpiringSoonCount(): Flow<Int> {
        val today = getTodayStartMillis()
        val weekLater = today + (7 * 24 * 60 * 60 * 1000L)
        return pantryDao.getExpiringSoonCount(today, weekLater)
    }

    // Get expired count
    fun getExpiredCount(): Flow<Int> {
        return pantryDao.getExpiredCount(getTodayStartMillis())
    }

    // Get items expiring within days
    fun getExpiringItems(days: Int): Flow<List<PantryItem>> {
        val today = getTodayStartMillis()
        val futureDate = today + (days * 24 * 60 * 60 * 1000L)
        return pantryDao.getExpiringItems(today, futureDate)
    }

    // Get items expiring today
    fun getItemsExpiringToday(): Flow<List<PantryItem>> {
        val todayStart = getTodayStartMillis()
        val todayEnd = todayStart + (24 * 60 * 60 * 1000L) - 1
        return pantryDao.getItemsExpiringToday(todayStart, todayEnd)
    }

    // Get expired items
    fun getExpiredItems(): Flow<List<PantryItem>> {
        return pantryDao.getExpiredItems(getTodayStartMillis())
    }

    // Get items by category
    fun getItemsByCategory(category: String): Flow<List<PantryItem>> {
        return pantryDao.getItemsByCategory(category)
    }

    // Search items
    fun searchItems(query: String): Flow<List<PantryItem>> {
        return pantryDao.searchItems(query)
    }

    // Get single item by ID
    suspend fun getItemById(id: Int): PantryItem? {
        return pantryDao.getItemById(id)
    }

    // Get single item as Flow
    fun getItemByIdFlow(id: Int): Flow<PantryItem?> {
        return pantryDao.getItemByIdFlow(id)
    }

    // Insert item
    suspend fun insert(item: PantryItem): Long {
        return pantryDao.insertItem(item)
    }

    // Update item
    suspend fun update(item: PantryItem) {
        pantryDao.updateItem(item.copy(updatedAt = System.currentTimeMillis()))
    }

    // Delete item
    suspend fun delete(item: PantryItem) {
        pantryDao.deleteItem(item)
    }

    // Delete item by ID
    suspend fun deleteById(id: Int) {
        pantryDao.deleteItemById(id)
    }

    // Mark as consumed
    suspend fun markAsConsumed(id: Int) {
        pantryDao.markAsConsumed(id)
    }

    // Delete all expired items
    suspend fun deleteAllExpired() {
        pantryDao.deleteAllExpiredItems(getTodayStartMillis())
    }

    // Helper function to get today's start timestamp
    private fun getTodayStartMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
