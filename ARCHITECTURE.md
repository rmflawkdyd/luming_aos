# 루밍 Android — 아키텍처 가이드

> **기준 스펙:** `.omc/specs/feature-spec-luming.md` v0.5  
> **플랫폼:** Android 12+ (API 31+) | Kotlin | Jetpack Compose

---

## 1. 아키텍처 개요

**Clean Architecture + MVVM** 을 적용합니다.

```
┌─────────────────────────────────────────────────────┐
│                  Presentation Layer                  │
│   Compose UI  ←→  ViewModel  ←→  UiState (sealed)   │
└────────────────────────┬────────────────────────────┘
                         │ (UseCase 호출)
┌────────────────────────▼────────────────────────────┐
│                   Domain Layer                       │
│     UseCase  ←→  Domain Model  ←→  Repository (I/F) │
└────────────────────────┬────────────────────────────┘
                         │ (구현체)
┌────────────────────────▼────────────────────────────┐
│                    Data Layer                        │
│  Remote (Retrofit)  Local (DataStore / Assets)  TFLite│
└─────────────────────────────────────────────────────┘
```

### 레이어 원칙

| 규칙 | 설명 |
|---|---|
| **의존성 방향** | Presentation → Domain ← Data. Data는 Domain에만 의존 |
| **Domain은 순수** | Android 프레임워크 import 없음. Kotlin + kotlinx만 허용 |
| **ViewModel은 UI 상태만** | 비즈니스 로직은 UseCase에 위임 |
| **Repository는 인터페이스** | Domain에 정의, Data에서 구현 |

---

## 2. 기술 스택

| 분류 | 라이브러리 | 버전 | 용도 |
|---|---|---|---|
| UI | Jetpack Compose | BOM 최신 | 선언형 UI |
| 상태관리 | `StateFlow` + `collectAsStateWithLifecycle` | — | ViewModel → UI |
| DI | Hilt | 2.51+ | 의존성 주입 |
| 비동기 | Kotlin Coroutines + Flow | 1.8+ | 비동기 처리 |
| 네트워크 | Retrofit2 + OkHttp3 | — | Open-Meteo API |
| JSON | kotlinx.serialization | 1.6+ | API 응답 파싱, 액티비티 JSON |
| 날짜/시간 | kotlinx.datetime | 0.6+ | LocalDate, DST-safe 스트리크 |
| 영속성 | DataStore (Preferences) | 1.1+ | 스트리크 저장 |
| 위치 | FusedLocationProviderClient | play-services-location | Coarse 위치 |
| ML | TensorFlow Lite | 2.14+ | 다양성 선택기 (선택적) |
| 테스트 | JUnit5 + MockK + Turbine | — | 단위 테스트 |
| 테스트 | MockWebServer | — | AC-1b 네트워크 통합 테스트 |

---

## 3. 모듈 구조

v1은 **단일 `:app` 모듈**로 구성합니다. 레이어는 패키지로 분리합니다.

```
:app
├── Presentation (ui/)
├── Domain (domain/)
└── Data (data/)
```

> **v2 고려:** `:core:domain`, `:core:data`, `:feature:home`, `:feature:instruction` 으로 분리 가능.

---

## 4. 패키지 구조

