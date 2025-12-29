package com.ledang.todoapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ledang.todoapp.data.enums.TaskCategory
import com.ledang.todoapp.data.enums.TaskStatus

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,               // Tên task
    val description: String,        // Mô tả
    val startTime: Long,            // Thời gian bắt đầu (timestamp)
    val endTime: Long,              // Thời gian kết thúc (timestamp)
    val category: TaskCategory,     // Phân loại: WORK, PERSONAL, STUDY, SPORTS
    val status: TaskStatus = TaskStatus.TODO  // Trạng thái: TODO, IN_PROGRESS, COMPLETED, OVERDUE
)
