package com.ledang.todoapp.data.dao

import androidx.room.*
import com.ledang.todoapp.data.entity.Task
import com.ledang.todoapp.data.enums.TaskCategory
import com.ledang.todoapp.data.enums.TaskStatus

@Dao
interface TaskDao {
    
    @Insert
    fun insert(task: Task): Long
    
    @Update
    fun update(task: Task)
    
    @Delete
    fun delete(task: Task)
    
    @Query("SELECT * FROM tasks ORDER BY startTime DESC")
    fun getAllTasks(): List<Task>
    
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY startTime DESC")
    fun getTasksByStatus(status: TaskStatus): List<Task>
    
    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY startTime DESC")
    fun getTasksByCategory(category: TaskCategory): List<Task>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Task?
    
    @Query("SELECT * FROM tasks WHERE startTime >= :startOfDay AND startTime < :endOfDay ORDER BY startTime ASC")
    fun getTasksByDate(startOfDay: Long, endOfDay: Long): List<Task>
}