```
com.luming/
│
├── LumingApp.kt                     # Application, @HiltAndroidApp
├── MainActivity.kt                  # Single Activity, NavHost 진입점
│
├── presentation/
│   ├── navigation/
│   │   └── LumingNavGraph.kt        # NavHost, Route 정의
│   │
│   ├── home/
│   │   ├── HomeScreen.kt            # @Composable, UiState 수신
│   │   ├── HomeViewModel.kt         # @HiltViewModel, cold-start 시퀀서
│   │   ├── HomeUiState.kt           # sealed interface
│   │   └── components/
│   │       ├── ActivityCard.kt
│   │       ├── StreakRing.kt
│   │       ├── SevenDayStrip.kt
│   │       ├── RationaleBanner.kt
│   │       └── CompletionOverlay.kt
│   │
│   ├── instruction/
│   │   ├── InstructionScreen.kt
│   │   ├── InstructionViewModel.kt
│   │   ├── InstructionUiState.kt
│   │   └── components/
│   │       ├── StepPager.kt
│   │       └── StepNavBar.kt
│   │
│   └── theme/
│       ├── Color.kt                 # DESIGN.md 팔레트
│       ├── Typography.kt            # DESIGN.md 타이포그래피
│       ├── Shape.kt
│       └── LumingTheme.kt
│
├── domain/
│   ├── model/                       # 순수 Kotlin data class
│   │   ├── Activity.kt
│   │   ├── Step.kt
│   │   ├── Streak.kt
│   │   ├── ContextSnapshot.kt
│   │   ├── WeatherSnapshot.kt
│   │   ├── Recommendation.kt
│   │   ├── TimeBucket.kt            # enum (MORNING/AFTERNOON/EVENING/NIGHT)
│   │   ├── WeatherBucket.kt         # enum
│   │   └── ContextTag.kt            # enum + @SerialName
│   │
│   ├── repository/                  # 인터페이스만 선언
│   │   ├── ActivityRepository.kt
│   │   ├── WeatherRepository.kt
│   │   ├── LocationRepository.kt
│   │   └── StreakRepository.kt
│   │
│   └── usecase/
│       ├── GetRecommendationsUseCase.kt
│       ├── MarkActivityCompleteUseCase.kt
│       └── GetCurrentStreakUseCase.kt
│
├── data/
│   ├── activity/
│   │   ├── ActivityRepositoryImpl.kt
│   │   └── local/
│   │       ├── ActivityLocalDataSource.kt   # assets/activities.v1.json 로드
│   │       └── dto/ActivityDto.kt           # JSON ↔ Domain 매핑
│   │
│   ├── weather/
│   │   ├── WeatherRepositoryImpl.kt
│   │   ├── remote/
│   │   │   ├── OpenMeteoApi.kt              # Retrofit interface
│   │   │   ├── dto/WeatherResponseDto.kt
│   │   │   └── mapper/WeatherMapper.kt      # DTO → Domain
│   │   └── cache/
│   │       └── WeatherInMemoryCache.kt      # 30분 TTL, 프로세스-스코프
│   │
│   ├── location/
│   │   ├── LocationRepositoryImpl.kt
│   │   └── CoarseLocationDataSource.kt      # FusedLocationProvider
│   │
│   ├── streak/
│   │   ├── StreakRepositoryImpl.kt
│   │   └── local/
│   │       └── StreakDataStore.kt            # DataStore<Preferences>
│   │
│   └── recommender/
│       ├── RuleScorer.kt                    # scoring_rules.v1.json 구현
│       ├── TFLiteSelector.kt                # selector.tflite 래퍼 (선택적)
│       └── RecommenderImpl.kt               # 필터 → 점수 → 선택 → N 결정
│
└── di/
    ├── NetworkModule.kt             # Retrofit, OkHttp 제공
    ├── RepositoryModule.kt          # Repository 바인딩
    ├── UseCaseModule.kt             # UseCase 제공 (필요시)
    └── MlModule.kt                  # TFLite Interpreter 제공
```

---

## 5. Domain Layer 상세

### 5.1 Domain Model (Kotlin)

```kotlin
// model/Activity.kt
data class Activity(
    val id: String,
    val name: String,
    val category: Category,
    val durationMin: Int,           // [5, 20]
    val steps: List<Step>,          // count >= 2, sorted by order
    val contextTags: Set<ContextTag>,
)

enum class Category { stretch, meditation, breathing }

// model/Step.kt
data class Step(val order: Int, val text: String)

// model/ContextSnapshot.kt
data class ContextSnapshot(
    val timeBucket: TimeBucket,
    val weatherBucket: WeatherBucket,
    val dayOfWeekHash: Int,          // 0..6
    val isPrecipitating: Boolean,
)

// model/Recommendation.kt
data class Recommendation(
    val activity: Activity,
    val rationale: String,
    val rank: Int,
)

// model/Streak.kt
data class Streak(
    val currentCount: Int,           // >= 0
    val lastCompletedDate: LocalDate?,
    val last7Days: List<Boolean>,    // size = 7, index 0 = 6일 전, index 6 = 오늘
)
```

