package io.github.rmflawkdyd.luming.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.github.rmflawkdyd.luming.data.weather.remote.OpenMeteoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttp: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideOpenMeteoApi(retrofit: Retrofit): OpenMeteoApi =
        retrofit.create(OpenMeteoApi::class.java)
}
