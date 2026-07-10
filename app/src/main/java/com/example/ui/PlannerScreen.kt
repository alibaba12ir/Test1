package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SubTaskEntity
import com.example.data.TaskEntity
import com.example.data.TimeBlockEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuolingoMockWidget2x1(
    streakCount: Int,
    heartsCount: Int,
    totalXP: Int,
    onRefillHearts: () -> Unit
) {
    val level = (totalXP / 100) + 1
    val progressXP = totalXP % 100
    val progressPercent = progressXP.toFloat() / 100f

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF58CC02)), // Vibrant Duolingo Green
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .border(BorderStroke(2.5.dp, Color(0xFF46A302)), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Mascot Emoji on white background circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🦉", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Progress stats
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = JalaliCalendarHelper.toPersianDigits(streakCount.toString()) + " روز",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("❤️", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = JalaliCalendarHelper.toPersianDigits(heartsCount.toString()),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { if (heartsCount < 5) onRefillHearts() }
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                // Level label
                Text(
                    text = "سطح " + JalaliCalendarHelper.toPersianDigits(level.toString()) + " (${JalaliCalendarHelper.toPersianDigits(progressXP.toString())}/۱۰۰)",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // XP Progress visual block
            Box(
                modifier = Modifier
                    .width(55.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressPercent)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFC000))
                )
            }
        }
    }
}

@Composable
fun CompactStreakAndQuickAddWidget(
    streakCount: Int,
    onAddTask: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var taskText by remember { mutableStateOf("") }
    
    // Quick Add Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false 
                taskText = ""
            },
            title = {
                Text(
                    text = "افزودن سریع به تخلیه ذهن ⚡",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "کاری که در ذهن دارید را بنویسید تا بعداً دسته‌بندی کنید:",
                        fontSize = 12.sp,
                        color = DuoTextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = taskText,
                        onValueChange = { taskText = it },
                        placeholder = { Text("مثال: خرید نان، تماس با علی...", fontSize = 13.sp, color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_add_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DuoGreen,
                            unfocusedBorderColor = Color(0xFF37464F),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskText.isNotBlank()) {
                            onAddTask(taskText)
                            taskText = ""
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DuoGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ثبت کار ➕", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDialog = false 
                        taskText = ""
                    }
                ) {
                    Text("انصراف", color = DuoTextSecondary)
                }
            },
            containerColor = Color(0xFF1E2C33),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(BorderStroke(2.dp, Color(0xFF37464F)), RoundedCornerShape(20.dp))
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left item: Streak display card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2C33)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(BorderStroke(2.dp, Color(0xFFFC5C65)), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Large Fire Emoji
                Text("🔥", fontSize = 26.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "شعله روزانه",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoTextSecondary
                    )
                    Text(
                        text = JalaliCalendarHelper.toPersianDigits(streakCount.toString()) + " روز",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFC5C65)
                    )
                }
            }
        }

        // Right item: Quick Add Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2C33)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(BorderStroke(2.dp, Color(0xFF10AC84)), RoundedCornerShape(16.dp))
                .clickable { showDialog = true }
                .testTag("quick_add_widget_button")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text("⚡", fontSize = 26.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "ثبت سریع کار",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoTextSecondary
                    )
                    Text(
                        text = "افزودن جدید ➕",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF10AC84)
                    )
                }
            }
        }
    }
}