### 5.2 Repository 인터페이스

```kotlin
// repository/ActivityRepository.kt
interface ActivityRepository {
    suspend fun getActivities(): List<Activity>
}

// repository/WeatherRepository.kt
interface WeatherRepository {
    /** 30분 캐시 포함. 실패 시 null 반환 (graceful). */
    suspend fun getWeather(lat: Double, lon: Double): WeatherSnapshot?
}

// repository/LocationRepository.kt
interface LocationRepository {
    /** Coarse 위치. 권한 없으면 null. */
    suspend fun getCoarseLocation(): Pair<Double, Double>?
}

// repository/StreakRepository.kt
interface StreakRepository {
    fun getStreak(): Flow<Streak>
    suspend fun markCompleted(today: LocalDate): Streak
}
```

### 5.3 UseCase

```kotlin
// usecase/GetRecommendationsUseCase.kt
class GetRecommendationsUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val recommender: Recommender,
) {
    suspend operator fun invoke(ctx: ContextSnapshot): List<Recommendation> {
        val activities = activityRepository.getActivities()
        return recommender.recommend(activities, ctx)
    }
}

// usecase/MarkActivityCompleteUseCase.kt
class MarkActivityCompleteUseCase @Inject constructor(
    private val streakRepository: StreakRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Streak =
        streakRepository.markCompleted(clock.today())
}

// usecase/GetCurrentStreakUseCase.kt
class GetCurrentStreakUseCase @Inject constructor(
    private val streakRepository: StreakRepository,
) {
    operator fun invoke(): Flow<Streak> = streakRepository.getStreak()
}
```

---

## 6. Presentation Layer 상세

### 6.1 UiState

```kotlin
// home/HomeUiState.kt
sealed interface HomeUiState {
    /** T=0: 시간 기반 카드 렌더링. 스피너 없음. */
    data class TimeOnly(
        val recommendations: List<Recommendation>,
        val streak: Streak,
        val date: LocalDate,
    ) : HomeUiState

    /** T+Tweather: 날씨 반영 완료 */
    data class WeatherAware(
        val recommendations: List<Recommendation>,
        val streak: Streak,
        val date: LocalDate,
        val showCompletionOverlay: Boolean = false,
    ) : HomeUiState

    /** 날씨 실패 / 위치 거부 → TimeOnly와 동일 UI, 최종 상태 */
    data class TimeOnlyFinal(
        val recommendations: List<Recommendation>,
        val streak: Streak,
        val date: LocalDate,
        val showCompletionOverlay: Boolean = false,
    ) : HomeUiState

    /** 방어적 폴백 (발생 불가 설계) */
    data object Empty : HomeUiState
}

// instruction/InstructionUiState.kt
data class InstructionUiState(
    val activity: Activity,
    val currentStepIndex: Int = 0,        // 0-based
    val isCompleting: Boolean = false,    // CompleteButton 비활성화용
) {
    val currentStep: Step get() = activity.steps.sortedBy { it.order }[currentStepIndex]
    val totalSteps: Int get() = activity.steps.size
    val isLastStep: Boolean get() = currentStepIndex == totalSteps - 1
    val isFirstStep: Boolean get() = currentStepIndex == 0
}
```

