    package com.example.personallevelingsystem.adapter

    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView
    import com.example.personallevelingsystem.R
    import com.example.personallevelingsystem.model.Session

    class SessionsAdapter(private val onSessionClick: (Session) -> Unit) :
        RecyclerView.Adapter<SessionsAdapter.SessionViewHolder>() {

        private var sessions: List<Session> = emptyList()

        fun setSessions(sessions: List<Session>) {
            this.sessions = sessions
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_session, parent, false)
            return SessionViewHolder(view, onSessionClick)
        }

        override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
            holder.bind(sessions[position])
        }

        override fun getItemCount(): Int {
            return sessions.size
        }

        class SessionViewHolder(
            itemView: View,
            private val onSessionClick: (Session) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {
            private val sessionNameTextView: TextView = itemView.findViewById(R.id.session_name_text_view)
            private val exercisesContainer: android.widget.LinearLayout = itemView.findViewById(R.id.exercises_container)

            fun bind(session: Session) {
                sessionNameTextView.text = session.name
                exercisesContainer.visibility = View.GONE // Hide empty container for selection mode
                itemView.setOnClickListener { onSessionClick(session) }
            }
        }
    }
