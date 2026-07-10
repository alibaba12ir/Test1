package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dateString: String,
    val category: String, // "BRAIN_DUMP", "PRIMARY", "ROUTINE", "PERSONAL"
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"])]
)
data class SubTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val title: String,
    val durationMinutes: Int,
    val isCompleted: Boolean = false
)

@Entity(tableName = "time_blocks")
data class TimeBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String,
    val startTime: String,
    val endTime: String,
    val label: String,
    val assignedTaskId: Int? = null,
    val isRescueBlock: Boolean = false,
    val isCompleted: Boolean = false
)
