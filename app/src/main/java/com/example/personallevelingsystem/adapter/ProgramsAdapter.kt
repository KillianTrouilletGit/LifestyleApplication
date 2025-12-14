package com.example.personallevelingsystem.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.model.ProgramWithSessions

class ProgramsAdapter(
    private val onDeleteProgram: (ProgramWithSessions) -> Unit
) : RecyclerView.Adapter<ProgramsAdapter.ProgramViewHolder>() {

    private var programs: List<ProgramWithSessions> = listOf()

    fun setPrograms(programs: List<ProgramWithSessions>) {
        this.programs = programs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_program, parent, false)
        return ProgramViewHolder(view, onDeleteProgram)
    }

    override fun onBindViewHolder(holder: ProgramViewHolder, position: Int) {
        val program = programs[position]
        holder.bind(program)
    }

    override fun getItemCount(): Int {
        return programs.size
    }

    class ProgramViewHolder(
        itemView: View,
        private val onDeleteProgram: (ProgramWithSessions) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val programNameTextView: TextView = itemView.findViewById(R.id.program_name_text_view)
        private val sessionsContainer: LinearLayout = itemView.findViewById(R.id.sessions_container)
        private val deleteButton: Button = itemView.findViewById(R.id.button_delete_program)

        fun bind(programWithSessions: ProgramWithSessions) {
            programNameTextView.text = programWithSessions.program.name

            sessionsContainer.removeAllViews()
            for (sessionWithExercises in programWithSessions.sessions) {
                val sessionView = LayoutInflater.from(itemView.context).inflate(R.layout.item_session, sessionsContainer, false)
                val sessionNameTextView: TextView = sessionView.findViewById(R.id.session_name_text_view)
                val exercisesContainer: LinearLayout = sessionView.findViewById(R.id.exercises_container)

                sessionNameTextView.text = sessionWithExercises.session.name
                exercisesContainer.removeAllViews()
                for (exercise in sessionWithExercises.exercises) {
                    val exerciseView = LayoutInflater.from(itemView.context).inflate(R.layout.item_exercise, exercisesContainer, false)
                    val exerciseNameTextView: TextView = exerciseView.findViewById(R.id.exercise_name_text_view)
                    val exerciseSetsTextView: TextView = exerciseView.findViewById(R.id.exercise_sets_text_view)

                    exerciseNameTextView.text = exercise.name
                    exerciseSetsTextView.text = "Sets: ${exercise.sets}"
                    exercisesContainer.addView(exerciseView)
                }

                sessionsContainer.addView(sessionView)
            }

            deleteButton.setOnClickListener {
                onDeleteProgram(programWithSessions)
            }
        }
    }
}
