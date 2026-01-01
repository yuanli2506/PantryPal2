package com.example.pantrypal.ui.settings

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pantrypal.R
import com.example.pantrypal.databinding.FragmentEditProfileBinding
import com.example.pantrypal.util.PreferenceManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())

        loadUserProfile()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.etBirthdate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.tilBirthdate.setEndIconOnClickListener {
            showDatePickerDialog()
        }

        binding.btnSave.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateBirthdateInView()
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateBirthdateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.etBirthdate.setText(sdf.format(calendar.time))
    }

    private fun loadUserProfile() {
        binding.etName.setText(preferenceManager.getUserName())
        binding.etEmail.setText(preferenceManager.getUserEmail())

        when (preferenceManager.getUserGender()) {
            "Male" -> binding.rbMale.isChecked = true
            "Female" -> binding.rbFemale.isChecked = true
        }

        val birthdateStr = preferenceManager.getUserBirthdate()
        if (birthdateStr.isNotEmpty()) {
            binding.etBirthdate.setText(birthdateStr)
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                calendar.time = sdf.parse(birthdateStr)!!
            } catch (e: Exception) {
                // Keep calendar as today if parsing fails
            }
        }
    }

    private fun saveUserProfile() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val gender = when (binding.rgGender.checkedRadioButtonId) {
            R.id.rb_male -> "Male"
            R.id.rb_female -> "Female"
            else -> ""
        }
        val birthdate = binding.etBirthdate.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = "Name cannot be empty"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email address"
            return
        }

        binding.tilName.error = null
        binding.tilEmail.error = null

        preferenceManager.setUserName(name)
        preferenceManager.setUserEmail(email)
        preferenceManager.setUserGender(gender)
        preferenceManager.setUserBirthdate(birthdate)

        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
