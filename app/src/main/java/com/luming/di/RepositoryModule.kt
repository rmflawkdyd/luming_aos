package com.luming.di

import com.luming.data.activity.ActivityRepositoryImpl
import com.luming.data.location.LocationRepositoryImpl
import com.luming.data.recommender.RecommenderImpl
import com.luming.data.streak.StreakRepositoryImpl
import com.luming.data.weather.WeatherRepositoryImpl
import com.luming.domain.recommender.Recommender
import com.luming.domain.repository.ActivityRepository
import com.luming.domain.repository.LocationRepository
import com.luming.domain.repository.StreakRepository
import com.luming.domain.repository.WeatherRepository
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
