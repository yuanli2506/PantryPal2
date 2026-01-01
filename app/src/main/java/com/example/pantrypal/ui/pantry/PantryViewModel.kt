package com.example.pantrypal.ui.pantry

import androidx.lifecycle.*
import com.example.pantrypal.data.local.PantryDao
import com.example.pantrypal.data.model.ExpiryStatus
import com.example.pantrypal.data.model.PantryItem
import com.example.pantrypal.data.repository.PantryRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers


class PantryViewModel(pantryDao: PantryDao) : ViewModel() {

    private val repository = PantryRepository(pantryDao)

    // Filter state
    private val _currentFilter = MutableLiveData(PantryFilter.ALL)
    
    // Search query
    private val _searchQuery = MutableLiveData("")
    
    // Category filter
    private val _selectedCategory = MutableLiveData<String?>(null)

    // Loading state
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // All items from database
    private val allItems: LiveData<List<PantryItem>> = repository.allItems.asLiveData()

    // Filtered items
    val filteredItems: LiveData<List<PantryItem>> = MediatorLiveData<List<PantryItem>>().apply {
        addSource(allItems) { items ->
            value = applyFilters(items)
        }
        addSource(_currentFilter) {
            allItems.value?.let { items ->
                value = applyFilters(items)
            }
        }
        addSource(_searchQuery) {
            allItems.value?.let { items ->
                value = applyFilters(items)
            }
        }
        addSource(_selectedCategory) {
            allItems.value?.let { items ->
                value = applyFilters(items)
            }
        }
    }

    private fun applyFilters(items: List<PantryItem>): List<PantryItem> {
        var result = items

        // Apply status filter
        result = when (_currentFilter.value) {
            PantryFilter.FRESH -> result.filter { 
                it.getExpiryStatus() == ExpiryStatus.FRESH 
            }
            PantryFilter.EXPIRING_SOON -> result.filter { 
                val status = it.getExpiryStatus()
                status == ExpiryStatus.WARNING || status == ExpiryStatus.CRITICAL || status == ExpiryStatus.EXPIRES_TODAY
            }
            PantryFilter.EXPIRED -> result.filter { 
                it.getExpiryStatus() == ExpiryStatus.EXPIRED 
            }
            else -> result
        }

        // Apply category filter
        _selectedCategory.value?.let { category ->
            result = result.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Apply search query
        val query = _searchQuery.value ?: ""
        if (query.isNotEmpty()) {
            result = result.filter { 
                it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }

        return result.sortedBy { it.expiryDate }
    }

    fun setFilter(filter: PantryFilter) {
        _currentFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun refreshData() {
        // Trigger refresh by resetting filter
        _currentFilter.value = _currentFilter.value
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            repository.deleteById(itemId)
            _isLoading.postValue(false)
        }
    }

    fun markAsConsumed(itemId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markAsConsumed(itemId)
        }
    }
}

class PantryViewModelFactory(private val pantryDao: PantryDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PantryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PantryViewModel(pantryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
