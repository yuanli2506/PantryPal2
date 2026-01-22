package com.example.pantrypal

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.example.pantrypal.data.local.PantryDatabase
import com.example.pantrypal.util.PreferenceManager

class PantryPalApplication : Application() {

    // Lazy initialization of database
    val database: PantryDatabase by lazy {
        PantryDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Set theme based on saved preference
        val preferenceManager = PreferenceManager(this)
        val isDarkMode = preferenceManager.isDarkModeEnabled()
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)

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
