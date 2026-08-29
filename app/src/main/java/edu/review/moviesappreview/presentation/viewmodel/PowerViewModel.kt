package edu.review.moviesappreview.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.review.moviesappreview.domain.PowerState
import edu.review.moviesappreview.domain.repository.PowerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PowerViewModel @Inject constructor (
    private val powerRepository: PowerRepository
) : ViewModel() {

    val powerState: StateFlow<PowerState> = powerRepository.getPowerState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PowerState.PluggedStatusLoading(true)
        )
}
