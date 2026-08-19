package edu.review.moviesappreview.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.review.moviesappreview.data.repository.remote.MoviesRepository
import edu.review.moviesappreview.domain.repository.remote.IMoviesRepository
import edu.review.moviesappreview.domain.repository.remote.MoviesApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Instantiates and provides third-party network client instances
 * (Retrofit and MoviesApiService) to Hilt using @Provides.
 */
@Module
@InstallIn(SingletonComponent::class)
object MoviesNetworkModule {

    @Provides
    @Singleton
    fun provideBaseUrl(): String = "https://api.themoviedb.org/3/movie/"

    @Singleton
    @Provides
    fun providesMoviesService(): MoviesApiService {
        return Retrofit
            .Builder()
            .baseUrl(provideBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MoviesApiService::class.java)
    }
}

/**
 * Binds the concrete data implementation (MoviesRepository) to its
 * abstract domain interface contract (IMoviesRepository) using @Binds
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MoviesRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMoviesRepository(
        moviesApiService: MoviesRepository
    ): IMoviesRepository

}
