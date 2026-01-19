package com.example.pantrypal.ui.settings

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.backup.BackupManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.pantrypal.PantryPalApplication
import com.example.pantrypal.R
import com.example.pantrypal.data.repository.UserRepository
import com.example.pantrypal.databinding.DialogEditProfileBinding
import com.example.pantrypal.databinding.FragmentSettingsBinding
import com.example.pantrypal.ui.MainActivity
import com.example.pantrypal.ui.auth.LoginActivity
import com.example.pantrypal.util.NotificationReceiver
import com.example.pantrypal.util.NotificationScheduler
import com.example.pantrypal.util.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var userRepository: UserRepository

    // For edit profile dialog
    private var dialogBinding: DialogEditProfileBinding? = null
    private var selectedImageUri: Uri? = null

    // Image picker launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                updateDialogProfilePicture(uri)
            }
        }
    }

    // Permission launcher for storage/media access
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(requireContext(), "Permission required to select image", Toast.LENGTH_SHORT).show()
        }
    }

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

        // Initialize UserRepository
        val database = (requireActivity().application as PantryPalApplication).database
        userRepository = UserRepository(database.userDao())

        setupProfile()
        setupNotificationSettings()
        setupAppearanceSettings()
        setupClickListeners()
    }

    private fun setupProfile() {
        binding.tvUserName.text = preferenceManager.getUserName() ?: "Guest"
        binding.tvUserEmail.text = preferenceManager.getUserEmail() ?: "Not logged in"

        // Check if user has a profile picture
        val profilePictureUri = preferenceManager.getProfilePicture()

        if (!profilePictureUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(profilePictureUri)
                binding.ivSettingsProfilePicture.visibility = View.VISIBLE
                binding.tvAvatarLetter.visibility = View.GONE
                binding.viewAvatarBackground.visibility = View.GONE

                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .placeholder(R.drawable.bg_circle)
                    .error(R.drawable.bg_circle)
                    .into(binding.ivSettingsProfilePicture)
            } catch (e: Exception) {
                showAvatarLetter()
            }
        } else {
            showAvatarLetter()
        }
    }

    private fun showAvatarLetter() {
        binding.ivSettingsProfilePicture.visibility = View.GONE
        binding.tvAvatarLetter.visibility = View.VISIBLE
        binding.viewAvatarBackground.visibility = View.VISIBLE

        val name = preferenceManager.getUserName() ?: "G"
        if (name.isNotEmpty()) {
            binding.tvAvatarLetter.text = name.first().uppercase()
        }
    }

    private fun setupNotificationSettings() {
        binding.switchExpiryReminders.isChecked = preferenceManager.isNotificationsEnabled()
        binding.switchDailySummary.isChecked = preferenceManager.isDailySummaryEnabled()

        val reminderDays = preferenceManager.getReminderDays()
        binding.tvReminderDays.text = "$reminderDays day(s) before"
    }

    private fun setupAppearanceSettings() {
        binding.switchDarkMode.isChecked = preferenceManager.isDarkModeEnabled()
    }

    private fun setupClickListeners() {
        binding.switchExpiryReminders.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setNotificationsEnabled(isChecked)
        }

        binding.switchDailySummary.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setDailySummaryEnabled(isChecked)
        }

        binding.layoutReminderDays.setOnClickListener {
            showReminderDaysDialog()
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setDarkModeEnabled(isChecked)
            updateTheme(isChecked)
        }

        binding.layoutDarkMode.setOnClickListener {
            binding.switchDarkMode.toggle()
        }

        binding.layoutEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.layoutAbout.setOnClickListener {
            showAboutDialog()
        }

        binding.layoutLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.layoutAbout.setOnLongClickListener {
            showTestOptions()
            true
        }
    }

    private fun showEditProfileDialog() {
        dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)

        dialogBinding?.let { binding ->
            val currentName = preferenceManager.getUserName() ?: ""
            val currentEmail = preferenceManager.getUserEmail() ?: ""
            val currentProfilePicture = preferenceManager.getProfilePicture()

            binding.etUsername.setText(currentName)
            binding.etEmail.setText(currentEmail)

            if (currentName.isNotEmpty()) {
                binding.tvDialogAvatarLetter.text = currentName.first().uppercase()
            }

            if (currentProfilePicture != null) {
                try {
                    val uri = Uri.parse(currentProfilePicture)
                    binding.ivProfilePicture.visibility = View.VISIBLE
                    binding.tvDialogAvatarLetter.visibility = View.GONE
                    binding.viewAvatarBackground.visibility = View.GONE
                    Glide.with(this)
                        .load(uri)
                        .circleCrop()
                        .into(binding.ivProfilePicture)
                    selectedImageUri = uri
                } catch (e: Exception) {
                    binding.ivProfilePicture.visibility = View.GONE
                    binding.tvDialogAvatarLetter.visibility = View.VISIBLE
                    binding.viewAvatarBackground.visibility = View.VISIBLE
                }
            }

            // Click listener for changing profile picture
            binding.layoutCameraOverlay.setOnClickListener {
                checkPermissionAndOpenPicker()
            }

            binding.ivProfilePicture.setOnClickListener {
                checkPermissionAndOpenPicker()
            }

            binding.tvDialogAvatarLetter.setOnClickListener {
                checkPermissionAndOpenPicker()
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Profile")
                .setView(binding.root)
                .setPositiveButton("Save") { _, _ ->
                    saveProfile(binding)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    selectedImageUri = null
                    dialog.dismiss()
                }
                .setOnDismissListener {
                    dialogBinding = null
                }
                .show()
        }
    }

    private fun checkPermissionAndOpenPicker() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> openImagePicker()
            else -> {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        pickImageLauncher.launch(intent)
    }

    private fun updateDialogProfilePicture(uri: Uri) {
        dialogBinding?.let { binding ->
            binding.ivProfilePicture.visibility = View.VISIBLE
            binding.tvDialogAvatarLetter.visibility = View.GONE
            binding.viewAvatarBackground.visibility = View.GONE
            Glide.with(this).load(uri).circleCrop().into(binding.ivProfilePicture)
        }
    }

    private fun saveProfile(dialogBinding: DialogEditProfileBinding) {
        val newName = dialogBinding.etUsername.text.toString().trim()

        if (newName.isEmpty() || newName.length < 2) {
            Toast.makeText(requireContext(), "Username must be at least 2 characters", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = preferenceManager.getUserId()
        val profilePictureUri = selectedImageUri?.toString()

        lifecycleScope.launch {
            try {
                if (userId != -1) {
                    val currentUser = userRepository.getUserById(userId)
                    if (currentUser != null) {
                        val updatedUser = currentUser.copy(
                            name = newName,
                            profilePicture = profilePictureUri ?: currentUser.profilePicture
                        )

                        userRepository.updateUser(updatedUser)
                    }
                }


                withContext(Dispatchers.Main) {
                    preferenceManager.saveUserName(newName)
                    profilePictureUri?.let { preferenceManager.saveProfilePicture(it) }

                    setupProfile()


                    BackupManager(requireContext()).dataChanged()

                    Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        selectedImageUri = null
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
        dialogBinding = null
    }
}