package edu.review.moviesappreview.domain.remote

import edu.review.moviesappreview.data.movies.Movies
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface MoviesService {

    @GET
    suspend fun getMovies(
        @Url endPoint: String = "popular",
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): Response<Movies>
}

object RetrofitObject {
    val api : MoviesService = Retrofit
        .Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.themoviedb.org/3/movie/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MoviesService::class.java)

}