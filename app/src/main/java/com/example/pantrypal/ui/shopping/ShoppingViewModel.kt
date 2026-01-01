package com.example.pantrypal.ui.shopping

import androidx.lifecycle.*
import com.example.pantrypal.data.local.ShoppingDao
import com.example.pantrypal.data.model.ShoppingItem
import com.example.pantrypal.data.repository.ShoppingRepository
import kotlinx.coroutines.launch

class ShoppingViewModel(shoppingDao: ShoppingDao) : ViewModel() {

    private val repository = ShoppingRepository(shoppingDao)

    // All shopping items
    val allItems: LiveData<List<ShoppingItem>> = repository.allItems.asLiveData()

    // Unchecked count
    val uncheckedCount: LiveData<Int> = repository.uncheckedCount.asLiveData()

    // Add item
    fun addItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.insert(item)
        }
    }

    // Toggle checked status
    fun toggleChecked(itemId: Int) {
        viewModelScope.launch {
            repository.toggleChecked(itemId)
        }
    }

    // Delete item
    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    // Delete all checked items
    fun deleteAllChecked() {
        viewModelScope.launch {
            repository.deleteAllChecked()
        }
    }
}

class ShoppingViewModelFactory(private val shoppingDao: ShoppingDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingViewModel(shoppingDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
