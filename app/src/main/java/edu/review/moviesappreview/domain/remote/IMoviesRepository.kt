package edu.review.moviesappreview.domain.remote

import edu.review.moviesappreview.data.movies.Movies
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface IMoviesRepository {

    @GET
    suspend fun getMovies(
        @Url endPoint: String = "popular",
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): Response<Movies>
}

