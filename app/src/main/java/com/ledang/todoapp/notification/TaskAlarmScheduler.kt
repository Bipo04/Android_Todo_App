package com.ledang.todoapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ledang.todoapp.data.entity.Task

object TaskAlarmScheduler {
    
    private const val FIFTEEN_MINUTES_MS = 15 * 60 * 1000L
    
    fun scheduleTaskReminders(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val currentTime = System.currentTimeMillis()
        
        // Schedule start reminder (15 min before start)
        val startReminderTime = task.startTime - FIFTEEN_MINUTES_MS
        if (startReminderTime > currentTime) {
            scheduleAlarm(
                context = context,
                alarmManager = alarmManager,
                taskId = task.id,
                triggerTime = startReminderTime,
                type = NotificationHelper.NotificationType.START_REMINDER
            )
        }
        
        // Schedule deadline reminder (15 min before end)
        val deadlineReminderTime = task.endTime - FIFTEEN_MINUTES_MS
        if (deadlineReminderTime > currentTime) {
            scheduleAlarm(
                context = context,
                alarmManager = alarmManager,
                taskId = task.id,
                triggerTime = deadlineReminderTime,
                type = NotificationHelper.NotificationType.DEADLINE_REMINDER
            )
        }
        
        // Schedule overdue check (at end time)
        if (task.endTime > currentTime) {
            scheduleAlarm(
                context = context,
                alarmManager = alarmManager,
                taskId = task.id,
                triggerTime = task.endTime,
                type = NotificationHelper.NotificationType.OVERDUE
            )
        }
    }
    
    fun cancelTaskReminders(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Cancel all 3 types of reminders for this task
        NotificationHelper.NotificationType.values().forEach { type ->
            val pendingIntent = createPendingIntent(context, taskId, type)
            alarmManager.cancel(pendingIntent)
        }
    }
    
    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        taskId: Long,
        triggerTime: Long,
        type: NotificationHelper.NotificationType
    ) {
        val pendingIntent = createPendingIntent(context, taskId, type)
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    // Fallback to inexact alarm
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Permission not granted, use inexact alarm
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    private fun createPendingIntent(
        context: Context,
        taskId: Long,
        type: NotificationHelper.NotificationType
    ): PendingIntent {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskReminderReceiver.EXTRA_NOTIFICATION_TYPE, type.ordinal)
        }
        
        // Unique request code for each task + type combination
        val requestCode = (taskId * 10 + type.ordinal).toInt()
        
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    // Check for existing overdue tasks and show notifications
    fun checkExistingOverdueTasks(context: Context, tasks: List<Task>) {
        val currentTime = System.currentTimeMillis()
        
        tasks.forEach { task ->
            // If task endTime has passed and status is not COMPLETED
            if (task.endTime < currentTime && 
                task.status != com.ledang.todoapp.data.enums.TaskStatus.COMPLETED) {
                // Show overdue notification immediately
                NotificationHelper.showNotification(
                    context = context,
                    taskId = task.id,
                    taskName = task.name,
                    type = NotificationHelper.NotificationType.OVERDUE
                )
            }
        }
    }
}
