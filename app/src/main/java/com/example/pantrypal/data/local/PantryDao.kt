package com.example.pantrypal.data.local

import androidx.room.*
import com.example.pantrypal.data.model.PantryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {

    // ==================== CREATE ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PantryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllItems(items: List<PantryItem>)

    // ==================== READ ====================
    
    // Get all items (not consumed), sorted by expiry date
    @Query("SELECT * FROM pantry_items WHERE is_consumed = 0 ORDER BY expiry_date ASC")
    fun getAllItems(): Flow<List<PantryItem>>

    // Get all items including consumed
    @Query("SELECT * FROM pantry_items ORDER BY expiry_date ASC")
    fun getAllItemsIncludingConsumed(): Flow<List<PantryItem>>

    // Get single item by ID
    @Query("SELECT * FROM pantry_items WHERE id = :id")
    suspend fun getItemById(id: Int): PantryItem?

    // Get single item by ID as Flow (for observing changes)
    @Query("SELECT * FROM pantry_items WHERE id = :id")
    fun getItemByIdFlow(id: Int): Flow<PantryItem?>

    // Get items expiring within date range
    @Query("""
        SELECT * FROM pantry_items 
        WHERE expiry_date BETWEEN :startDate AND :endDate 
        AND is_consumed = 0 
        ORDER BY expiry_date ASC
    """)
    fun getExpiringItems(startDate: Long, endDate: Long): Flow<List<PantryItem>>

    // Get items expiring today
    @Query("""
        SELECT * FROM pantry_items 
        WHERE expiry_date >= :todayStart AND expiry_date <= :todayEnd 
        AND is_consumed = 0
    """)
    fun getItemsExpiringToday(todayStart: Long, todayEnd: Long): Flow<List<PantryItem>>

    // Get expired items
    @Query("SELECT * FROM pantry_items WHERE expiry_date < :today AND is_consumed = 0")
    fun getExpiredItems(today: Long): Flow<List<PantryItem>>

    // Get items by category
    @Query("SELECT * FROM pantry_items WHERE category = :category AND is_consumed = 0 ORDER BY expiry_date ASC")
    fun getItemsByCategory(category: String): Flow<List<PantryItem>>

    // Search items by name
    @Query("SELECT * FROM pantry_items WHERE name LIKE '%' || :query || '%' AND is_consumed = 0 ORDER BY expiry_date ASC")
    fun searchItems(query: String): Flow<List<PantryItem>>

    // Get total item count
    @Query("SELECT COUNT(*) FROM pantry_items WHERE is_consumed = 0")
    fun getTotalItemCount(): Flow<Int>

    // Get expiring soon count (within 7 days)
    @Query("""
        SELECT COUNT(*) FROM pantry_items 
        WHERE expiry_date BETWEEN :today AND :weekLater 
        AND is_consumed = 0
    """)
    fun getExpiringSoonCount(today: Long, weekLater: Long): Flow<Int>

    // Get expired count
    @Query("SELECT COUNT(*) FROM pantry_items WHERE expiry_date < :today AND is_consumed = 0")
    fun getExpiredCount(today: Long): Flow<Int>

    // ==================== UPDATE ====================
    
    @Update
    suspend fun updateItem(item: PantryItem)

    // Mark item as consumed
    @Query("UPDATE pantry_items SET is_consumed = 1, updated_at = :timestamp WHERE id = :id")
    suspend fun markAsConsumed(id: Int, timestamp: Long = System.currentTimeMillis())

    // ==================== DELETE ====================
    
    @Delete
    suspend fun deleteItem(item: PantryItem)

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    // Delete all expired items
    @Query("DELETE FROM pantry_items WHERE expiry_date < :today AND is_consumed = 0")
    suspend fun deleteAllExpiredItems(today: Long)

    // Delete all items
    @Query("DELETE FROM pantry_items")
    suspend fun deleteAllItems()
}
