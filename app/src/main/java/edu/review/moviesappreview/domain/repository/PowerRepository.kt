package edu.review.moviesappreview.domain.repository

import edu.review.moviesappreview.domain.model.PowerState
import kotlinx.coroutines.flow.Flow

interface PowerRepository {
    fun getPowerState(): Flow<PowerState>
}
