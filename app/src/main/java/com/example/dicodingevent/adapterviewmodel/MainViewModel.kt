package com.example.dicodingevent.adapterviewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicodingevent.data.local.SettingPreferences
import com.example.dicodingevent.data.local.favorite.RoomDBFavoriteEvent
import com.example.dicodingevent.data.repository.EventRepository
import com.example.dicodingevent.data.response.EventDetailResponse
import com.example.dicodingevent.data.response.EventResponse
import com.example.dicodingevent.data.response.ListEventsItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(private val repository: EventRepository, private val settingPreferences: SettingPreferences) : ViewModel() {

    private val _activeEvents = MutableLiveData<List<ListEventsItem>>()
    val activeEvents: LiveData<List<ListEventsItem>> = _activeEvents

    private val _finishedEvents = MutableLiveData<List<ListEventsItem>?>()
    val finishedEvents: LiveData<List<ListEventsItem>?> = _finishedEvents

    private val _eventDetail = MutableLiveData<EventDetailResponse?>()
    val eventDetail: LiveData<EventDetailResponse?> = _eventDetail

    private val _isLoadingActive = MutableLiveData<Boolean>()
    val isLoadingActive: LiveData<Boolean> = _isLoadingActive

    private val _isLoadingFinished = MutableLiveData<Boolean>()
    val isLoadingFinished: LiveData<Boolean> = _isLoadingFinished

    private val _isLoadingFavorite = MutableLiveData<Boolean>()
    val isLoadingFavorite: LiveData<Boolean> = _isLoadingFavorite

    private val _favoriteEvents = MutableLiveData<List<RoomDBFavoriteEvent>>()
    val favoriteEvents: LiveData<List<RoomDBFavoriteEvent>> = _favoriteEvents

    private val _isLoadingDetail = MutableLiveData<Boolean>()
    val isLoadingDetail: LiveData<Boolean> = _isLoadingDetail

    private val _themeSetting = MutableLiveData<Boolean>()

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        viewModelScope.launch {
            settingPreferences.themeSetting.collect { isDarkMode ->
                _themeSetting.value = isDarkMode
            }
        }
    }

    // Fetch active events
    fun getActiveEvents() {
        _isLoadingActive.value?.takeIf { it || !_activeEvents.value.isNullOrEmpty() }?.let { return }

        _isLoadingActive.value = true
        viewModelScope.launch {
            try {
                val response: Response<EventResponse> = repository.getActiveEvents()
                if (response.isSuccessful) {
                    _activeEvents.value = response.body()?.listEvents ?: listOf()
                    clearErrorMessage()
                } else {
                    _errorMessage.value = "Failed to fetch active events: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoadingActive.value = false
            }
        }
    }

    // Fetch finished events
    fun getFinishedEvents() {
        _isLoadingFinished.value?.takeIf { it || !_finishedEvents.value.isNullOrEmpty() }?.let { return }

        _isLoadingFinished.value = true
        viewModelScope.launch {
            try {
                val response: Response<EventResponse> = repository.getFinishedEvents()
                if (response.isSuccessful) {
                    _finishedEvents.value = response.body()?.listEvents ?: listOf()
                    clearErrorMessage()
                } else {
                    _errorMessage.value = "Failed to fetch finished events: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoadingFinished.value = false
            }
        }
    }

    // Fetch event detail
    fun getEventDetail(eventId: Int) {
        _isLoadingDetail.value = true
        getFavoriteEvents()

        viewModelScope.launch {
            try {
                val response: Response<EventDetailResponse> = repository.getEventDetail(eventId)
                if (response.isSuccessful) {
                    _eventDetail.value = response.body()
                } else {
                    _errorMessage.value = "Failed to fetch event detail: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoadingDetail.value = false
            }
        }
    }

    // Search finished events
    fun searchFinishedEvents(query: String): LiveData<List<ListEventsItem>?> {
        val filteredEvents = MutableLiveData<List<ListEventsItem>?>()
        _finishedEvents.value?.let { events ->
            filteredEvents.value = events.filter { event ->
                event.name?.contains(query, ignoreCase = true) ?: false
            }
        }
        return filteredEvents
    }

    // Fetch favorite events
    fun getFavoriteEvents() {
        _isLoadingFavorite.value = false
        viewModelScope.launch {
            try {
                _favoriteEvents.value = repository.getFavoriteEvents()
                clearErrorMessage()
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoadingFavorite.value = false
            }
        }
    }

    //condition if already event click in the database favorite_db_event
    fun isEventFavorited(eventId: Int): Boolean {
        return _favoriteEvents.value?.any { it.eventID == eventId } == true
    }

    fun addItemFavorite(event: RoomDBFavoriteEvent) {
        viewModelScope.launch {
            repository.insertItemFavorite(event)
            getFavoriteEvents()
        }
    }
    fun removeItemFavorite(eventId: Int) {
        viewModelScope.launch {
            repository.deleteFavoriteEvent(eventId.toString())
            _favoriteEvents.value = _favoriteEvents.value?.filter { it.eventID != eventId }
            getFavoriteEvents()
        }
    }
    suspend fun getThemeSetting(): Boolean {
        return settingPreferences.themeSetting.first()
    }
    suspend fun saveThemeSetting(isDarkMode: Boolean) {
        _themeSetting.postValue(isDarkMode)
        settingPreferences.saveThemeSetting(isDarkMode)
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
