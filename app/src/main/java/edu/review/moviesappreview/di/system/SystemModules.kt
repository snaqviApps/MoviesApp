package edu.review.moviesappreview.di.system

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.review.moviesappreview.data.repository.system.MoviesNotifierRepository
import edu.review.moviesappreview.data.repository.system.PowerRepository
import edu.review.moviesappreview.domain.repository.IPowerRepository
import edu.review.moviesappreview.domain.repository.system.MovieNotifier
import javax.inject.Singleton


/**
 * System / Platform Modules
 * (which handle Android OS APIs like PowerManager or LocationManager)
 *
 */


/**
 * Contacts, PowerManager to PowerRepository
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MoviesSystemPowerModules {

    @Binds
    @Singleton
    abstract fun bindsPowerRepository(
        powerRepository: PowerRepository
    ): IPowerRepository
}

/**
 *  Binds (establishes Contract) Notification OS API to
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class MoviesSystemNotificationModule {
    @Binds
    @Singleton
    abstract fun bindsMovieNotifier(
        movieBroadcastNotifier: MoviesNotifierRepository
    ): MovieNotifier
}
