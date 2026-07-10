package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PlannerDatabase
import com.example.data.PlannerRepository
import com.example.data.SubTaskEntity
import com.example.data.TaskEntity
import com.example.data.TimeBlockEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PlannerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PlannerRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("fa", "IR"))
    private val prefs = application.getSharedPreferences("duo_planner_prefs", Context.MODE_PRIVATE)

    private val _currentDate = MutableStateFlow(dateFormat.format(Date()))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    // Gamification state flows
    private val _totalXP = MutableStateFlow(prefs.getInt("total_xp", 120)) // default starting XP for nice progress
    val totalXP: StateFlow<Int> = _totalXP.asStateFlow()

    private val _isLimitBoosted = MutableStateFlow(prefs.getBoolean("is_limit_boosted", false))
    val isLimitBoosted: StateFlow<Boolean> = _isLimitBoosted.asStateFlow()

    private val _primaryLimit = MutableStateFlow(prefs.getInt("limit_primary", 3))
    val primaryLimit: StateFlow<Int> = _primaryLimit.asStateFlow()

    private val _routineLimit = MutableStateFlow(prefs.getInt("limit_routine", 4))
    val routineLimit: StateFlow<Int> = _routineLimit.asStateFlow()

    private val _personalLimit = MutableStateFlow(prefs.getInt("limit_personal", 3))
    val personalLimit: StateFlow<Int> = _personalLimit.asStateFlow()

    private val _heartsCount = MutableStateFlow(prefs.getInt("hearts_count", 5))
    val heartsCount: StateFlow<Int> = _heartsCount.asStateFlow()

    private val _mascotQuote = MutableStateFlow("سلام! امروز آماده‌ای تا با برنامه‌ریزی دوجین، طوفان به پا کنی؟ 🦉")
    val mascotQuote: StateFlow<String> = _mascotQuote.asStateFlow()

    init {
        val database = PlannerDatabase.getDatabase(application)
        repository = PlannerRepository(database.plannerDao())
        
        // Reset/Refill hearts if it's a new day
        val lastActiveDay = prefs.getString("last_active_day", "")
        val todayStr = dateFormat.format(Date())
        if (lastActiveDay != todayStr) {
            _heartsCount.value = 5
            prefs.edit().putInt("hearts_count", 5).putString("last_active_day", todayStr).apply()
        }
        updateMascotQuote()
    }

    // Reactively observe tasks for the selected date
    val tasks: StateFlow<List<TaskEntity>> = _currentDate
        .flatMapLatest { date -> repository.getTasksForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactively observe subtasks for the selected date
    val subtasks: StateFlow<List<SubTaskEntity>> = _currentDate
        .flatMapLatest { date -> repository.getSubTasksForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactively observe time blocks for the selected date
    val timeBlocks: StateFlow<List<TimeBlockEntity>> = _currentDate
        .flatMapLatest { date -> repository.getTimeBlocksForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Auto-populate default time blocks if they do not exist for the day, and sync routines
    init {
        viewModelScope.launch {
            _currentDate.collect { date ->
                repository.getTimeBlocksForDate(date).collect { blocks ->
                    if (blocks.isEmpty()) {
                        initializeDefaultTimeBlocks(date)
                    }
                }
            }
        }
        viewModelScope.launch {
            _currentDate.collect { date ->
                syncRoutinesForDate(date)
            }
        }
    }

    // Dynamic dopamine points score calculation:
    // +10 points for each completed subtask (atomized task)
    // +25 points for each completed full task
    // +15 points for completing a 90-minute time block
    val dopaminePoints: StateFlow<Int> = combine(tasks, subtasks, timeBlocks) { tList, sList, bList ->
        val subtaskScore = sList.count { it.isCompleted } * 10
        val taskScore = tList.count { it.isCompleted } * 25
        val blockScore = bList.count { it.isCompleted } * 15
        subtaskScore + taskScore + blockScore
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Formatted Persian Date for the UI
    val persianDateDisplay: StateFlow<String> = _currentDate
        .flatMapLatest { dateStr ->
            val flow = MutableStateFlow("")
            try {
                val jDate = JalaliCalendarHelper.getJalaliDate(dateStr)
                val dayName = jDate.dayOfWeekName
                val dayStr = JalaliCalendarHelper.toPersianDigits(jDate.day.toString())
                val monthName = jDate.monthName
                val yearStr = JalaliCalendarHelper.toPersianDigits(jDate.year.toString())
                flow.value = "$dayName، $dayStr $monthName $yearStr"
            } catch (e: Exception) {
                flow.value = dateStr
            }
            flow
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Current holiday name for the day if any
    val holidayName: StateFlow<String?> = _currentDate
        .flatMapLatest { dateStr ->
            val flow = MutableStateFlow<String?>(null)
            try {
                val jDate = JalaliCalendarHelper.getJalaliDate(dateStr)
                flow.value = jDate.holidayName
            } catch (e: Exception) {
                flow.value = null
            }
            flow
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Reactive Completed Dates for Streaks
    val completedDates: StateFlow<List<String>> = repository.getCompletedDates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive Streak Count dynamically computed from the database of completed tasks
    val streakCount: StateFlow<Int> = completedDates.flatMapLatest { datesList ->
        val flow = MutableStateFlow(0)
        try {
            val dateSet = datesList.toSet()
            val todayStr = dateFormat.format(Date())
            
            val cal = Calendar.getInstance()
            var currentCheckStr = todayStr
            
            val hasToday = dateSet.contains(todayStr)
            
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = dateFormat.format(cal.time)
            val hasYesterday = dateSet.contains(yesterdayStr)
            
            if (hasToday || hasYesterday) {
                var count = 0
                val searchCal = Calendar.getInstance()
                if (!hasToday && hasYesterday) {
                    searchCal.add(Calendar.DAY_OF_YEAR, -1)
                }
                
                while (true) {
                    val checkStr = dateFormat.format(searchCal.time)
                    if (dateSet.contains(checkStr)) {
                        count++
                        searchCal.add(Calendar.DAY_OF_YEAR, -1)
                    } else {
                        break
                    }
                }
                flow.value = count
            } else {
                flow.value = 0
            }
        } catch (e: Exception) {
            flow.value = 0
        }
        flow
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Date navigation
    fun navigateDays(days: Int) {
        viewModelScope.launch {
            try {
                val parsedDate = dateFormat.parse(_currentDate.value) ?: Date()
                val calendar = Calendar.getInstance()
                calendar.time = parsedDate
                calendar.add(Calendar.DAY_OF_YEAR, days)
                _currentDate.value = dateFormat.format(calendar.time)
                updateMascotQuote()
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    fun navigateToToday() {
        _currentDate.value = dateFormat.format(Date())
        updateMascotQuote()
    }

    // --- Task Limit Boost Setting (Enabling up to 20 tasks per day) ---
    fun toggleLimitBoost() {
        val newVal = !_isLimitBoosted.value
        _isLimitBoosted.value = newVal
        prefs.edit().putBoolean("is_limit_boosted", newVal).apply()
        updateMascotQuote()
    }

    // --- Gamification Helpers ---
    private fun addXP(amount: Int) {
        val newXP = (_totalXP.value + amount).coerceAtLeast(0)
        _totalXP.value = newXP
        prefs.edit().putInt("total_xp", newXP).apply()
        updateMascotQuote()
    }

    fun refillHearts() {
        if (_totalXP.value >= 50 && _heartsCount.value < 5) {
            addXP(-50)
            val newHearts = 5
            _heartsCount.value = newHearts
            prefs.edit().putInt("hearts_count", newHearts).apply()
            _mascotQuote.value = "قلب‌های تو کامل پر شد! برو که توالی روزانه‌ات قطع نشه! 🦉💖"
        }
    }

    fun loseHeart() {
        val newHearts = (_heartsCount.value - 1).coerceAtLeast(0)
        _heartsCount.value = newHearts
        prefs.edit().putInt("hearts_count", newHearts).apply()
        _mascotQuote.value = "آخ! یک قلب کم شد. نذار صفر بشه؛ کارها رو مرتب انجام بده! 😢"
    }

    fun updateMascotQuote() {
        val streak = (streakCount as? StateFlow<Int>)?.value ?: 0
        val boosted = _isLimitBoosted.value
        val hearts = _heartsCount.value

        val quotes = mutableListOf(
            "جغد دانا همه کارها رو زیر نظر داره... نذار تنبلی تو رو شکست بده! 🦉",
            "کارهای امروزت رو به گام‌های کوچک اتمیک تقسیم کردی؟ این راز موفقیته! ⚡",
            "هر گام اتمی، تو رو به سطوح بالاتر هدایت می‌کنه! بازی کن و یاد بگیر!",
            "امروز بهترین فرصت برای اضافه کردن کارهای بیشتر و بردن جایزه‌ است! 🌟"
        )

        if (streak > 0) {
            quotes.add("واو! تو در یک توالی شگفت‌انگیز $streak روزه هستی! شعله رو روشن نگه دار! 🔥")
        }
        if (boosted) {
            quotes.add("ظرفیت ۲۰ کار در روز فعال شد! تو واقعاً آماده طوفانی دوجین هستی! 🚀")
        }
        if (hearts <= 2) {
            quotes.add("قلب‌هات داره کم میشه! یکی از کارهای اتمیک رو تموم کن یا با ۵۰ امتیاز تجربه پرش کن! ❤️")
        }

        _mascotQuote.value = quotes.random()
    }

    fun setPrimaryLimit(limit: Int) {
        val safeLimit = limit.coerceAtLeast(1)
        _primaryLimit.value = safeLimit
        prefs.edit().putInt("limit_primary", safeLimit).apply()
        updateMascotQuote()
    }

    fun setRoutineLimit(limit: Int) {
        val safeLimit = limit.coerceAtLeast(1)
        _routineLimit.value = safeLimit
        prefs.edit().putInt("limit_routine", safeLimit).apply()
        updateMascotQuote()
    }

    fun setPersonalLimit(limit: Int) {
        val safeLimit = limit.coerceAtLeast(1)
        _personalLimit.value = safeLimit
        prefs.edit().putInt("limit_personal", safeLimit).apply()
        updateMascotQuote()
    }

    fun syncRoutinesForDate(date: String) {
        viewModelScope.launch {
            try {
                val allRoutines = repository.getAllRoutines()
                val currentTasks = repository.getTasksForDateOnce(date)
                val currentRoutineTitles = currentTasks
                    .filter { it.category == "ROUTINE" }
                    .map { it.title.trim().lowercase() }
                    .toSet()

                val routinesToCopy = allRoutines
                    .filter { it.title.trim().lowercase() !in currentRoutineTitles }
                    .distinctBy { it.title.trim().lowercase() }

                for (routine in routinesToCopy) {
                    val newRoutine = TaskEntity(
                        title = routine.title,
                        dateString = date,
                        category = "ROUTINE",
                        isCompleted = false,
                        orderIndex = routine.orderIndex
                    )
                    repository.insertTask(newRoutine)
                }
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    // --- Task Operations ---
    fun addTaskToBrainDump(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                dateString = _currentDate.value,
                category = "BRAIN_DUMP"
            )
            repository.insertTask(task)
            updateMascotQuote()
        }
    }

    fun moveTaskCategory(task: TaskEntity, targetCategory: String): Boolean {
        val currentTasksInTarget = tasks.value.count { it.category == targetCategory }
        
        val limit = when (targetCategory) {
            "PRIMARY" -> _primaryLimit.value
            "ROUTINE" -> _routineLimit.value
            "PERSONAL" -> _personalLimit.value
            else -> 999
        }

        if (currentTasksInTarget >= limit && targetCategory != "BRAIN_DUMP" && targetCategory != "BACKLOG") {
            _mascotQuote.value = "از مرز کارهای توصیه‌شده عبور کردی! اما به تو اعتماد دارم؛ پرقدرت ادامه بده! 🦉🔥"
        }

        viewModelScope.launch {
            repository.updateTask(task.copy(category = targetCategory))
            updateMascotQuote()
            
            // If it's a routine, automatically sync it for the current date and ensure copies are active
            if (targetCategory == "ROUTINE") {
                syncRoutinesForDate(_currentDate.value)
            }
        }
        return true
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val nextState = !task.isCompleted
            repository.updateTask(task.copy(isCompleted = nextState))
            if (nextState) {
                addXP(25)
                if (_heartsCount.value < 5) {
                    _heartsCount.value = (_heartsCount.value + 1).coerceAtMost(5)
                    prefs.edit().putInt("hearts_count", _heartsCount.value).apply()
                }
                _mascotQuote.value = "عالی بود! ۲۵ امتیاز تجربه دشت کردی و قلبت شارژ شد! 🎉"
            } else {
                addXP(-25)
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            if (task.category == "ROUTINE") {
                repository.deleteRoutinesByTitle(task.title)
            } else {
                repository.deleteTask(task)
            }
            loseHeart()
        }
    }

    // --- SubTask (Atomization) Operations ---
    fun addSubTask(taskId: Int, title: String, durationMinutes: Int) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val subTask = SubTaskEntity(
                taskId = taskId,
                title = title,
                durationMinutes = durationMinutes,
                isCompleted = false
            )
            repository.insertSubTask(subTask)
            updateMascotQuote()
        }
    }

    fun toggleSubTaskCompletion(subTask: SubTaskEntity) {
        viewModelScope.launch {
            val nextState = !subTask.isCompleted
            repository.updateSubTask(subTask.copy(isCompleted = nextState))
            if (nextState) {
                addXP(10)
                _mascotQuote.value = "گام اتمی حل شد! +۱۰ امتیاز تجربه شیرین به جیب زدی! ⚡"
            } else {
                addXP(-10)
            }
        }
    }

    fun deleteSubTask(subTask: SubTaskEntity) {
        viewModelScope.launch {
            repository.deleteSubTask(subTask)
            updateMascotQuote()
        }
    }

    // --- Time Block Operations ---
    fun toggleTimeBlockCompletion(block: TimeBlockEntity) {
        viewModelScope.launch {
            val nextState = !block.isCompleted
            repository.updateTimeBlock(block.copy(isCompleted = nextState))
            if (nextState) {
                addXP(15)
                _mascotQuote.value = "بلوک زمانی با موفقیت سپری شد! ۱۵ امتیاز تجربه به تو تعلق گرفت! 🕰️"
            } else {
                addXP(-15)
            }
        }
    }

    fun assignTaskToTimeBlock(block: TimeBlockEntity, taskId: Int?) {
        viewModelScope.launch {
            repository.updateTimeBlock(block.copy(assignedTaskId = taskId))
        }
    }

    private suspend fun initializeDefaultTimeBlocks(date: String) {
        val defaults = listOf(
            TimeBlockEntity(dateString = date, startTime = "06:30", endTime = "08:00", label = "بلوک اول (آموزش/تمرین اصلی)"),
            TimeBlockEntity(dateString = date, startTime = "08:30", endTime = "10:00", label = "بلوک دوم (کار عمیق اصلی)"),
            TimeBlockEntity(dateString = date, startTime = "10:30", endTime = "12:00", label = "بلوک سوم (کار عمیق اصلی)"),
            TimeBlockEntity(dateString = date, startTime = "12:30", endTime = "14:00", label = "بلوک چهارم (مرور/روتین)"),
            TimeBlockEntity(dateString = date, startTime = "15:00", endTime = "16:30", label = "بلوک پنجم (مرور/روتین)"),
            TimeBlockEntity(dateString = date, startTime = "17:00", endTime = "18:30", label = "بلوک ششم (مرور/روتین)"),
            TimeBlockEntity(dateString = date, startTime = "19:00", endTime = "20:30", label = "بلوک نجات (Rescue Block - جبران یا پاداش)", isRescueBlock = true)
        )
        repository.insertTimeBlocks(defaults)
    }
}
