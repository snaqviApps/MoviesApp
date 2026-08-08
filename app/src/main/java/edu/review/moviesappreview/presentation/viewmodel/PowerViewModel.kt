package edu.review.moviesappreview.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.review.moviesappreview.data.repository.system.PowerRepository
import edu.review.moviesappreview.domain.PowerState
import edu.review.moviesappreview.domain.repository.IPowerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PowerViewModel @Inject constructor (
//    private val iPowerRepository: IPowerRepository
    private val iPowerRepository: PowerRepository
) : ViewModel() {

    val powerState: StateFlow<PowerState> = iPowerRepository.getPowerState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PowerState.PluggedStatusLoading(true)
        )
}
