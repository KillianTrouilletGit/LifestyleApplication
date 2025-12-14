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
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.viewmodel.MissionViewModel

class MissionAdapter(
    private val context: Context,
    private var missions: List<Any>,
    private val userId: Int,
    private var viewModel: MissionViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MissionPrefs", Context.MODE_PRIVATE)
    private var checkedStates: BooleanArray = BooleanArray(0)



    override fun getItemViewType(position: Int): Int {
        return if (missions[position] is String) {
            TYPE_HEADER
        } else {
            TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mission, parent, false)
            MissionViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MissionViewHolder) {
            val mission = missions[position] as Mission
            holder.bind(mission,  userId, position)
        } else if (holder is HeaderViewHolder) {
            val title = missions[position] as String
            holder.bind(title)
        }
    }

    override fun getItemCount(): Int = missions.size

    fun updateMissions(newDailyMissions: List<Mission>, newWeeklyMissions: List<Mission>) {
        val combinedList = mutableListOf<Any>()
        combinedList.add("Daily Missions")
        combinedList.addAll(newDailyMissions)
        combinedList.add("Weekly Missions")
        combinedList.addAll(newWeeklyMissions)
        missions = combinedList

        // Update the size of checkedStates to match the new size of missions
        checkedStates = BooleanArray(missions.size) { index ->
            val mission = missions.getOrNull(index)
            if (mission is Mission) {
                sharedPreferences.getBoolean(mission.id.toString(), false)
            } else {
                false
            }
        }

        notifyDataSetChanged()
    }

    fun getMissions(): List<Mission> {
        return missions.filterIsInstance<Mission>()
    }

    private fun saveCheckedStates() {
        val editor = sharedPreferences.edit()
        for (i in checkedStates.indices) {
            val mission = missions.getOrNull(i)
            if (mission is Mission) {
                editor.putBoolean(mission.id, checkedStates[i])
            }
        }
        editor.apply()
    }



    inner class MissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val missionDescription: TextView = itemView.findViewById(R.id.missionDescription)
        private val missionCheckBox: CheckBox = itemView.findViewById(R.id.missionCheckBox)

        fun bind(mission: Mission, userId: Int, position: Int) {
            missionDescription.text = mission.description
            missionCheckBox.setOnCheckedChangeListener(null) // Clear previous listener
            missionCheckBox.isChecked = mission.isCompleted
            missionCheckBox.isEnabled = !mission.isCompleted
            missionCheckBox.setOnCheckedChangeListener { _, isChecked ->

                saveCheckedStates()
                if (isChecked != mission.isCompleted) {
                    checkedStates[position] = isChecked
                    viewModel.completeMission(mission,userId)
                    // Pass userId here
                }
            }
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTitle: TextView = itemView.findViewById(R.id.headerTitle)

        fun bind(title: String) {
            headerTitle.text = title
        }
    }
}
