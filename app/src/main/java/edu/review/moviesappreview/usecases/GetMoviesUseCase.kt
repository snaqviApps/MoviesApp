package edu.review.moviesappreview.usecases

import edu.review.moviesappreview.domain.remote.IMoviesRepository
import javax.inject.Inject

class GetMoviesUseCase @Inject constructor(
    private val moviesRepository: IMoviesRepository
) {

    suspend operator fun invoke(
        endPoint: String,
        apiKey: String,
        page: Int
    ) = moviesRepository.getMovies(endPoint, apiKey, page)

}