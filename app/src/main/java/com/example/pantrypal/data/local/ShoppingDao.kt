package com.example.pantrypal.data.local

import androidx.room.*
import com.example.pantrypal.data.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {

    // ==================== CREATE ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllItems(items: List<ShoppingItem>)

    // ==================== READ ====================
    
    // Get all shopping items
    @Query("SELECT * FROM shopping_items ORDER BY is_checked ASC, created_at DESC")
    fun getAllItems(): Flow<List<ShoppingItem>>

    // Get unchecked items only
    @Query("SELECT * FROM shopping_items WHERE is_checked = 0 ORDER BY created_at DESC")
    fun getUncheckedItems(): Flow<List<ShoppingItem>>

    // Get checked items only
    @Query("SELECT * FROM shopping_items WHERE is_checked = 1 ORDER BY created_at DESC")
    fun getCheckedItems(): Flow<List<ShoppingItem>>

    // Get item by ID
    @Query("SELECT * FROM shopping_items WHERE id = :id")
    suspend fun getItemById(id: Int): ShoppingItem?

    // Get unchecked count
    @Query("SELECT COUNT(*) FROM shopping_items WHERE is_checked = 0")
    fun getUncheckedCount(): Flow<Int>

    // ==================== UPDATE ====================
    
    @Update
    suspend fun updateItem(item: ShoppingItem)

    // Toggle checked status
    @Query("UPDATE shopping_items SET is_checked = NOT is_checked WHERE id = :id")
    suspend fun toggleChecked(id: Int)

    // Mark as checked
    @Query("UPDATE shopping_items SET is_checked = 1 WHERE id = :id")
    suspend fun markAsChecked(id: Int)

    // Mark as unchecked
    @Query("UPDATE shopping_items SET is_checked = 0 WHERE id = :id")
    suspend fun markAsUnchecked(id: Int)

    // ==================== DELETE ====================
    
    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    // Delete all checked items
    @Query("DELETE FROM shopping_items WHERE is_checked = 1")
    suspend fun deleteAllCheckedItems()

    // Delete all items
    @Query("DELETE FROM shopping_items")
    suspend fun deleteAllItems()
}
