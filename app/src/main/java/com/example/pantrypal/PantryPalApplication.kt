package com.example.pantrypal

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.pantrypal.data.local.PantryDatabase
import com.example.pantrypal.data.repository.UserRepository

class PantryPalApplication : Application() {

    // Lazy initialization of database
    val database: PantryDatabase by lazy {
        PantryDatabase.getDatabase(this)
    }
    
    // Lazy initialization of repository
    val userRepository: UserRepository by lazy {
        UserRepository(database.userDao())
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Expiry Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for items about to expire"
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "pantry_expiry_channel"
    }
}
