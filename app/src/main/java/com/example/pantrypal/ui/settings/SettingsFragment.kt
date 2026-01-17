package com.example.pantrypal.ui.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.pantrypal.databinding.FragmentSettingsBinding
import com.example.pantrypal.ui.MainActivity
import com.example.pantrypal.ui.auth.LoginActivity
import com.example.pantrypal.util.NotificationReceiver
import com.example.pantrypal.util.NotificationScheduler
import com.example.pantrypal.util.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())

        setupProfile()
        setupNotificationSettings()
        setupAppearanceSettings()
        setupClickListeners()
    }

    private fun setupProfile() {
        binding.tvUserName.text = preferenceManager.getUserName() ?: "Guest"
        binding.tvUserEmail.text = preferenceManager.getUserEmail() ?: "Not logged in"

        // Show first letter as avatar
        val name = preferenceManager.getUserName() ?: "G"
        binding.tvAvatarLetter.text = name.first().uppercase()
    }

    private fun setupNotificationSettings() {
        // Load saved preferences
        binding.switchExpiryReminders.isChecked = preferenceManager.isNotificationsEnabled()
        binding.switchDailySummary.isChecked = preferenceManager.isDailySummaryEnabled()

        // Update reminder days text
        val reminderDays = preferenceManager.getReminderDays()
        binding.tvReminderDays.text = "$reminderDays day(s) before"
    }

    private fun setupAppearanceSettings() {
        binding.switchDarkMode.isChecked = preferenceManager.isDarkModeEnabled()
    }

    private fun setupClickListeners() {
        // Expiry reminders toggle
        binding.switchExpiryReminders.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setNotificationsEnabled(isChecked)
        }

        // Daily summary toggle
        binding.switchDailySummary.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setDailySummaryEnabled(isChecked)
        }

        // Reminder days setting
        binding.layoutReminderDays.setOnClickListener {
            showReminderDaysDialog()
        }

        // Dark Mode toggle
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setDarkModeEnabled(isChecked)
            updateTheme(isChecked)
        }

        binding.layoutDarkMode.setOnClickListener {
            binding.switchDarkMode.toggle()
        }

        // Edit profile
        binding.layoutEditProfile.setOnClickListener {
            // TODO: Implement edit profile
        }

        // About
        binding.layoutAbout.setOnClickListener {
            showAboutDialog()
        }

        // Logout
        binding.layoutLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.layoutAbout.setOnLongClickListener {
            showTestOptions()
            true
        }
    }

    private fun updateTheme(isDarkMode: Boolean) {
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun showTestOptions() {
        val options = arrayOf(
            "📩 Instant Test",
            "⏰ 1-Min Test",
            "✅ Schedule Real 9 AM",
            "📊 Check Scheduled Time"
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Notification Tests")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> testNotification()
                    1 -> test1MinuteAlarm()
                    2 -> {
                        NotificationScheduler.scheduleDailyCheck(requireContext())
                        android.widget.Toast.makeText(requireContext(), "✅ Scheduled for 9 AM daily", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    3 -> showScheduledTime()
                }
            }
            .show()
    }

    private fun testNotification() {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        val intent = Intent(requireContext(), MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            requireContext(),
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(requireContext(), "pantry_expiry_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔔 Test Notification")
            .setContentText("If you see this, notifications work!")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(9999, notification)

        android.widget.Toast.makeText(requireContext(), "Notification sent!", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun test1MinuteAlarm() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_TITLE, "🔔 1-Min Test")
            putExtra(NotificationReceiver.EXTRA_MESSAGE, "This is a test notification!")
            putExtra(NotificationReceiver.EXTRA_NOTIFICATION_ID, 9999)
        }
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 9999, intent, PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 60000,
            pendingIntent
        )

        android.widget.Toast.makeText(requireContext(), "⏰ Test in 1 minute", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun showScheduledTime() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault())
        val scheduledTime = dateFormat.format(calendar.time)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Next Notification")
            .setMessage("📅 Scheduled for:\n$scheduledTime")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showReminderDaysDialog() {
        val options = arrayOf("1 day before", "2 days before", "3 days before", "7 days before")
        val values = arrayOf(1, 2, 3, 7)
        val currentDays = preferenceManager.getReminderDays()
        val selectedIndex = values.indexOf(currentDays).coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reminder Days")
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                preferenceManager.setReminderDays(values[which])
                binding.tvReminderDays.text = "${values[which]} day(s) before"
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About PantryPal")
            .setMessage("""
                PantryPal v1.0
                
                A smart grocery inventory app that helps you reduce food waste by tracking expiry dates and sending timely notifications.
                
                © 2024 BITP3453 Mobile App Development
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        preferenceManager.clearSession()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