### 6.2 HomeViewModel — Cold-Start 시퀀서

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecommendations: GetRecommendationsUseCase,
    private val getCurrentStreak: GetCurrentStreakUseCase,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Empty)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            coldStart()
        }
    }

    private suspend fun coldStart() {
        val today = clock.today()
        val streak = getCurrentStreak().first()

        // T=0: 동기 시간 기반 렌더링 (I/O 없음)
        val timeCtx = buildContext(timeBucket = clock.timeBucket(), weather = null)
        val timeRecs = getRecommendations(timeCtx)
        _uiState.value = HomeUiState.TimeOnly(timeRecs, streak, today)

        // T+Tweather: 위치 + 날씨 비동기 fetch (최대 3초)
        val weather = withTimeoutOrNull(3_000L) {
            val location = locationRepository.getCoarseLocation() ?: return@withTimeoutOrNull null
            weatherRepository.getWeather(location.first, location.second)
        }

        val currentStreak = getCurrentStreak().first()
        if (weather != null) {
            val weatherCtx = buildContext(timeBucket = clock.timeBucket(), weather = weather)
            val weatherRecs = getRecommendations(weatherCtx)
            _uiState.value = HomeUiState.WeatherAware(weatherRecs, currentStreak, today)
        } else {
            // 날씨 실패: 시간 기반 카드 유지, 상태만 Final로 전환
            val current = _uiState.value as? HomeUiState.TimeOnly
            _uiState.value = HomeUiState.TimeOnlyFinal(
                recommendations = current?.recommendations ?: timeRecs,
                streak = currentStreak,
                date = today,
            )
        }
    }

    fun refresh() {
        // Pull-to-refresh: 캐시 무효화 후 날씨 재시도
        viewModelScope.launch {
            weatherRepository.clearCache()
            coldStart()
        }
    }

    fun onCompletionOverlayDismissed() {
        _uiState.update { state ->
            when (state) {
                is HomeUiState.WeatherAware -> state.copy(showCompletionOverlay = false)
                is HomeUiState.TimeOnlyFinal -> state.copy(showCompletionOverlay = false)
                else -> state
            }
        }
    }
}
```

### 6.3 InstructionViewModel

```kotlin
@HiltViewModel
class InstructionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val markActivityComplete: MarkActivityCompleteUseCase,
) : ViewModel() {

    private val activityId: String = checkNotNull(savedStateHandle["activityId"])

    private val _uiState = MutableStateFlow<InstructionUiState?>(null)
    val uiState: StateFlow<InstructionUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val activity = activityRepository.getActivities().first { it.id == activityId }
            _uiState.value = InstructionUiState(activity)
        }
    }

    fun goToNextStep() {
        _uiState.update { state ->
            state?.takeIf { !it.isLastStep }?.copy(currentStepIndex = it.currentStepIndex + 1)
        }
    }

    fun goToPreviousStep() {
        _uiState.update { state ->
            state?.takeIf { !it.isFirstStep }?.copy(currentStepIndex = it.currentStepIndex - 1)
        }
    }

    /** 완료 버튼 탭 → 스트리크 저장 후 Home으로 pop 신호 */
    fun complete(onComplete: () -> Unit) {
        _uiState.update { it?.copy(isCompleting = true) }
        viewModelScope.launch {
            markActivityComplete()
            onComplete()             // NavController.popBackStack()
        }
    }
}
```

### 6.4 Navigation

```kotlin
// navigation/LumingNavGraph.kt
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data class Instruction(val activityId: String) : Screen("instruction/{activityId}") {
        companion object {
            const val ROUTE = "instruction/{activityId}"
            fun createRoute(id: String) = "instruction/$id"
        }
    }
}

