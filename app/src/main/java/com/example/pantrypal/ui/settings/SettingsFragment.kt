package com.example.pantrypal.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pantrypal.R
import com.example.pantrypal.databinding.FragmentSettingsBinding
import com.example.pantrypal.ui.auth.LoginActivity
import com.example.pantrypal.util.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        setupProfile()
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

        // Edit profile
        binding.layoutEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_editProfileFragment)
        }

        // About
        binding.layoutAbout.setOnClickListener {
            showAboutDialog()
        }

        // Logout
        binding.layoutLogout.setOnClickListener {
            showLogoutConfirmation()
        }
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
