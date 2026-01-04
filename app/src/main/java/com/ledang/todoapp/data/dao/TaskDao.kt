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
    
    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY startTime ASC")
    fun getTasksByCategory(category: TaskCategory): List<Task>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Task?
    
    @Query("SELECT * FROM tasks WHERE startTime >= :startOfDay AND startTime < :endOfDay ORDER BY startTime ASC")
    fun getTasksByDate(startOfDay: Long, endOfDay: Long): List<Task>
    
    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    fun updateStatus(taskId: Long, status: TaskStatus)
    
    // Get tasks that should be IN_PROGRESS (started but not ended, and still TODO)
    @Query("SELECT * FROM tasks WHERE status = 'TODO' AND startTime <= :currentTime AND endTime > :currentTime")
    fun getTasksToStartProgress(currentTime: Long): List<Task>
    
    // Get tasks that should be OVERDUE (ended but not completed)
    @Query("SELECT * FROM tasks WHERE status IN ('TODO', 'IN_PROGRESS') AND endTime <= :currentTime")
    fun getTasksToMarkOverdue(currentTime: Long): List<Task>
    
    // Count total tasks by category
    @Query("SELECT COUNT(*) FROM tasks WHERE category = :category")
    fun countTasksByCategory(category: TaskCategory): Int
    
    // Count completed tasks by category
    @Query("SELECT COUNT(*) FROM tasks WHERE category = :category AND status = 'COMPLETED'")
    fun countCompletedTasksByCategory(category: TaskCategory): Int
    
    // Count today's tasks
    @Query("SELECT COUNT(*) FROM tasks WHERE startTime >= :startOfDay AND startTime < :endOfDay")
    fun countTasksForToday(startOfDay: Long, endOfDay: Long): Int
    
    // Count today's completed tasks
    @Query("SELECT COUNT(*) FROM tasks WHERE startTime >= :startOfDay AND startTime < :endOfDay AND status = 'COMPLETED'")
    fun countCompletedTasksForToday(startOfDay: Long, endOfDay: Long): Int
    
    // Count in progress tasks
    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'IN_PROGRESS'")
    fun countInProgressTasks(): Int
    
    // Delete task by ID
    @Query("DELETE FROM tasks WHERE id = :taskId")
    fun deleteById(taskId: Long)
}
