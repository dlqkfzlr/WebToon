package com.pluu.webtoon.weekly.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pluu.webtoon.ui.model.FavoriteResult

class ToonViewModel @ViewModelInject constructor() : ViewModel() {
    private val _updateEvent: MutableLiveData<FavoriteResult> = MutableLiveData()
    val updateEvent: LiveData<FavoriteResult>
        get() = _updateEvent

    fun updateFavorite(favorite: FavoriteResult) {
        _updateEvent.value = favorite
    }
}
