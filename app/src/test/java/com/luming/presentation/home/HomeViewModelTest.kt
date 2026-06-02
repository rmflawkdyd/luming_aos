package com.luming.presentation.home

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.common.truth.Truth.assertThat
import com.luming.data.slotcompletion.SlotCompletionStore
import com.luming.domain.model.Activity
import com.luming.domain.model.Category
import com.luming.domain.model.ContextSnapshot
import com.luming.domain.model.ContextTag
import com.luming.domain.model.Recommendation
import com.luming.domain.model.Step
import com.luming.domain.model.Streak
import com.luming.domain.model.TimeBucket
import com.luming.domain.model.WeatherBucket
import com.luming.domain.model.WeatherCondition
import com.luming.domain.model.WeatherSnapshot
import com.luming.domain.recommender.Recommender
import com.luming.domain.repository.ActivityRepository
import com.luming.domain.repository.LocationRepository
import com.luming.domain.repository.StreakRepository
import com.luming.domain.repository.WeatherRepository
import com.luming.domain.usecase.GetCurrentStreakUseCase
import com.luming.domain.usecase.GetRecommendationsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val tempFile = File.createTempFile("slot_home_test", ".preferences_pb")
        .also { it.deleteOnExit() }
    private val dataStore = PreferenceDataStoreFactory.create(
        scope = testScope.backgroundScope,
        produceFile = { tempFile },
    )

    private val today = LocalDate(2026, 6, 2)
    private val slotStore = SlotCompletionStore(dataStore)

    private val stubActivity = Activity(
        id = "stretch_test_01",
        name = "Test Stretch",
        category = Category.STRETCH,
        durationMin = 8,
        steps = listOf(Step(1, "Step 1"), Step(2, "Step 2")),
        contextTags = setOf(ContextTag.MORNING, ContextTag.INDOOR),
    )
    private val stubRecommendations = listOf(
        Recommendation(activity = stubActivity, rationale = "Based on time of day.", rank = 1),
    )
    private val stubWeather = WeatherSnapshot(
        condition = WeatherCondition.CLEAR,
        temperatureC = 20.0,
        isPrecipitating = false,
        fetchedAt = kotlinx.datetime.Clock.System.now(),
    )

    @Before fun setup() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain(); tempFile.delete() }

    private fun buildViewModel(
        locationRepo: LocationRepository = FakeLocationRepository(),
        weatherRepo: WeatherRepository = FakeWeatherRepository(),
        timeBucket: TimeBucket = TimeBucket.MORNING,
        streakRepo: StreakRepository = FakeStreakRepository(),
    ): HomeViewModel = HomeViewModel(
        getRecommendations = GetRecommendationsUseCase(
            activityRepository = FakeActivityRepository(listOf(stubActivity)),
            recommender = FakeRecommender(stubRecommendations),
        ),
        getCurrentStreak = GetCurrentStreakUseCase(streakRepo),
        locationRepository = locationRepo,
        weatherRepository = weatherRepo,
        slotStore = slotStore,
        clock = FakeClock(today, timeBucket),
    )

    // ─── AC-9: 위치 권한 거부 케이스 ────────────────────────────────────────────

    @Test fun `권한 거부 + 캐시 없음 - LocationFailed 전환`() = testScope.runTest {
        val vm = buildViewModel(
            locationRepo = FakeLocationRepository(permissionGranted = false),
            weatherRepo = FakeWeatherRepository(cachedWeather = null),
        )
        advanceUntilIdle()
        assertThat(vm.uiState.value).isEqualTo(HomeUiState.LocationFailed)
    }

    @Test fun `권한 거부 + 캐시 있음 - WeatherAware 유지 (LocationFailed 미전환)`() = testScope.runTest {
        val vm = buildViewModel(
            locationRepo = FakeLocationRepository(permissionGranted = false),
            weatherRepo = FakeWeatherRepository(cachedWeather = stubWeather),
        )
        advanceUntilIdle()
        assertThat(vm.uiState.value).isInstanceOf(HomeUiState.WeatherAware::class.java)
    }

    // ─── AC-9: 위치 fetch 실패(타임아웃/null) 케이스 ────────────────────────────

    @Test fun `권한 있음 + 위치 fetch null + 캐시 없음 - LocationFailed 전환`() = testScope.runTest {
        val vm = buildViewModel(
            locationRepo = FakeLocationRepository(permissionGranted = true, location = null),
            weatherRepo = FakeWeatherRepository(cachedWeather = null),
        )
        advanceUntilIdle()
        assertThat(vm.uiState.value).isEqualTo(HomeUiState.LocationFailed)
    }

    @Test fun `권한 있음 + 위치 fetch null + 캐시 있음 - WeatherAware 유지`() = testScope.runTest {
        val vm = buildViewModel(
            locationRepo = FakeLocationRepository(permissionGranted = true, location = null),
            weatherRepo = FakeWeatherRepository(cachedWeather = stubWeather),
        )
        advanceUntilIdle()
        assertThat(vm.uiState.value).isInstanceOf(HomeUiState.WeatherAware::class.java)
    }

    // ─── AC-9: 날씨 fetch 실패 케이스 ───────────────────────────────────────────

    @Test fun `권한 있음 + 날씨 fetch null + 캐시 없음 - WeatherFailed 전환`() = testScope.runTest {
        val vm = buildViewModel(
            locationRepo = FakeLocationRepository(
                permissionGranted = true,
                location = Pair(37.5, 127.0),
            ),
            weatherRepo = FakeWeatherRepository(cachedWeather = null, liveWeather = null),
        )
        advanceUntilIdle()
        assertThat(vm.uiState.value).isEqualTo(HomeUiState.WeatherFailed)
    }

    @Test fun `권한 있음 + 날씨 fetch null + 캐시 있음 - WeatherAware 유지`() = testScope.runTest {
        val vm = buildViewModel(
            locationRepo = FakeLocationRepository(
                permissionGranted = true,
                location = Pair(37.5, 127.0),
            ),
            weatherRepo = FakeWeatherRepository(cachedWeather = stubWeather, liveWeather = null),
        )
        advanceUntilIdle()
        assertThat(vm.uiState.value).isInstanceOf(HomeUiState.WeatherAware::class.java)
    }

    // ─── 정상 경로 ────────────────────────────────────────────────────────────────

    @Test fun `정상 경로 - WeatherAware 상태`() = testScope.runTest {
        val vm = buildViewModel(
            locationRepo = FakeLocationRepository(
                permissionGranted = true,
                location = Pair(37.5, 127.0),
            ),
            weatherRepo = FakeWeatherRepository(cachedWeather = null, liveWeather = stubWeather),
        )
        advanceUntilIdle()
        assertThat(vm.uiState.value).isInstanceOf(HomeUiState.WeatherAware::class.java)
    }

    @Test fun `캐시된 날씨 있음 - 즉시 WeatherAware로 초기 렌더`() = testScope.runTest {
        val vm = buildViewModel(
            locationRepo = FakeLocationRepository(
                permissionGranted = true,
                location = Pair(37.5, 127.0),
            ),
            weatherRepo = FakeWeatherRepository(
                cachedWeather = stubWeather,
                liveWeather = stubWeather,
            ),
        )
        advanceUntilIdle()
        assertThat(vm.uiState.value).isInstanceOf(HomeUiState.WeatherAware::class.java)
    }

    // ─── showCompletionOverlay: NIGHT 슬롯 streak 갱신 ──────────────────────────

    @Test fun `showCompletionOverlay - NIGHT 슬롯 완료 시 streak이 fresh 값으로 갱신됨`() =
        testScope.runTest {
            // coldStart 때 count=0, showCompletionOverlay 때 count=5 반환하는 순차 repo
            val updatedStreak = Streak(5, today, List(7) { it == 6 })
            val streakRepo = SequentialStreakRepository(
                initial = Streak(0, null, List(7) { false }),
                updated = updatedStreak,
            )
            val vm = buildViewModel(
                timeBucket = TimeBucket.NIGHT,
                streakRepo = streakRepo,
                locationRepo = FakeLocationRepository(permissionGranted = false),
                weatherRepo = FakeWeatherRepository(cachedWeather = stubWeather),
            )
            advanceUntilIdle()
            // 초기 상태: WeatherAware with streak.count=0
            val initial = vm.uiState.value as HomeUiState.WeatherAware
            assertThat(initial.streak.currentCount).isEqualTo(0)

            vm.showCompletionOverlay()
            advanceUntilIdle()

            // NIGHT이므로 CompletedSlot이 아니라 WeatherAware 유지, streak만 갱신
            val after = vm.uiState.value as HomeUiState.WeatherAware
            assertThat(after.streak.currentCount).isEqualTo(5)
        }

    // ─── onResume: LocationFailed 복구 ──────────────────────────────────────────

    @Test fun `onResume - LocationFailed 상태에서 권한 부여 시 WeatherAware로 복구`() =
        testScope.runTest {
            val locationRepo = ConfigurableLocationRepository(
                permissionGranted = false,
                location = Pair(37.5, 127.0),
            )
            val weatherRepo = FakeWeatherRepository(cachedWeather = null, liveWeather = stubWeather)
            val vm = buildViewModel(locationRepo = locationRepo, weatherRepo = weatherRepo)
            advanceUntilIdle()
            assertThat(vm.uiState.value).isEqualTo(HomeUiState.LocationFailed)

            locationRepo.permissionGranted = true
            vm.onResume()
            advanceUntilIdle()
            assertThat(vm.uiState.value).isInstanceOf(HomeUiState.WeatherAware::class.java)
        }

    // ─── Fakes ───────────────────────────────────────────────────────────────────

    private class FakeClock(
        private val date: LocalDate,
        private val bucket: TimeBucket = TimeBucket.MORNING,
    ) : com.luming.domain.util.Clock {
        override fun today() = date
        override fun timeBucket() = bucket
        override fun dayOfWeekHash() = 1
    }

    private class FakeLocationRepository(
        private val permissionGranted: Boolean = true,
        private val location: Pair<Double, Double>? = Pair(37.5, 127.0),
    ) : LocationRepository {
        override fun hasPermission() = permissionGranted
        override suspend fun getCoarseLocation() = location
    }

    private class ConfigurableLocationRepository(
        var permissionGranted: Boolean,
        private val location: Pair<Double, Double>?,
    ) : LocationRepository {
        override fun hasPermission() = permissionGranted
        override suspend fun getCoarseLocation() = if (permissionGranted) location else null
    }

    private class FakeWeatherRepository(
        private val cachedWeather: WeatherSnapshot? = null,
        private val liveWeather: WeatherSnapshot? = null,
    ) : WeatherRepository {
        override suspend fun getWeather(lat: Double, lon: Double) = liveWeather
        override suspend fun getLastCachedWeather() = cachedWeather
        override fun clearCache() {}
        override fun isWeatherCacheStale() = cachedWeather == null
    }

    private class FakeStreakRepository(
        private val streak: Streak = Streak(0, null, List(7) { false }),
    ) : StreakRepository {
        override fun getStreak(): Flow<Streak> = flowOf(streak)
        override suspend fun markCompleted(today: LocalDate) =
            Streak(1, today, List(7) { it == 6 })
    }

    /** coldStart(초기) 때는 initial, 이후 호출부터는 updated를 반환한다. */
    private class SequentialStreakRepository(
        private val initial: Streak,
        private val updated: Streak,
    ) : StreakRepository {
        private var callCount = 0
        override fun getStreak(): Flow<Streak> =
            flowOf(if (callCount++ == 0) initial else updated)
        override suspend fun markCompleted(today: LocalDate) = updated
    }

    private class FakeActivityRepository(
        private val activities: List<Activity>,
    ) : ActivityRepository {
        override suspend fun getActivities() = activities
    }

    private class FakeRecommender(
        private val result: List<Recommendation>,
    ) : Recommender {
        override fun recommend(library: List<Activity>, ctx: ContextSnapshot) = result
    }
}
