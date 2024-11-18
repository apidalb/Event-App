package com.example.dicodingevent.adapterviewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dicodingevent.data.local.SettingPreferences
import com.example.dicodingevent.data.repository.EventRepository
import com.example.dicodingevent.di.Injection

class MainViewModelFactory(private val repository: EventRepository, private val settingPreferences: SettingPreferences) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, settingPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        fun getInstance(context: Context): MainViewModelFactory {
            val repository = Injection.provideEventRepository(context)
            val settingPreferences = Injection.provideSettingPreferences(context)
            return MainViewModelFactory(repository, settingPreferences)
        }
    }
}
