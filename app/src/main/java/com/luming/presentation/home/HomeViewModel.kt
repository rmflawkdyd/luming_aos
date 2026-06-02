package com.luming.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luming.data.slotcompletion.SlotCompletionStore
import com.luming.data.weather.remote.mapper.WeatherMapper
import com.luming.domain.model.ContextSnapshot
import com.luming.domain.model.TimeBucket
import com.luming.domain.model.WeatherSnapshot
import com.luming.domain.repository.LocationRepository
import com.luming.domain.repository.WeatherRepository
import com.luming.domain.usecase.GetCurrentStreakUseCase
import com.luming.domain.usecase.GetRecommendationsUseCase
import com.luming.domain.util.Clock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecommendations: GetRecommendationsUseCase,
    private val getCurrentStreak: GetCurrentStreakUseCase,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val slotStore: SlotCompletionStore,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Empty)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var coldStartJob: Job? = null
    private var lastTimeBucket: TimeBucket? = null

    init {
        launchColdStart()
    }

    fun onResume() {
        val current = clock.timeBucket()
        val timeBucketChanged = lastTimeBucket != null && lastTimeBucket != current
        val recoverable = _uiState.value == HomeUiState.LocationFailed && locationRepository.hasPermission()
        // Re-check completed state on resume to handle date changes (AC-S7)
        val inCompletedSlot = _uiState.value is HomeUiState.CompletedSlot
        // Re-fetch if in-memory cache expired while backgrounded (§2.1 Background→foreground)
        val weatherCacheStale = weatherRepository.isWeatherCacheStale()
        // Re-fetch if date changed while bucket stayed the same (e.g. NIGHT across midnight)
        val today = clock.today()
        val dateChanged = when (val state = _uiState.value) {
            is HomeUiState.WeatherAware -> state.date != today
            is HomeUiState.TimeOnly -> state.date != today
            is HomeUiState.CompletedSlot -> state.date != today
            else -> false
        }
        if (timeBucketChanged || recoverable || inCompletedSlot || weatherCacheStale || dateChanged) {
            viewModelScope.launch {
                if (timeBucketChanged) weatherRepository.clearCache()
                launchColdStart()
            }
        }
    }

    private fun launchColdStart() {
        coldStartJob?.cancel()
        coldStartJob = viewModelScope.launch { coldStart() }
    }

    private suspend fun coldStart() {
        val today = clock.today()
        val timeBucket = clock.timeBucket()
        lastTimeBucket = timeBucket

        if (timeBucket != TimeBucket.NIGHT && slotStore.isCompleted(timeBucket, today)) {
            val streak = getCurrentStreak().first()
            val cachedWeather = weatherRepository.getLastCachedWeather()
            _uiState.value = HomeUiState.CompletedSlot(
                slot = timeBucket,
                streak = streak,
                date = today,
                weatherBucket = WeatherMapper.toWeatherBucket(cachedWeather),
            )
            return
        }

        // 캐시된 날씨가 있으면 즉시 WeatherAware로 렌더, 없으면 TimeOnly
        val cachedWeather = weatherRepository.getLastCachedWeather()
        val streak = getCurrentStreak().first()
        if (cachedWeather != null) {
            val ctx = buildContext(timeBucket, cachedWeather)
            _uiState.value = HomeUiState.WeatherAware(
                recommendations = getRecommendations(ctx),
                streak = streak,
                date = today,
                weatherBucket = WeatherMapper.toWeatherBucket(cachedWeather),
            )
        } else {
            val timeCtx = buildContext(timeBucket, weather = null)
            _uiState.value = HomeUiState.TimeOnly(getRecommendations(timeCtx), streak, today)
        }

        if (!locationRepository.hasPermission()) {
            if (cachedWeather == null) _uiState.value = HomeUiState.LocationFailed
            return
        }

        val location = withTimeoutOrNull(5_000L) { locationRepository.getCoarseLocation() }
        if (location == null) {
            if (cachedWeather == null) _uiState.value = HomeUiState.LocationFailed
            return
        }

        val weather = withTimeoutOrNull(5_000L) { weatherRepository.getWeather(location.first, location.second) }
        val currentStreak = getCurrentStreak().first()
        if (weather != null) {
            val currentBucket = clock.timeBucket()
            lastTimeBucket = currentBucket
            val weatherCtx = buildContext(currentBucket, weather)
            _uiState.value = HomeUiState.WeatherAware(
                recommendations = getRecommendations(weatherCtx),
                streak = currentStreak,
                date = today,
                weatherBucket = WeatherMapper.toWeatherBucket(weather),
            )
        } else if (cachedWeather == null) {
            _uiState.value = HomeUiState.WeatherFailed
        }
    }

    fun refresh() {
        // completed.* 상태에서는 호출되지 않음 (UI에서 pull-to-refresh 비활성)
        viewModelScope.launch {
            weatherRepository.clearCache()
            launchColdStart()
        }
    }

    fun showCompletionOverlay() {
        _uiState.update { it.withOverlay(true) }
        viewModelScope.launch {
            val today = clock.today()
            val bucket = clock.timeBucket()
            val freshStreak = getCurrentStreak().first()
            if (bucket != TimeBucket.NIGHT && slotStore.isCompleted(bucket, today)) {
                // 비-NIGHT 슬롯: CompletedSlot 상태로 전환 (AC-S1~S3)
                _uiState.update { current ->
                    val weatherBucket = (current as? HomeUiState.WeatherAware)?.weatherBucket
                    HomeUiState.CompletedSlot(
                        slot = bucket,
                        streak = freshStreak,
                        date = today,
                        weatherBucket = weatherBucket,
                        showCompletionOverlay = current.showsOverlay(),
                    )
                }
            } else {
                // NIGHT 완료: CompletedSlot 전환 없이 streak만 갱신 (스펙 §2.1 "streak은 정상 반영")
                _uiState.update { current ->
                    when (current) {
                        is HomeUiState.WeatherAware -> current.copy(streak = freshStreak)
                        is HomeUiState.TimeOnly -> current.copy(streak = freshStreak)
                        else -> current
                    }
                }
            }
        }
    }

    fun onCompletionOverlayDismissed() {
        _uiState.update { it.withOverlay(false) }
    }

    private fun buildContext(timeBucket: TimeBucket, weather: WeatherSnapshot?): ContextSnapshot =
        ContextSnapshot(
            timeBucket = timeBucket,
            weatherBucket = WeatherMapper.toWeatherBucket(weather),
            dayOfWeekHash = clock.dayOfWeekHash(),
            isPrecipitating = weather?.isPrecipitating ?: false,
        )
}
