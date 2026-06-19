package io.github.rmflawkdyd.luming.presentation.instruction

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import io.github.rmflawkdyd.luming.data.slotcompletion.SlotCompletionStore
import io.github.rmflawkdyd.luming.domain.model.Activity
import io.github.rmflawkdyd.luming.domain.model.Category
import io.github.rmflawkdyd.luming.domain.model.Step
import io.github.rmflawkdyd.luming.domain.model.Streak
import io.github.rmflawkdyd.luming.domain.model.TimeBucket
import io.github.rmflawkdyd.luming.domain.repository.ActivityRepository
import io.github.rmflawkdyd.luming.domain.repository.StreakRepository
import io.github.rmflawkdyd.luming.domain.usecase.MarkActivityCompleteUseCase
import io.github.rmflawkdyd.luming.domain.util.Clock
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
class InstructionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val tempFile = File.createTempFile("slot_vm_test", ".preferences_pb")
        .also { it.deleteOnExit() }
    private val dataStore = PreferenceDataStoreFactory.create(
        scope = testScope.backgroundScope,
        produceFile = { tempFile },
    )
    private val today = LocalDate(2026, 6, 1)
    private val slotStore = SlotCompletionStore(dataStore)

    private val testActivity = Activity(
        id = "test_stretch",
        name = "Test Stretch",
        category = Category.STRETCH,
        durationMin = 10,
        steps = listOf(Step(order = 1, text = "Step 1")),
        contextTags = emptySet(),
    )

    @Before fun setup() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain(); tempFile.delete() }

    private fun buildViewModel(bucket: TimeBucket = TimeBucket.MORNING): InstructionViewModel {
        val clock = FakeClock(today, bucket)
        return InstructionViewModel(
            savedStateHandle = SavedStateHandle(mapOf("activityId" to testActivity.id)),
            activityRepository = FakeActivityRepository(listOf(testActivity)),
            markActivityComplete = MarkActivityCompleteUseCase(FakeStreakRepository(), clock),
            slotStore = slotStore,
            clock = clock,
        )
    }

    // AC-T1 / AC-T7: 타이머 미시작 상태
    @Test fun `초기 상태 - isTimerRunning false`() = testScope.runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        assertThat(vm.uiState.value?.isTimerRunning).isFalse()
    }

    @Test fun `초기 상태 - timerStartedAt null`() = testScope.runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        assertThat(vm.uiState.value?.timerStartedAt).isNull()
    }

    // AC-T2: startTimer 후 타이머 실행 중
    @Test fun `startTimer 후 isTimerRunning true`() = testScope.runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.startTimer()
        assertThat(vm.uiState.value?.isTimerRunning).isTrue()
    }

    // AC-T4: elapsed < 80% → TimerWarningDialog 표시
    @Test fun `requestComplete - elapsed 80% 미만 → showTimerWarning true`() = testScope.runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.startTimer()
        val threshold = (testActivity.durationMin * 60_000L * 0.8).toLong()
        vm.requestComplete(elapsedMs = threshold - 1L, onNavigate = {})
        assertThat(vm.uiState.value?.showTimerWarning).isTrue()
    }

    // AC-T5: elapsed >= 80% → 다이얼로그 없이 즉시 완료
    @Test fun `requestComplete - elapsed 80% 이상 → 다이얼로그 없이 navigate`() = testScope.runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.startTimer()
        val threshold = (testActivity.durationMin * 60_000L * 0.8).toLong()
        var navigated = false
        vm.requestComplete(elapsedMs = threshold, onNavigate = { navigated = true })
        advanceUntilIdle()
        assertThat(vm.uiState.value?.showTimerWarning).isFalse()
        assertThat(navigated).isTrue()
    }

    // AC-T6: confirmComplete → 완료 처리
    @Test fun `confirmComplete - 다이얼로그 닫히고 navigate 호출`() = testScope.runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.startTimer()
        vm.requestComplete(elapsedMs = 0L, onNavigate = {})
        assertThat(vm.uiState.value?.showTimerWarning).isTrue()
        var navigated = false
        vm.confirmComplete(onNavigate = { navigated = true })
        advanceUntilIdle()
        assertThat(vm.uiState.value?.showTimerWarning).isFalse()
        assertThat(navigated).isTrue()
    }

    // AC-T6: dismissWarning → 타이머 계속 진행
    @Test fun `dismissWarning - 다이얼로그만 닫히고 navigate 미호출`() = testScope.runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.startTimer()
        vm.requestComplete(elapsedMs = 0L, onNavigate = {})
        var navigated = false
        vm.dismissWarning()
        assertThat(vm.uiState.value?.showTimerWarning).isFalse()
        assertThat(navigated).isFalse()
        assertThat(vm.uiState.value?.isTimerRunning).isTrue()
    }

    // 스펙 §2.2 aborting: 타이머 미시작 시 뒤로가기 → 다이얼로그 없이 즉시 navigateBack
    @Test fun `requestAbort - 타이머 미시작 → 다이얼로그 없이 navigateBack true`() = testScope.runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.requestAbort()
        assertThat(vm.uiState.value?.showAbortWarning).isFalse()
        assertThat(vm.uiState.value?.navigateBack).isTrue()
    }

    // 스펙 §2.2 aborting: 타이머 진행 중 뒤로가기 → 이탈 다이얼로그 표시, navigateBack 미발행
    @Test fun `requestAbort - 타이머 진행 중 → showAbortWarning true, navigateBack false`() = testScope.runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.startTimer()
        vm.requestAbort()
        assertThat(vm.uiState.value?.showAbortWarning).isTrue()
        assertThat(vm.uiState.value?.navigateBack).isFalse()
        assertThat(vm.uiState.value?.isTimerRunning).isTrue()
    }

    // 그만두기: 다이얼로그 닫히고(먼저) 타이머 폐기 후 navigateBack 발행
    @Test fun `confirmAbort - 다이얼로그 닫히고 타이머 폐기, navigateBack true`() = testScope.runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.startTimer()
        vm.requestAbort()
        vm.confirmAbort()
        assertThat(vm.uiState.value?.showAbortWarning).isFalse()
        assertThat(vm.uiState.value?.isTimerRunning).isFalse()
        assertThat(vm.uiState.value?.navigateBack).isTrue()
    }

    // 계속하기: 다이얼로그만 닫히고 타이머 유지, navigateBack 미발행
    @Test fun `dismissAbort - 다이얼로그만 닫히고 타이머 유지, navigateBack false`() = testScope.runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.startTimer()
        vm.requestAbort()
        vm.dismissAbort()
        assertThat(vm.uiState.value?.showAbortWarning).isFalse()
        assertThat(vm.uiState.value?.isTimerRunning).isTrue()
        assertThat(vm.uiState.value?.navigateBack).isFalse()
    }

    // 이탈 시 streak/슬롯 기록 없음 (no side effects)
    @Test fun `confirmAbort - SlotCompletionStore 기록 없음`() = testScope.runTest {
        val vm = buildViewModel(bucket = TimeBucket.MORNING)
        advanceUntilIdle()
        vm.startTimer()
        vm.requestAbort()
        vm.confirmAbort()
        advanceUntilIdle()
        assertThat(slotStore.isCompleted(TimeBucket.MORNING, today)).isFalse()
    }

    // ADR-011: NIGHT 슬롯 완료는 SlotCompletionStore에 기록 안 됨
    @Test fun `NIGHT 슬롯에서 완료 - SlotCompletionStore 기록 없음`() = testScope.runTest {
        val vm = buildViewModel(bucket = TimeBucket.NIGHT)
        advanceUntilIdle()
        vm.startTimer()
        val targetMs = testActivity.durationMin * 60_000L
        vm.requestComplete(elapsedMs = targetMs, onNavigate = {})
        advanceUntilIdle()
        assertThat(slotStore.isCompleted(TimeBucket.NIGHT, today)).isFalse()
        assertThat(slotStore.isCompleted(TimeBucket.MORNING, today)).isFalse()
    }

    // Fake 헬퍼
    private class FakeClock(
        private val date: LocalDate,
        private val bucket: TimeBucket = TimeBucket.MORNING,
    ) : Clock {
        override fun today() = date
        override fun timeBucket() = bucket
        override fun dayOfWeekHash() = 0
        override fun isRestHour() = false
    }

    private class FakeActivityRepository(private val activities: List<Activity>) : ActivityRepository {
        override suspend fun getActivities(): List<Activity> = activities
    }

    private class FakeStreakRepository : StreakRepository {
        override fun getStreak(): Flow<Streak> = flowOf(Streak(0, null, List(7) { false }))
        override suspend fun markCompleted(today: LocalDate) = Streak(1, today, List(7) { false })
    }
}
