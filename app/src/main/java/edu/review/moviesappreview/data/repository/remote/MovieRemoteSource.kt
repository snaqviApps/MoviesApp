package edu.review.moviesappreview.data.repository.remote

import edu.review.moviesappreview.data.movies.Movies
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Remote Data Source provider's Contract
 * here by Retrofit
 * @see [edu.review.moviesappreview.di.MoviesNetworkModule.providesMoviesService]
 */
interface MovieRemoteSource {

    @GET
    suspend fun getMovies(
        @Url endPoint: String = "popular",
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): Response<Movies>
}