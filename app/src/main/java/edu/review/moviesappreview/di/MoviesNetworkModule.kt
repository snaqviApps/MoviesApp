package edu.review.moviesappreview.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.review.moviesappreview.domain.remote.IMoviesRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MoviesNetworkModule {

    @Provides
    @Singleton
    fun provideBaseUrl(): String = "https://api.themoviedb.org/3/movie/"

    @Singleton
    @Provides
    fun provideMoviesService(): IMoviesRepository {
        return Retrofit
            .Builder()
            .baseUrl(provideBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IMoviesRepository::class.java)
    }

}
