package io.github.rmflawkdyd.luming.di

import android.content.Context
import io.github.rmflawkdyd.luming.data.recommender.ActivityIndexTableDto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MlModule {

    private val json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideTFLiteInterpreter(@ApplicationContext ctx: Context): Interpreter? = try {
        val bytes = ctx.assets.open("selector.tflite").use { it.readBytes() }
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder())
        val options = Interpreter.Options().apply { setNumThreads(2) }
        Interpreter(buffer, options)
    } catch (e: Exception) {
        null
    }

    @Provides
    @Singleton
    @Named("activityIndexTable")
    fun provideActivityIndexTable(@ApplicationContext ctx: Context): Map<String, Int> = try {
        val text = ctx.assets.open("activity_index_table.v1.json").bufferedReader().readText()
        json.decodeFromString<ActivityIndexTableDto>(text).table
    } catch (e: Exception) {
        emptyMap()
    }
}