@Composable
fun LumingNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(
                uiState = vm.uiState.collectAsStateWithLifecycle().value,
                onActivityClick = { activity ->
                    navController.navigate(Screen.Instruction.createRoute(activity.id))
                },
                onRefresh = vm::refresh,
                onOverlayDismissed = vm::onCompletionOverlayDismissed,
            )
        }
        composable(
            route = Screen.Instruction.ROUTE,
            arguments = listOf(navArgument("activityId") { type = NavType.StringType }),
        ) {
            val vm: InstructionViewModel = hiltViewModel()
            InstructionScreen(
                uiState = vm.uiState.collectAsStateWithLifecycle().value,
                onNext = vm::goToNextStep,
                onPrevious = vm::goToPreviousStep,
                onComplete = { vm.complete { navController.popBackStack() } },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
```

---

## 7. Data Layer 상세

### 7.1 RecommenderImpl

```kotlin
class RecommenderImpl @Inject constructor(
    private val ruleScorer: RuleScorer,
    private val selector: TFLiteSelector,        // 선택적 (isLoaded 체크)
) : Recommender {

    override fun recommend(library: List<Activity>, ctx: ContextSnapshot): List<Recommendation> {
        // 1. Hard filter
        val candidates = applyFilters(library, ctx)
            .ifEmpty { library }                 // 빈 경우 필터 해제 폴백

        // 2. Rule scoring
        val scored = candidates
            .map { it to ruleScorer.score(it, ctx) }
            .sortedWith(compareByDescending<Pair<Activity, Int>> { it.second }.thenBy { it.first.id })

        // 3. Top-K = 3
        val topK = scored.take(3)

        // 4. Rule-relative N decision scores
        val ruleTop1Score = topK.getOrNull(0)?.second ?: 0
        val ruleTop2Score = topK.getOrNull(1)?.second ?: 0

        // 5. Selector (optional)
        val ordered = if (selector.isLoaded && topK.isNotEmpty()) {
            val chosenIdx = selector.pickIndex(ctx, topK)
            val chosen = topK[chosenIdx]
            val remaining = topK.filterIndexed { i, _ -> i != chosenIdx }
                .sortedWith(compareByDescending<Pair<Activity, Int>> { it.second }.thenBy { it.first.id })
            listOf(chosen) + remaining
        } else {
            topK
        }

        // 6. N decision
        val allZero = topK.all { it.second == 0 }
        val n = when {
            allZero -> ordered.size.coerceAtMost(3)
            ruleTop1Score >= 2 * ruleTop2Score -> 1
            ordered.size >= 3 && (ordered[1].second - ordered[2].second) > GAP_THRESHOLD -> 2
            else -> ordered.size.coerceAtMost(3)
        }

        return ordered.take(n).mapIndexed { idx, (activity, _) ->
            Recommendation(
                activity = activity,
                rationale = rationaleFor(activity, ctx),
                rank = idx + 1,
            )
        }
    }

    private companion object {
        const val GAP_THRESHOLD = 1
    }
}
```

### 7.2 TFLiteSelector

```kotlin
class TFLiteSelector @Inject constructor(
    private val interpreter: Interpreter?,      // null = 모델 없음 (selector-excluded 모드)
) {
    val isLoaded: Boolean get() = interpreter != null

    /**
     * context_one_hot: Float32[1, 17] (time 4 + weather 6 + day_hash 7)
     * candidate_indices: Int32[1, 3]  (PAD_INDEX=0 for missing slots)
     * 출력: argmax ∈ {0, 1, 2}
     */
    fun pickIndex(ctx: ContextSnapshot, topK: List<Pair<Activity, Int>>): Int {
        val interp = interpreter ?: return 0

        val contextOneHot = buildContextOneHot(ctx)     // FloatArray(17)
        val candidateIndices = buildCandidateIndices(topK) // IntArray(3), PAD=0

        val input = mapOf(
            0 to arrayOf(contextOneHot),
            1 to arrayOf(candidateIndices),
        )
        val logits = Array(1) { FloatArray(3) }
        val output = mapOf(0 to logits)

        interp.runSignature(input, output)

        val result = logits[0]
        // 패딩 위치 마스킹 (-inf)
        candidateIndices.forEachIndexed { i, idx ->
            if (idx == PAD_INDEX) result[i] = Float.NEGATIVE_INFINITY
        }
        return result.indices.maxByOrNull { result[it] } ?: 0
    }

    private companion object { const val PAD_INDEX = 0 }
}
```

### 7.3 WeatherInMemoryCache

```kotlin
class WeatherInMemoryCache {
    private data class Entry(val snapshot: WeatherSnapshot, val expiresAt: Instant)
    private val cache = ConcurrentHashMap<String, Entry>()

    fun get(lat: Double, lon: Double): WeatherSnapshot? {
        val key = cacheKey(lat, lon)
        val entry = cache[key] ?: return null
        return if (Clock.System.now() < entry.expiresAt) entry.snapshot else null.also { cache.remove(key) }
    }

    fun put(lat: Double, lon: Double, snapshot: WeatherSnapshot) {
        cache[cacheKey(lat, lon)] = Entry(
            snapshot = snapshot,
            expiresAt = Clock.System.now() + 30.minutes,
        )
    }

    fun clear() = cache.clear()

    private fun cacheKey(lat: Double, lon: Double) =
        "weather/${lat.roundTo1Dp()},${lon.roundTo1Dp()}"
}
```

### 7.4 StreakDataStore

```kotlin
class StreakDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val clock: Clock,
) {
    private val KEY = stringPreferencesKey("streak_completion_dates")

    fun getStreak(): Flow<Streak> = dataStore.data.map { prefs ->
        val dates = prefs[KEY]?.split(",")
            ?.filter { it.isNotBlank() }
            ?.map { LocalDate.parse(it) }
            ?.sorted()
            ?: emptyList()
        buildStreak(dates, clock.today())
    }

    suspend fun markCompleted(today: LocalDate): Streak {
        var result: Streak? = null
        dataStore.edit { prefs ->
            val dates = prefs[KEY]?.split(",")
                ?.filter { it.isNotBlank() }
                ?.map { LocalDate.parse(it) }
                ?.sorted()
                ?.toMutableList()
                ?: mutableListOf()

            val last = dates.lastOrNull()
            when {
                last == today -> { /* no-op */ }
                last == today.minus(1, DateTimeUnit.DAY) -> dates.add(today)
                else -> { dates.clear(); dates.add(today) }
            }

            val pruned = dates.takeLast(30)
            prefs[KEY] = pruned.joinToString(",") { it.toString() }
            result = buildStreak(pruned, today)
        }
        return result!!
    }

    private fun buildStreak(dates: List<LocalDate>, today: LocalDate): Streak {
        var count = 0
        var cursor = today
        while (dates.contains(cursor)) { count++; cursor = cursor.minus(1, DateTimeUnit.DAY) }
        val last7 = (6 downTo 0).map { offset -> dates.contains(today.minus(offset.toLong(), DateTimeUnit.DAY)) }
        return Streak(count, dates.lastOrNull(), last7)
    }
}
```

---

## 8. DI 모듈

### 8.1 NetworkModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun provideRetrofit(okHttp: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(okHttp)
        .addConverterFactory(kotlinx.serialization.json.Json.asConverterFactory(...))
        .build()

    @Provides @Singleton
    fun provideOpenMeteoApi(retrofit: Retrofit): OpenMeteoApi =
        retrofit.create(OpenMeteoApi::class.java)

    @Provides @Singleton
    fun provideWeatherCache(): WeatherInMemoryCache = WeatherInMemoryCache()
}
```

### 8.2 MlModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object MlModule {

    @Provides @Singleton
    fun provideTFLiteInterpreter(@ApplicationContext ctx: Context): Interpreter? = try {
        val model = FileUtil.loadMappedFile(ctx, "selector.tflite")
        val options = Interpreter.Options().apply {
            addDelegate(NnApiDelegate())         // NNAPI 가속; 불가 시 CPU 폴백
        }
        Interpreter(model, options)
    } catch (e: Exception) {
        null    // selector-excluded 모드: isLoaded = false
    }

    @Provides @Singleton
    fun provideTFLiteSelector(interpreter: Interpreter?): TFLiteSelector =
        TFLiteSelector(interpreter)
}
```

### 8.3 RepositoryModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindActivityRepo(impl: ActivityRepositoryImpl): ActivityRepository
    @Binds abstract fun bindWeatherRepo(impl: WeatherRepositoryImpl): WeatherRepository
    @Binds abstract fun bindLocationRepo(impl: LocationRepositoryImpl): LocationRepository
    @Binds abstract fun bindStreakRepo(impl: StreakRepositoryImpl): StreakRepository
}
```

