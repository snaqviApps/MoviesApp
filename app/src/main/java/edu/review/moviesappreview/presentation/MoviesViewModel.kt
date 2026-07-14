package edu.review.moviesappreview.presentation

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.review.moviesappreview.BuildConfig.API_KEY
import edu.review.moviesappreview.data.remote.MoviesRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException // FIXED: Correct import path

class MoviesViewModel(
    private val application: Application,
    private val moviesRepository: MoviesRepository = MoviesRepository()
) : ViewModel() {
    private val _moviesState = MutableStateFlow<MovieUIState>(MovieUIState.Loading)
    val moviesState: StateFlow<MovieUIState> = _moviesState.asStateFlow()
    var showMovies by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var currentEndPoint by mutableStateOf("popular")
        private set


    init {
        enableMoviesDataFetching(currentEndPoint)
    }

    fun fetchPopularOrTopRatedMovies(endPoint: String) {
        _moviesState.value = MovieUIState.Loading

        viewModelScope.launch {

            if (!showMovies) return@launch

            // References to keep track of both sibling jobs
            var jobPopular: Job?                            //  initialized, when assigned to 'launch'
            var jobTopRated: Job? = null

            // Capture the exact baseline start time
            val startTime = System.currentTimeMillis()

            jobPopular = launch(start = CoroutineStart.LAZY) {
                try {
                    val popularMovies = moviesRepository.getMovies(endPoint, API_KEY, 5)
                    if (popularMovies.isSuccessful) {

                        // WE HAVE A WINNER! Cancel the other job immediately
                        jobTopRated?.cancel()

                        // Capture the exact baseline end time
                        val endTime = System.currentTimeMillis()
                        Log.d("time_taken_popular", "time_taken: ${endTime - startTime}")

                        _moviesState.update {
                            // Update the current endpoint for UI Consumption @MoviesScreen.kt
                            currentEndPoint = endPoint
                            MovieUIState.Success(popularMovies.body()?.results ?: emptyList(), endPoint)
                        }
                        sendWinnerBroadcast(application, endPoint)

                    }
                } catch (e: Exception) {

                    // 🛑 CRITICAL: Ignore cancellation exception
                    if(e is CancellationException) throw e

                    // Only update error state if the job wasn't canceled by the winner
                    Log.e("MovieRace", "Error or cancellation in $endPoint", e)
                    _moviesState.value = MovieUIState.Error(e.message ?: "Something went wrong")
                }
            }

            jobTopRated = launch(start = CoroutineStart.LAZY) {

                val endPointTopRated = "top_rated"
                try {
                    val topRatedMovies = moviesRepository.getMovies(endPointTopRated, API_KEY, 5)
                    if (topRatedMovies.isSuccessful) {

                        // WE HAVE A WINNER! Cancel the other job immediately
                        jobPopular.cancel()

                        // Capture the exact baseline end time
                        val endTime = System.currentTimeMillis()
                        Log.d("time_taken_topRated", "time_taken: ${endTime - startTime}")

                        _moviesState.update {
                            // Update the current endpoint for UI Consumption @MoviesScreen.kt
                            currentEndPoint = endPointTopRated
                            MovieUIState.Success(topRatedMovies.body()?.results ?: emptyList(), endPointTopRated)
                        }
                        sendWinnerBroadcast(application, endPointTopRated)

                    }

                } catch (e: Exception) {

                    // 🛑 CRITICAL: Ignore cancellation exception
                    if(e is CancellationException) throw e

                    // Only update error state if the job wasn't canceled by the winner
                    Log.e("MovieRace", "Error or cancellation in $endPointTopRated", e)
                    _moviesState.value = MovieUIState.Error(e.message ?: "Something went wrong")
                }
            }

            // 4. BOTH jobs are now fully non-null and safely allocated.
            // Now we pull the trigger on both at the exact same instant!
            jobPopular.start()
            jobTopRated.start()
        }
    }

    private fun sendWinnerBroadcast(appContext: Application, winnerEndPoint: String) {
        val broadcastIntent = Intent(appContext, MovieBroadcastReceiver::class.java).apply {
            action = MovieBroadcastReceiver.ACTION_RACE_COMPLETE
            putExtra(MovieBroadcastReceiver.EXTRA_WINNER, winnerEndPoint)
        }
        appContext.sendBroadcast(broadcastIntent)
    }

    fun enableMoviesDataFetching(endPoint: String): String {
        if (!showMovies || currentEndPoint != endPoint) {
            showMovies = true
            currentEndPoint = endPoint
            isLoading = false
            fetchPopularOrTopRatedMovies(endPoint)
        }
        return endPoint
    }

}