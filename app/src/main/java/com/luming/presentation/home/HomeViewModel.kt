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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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

    private var loadJob: Job? = null
    private var lastTimeBucket: TimeBucket? = null

    init {
        launchLoad()
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
            is HomeUiState.RestPrompt -> state.date != today
            else -> false
        }
        if (timeBucketChanged || recoverable || inCompletedSlot || weatherCacheStale || dateChanged) {
            viewModelScope.launch {
                if (timeBucketChanged) weatherRepository.clearCache()
                launchLoad()
            }
        }
    }

    private fun launchLoad() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { loadHomeState() }
    }

    private suspend fun loadHomeState() {
        val today = clock.today()
        val timeBucket = clock.timeBucket()
        lastTimeBucket = timeBucket

        if (clock.isRestHour()) {
            val streak = getCurrentStreak().first()
            _uiState.value = HomeUiState.RestPrompt(streak = streak, date = today)
            return
        }

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

        // 신선한(≤30분) 날씨 캐시가 있으면 즉시 WeatherAware로 렌더 (대기 0초).
        // 없으면 TimeOnly로 깜빡이지 않도록 로딩(Empty)을 유지하고, 데드라인 안에
        // 최신 날씨를 받아 한 번에 렌더한다 (받기 전엔 스피너).
        val cachedWeather = weatherRepository.getLastCachedWeather()
        val streak = getCurrentStreak().first()
        if (cachedWeather != null) {
            _uiState.value = HomeUiState.WeatherAware(
                recommendations = getRecommendations(buildContext(timeBucket, cachedWeather)),
                streak = streak,
                date = today,
                weatherBucket = WeatherMapper.toWeatherBucket(cachedWeather),
            )
        }

        if (!locationRepository.hasPermission()) {
            if (cachedWeather == null) _uiState.value = HomeUiState.LocationFailed
            return
        }

        coroutineScope {
            // 신선한 캐시가 없을 때만: 데드라인(2.5초)을 넘기면 시간 기반으로 폴백.
            // 그 전에 최신 날씨가 오면 폴백은 취소되고 곧장 WeatherAware로 렌더된다.
            val fallbackJob = if (cachedWeather == null) {
                launch {
                    delay(FIRST_PAINT_DEADLINE_MS)
                    if (_uiState.value is HomeUiState.Empty) {
                        _uiState.value = HomeUiState.TimeOnly(
                            getRecommendations(buildContext(clock.timeBucket(), weather = null)),
                            streak,
                            today,
                        )
                    }
                }
            } else {
                null
            }

            val location = withTimeoutOrNull(5_000L) { locationRepository.getCoarseLocation() }
            if (location == null) {
                fallbackJob?.cancel()
                if (cachedWeather == null) _uiState.value = HomeUiState.LocationFailed
                return@coroutineScope
            }

            val weather = withTimeoutOrNull(5_000L) {
                weatherRepository.getWeather(location.first, location.second)
            }
            fallbackJob?.cancel()
            val currentStreak = getCurrentStreak().first()
            if (weather != null) {
                val currentBucket = clock.timeBucket()
                lastTimeBucket = currentBucket
                val newBucket = WeatherMapper.toWeatherBucket(weather)
                // 날씨 버킷·시간대·날짜가 그대로면 재렌더하지 않아 목록 깜빡임을 막는다.
                val current = _uiState.value
                val unchanged = current is HomeUiState.WeatherAware &&
                    current.weatherBucket == newBucket &&
                    current.date == today &&
                    currentBucket == timeBucket
                if (!unchanged) {
                    _uiState.value = HomeUiState.WeatherAware(
                        recommendations = getRecommendations(buildContext(currentBucket, weather)),
                        streak = currentStreak,
                        date = today,
                        weatherBucket = newBucket,
                    )
                }
            } else if (cachedWeather == null) {
                _uiState.value = HomeUiState.WeatherFailed
            }
        }
    }

    fun refresh() {
        // completed.* 상태에서는 호출되지 않음 (UI에서 pull-to-refresh 비활성)
        viewModelScope.launch {
            weatherRepository.clearCache()
            launchLoad()
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

    private companion object {
        // 신선한 날씨 캐시가 없을 때 첫 화면을 위해 최신 날씨를 기다리는 최대 시간.
        // 이 시간을 넘기면 시간 기반(TimeOnly)으로 폴백한다.
        const val FIRST_PAINT_DEADLINE_MS = 2_500L
    }
}