@Composable
fun PlannerScreen(viewModel: PlannerViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val subtasks by viewModel.subtasks.collectAsState()
    val timeBlocks by viewModel.timeBlocks.collectAsState()
    val dopaminePoints by viewModel.dopaminePoints.collectAsState()
    val persianDate by viewModel.persianDateDisplay.collectAsState()
    val holidayName by viewModel.holidayName.collectAsState()
    val currentDateStr by viewModel.currentDate.collectAsState()

    // Duolingo Gamification States
    val totalXP by viewModel.totalXP.collectAsState()
    val isLimitBoosted by viewModel.isLimitBoosted.collectAsState()
    val heartsCount by viewModel.heartsCount.collectAsState()
    val mascotQuote by viewModel.mascotQuote.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()

    // Custom limit states
    val primaryLimit by viewModel.primaryLimit.collectAsState()
    val routineLimit by viewModel.routineLimit.collectAsState()
    val personalLimit by viewModel.personalLimit.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Enforce RTL Layout Direction for Persian language compatibility
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = Color(0xFF131F24), // Fully Duolingo dark background
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF1E2C33),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .border(BorderStroke(1.5.dp, Color(0xFF37464F)), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Lightbulb, contentDescription = "تخلیه ذهن", modifier = Modifier.size(22.dp)) },
                        label = { Text("تخلیه ذهن", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DuoBlue,
                            selectedTextColor = DuoBlue,
                            unselectedIconColor = DuoTextSecondary,
                            unselectedTextColor = DuoTextSecondary,
                            indicatorColor = Color(0xFF131F24)
                        ),
                        modifier = Modifier.testTag("tab_dump")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "لیست طلایی", modifier = Modifier.size(22.dp)) },
                        label = { Text("لیست طلایی", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DuoGreen,
                            selectedTextColor = DuoGreen,
                            unselectedIconColor = DuoTextSecondary,
                            unselectedTextColor = DuoTextSecondary,
                            indicatorColor = Color(0xFF131F24)
                        ),
                        modifier = Modifier.testTag("tab_tasks")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Star, contentDescription = "سطح و تقویم", modifier = Modifier.size(22.dp)) },
                        label = { Text("سطح و تقویم", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DuoYellow,
                            selectedTextColor = DuoYellow,
                            unselectedIconColor = DuoTextSecondary,
                            unselectedTextColor = DuoTextSecondary,
                            indicatorColor = Color(0xFF131F24)
                        ),
                        modifier = Modifier.testTag("tab_stats")
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // A beautiful visual simulated widget on the main screen (horizontal 2x1 icons size)
                Spacer(modifier = Modifier.height(8.dp))
                DuolingoMockWidget2x1(
                    streakCount = streakCount,
                    heartsCount = heartsCount,
                    totalXP = totalXP,
                    onRefillHearts = { viewModel.refillHearts() }
                )
                Spacer(modifier = Modifier.height(4.dp))
                CompactStreakAndQuickAddWidget(
                    streakCount = streakCount,
                    onAddTask = { viewModel.addTaskToBrainDump(it) }
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    when (selectedTab) {
                        0 -> DumpAndSortTab(
                            tasks = tasks,
                            isLimitBoosted = isLimitBoosted,
                            primaryLimit = primaryLimit,
                            routineLimit = routineLimit,
                            personalLimit = personalLimit,
                            onAddTask = { viewModel.addTaskToBrainDump(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onCategorize = { task, cat ->
                                viewModel.moveTaskCategory(task, cat)
                            }
                        )
                        1 -> TasksAndAtomizerTab(
                            tasks = tasks,
                            subtasks = subtasks,
                            isLimitBoosted = isLimitBoosted,
                            primaryLimit = primaryLimit,
                            routineLimit = routineLimit,
                            personalLimit = personalLimit,
                            onToggleTask = { viewModel.toggleTaskCompletion(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onAddSubtask = { taskId, title, min -> viewModel.addSubTask(taskId, title, min) },
                            onToggleSubtask = { viewModel.toggleSubTaskCompletion(it) },
                            onDeleteSubtask = { viewModel.deleteSubTask(it) },
                            onMoveBackToDump = { viewModel.moveTaskCategory(it, "BRAIN_DUMP") }
                        )
                        2 -> StatsAndCalendarTab(
                            persianDate = persianDate,
                            dopaminePoints = dopaminePoints,
                            holidayName = holidayName,
                            totalXP = totalXP,
                            heartsCount = heartsCount,
                            streakCount = streakCount,
                            mascotQuote = mascotQuote,
                            isLimitBoosted = isLimitBoosted,
                            primaryLimit = primaryLimit,
                            routineLimit = routineLimit,
                            personalLimit = personalLimit,
                            onPrevDay = { viewModel.navigateDays(-1) },
                            onNextDay = { viewModel.navigateDays(1) },
                            onToday = { viewModel.navigateToToday() },
                            onToggleBoost = { viewModel.toggleLimitBoost() },
                            onRefillHearts = { viewModel.refillHearts() },
                            onSetPrimaryLimit = { viewModel.setPrimaryLimit(it) },
                            onSetRoutineLimit = { viewModel.setRoutineLimit(it) },
                            onSetPersonalLimit = { viewModel.setPersonalLimit(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsAndCalendarTab(
    persianDate: String,
    dopaminePoints: Int,
    holidayName: String?,
    totalXP: Int,
    heartsCount: Int,
    streakCount: Int,
    mascotQuote: String,
    isLimitBoosted: Boolean,
    primaryLimit: Int,
    routineLimit: Int,
    personalLimit: Int,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onToggleBoost: () -> Unit,
    onRefillHearts: () -> Unit,
    onSetPrimaryLimit: (Int) -> Unit,
    onSetRoutineLimit: (Int) -> Unit,
    onSetPersonalLimit: (Int) -> Unit
) {
    val level = (totalXP / 100) + 1
    val progressXP = totalXP % 100
    val progressPercent = progressXP.toFloat() / 100f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Date switcher card (much smaller now!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2C33)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(2.dp, Color(0xFF37464F)), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تقویم و جابه‌جایی روزها 📅",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onPrevDay) {
                            Icon(Icons.Default.ArrowRight, contentDescription = "روز قبل", tint = Color.LightGray, modifier = Modifier.size(24.dp))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onToday() }) {
                            Text(
                                text = persianDate,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (holidayName != null) Color(0xFFEB4D4B) else Color.White
                            )
                            if (holidayName != null) {
                                Text(
                                    text = holidayName,
                                    fontSize = 10.sp,
                                    color = Color(0xFFEB4D4B),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            } else {
                                Text(
                                    text = "امروز (بازگشت به حال)",
                                    fontSize = 10.sp,
                                    color = DuoBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(onClick = onNextDay) {
                            Icon(Icons.Default.ArrowLeft, contentDescription = "روز بعد", tint = Color.LightGray, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }

        // Mascot speaking bubble row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DuoGreen)
                        .border(BorderStroke(2.dp, DuoDarkGreen), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🦉", fontSize = 26.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131F24)),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(BorderStroke(1.5.dp, Color(0xFF37464F)), RoundedCornerShape(topStart = 0.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp))
                ) {
                    Text(
                        text = mascotQuote,
                        color = DuoTextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Streak details & hearts card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2C33)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(2.dp, Color(0xFF37464F)), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "داشبورد انگیزه دوجین 🏆",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("توالی روزها 🔥", fontSize = 11.sp, color = DuoTextSecondary)
                            Text(JalaliCalendarHelper.toPersianDigits(streakCount.toString()) + " روز مکرر", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DuoOrange)
                        }
                        Column {
                            Text("جان‌های باقیمانده ❤️", fontSize = 11.sp, color = DuoTextSecondary)
                            Text(JalaliCalendarHelper.toPersianDigits(heartsCount.toString()) + " از ۵", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DuoRed)
                        }
                        Column {
                            Text("مجموع امتیاز ⚡", fontSize = 11.sp, color = DuoTextSecondary)
                            Text(JalaliCalendarHelper.toPersianDigits(totalXP.toString()) + " XP", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DuoYellow)
                        }
                    }

                    if (heartsCount < 5) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onRefillHearts,
                            colors = ButtonDefaults.buttonColors(containerColor = DuoYellow),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("شارژ ۵ قلب با پرداخت ۵۰ امتیاز ⚡", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Limit booster setting (Super Duolingo style!)
        item {
            Button(
                onClick = onToggleBoost,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLimitBoosted) DuoGreen else Color(0xFF2E3C42)
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .border(
                        border = BorderStroke(
                            width = 2.dp,
                            color = if (isLimitBoosted) DuoDarkGreen else Color(0xFF1D262A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Text(
                    text = if (isLimitBoosted) "🚀 حالت سوپر دوجین فعال (ظرفیت ۲۰ کار)" else "⚡ فعال‌سازی ظرفیت ۲۰ کار روزانه (سوپر دوجین)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // Custom Limit Adjuster Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2C33)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(2.dp, Color(0xFF37464F)), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "تنظیم ظرفیت برنامه‌ریزی روزانه ⚙️",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DuoBlue,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "در این بخش می‌توانید محدودیت هر بازه را مطابق با توان و کشش روزانه خود تغییر دهید. تغییرات به صورت آنی اعمال می‌شوند.",
                        fontSize = 11.sp,
                        color = DuoTextSecondary,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    
                    LimitAdjusterRow(
                        label = "ظرفیت کارهای اصلی ⭐",
                        limit = primaryLimit,
                        color = Color(0xFFFA8231),
                        onDecrease = { onSetPrimaryLimit((primaryLimit - 1).coerceAtLeast(1)) },
                        onIncrease = { onSetPrimaryLimit(primaryLimit + 1) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LimitAdjusterRow(
                        label = "ظرفیت کارهای روتین 🔄",
                        limit = routineLimit,
                        color = Color(0xFF2D98DA),
                        onDecrease = { onSetRoutineLimit((routineLimit - 1).coerceAtLeast(1)) },
                        onIncrease = { onSetRoutineLimit(routineLimit + 1) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LimitAdjusterRow(
                        label = "ظرفیت کارهای شخصی 🎮",
                        limit = personalLimit,
                        color = Color(0xFF26DE81),
                        onDecrease = { onSetPersonalLimit((personalLimit - 1).coerceAtLeast(1)) },
                        onIncrease = { onSetPersonalLimit(personalLimit + 1) }
                    )
                }
            }
        }

        // Educational Section on Alternative Planning Models
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131F24)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, Color(0xFF37464F)), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 مدل‌های برنامه‌ریزی جایگزین برای رشد حداکثری",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = DuoBlue,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "ما برای افزایش انگیزه و کارایی شما، از تلفیق برترین مدل‌های برنامه‌ریزی دنیا استفاده کرده‌ایم. می‌توانید کارهای خود را بر این اساس دسته‌بندی کنید:",
                        fontSize = 11.5.sp,
                        color = DuoTextPrimary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "⭐ روش آیوی لی (Ivy Lee Method):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = DuoOrange
                    )
                    Text(
                        text = "هر شب دقیقاً ۶ کار مهم فردا را بنویسید و اولویت‌بندی کنید. فردا تا کار اول تمام نشده، به کار دوم نروید. این تکنیک ساده تمرکز فوق‌العاده‌ای ایجاد می‌کند.",
                        fontSize = 11.sp,
                        color = DuoTextSecondary,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                    )

                    Text(
                        text = "⏹️ ماتریس آیزنهاور (Eisenhower Matrix):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = DuoYellow
                    )
                    Text(
                        text = "کارها را به چهار بخش تقسیم کنید: ۱) فوری و مهم (کارهای اصلی)، ۲) غیرفوری اما مهم (روتین‌های رشد)، ۳) فوری اما غیرمهم (شخصی و تفویض)، ۴) غیرفوری و غیرمهم (حذف شوند!).",
                        fontSize = 11.sp,
                        color = DuoTextSecondary,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                    )

                    Text(
                        text = "🎯 سیستم اتمی گام‌های خرد (Atomic Steps):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = DuoGreen
                    )
                    Text(
                        text = "با زدن روی کارها، آن‌ها را به ریز کارهای زیر ۱۵ دقیقه‌ای خرد کنید. ذهن ما در مواجهه با ریز کارهای کوچک مقاومت کمتری نشان می‌دهد و غول کمال‌گرایی می‌شکند.",
                        fontSize = 11.sp,
                        color = DuoTextSecondary,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LimitAdjusterRow(
    label: String,
    limit: Int,
    color: Color,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Minus Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF131F24))
                    .clickable { onDecrease() }
                    .border(BorderStroke(1.5.dp, Color(0xFF37464F)), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Text(
                text = JalaliCalendarHelper.toPersianDigits(limit.toString()),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            
            // Plus Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF131F24))
                    .clickable { onIncrease() }
                    .border(BorderStroke(1.5.dp, Color(0xFF37464F)), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DumpAndSortTab(
    tasks: List<TaskEntity>,
    isLimitBoosted: Boolean,
    primaryLimit: Int,
    routineLimit: Int,
    personalLimit: Int,
    onAddTask: (String) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onCategorize: (TaskEntity, String) -> Unit
) {
    var textState by remember { mutableStateOf("") }
    val brainDumpTasks = tasks.filter { it.category == "BRAIN_DUMP" }

    // Counts for limit displays
    val primaryCount = tasks.count { it.category == "PRIMARY" }
    val routineCount = tasks.count { it.category == "ROUTINE" }
    val personalCount = tasks.count { it.category == "PERSONAL" }
    val backlogCount = tasks.count { it.category == "BACKLOG" }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "گام اول: تخلیه ذهن (Brain Dump) 🧠",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "تمام کارهایی که باید انجام دهید را بنویسید تا پردازشگر مغز شما آزاد شود. سپس کارهای مهم را دسته‌بندی کنید.",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Add task inputs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textState,
                onValueChange = { textState = it },
                label = { Text("افزودن کار خام به تخلیه ذهن...", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("dump_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFF3B4252),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (textState.isNotBlank()) {
                        onAddTask(textState)
                        textState = ""
                    }
                },
                modifier = Modifier
                    .height(56.dp)
                    .testTag("dump_add_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن")
            }
        }

        // Display slot quotas as a single compact row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                QuotaBadge("اصلی ⭐", primaryCount, primaryLimit, Color(0xFFFA8231))
            }
            Box(modifier = Modifier.weight(1f)) {
                QuotaBadge("روتین 🔄", routineCount, routineLimit, Color(0xFF2D98DA))
            }
            Box(modifier = Modifier.weight(1f)) {
                QuotaBadge("شخصی 🎮", personalCount, personalLimit, Color(0xFF26DE81))
            }
            Box(modifier = Modifier.weight(1f)) {
                QuotaBadge("خلوت 🔋", backlogCount, -1, Color(0xFFFFC800))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (brainDumpTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CloudQueue,
                        contentDescription = "ذهن تمیز",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ذهن شما در این لحظه کاملاً سبک و آرام است! ✨",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(brainDumpTasks, key = { it.id }) { task ->
                    BrainDumpTaskCard(
                        task = task,
                        onDelete = { onDeleteTask(task) },
                        onCategorize = { cat -> onCategorize(task, cat) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuotaBadge(label: String, current: Int, max: Int, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = DuoTextSecondary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            val text = if (max == -1) {
                JalaliCalendarHelper.toPersianDigits(current.toString()) + " (∞)"
            } else {
                JalaliCalendarHelper.toPersianDigits(current.toString()) + " از " + JalaliCalendarHelper.toPersianDigits(max.toString())
            }
            Text(
                text = text,
                fontSize = 11.sp,
                color = if (max > 0 && current >= max) Color(0xFFEB4D4B) else Color.White,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun BrainDumpTaskCard(
    task: TaskEntity,
    onDelete: () -> Unit,
    onCategorize: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2E3440), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "مرتب‌سازی و انتساب به یکی از بازه‌های برنامه‌ریزی:",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { onCategorize("PRIMARY") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA8231)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("اصلی ⭐", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Button(
                    onClick = { onCategorize("ROUTINE") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D98DA)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("روتین 🔄", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Button(
                    onClick = { onCategorize("PERSONAL") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26DE81)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("شخصی 🎮", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Button(
                    onClick = { onCategorize("BACKLOG") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC800)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("خلوت 🔋", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun TasksAndAtomizerTab(
    tasks: List<TaskEntity>,
    subtasks: List<SubTaskEntity>,
    isLimitBoosted: Boolean,
    primaryLimit: Int,
    routineLimit: Int,
    personalLimit: Int,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onAddSubtask: (Int, String, Int) -> Unit,
    onToggleSubtask: (SubTaskEntity) -> Unit,
    onDeleteSubtask: (SubTaskEntity) -> Unit,
    onMoveBackToDump: (TaskEntity) -> Unit
) {
    val primaries = tasks.filter { it.category == "PRIMARY" }
    val routines = tasks.filter { it.category == "ROUTINE" }
    val personals = tasks.filter { it.category == "PERSONAL" }
    val backlogs = tasks.filter { it.category == "BACKLOG" }

    var expandedTaskId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Primary Tasks
        item {
            CategoryHeader("${JalaliCalendarHelper.toPersianDigits(primaryLimit.toString())} کار اصلی و سنگین روزانه ⭐", Color(0xFFFA8231), primaries.size, primaryLimit)
        }
        if (primaries.isEmpty()) {
            item { EmptyCategoryCard("تسک اصلی تعیین نشده است. از تب 'تخلیه ذهن' کار اضافه کنید.") }
        } else {
            items(primaries) { task ->
                TaskWithAtomizerCard(
                    task = task,
                    subtasks = subtasks.filter { it.taskId == task.id },
                    isExpanded = expandedTaskId == task.id,
                    onExpandToggle = { expandedTaskId = if (expandedTaskId == task.id) null else task.id },
                    onToggleTask = { onToggleTask(task) },
                    onDeleteTask = { onDeleteTask(task) },
                    onAddSubtask = { title, min -> onAddSubtask(task.id, title, min) },
                    onToggleSubtask = onToggleSubtask,
                    onDeleteSubtask = onDeleteSubtask,
                    onMoveBack = { onMoveBackToDump(task) }
                )
            }
        }

        // Routine Tasks
        item {
            CategoryHeader("${JalaliCalendarHelper.toPersianDigits(routineLimit.toString())} کار روتین و فرعی روزانه 🔄", Color(0xFF2D98DA), routines.size, routineLimit)
        }
        if (routines.isEmpty()) {
            item { EmptyCategoryCard("تسک روتین تعیین نشده است.") }
        } else {
            items(routines) { task ->
                TaskWithAtomizerCard(
                    task = task,
                    subtasks = subtasks.filter { it.taskId == task.id },
                    isExpanded = expandedTaskId == task.id,
                    onExpandToggle = { expandedTaskId = if (expandedTaskId == task.id) null else task.id },
                    onToggleTask = { onToggleTask(task) },
                    onDeleteTask = { onDeleteTask(task) },
                    onAddSubtask = { title, min -> onAddSubtask(task.id, title, min) },
                    onToggleSubtask = onToggleSubtask,
                    onDeleteSubtask = onDeleteSubtask,
                    onMoveBack = { onMoveBackToDump(task) }
                )
            }
        }

        // Personal Tasks
        item {
            CategoryHeader("${JalaliCalendarHelper.toPersianDigits(personalLimit.toString())} کار شخصی و تعادلی روزانه 🎮", Color(0xFF26DE81), personals.size, personalLimit)
        }
        if (personals.isEmpty()) {
            item { EmptyCategoryCard("تسک شخصی تعیین نشده است.") }
        } else {
            items(personals) { task ->
                TaskWithAtomizerCard(
                    task = task,
                    subtasks = subtasks.filter { it.taskId == task.id },
                    isExpanded = expandedTaskId == task.id,
                    onExpandToggle = { expandedTaskId = if (expandedTaskId == task.id) null else task.id },
                    onToggleTask = { onToggleTask(task) },
                    onDeleteTask = { onDeleteTask(task) },
                    onAddSubtask = { title, min -> onAddSubtask(task.id, title, min) },
                    onToggleSubtask = onToggleSubtask,
                    onDeleteSubtask = onDeleteSubtask,
                    onMoveBack = { onMoveBackToDump(task) }
                )
            }
        }

        // Backlog / Spare Tasks
        item {
            CategoryHeader("کارهای ذخیره و فرصت‌های فوق‌برنامه 🔋", Color(0xFFFFC800), backlogs.size, -1)
        }
        if (backlogs.isEmpty()) {
            item { EmptyCategoryCard("تسک ذخیره‌ای تعیین نشده است. کارهای اختیاری را در زمان‌های خالی اینجا قرار دهید.") }
        } else {
            items(backlogs) { task ->
                TaskWithAtomizerCard(
                    task = task,
                    subtasks = subtasks.filter { it.taskId == task.id },
                    isExpanded = expandedTaskId == task.id,
                    onExpandToggle = { expandedTaskId = if (expandedTaskId == task.id) null else task.id },
                    onToggleTask = { onToggleTask(task) },
                    onDeleteTask = { onDeleteTask(task) },
                    onAddSubtask = { title, min -> onAddSubtask(task.id, title, min) },
                    onToggleSubtask = onToggleSubtask,
                    onDeleteSubtask = onDeleteSubtask,
                    onMoveBack = { onMoveBackToDump(task) }
                )
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String, color: Color, count: Int, limit: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 15.sp
        )
        val limitText = if (limit == -1) {
            "تعداد کل: " + JalaliCalendarHelper.toPersianDigits(count.toString()) + " (نامحدود)"
        } else {
            "ظرفیت: " + JalaliCalendarHelper.toPersianDigits(count.toString()) + " از " + JalaliCalendarHelper.toPersianDigits(limit.toString())
        }
        Text(
            text = limitText,
            fontSize = 12.sp,
            color = if (limit != -1 && count > limit) Color.Red else Color.LightGray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyCategoryCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2E3440).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
    ) {
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
fun TaskWithAtomizerCard(
    task: TaskEntity,
    subtasks: List<SubTaskEntity>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onToggleTask: () -> Unit,
    onDeleteTask: () -> Unit,
    onAddSubtask: (String, Int) -> Unit,
    onToggleSubtask: (SubTaskEntity) -> Unit,
    onDeleteSubtask: (SubTaskEntity) -> Unit,
    onMoveBack: () -> Unit
) {
    var subtaskTitle by remember { mutableStateOf("") }
    var subtaskDuration by remember { mutableStateOf(15) }

    val completedCount = subtasks.count { it.isCompleted }
    val totalCount = subtasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = 1.dp,
                color = if (task.isCompleted) Color(0xFF26DE81).copy(alpha = 0.5f) else Color(0xFF2E3440),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Task Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Red Tick/Completion Box
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleTask() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFEB4D4B), // Dopamine Red Tick
                        uncheckedColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted) Color.Gray else Color.White,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    if (totalCount > 0) {
                        Text(
                            text = "گام‌های اتمیزه: " + JalaliCalendarHelper.toPersianDigits(completedCount.toString()) + " از " + JalaliCalendarHelper.toPersianDigits(totalCount.toString()),
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                // Expand button
                IconButton(onClick = onExpandToggle) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "جزییات",
                        tint = Color.LightGray
                    )
                }
            }

            // Simple progress bar for atomized steps
            if (totalCount > 0) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF26DE81),
                    trackColor = Color(0xFF2E3440)
                )
            }

            // Expanding subtask / Atomizer block
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(Color(0xFF161920), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🔬 اتمیزه کردن تکلیف (شکستن غول بزرگ):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Subtask Form
                    OutlinedTextField(
                        value = subtaskTitle,
                        onValueChange = { subtaskTitle = it },
                        label = { Text("مثلاً: مطالعه صفحات ۱۲ تا ۱۵", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF2E3440),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Duration Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "زمان کلیدی: " + JalaliCalendarHelper.toPersianDigits(subtaskDuration.toString()) + " دقیقه",
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Slider(
                            value = subtaskDuration.toFloat(),
                            onValueChange = { subtaskDuration = it.toInt() },
                            valueRange = 5f..90f,
                            steps = 17, // 5 min intervals
                            modifier = Modifier.width(180.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                if (subtaskTitle.isNotBlank()) {
                                    onAddSubtask(subtaskTitle, subtaskDuration)
                                    subtaskTitle = ""
                                    subtaskDuration = 15
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("افزودن گام اتمی روزانه", fontSize = 11.sp)
                        }

                        TextButton(onClick = onMoveBack) {
                            Text("انتقال مجدد به تخلیه ذهن 🔄", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subtask list
                    if (subtasks.isEmpty()) {
                        Text(
                            text = "هیچ گام ریزی تعریف نشده است. تکلیف بزرگ را خرد کنید تا شروع کار آسان شود!",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    } else {
                        subtasks.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = sub.isCompleted,
                                    onCheckedChange = { onToggleSubtask(sub) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFEB4D4B), // Dopamine Red Tick
                                        uncheckedColor = Color.LightGray
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sub.title,
                                        fontSize = 12.sp,
                                        color = if (sub.isCompleted) Color.Gray else Color.White,
                                        textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Text(
                                        text = "مدت زمان مورد نیاز: " + JalaliCalendarHelper.toPersianDigits(sub.durationMinutes.toString()) + " دقیقه",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                IconButton(onClick = { onDeleteSubtask(sub) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف گام",
                                        tint = Color.Red.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color(0xFF2E3440))
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onDeleteTask,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("حذف این تسک کلاً", color = Color.Red.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}