---

## 9. 상태 흐름 다이어그램

```
앱 실행
   │
   ▼
HomeViewModel.init()
   │
   ├─ [동기, T=0] buildContext(time only)
   │              └─ GetRecommendationsUseCase(timeCtx)
   │                        │
   │                        ▼
   │              emit HomeUiState.TimeOnly      ← Compose 즉시 렌더링 (≤2s)
   │
   └─ [비동기, 최대 3s] withTimeoutOrNull(3000ms)
         │
         ├─ LocationRepository.getCoarseLocation()
         │        │ null(권한 없음)
         │        ▼
         │  WeatherRepository.getWeather(lat, lon)
         │        │                    │
         │        │ 성공               │ 실패/타임아웃
         │        ▼                    ▼
         │  emit WeatherAware    emit TimeOnlyFinal
         │  (카드 재정렬)         (카드 유지)
         │
         ▼
      UI 안정 상태
         │
         │ ActivityCard 탭
         ▼
InstructionScreen
         │
         │ CompleteButton (마지막 스텝)
         ▼
MarkActivityCompleteUseCase
         │
         │ popBackStack()
         ▼
HomeScreen + CompletionOverlay 표시
         │
         │ 1500ms or 탭
         ▼
HomeScreen (StreakRing 업데이트)
```

