package com.example.personallevelingsystem.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personallevelingsystem.R
import com.google.api.services.calendar.model.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventsAdapter(
    private val events: List<Event>,
    private val context: Context,
    private val listener: OnEventCheckListener
) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("EventPrefs", Context.MODE_PRIVATE)
    private val checkedStates: BooleanArray = BooleanArray(events.size) { false }

    init {
        loadCheckedStates()
    }

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventCheckBox: CheckBox = view.findViewById(R.id.eventCheckBox)
        val eventSummary: TextView = view.findViewById(R.id.eventSummary)
        val eventDate: TextView = view.findViewById(R.id.eventDate)
        val eventStartTime: TextView = view.findViewById(R.id.eventStartTime)
        val eventEndTime: TextView = view.findViewById(R.id.eventEndTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.eventSummary.text = event.summary

        // Define the input date format for parsing the RFC3339 date-time strings
        val inputDateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Define the output date and time formats
        val outputDateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        val outputTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        // Parse the start and end times
        val startTimeString = event.start.dateTime?.toStringRfc3339() ?: event.start.date.toString()
        val endTimeString = event.end.dateTime?.toStringRfc3339() ?: event.end.date.toString()

        // Determine if the event has time or only date and parse accordingly
        val startDateTime: Date? = if (event.start.dateTime != null) {
            inputDateTimeFormat.parse(startTimeString)
        } else {
            inputDateFormat.parse(startTimeString)
        }
        val endDateTime: Date? = if (event.end.dateTime != null) {
            inputDateTimeFormat.parse(endTimeString)
        } else {
            inputDateFormat.parse(endTimeString)
        }

        // Format the start and end times into more readable strings
        val formattedDate = startDateTime?.let { outputDateFormat.format(it) } ?: startTimeString
        val formattedStartTime = startDateTime?.let { outputTimeFormat.format(it) } ?: startTimeString
        val formattedEndTime = endDateTime?.let { outputTimeFormat.format(it) } ?: endTimeString

        // Set the formatted date and times to the TextViews
        holder.eventDate.text = formattedDate
        holder.eventStartTime.text = "Start Time: $formattedStartTime"
        holder.eventEndTime.text = "End Time: $formattedEndTime"

        holder.eventCheckBox.isChecked = checkedStates[position]
        holder.eventCheckBox.setOnCheckedChangeListener { _, isChecked ->
            checkedStates[position] = isChecked
            saveCheckedStates()
            listener.onEventCheckChanged()
        }
    }

    override fun getItemCount(): Int = events.size

    private fun saveCheckedStates() {
        val editor = sharedPreferences.edit()
        for (i in checkedStates.indices) {
            editor.putBoolean(events[i].id, checkedStates[i])
        }
        editor.apply()
    }

    private fun loadCheckedStates() {
        for (i in events.indices) {
            checkedStates[i] = sharedPreferences.getBoolean(events[i].id, false)
        }
    }

    fun areAllTodayEventsChecked(): Boolean {
        val today = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayString = dateFormat.format(today)

        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val inputDateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        events.forEachIndexed { index, event ->
            val eventDate = if (event.start.dateTime != null) {
                inputDateFormat.format(event.start.dateTime.value)
            } else {
                inputDateOnlyFormat.format(event.start.date.value)
            }

            if (eventDate.startsWith(todayString) && !checkedStates[index]) {
                return false
            }
        }
        return true
    }
}
interface OnEventCheckListener {
    fun onEventCheckChanged()
}