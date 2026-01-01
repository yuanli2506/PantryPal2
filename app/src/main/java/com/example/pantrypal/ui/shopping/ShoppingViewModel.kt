package com.example.pantrypal.ui.shopping

import androidx.lifecycle.*
import com.example.pantrypal.data.local.ShoppingDao
import com.example.pantrypal.data.model.ShoppingItem
import com.example.pantrypal.data.repository.ShoppingRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers


class ShoppingViewModel(shoppingDao: ShoppingDao) : ViewModel() {

    private val repository = ShoppingRepository(shoppingDao)

    // All shopping items
    val allItems: LiveData<List<ShoppingItem>> = repository.allItems.asLiveData()

    // Unchecked count
    val uncheckedCount: LiveData<Int> = repository.uncheckedCount.asLiveData()

    // Add item
    fun addItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(item)
        }
    }

    fun toggleChecked(itemId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleChecked(itemId)
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(item)
        }
    }

    fun deleteAllChecked() {
        viewModelScope.launch(Dispatchers.IO) {
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