---

## 10. 테스트 전략

| 레이어 | 대상 | 도구 | AC |
|---|---|---|---|
| Domain | `RecommenderImpl` | JUnit5 + MockK | AC-5, AC-13 |
| Domain | `StreakDataStore` | JUnit5 + DataStore(in-memory) + MockK Clock | AC-7 |
| Domain | `RuleScorer` | JUnit5, golden_recommendations.json | AC-5 |
| Data | `OpenMeteoClient` | MockWebServer (OkHttp) | AC-1b, AC-2, AC-9 |
| Data | `TFLiteSelector` | JUnit5, 사전 학습 모델 | AC-5, AC-6 |
| Presentation | `HomeViewModel` cold-start | Turbine (Flow 테스트) | AC-1a, AC-1b |
| UI | 접근성 | Compose semantics test | AC-16 |
| CI | 정적 분석 | `grep ACCESS_FINE_LOCATION AndroidManifest.xml` | AC-10 |
| CI | 번들 크기 | `bundletool get-size total` | AC-11 |

### 핵심 테스트 패턴

```kotlin
// HomeViewModel cold-start 시퀀스 검증 (Turbine)
@Test fun `cold start emits TimeOnly then WeatherAware`() = runTest {
    val vm = HomeViewModel(/* mock deps */)
    vm.uiState.test {
        assertIs<HomeUiState.TimeOnly>(awaitItem())
        assertIs<HomeUiState.WeatherAware>(awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}

// StreakStore DST-safe 검증
@Test fun `consecutive days increment streak`() = runTest {
    val clock = FakeClock(LocalDate(2026, 5, 20))
    val store = StreakDataStore(testDataStore, clock)
    store.markCompleted(clock.today())

    clock.advanceDay()
    val streak = store.markCompleted(clock.today())

    assertEquals(2, streak.currentCount)
}
```

---

## 11. 퍼미션 선언 (AndroidManifest.xml)

```xml
<!-- 허용 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- 절대 포함 금지 (AC-10) -->
<!-- android.permission.ACCESS_FINE_LOCATION -->
<!-- android.permission.ACCESS_BACKGROUND_LOCATION -->
```

---

## 12. 주요 제약 체크리스트

| 항목 | 구현 위치 | AC |
|---|---|---|
| GPS 미사용 | `CoarseLocationDataSource`: `PRIORITY_LOW_POWER` only | AC-10 |
| 날씨 실패 시 time-only | `HomeViewModel.coldStart()` withTimeoutOrNull | AC-9 |
| 온 디바이스 AI | `MlModule`: 번들 내 `.tflite` | AC-6, AC-11 |
| 30분 날씨 캐시 | `WeatherInMemoryCache` (프로세스 스코프) | AC-2 |
| 스트리크 DST-safe | `LocalDate.now(ZoneId.systemDefault())` | AC-7 |
| 완료 버튼 더블탭 방지 | `InstructionUiState.isCompleting` | AC-14 |
| 셀렉터 없이도 동작 | `TFLiteSelector.isLoaded` 폴백 | AC-6 |
| Open-Meteo 외 아웃바운드 없음 | `NetworkModule` baseUrl 고정 | AC-12 |
