package com.example.pantrypal.ui.home

import androidx.lifecycle.*
import com.example.pantrypal.data.local.PantryDao
import com.example.pantrypal.data.model.PantryItem
import com.example.pantrypal.data.repository.PantryRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlinx.coroutines.Dispatchers


class HomeViewModel(pantryDao: PantryDao) : ViewModel() {

    private val repository = PantryRepository(pantryDao)

    // Expiring items (within 7 days)
    val expiringItems: LiveData<List<PantryItem>> = repository.getExpiringItems(7).asLiveData()

    // Total items count
    val totalCount: LiveData<Int> = repository.totalItemCount.asLiveData()

    // Expiring today count
    private val todayStart = getTodayStartMillis()
    private val todayEnd = todayStart + (24 * 60 * 60 * 1000L) - 1
    
    val expiringTodayCount: LiveData<Int> = repository.getExpiringItems(1).asLiveData().map { it.size }

    // Expiring this week count
    val expiringWeekCount: LiveData<Int> = repository.getExpiringSoonCount().asLiveData()

    // Expired count
    val expiredCount: LiveData<Int> = repository.getExpiredCount().asLiveData()

    // Delete item
    fun deleteItem(item: PantryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(item)
        }
    }

    //marked as consumed
    fun markAsConsumed(itemId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markAsConsumed(itemId)
        }
    }

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

class HomeViewModelFactory(private val pantryDao: PantryDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(pantryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
