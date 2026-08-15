package edu.review.moviesappreview.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.review.moviesappreview.data.repository.system.PowerRepository
import edu.review.moviesappreview.domain.repository.IPowerRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PowerModule {

    @Binds
    @Singleton
    abstract fun bindPowerRepository(
        powerRepository: IPowerRepository
    ): IPowerRepository
}
