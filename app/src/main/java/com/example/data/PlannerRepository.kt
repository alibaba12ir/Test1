package com.example.data

import kotlinx.coroutines.flow.Flow

class PlannerRepository(private val plannerDao: PlannerDao) {
    fun getTasksForDate(dateString: String): Flow<List<TaskEntity>> =
        plannerDao.getTasksForDate(dateString)

    fun getSubTasksForDate(dateString: String): Flow<List<SubTaskEntity>> =
        plannerDao.getSubTasksForDate(dateString)

    fun getSubTasksForTask(taskId: Int): Flow<List<SubTaskEntity>> =
        plannerDao.getSubTasksForTask(taskId)

    fun getTimeBlocksForDate(dateString: String): Flow<List<TimeBlockEntity>> =
        plannerDao.getTimeBlocksForDate(dateString)

    fun getCompletedDates(): Flow<List<String>> =
        plannerDao.getCompletedDates()

    suspend fun getAllRoutines(): List<TaskEntity> =
        plannerDao.getAllRoutines()

    suspend fun getTasksForDateOnce(dateString: String): List<TaskEntity> =
        plannerDao.getTasksForDateOnce(dateString)

    suspend fun deleteRoutinesByTitle(title: String) =
        plannerDao.deleteRoutinesByTitle(title)

    suspend fun insertTask(task: TaskEntity): Long =
        plannerDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) =
        plannerDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) =
        plannerDao.deleteTask(task)

    suspend fun deleteTaskById(id: Int) =
        plannerDao.deleteTaskById(id)

    suspend fun insertSubTask(subTask: SubTaskEntity): Long =
        plannerDao.insertSubTask(subTask)

    suspend fun updateSubTask(subTask: SubTaskEntity) =
        plannerDao.updateSubTask(subTask)

    suspend fun deleteSubTask(subTask: SubTaskEntity) =
        plannerDao.deleteSubTask(subTask)

    suspend fun insertTimeBlock(timeBlock: TimeBlockEntity): Long =
        plannerDao.insertTimeBlock(timeBlock)

    suspend fun updateTimeBlock(timeBlock: TimeBlockEntity) =
        plannerDao.updateTimeBlock(timeBlock)

    suspend fun insertTimeBlocks(timeBlocks: List<TimeBlockEntity>) =
        plannerDao.insertTimeBlocks(timeBlocks)
}
