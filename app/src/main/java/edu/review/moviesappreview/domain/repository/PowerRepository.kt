package edu.review.moviesappreview.domain.repository

import edu.review.moviesappreview.domain.PowerState
import kotlinx.coroutines.flow.Flow

interface PowerRepository {
    fun getPowerState(): Flow<PowerState>
}
