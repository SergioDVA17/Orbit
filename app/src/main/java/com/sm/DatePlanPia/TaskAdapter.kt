package com.sm.DatePlanPia

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.util.GregorianCalendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskAdapter(
    private val taskList: List<TaskSummary>,
    private val onClick: View.OnClickListener,
) : RecyclerView.Adapter<TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.task_list_item, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val taskSummary = taskList[position]
        holder.bind(taskSummary)
        holder.itemView.tag = taskSummary.taskID
        holder.itemView.setOnClickListener(onClick)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}

class TaskViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    private val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
    private val taskType: TextView = itemView.findViewById(R.id.taskType)
    private val taskDate: TextView = itemView.findViewById(R.id.taskDueDate)
    private val taskInfo: TextView = itemView.findViewById(R.id.taskInfo)
    private val taskPriority: TextView = itemView.findViewById(R.id.taskPriority)
    private val taskPriorityColor: TextView = itemView.findViewById(R.id.taskPriorityColor)

    fun bind(taskSummary: TaskSummary) {
        taskTitle.text = taskSummary.taskTitle
        taskType.text = taskSummary.taskType

        // Set task type background color
        when (taskSummary.taskType) {
            "School" -> taskType.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.schoolwork))
            "Recreation" -> taskType.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.recreation))
            "Social" -> taskType.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.social))
            "Work" -> taskType.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.work))
            "Meeting" -> taskType.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.meeting))
        }

        val splitDate = taskSummary.taskDate.split("-")
        val formattedDate = "Due: ${splitDate[1]}/${splitDate[2]}/${splitDate[0]} at "

        val dayOfWeek = taskSummary.dayOfWeek
        var recurring = ""
        if (taskSummary.isRecurring) {
            recurring = "\nRecurring ${dayOfWeek}s"
        }

        val splitTime = taskSummary.taskTime.split(":")
        var formattedTime = "XX:XX"
        CoroutineScope(Dispatchers.IO).launch {
            formattedTime = formatTime(taskSummary.taskTime)
            val formattedDateFinal = formattedDate + formattedTime + recurring
            taskDate.text = formattedDateFinal
        }

        val formattedDateFinal = formattedDate + formattedTime + recurring

        taskDate.text = formattedDateFinal
        taskInfo.text = taskSummary.taskInfo


        // Get day of week from Calendar
        val day = splitDate[2].toInt()
        val month = splitDate[1].toInt() - 1 // 0 indexed
        val year = splitDate[0].toInt()
        val hour = splitTime[0].toInt()
        val minute = splitTime[1].toInt()

        // Set priority
        val compareTime = GregorianCalendar(year, month, day, hour, minute)
        compareTime.add(Calendar.DATE, -1) // Due tomorrow
        val prioritySoon = Calendar.getInstance().time.after(compareTime.time)
        compareTime.add(Calendar.DATE, -6) // Due in one week
        val priorityMild = Calendar.getInstance().time.after(compareTime.time)

        var finalPriority = "Priority: Not Due Soon"
        var priorityColor = R.color.priorityNotSoon
        if (priorityMild) {
            finalPriority = "Priority: Mild"
            priorityColor = R.color.priorityMild
        }
        if (prioritySoon) {
            finalPriority = "Priority: Due soon"
            priorityColor = R.color.prioritySoon
        }

        taskPriority.text = finalPriority
        taskPriorityColor.setBackgroundColor(ContextCompat.getColor(itemView.context, priorityColor))
    }

    // Formats time
    private suspend fun formatTime(time: String): String {
        return withContext(Dispatchers.IO) {
            val splitTime = time.split(":").toMutableList()

            // Use 12/24 hour based on user's setting
            val sharedPrefs =
                itemView.context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val is24HourFormat = sharedPrefs.getBoolean("is24HourFormat", true)

            if (splitTime[1].length == 1) {
                splitTime[1] += "0"
            }

            var timeOfDay = " AM"
            if (is24HourFormat) {
                timeOfDay = ""
            } else {
                if (splitTime[0].toInt() > 12) {
                    splitTime[0] = (splitTime[0].toInt() - 12).toString()
                    timeOfDay = " PM"
                }
            }

            return@withContext "${splitTime[0]}:${splitTime[1]}$timeOfDay"
        }
    }
}
