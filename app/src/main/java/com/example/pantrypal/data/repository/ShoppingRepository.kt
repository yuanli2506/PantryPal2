package com.example.pantrypal.data.repository

import com.example.pantrypal.data.local.ShoppingDao
import com.example.pantrypal.data.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

class ShoppingRepository(private val shoppingDao: ShoppingDao) {

    // Get all shopping items
    val allItems: Flow<List<ShoppingItem>> = shoppingDao.getAllItems()

    // Get unchecked items
    val uncheckedItems: Flow<List<ShoppingItem>> = shoppingDao.getUncheckedItems()

    // Get unchecked count
    val uncheckedCount: Flow<Int> = shoppingDao.getUncheckedCount()

    // Get item by ID
    suspend fun getItemById(id: Int): ShoppingItem? {
        return shoppingDao.getItemById(id)
    }

    // Insert item
    suspend fun insert(item: ShoppingItem): Long {
        return shoppingDao.insertItem(item)
    }

    // Insert multiple items
    suspend fun insertAll(items: List<ShoppingItem>) {
        shoppingDao.insertAllItems(items)
    }

    // Update item
    suspend fun update(item: ShoppingItem) {
        shoppingDao.updateItem(item)
    }

    // Toggle checked status
    suspend fun toggleChecked(id: Int) {
        shoppingDao.toggleChecked(id)
    }

    // Delete item
    suspend fun delete(item: ShoppingItem) {
        shoppingDao.deleteItem(item)
    }

    // Delete by ID
    suspend fun deleteById(id: Int) {
        shoppingDao.deleteItemById(id)
    }

    // Delete all checked items
    suspend fun deleteAllChecked() {
        shoppingDao.deleteAllCheckedItems()
    }

    // Delete all items
    suspend fun deleteAll() {
        shoppingDao.deleteAllItems()
    }
}
