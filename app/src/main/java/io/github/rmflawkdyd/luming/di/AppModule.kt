package io.github.rmflawkdyd.luming.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import io.github.rmflawkdyd.luming.data.util.ClockImpl
import io.github.rmflawkdyd.luming.domain.util.Clock
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

private val Context.streakDataStore: DataStore<Preferences> by preferencesDataStore(name = "streak_prefs")
private val Context.weatherDataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_prefs")
private val Context.slotDataStore: DataStore<Preferences> by preferencesDataStore(name = "slot_prefs")
private val Context.notifDataStore: DataStore<Preferences> by preferencesDataStore(name = "notif_prefs")

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindClock(impl: ClockImpl): Clock

    companion object {
        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.streakDataStore

        @Provides
        @Singleton
        @Named("weather")
        fun provideWeatherDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.weatherDataStore

        @Provides
        @Singleton
        @Named("slot")
        fun provideSlotDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.slotDataStore

        @Provides
        @Singleton
        @Named("notif")
        fun provideNotifDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.notifDataStore
    }
}
