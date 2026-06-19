package io.github.rmflawkdyd.luming.di

import io.github.rmflawkdyd.luming.data.activity.ActivityRepositoryImpl
import io.github.rmflawkdyd.luming.data.location.LocationRepositoryImpl
import io.github.rmflawkdyd.luming.data.recommender.RecommenderImpl
import io.github.rmflawkdyd.luming.data.streak.StreakRepositoryImpl
import io.github.rmflawkdyd.luming.data.weather.WeatherRepositoryImpl
import io.github.rmflawkdyd.luming.domain.recommender.Recommender
import io.github.rmflawkdyd.luming.domain.repository.ActivityRepository
import io.github.rmflawkdyd.luming.domain.repository.LocationRepository
import io.github.rmflawkdyd.luming.domain.repository.StreakRepository
import io.github.rmflawkdyd.luming.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    abstract fun bindStreakRepository(impl: StreakRepositoryImpl): StreakRepository

    @Binds
    abstract fun bindRecommender(impl: RecommenderImpl): Recommender
}
