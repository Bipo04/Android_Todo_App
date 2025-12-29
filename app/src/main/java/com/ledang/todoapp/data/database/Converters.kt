package com.ledang.todoapp.data.database

import androidx.room.TypeConverter
import com.ledang.todoapp.data.enums.TaskCategory
import com.ledang.todoapp.data.enums.TaskStatus

class Converters {
    
    @TypeConverter
    fun fromTaskCategory(category: TaskCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory {
        return TaskCategory.valueOf(value)
    }
    
    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus {
        return TaskStatus.valueOf(value)
    }
}
