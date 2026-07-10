package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDao {
    @Query("SELECT * FROM tasks WHERE dateString = :dateString ORDER BY orderIndex ASC")
    fun getTasksForDate(dateString: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM subtasks WHERE taskId IN (SELECT id FROM tasks WHERE dateString = :dateString)")
    fun getSubTasksForDate(dateString: String): Flow<List<SubTaskEntity>>

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId")
    fun getSubTasksForTask(taskId: Int): Flow<List<SubTaskEntity>>

    @Query("SELECT * FROM time_blocks WHERE dateString = :dateString ORDER BY startTime ASC")
    fun getTimeBlocksForDate(dateString: String): Flow<List<TimeBlockEntity>>

    @Query("SELECT DISTINCT dateString FROM tasks WHERE isCompleted = 1")
    fun getCompletedDates(): Flow<List<String>>

    @Query("SELECT * FROM tasks WHERE category = 'ROUTINE'")
    suspend fun getAllRoutines(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE dateString = :dateString")
    suspend fun getTasksForDateOnce(dateString: String): List<TaskEntity>

    @Query("DELETE FROM tasks WHERE category = 'ROUTINE' AND title = :title")
    suspend fun deleteRoutinesByTitle(title: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTaskEntity): Long

    @Update
    suspend fun updateSubTask(subTask: SubTaskEntity)

    @Delete
    suspend fun deleteSubTask(subTask: SubTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeBlock(timeBlock: TimeBlockEntity): Long

    @Update
    suspend fun updateTimeBlock(timeBlock: TimeBlockEntity)

    @Delete
    suspend fun deleteTimeBlock(timeBlock: TimeBlockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeBlocks(timeBlocks: List<TimeBlockEntity>)
}
