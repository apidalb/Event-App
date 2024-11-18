package com.example.dicodingevent.ui.settings

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.dicodingevent.R
import com.example.dicodingevent.adapterviewmodel.MainViewModel
import com.example.dicodingevent.adapterviewmodel.MainViewModelFactory
import com.example.dicodingevent.databinding.FragmentSettingsBinding
import com.example.dicodingevent.notifreminder.DailyReminder
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private val prefDailyReminderKey = "pref_daily_reminder"
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val settingsViewModel: MainViewModel by viewModels {
        MainViewModelFactory.getInstance(requireContext())
    }

    private var previousThemeSetting: Boolean = false
    private lateinit var switchDailyReminderNotification: SwitchMaterial

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupProfile()

        setupSettings()
    }

    private fun setupProfile() {
        binding.apply {
            nameTextView.text = "Muhammad Hafizh Albanthany"
            emailTextView.text = "21120122140148"
        }
    }

    private fun setupSettings() {
        sharedPreferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)

        val switchTheme = binding.root.findViewById<SwitchMaterial>(R.id.switch_theme)
        switchDailyReminderNotification = binding.root.findViewById(R.id.switchDailyReminderNotification)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                DailyReminder.setupDailyReminder(requireContext())
                switchDailyReminderNotification.isChecked = true
                saveDailyReminderSetting(true)
                Toast.makeText(
                    requireContext(),
                    "Izin Notifikasi telah diberikan",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                switchDailyReminderNotification.isChecked = false
                saveDailyReminderSetting(false)
                Toast.makeText(
                    requireContext(),
                    "Permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Theme Settings
        lifecycleScope.launch {
            val isDarkMode = settingsViewModel.getThemeSetting()
            previousThemeSetting = isDarkMode
            switchTheme.isChecked = previousThemeSetting

            AppCompatDelegate.setDefaultNightMode(
                if (previousThemeSetting) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != previousThemeSetting) {
                lifecycleScope.launch {
                    settingsViewModel.saveThemeSetting(isChecked)
                    previousThemeSetting = isChecked
                    AppCompatDelegate.setDefaultNightMode(
                        if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                }
            }
        }

        // Daily Reminder Settings
        val isDailyReminderEnabled = sharedPreferences.getBoolean(prefDailyReminderKey, false)
        switchDailyReminderNotification.isChecked = isDailyReminderEnabled

        switchDailyReminderNotification.setOnCheckedChangeListener { _, isChecked ->
            saveDailyReminderSetting(isChecked)

            if (isChecked) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    DailyReminder.setupDailyReminder(requireContext())
                }
            } else {
                DailyReminder.cancelDailyReminder(requireContext())
            }
        }
    }

    private fun saveDailyReminderSetting(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(prefDailyReminderKey, isEnabled).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
