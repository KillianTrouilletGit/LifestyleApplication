package com.example.personallevelingsystem.ui

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.adapter.EventsAdapter
import com.example.personallevelingsystem.model.MissionType
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar as JavaCalendar
import com.example.personallevelingsystem.adapter.OnEventCheckListener
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.viewmodel.MissionViewModel
import com.example.personallevelingsystem.viewmodel.MissionViewModelFactory
import kotlinx.coroutines.CoroutineScope


class GoogleCalendarActivity : AppCompatActivity(),OnEventCheckListener {

    private lateinit var credential: GoogleAccountCredential
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventsAdapter: EventsAdapter
    private var eventsList: List<Event> = mutableListOf()
    private lateinit var viewModel: MissionViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var db: AppDatabase

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_calendar)
        supportActionBar?.title = ""
        val btnOpenCalendar: Button = findViewById(R.id.btn_open_calendar)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        eventsAdapter = EventsAdapter(eventsList, this, this)
        recyclerView.adapter = eventsAdapter
        db = AppDatabase.getDatabase(this)
        userRepository = UserRepository(db.userDao(),this)
        val factory = MissionViewModelFactory(application, userRepository) // Use the custom factory
        viewModel = ViewModelProvider(this, factory)[MissionViewModel::class.java]
        btnOpenCalendar.setOnClickListener {
            openGoogleCalendar()
        }
        credential = GoogleAccountCredential.usingOAuth2(
            this, listOf(CalendarScopes.CALENDAR)
        )

        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK && data != null && data.extras != null) {
            val accountName = data.extras!!.getString(AccountManager.KEY_ACCOUNT_NAME)
            Log.d(TAG, "Account name: $accountName")
            if (accountName != null) {
                credential.selectedAccountName = accountName
                // Now you can make API calls
                fetchWeeklyEvents()
            }
        } else if (requestCode == REQUEST_AUTHORIZATION && resultCode == RESULT_OK) {
            // Retry fetching events after authorization
            fetchWeeklyEvents()
        } else {
            Log.e(TAG, "Account selection failed or no account selected.")
        }
    }

    private fun openGoogleCalendar() {
        val uri = Uri.parse("content://com.android.calendar/time/")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun fetchWeeklyEvents() {
        lifecycleScope.launch {
            try {
                val calendar = Calendar.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, credential
                )
                    .setApplicationName("Personal Leveling System")
                    .build()


                val startOfWeek: DateTime
                val endOfWeek: DateTime
                JavaCalendar.getInstance().apply {
                    firstDayOfWeek = JavaCalendar.MONDAY
                    set(JavaCalendar.DAY_OF_WEEK, JavaCalendar.MONDAY)
                    set(JavaCalendar.HOUR_OF_DAY, 0)
                    set(JavaCalendar.MINUTE, 0)
                    set(JavaCalendar.SECOND, 0)
                    set(JavaCalendar.MILLISECOND, 0)
                    startOfWeek = DateTime(time)

                    add(JavaCalendar.DAY_OF_YEAR, 6)
                    set(JavaCalendar.HOUR_OF_DAY, 23)
                    set(JavaCalendar.MINUTE, 59)
                    set(JavaCalendar.SECOND, 59)
                    set(JavaCalendar.MILLISECOND, 999)
                    endOfWeek = DateTime(time)
                }

                val events = withContext(Dispatchers.IO) {
                    calendar.events().list("primary")
                        .setTimeMin(startOfWeek)
                        .setTimeMax(endOfWeek)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute()
                        .items
                }

                eventsList = events ?: listOf()
                eventsAdapter = EventsAdapter(eventsList, this@GoogleCalendarActivity, this@GoogleCalendarActivity)
                recyclerView.adapter = eventsAdapter

                if (eventsList.isEmpty()) {
                    Toast.makeText(this@GoogleCalendarActivity, "No events found for the week.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@GoogleCalendarActivity, "Events fetched successfully.", Toast.LENGTH_SHORT).show()
                    for (event in eventsList) {
                        Log.d(TAG, "Event: ${event.summary} at ${event.start.dateTime ?: event.start.date}")
                    }
                }

            } catch (e: GoogleJsonResponseException) {
                Log.e(TAG, "Error fetching events: ", e)
            } catch (e: UserRecoverableAuthIOException) {
                startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events: ", e)
            }
        }
    }
    override fun onEventCheckChanged() {
        if (eventsAdapter.areAllTodayEventsChecked()) {
            CoroutineScope(Dispatchers.IO).launch {
                val missionId = "daily_planning"
                val missionType = MissionType.DAILY
                val userId = getCurrentUserId()

                if (userId != null) {
                    withContext(Dispatchers.Main) {
                        viewModel.completeMissionById(missionId, missionType, userId)
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_ACCOUNT_PICKER = 1000
        private const val REQUEST_AUTHORIZATION = 1001
        private const val TAG = "GoogleCalendarActivity"
        private val HTTP_TRANSPORT = NetHttpTransport()
        private val JSON_FACTORY = GsonFactory.getDefaultInstance()
    }


    private suspend fun getCurrentUserId(): Int? {
        return withContext(Dispatchers.IO) {
            val latestUser = db.userDao().getLatestUser()
            latestUser?.id
        }
    }
}
