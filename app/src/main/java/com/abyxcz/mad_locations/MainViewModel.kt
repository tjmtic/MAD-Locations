package com.abyxcz.mad_locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class MainViewModel: ViewModel() {

    private val _state : MutableStateFlow<MainViewModelUiState> = MutableStateFlow(MainViewModelUiState())
    val state : StateFlow<MainViewModelUiState> = _state.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = MainViewModelUiState()
    )

    private fun onEvent(event: MainViewModelEvent){
        when(event){
            is MainViewModelEvent.ShowLocations -> { _state.update {  it.copy( viewState = MainViewModelViewState.Location ) }}
            is MainViewModelEvent.ShowGeofences -> { _state.update { it.copy( viewState = MainViewModelViewState.Geofence ) }}
        }

    }

    fun showLocation(){
        onEvent(MainViewModelEvent.ShowLocations)
    }

    fun showGeofence(){
        onEvent(MainViewModelEvent.ShowGeofences)
    }

}
sealed class MainViewModelEvent{
    object ShowLocations : MainViewModelEvent()
    object ShowGeofences : MainViewModelEvent()
}

data class MainViewModelUiState(
    val viewState : MainViewModelViewState = MainViewModelViewState.Default
)

sealed class MainViewModelViewState{
    object Default : MainViewModelViewState()
    object Location : MainViewModelViewState()
    object Geofence : MainViewModelViewState()
}