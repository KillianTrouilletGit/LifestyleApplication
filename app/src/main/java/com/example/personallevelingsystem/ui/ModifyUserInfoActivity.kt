package com.example.personallevelingsystem.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.personallevelingsystem.MainActivity
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.User
import com.example.personallevelingsystem.databinding.ActivityModifyUserInfoBinding
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.viewmodel.UserViewModel
import com.example.personallevelingsystem.viewmodel.UserViewModelFactory
import kotlinx.coroutines.launch
import java.util.*

class ModifyUserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModifyUserInfoBinding

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(AppDatabase.getDatabase(this).userDao(), applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModifyUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = ""

        val userId = 1  // Current user ID, to be retrieved dynamically according to your logic

        // Load existing user info if available
        observeUser()

        // Initialize the date input formatter
        formatDateInput()

        binding.btnSaveUserInfo.setOnClickListener {
            val name = binding.etName.text.toString()
            val weight = binding.etWeight.text.toString().toFloatOrNull() ?: 0f
            val height = binding.etHeight.text.toString().toFloatOrNull() ?: 0f
            val dateOfBirth = binding.etDateOfBirth.text.toString()

            lifecycleScope.launch {
                val user = userViewModel.user.value
                if (user != null) {
                    user.name = name
                    user.weight = weight
                    user.height = height
                    user.dateOfBirth = dateOfBirth
                    userViewModel.updateUser(user)
                } else {
                    val newUser = User(id = userId, name = name, weight = weight, height = height, dateOfBirth = dateOfBirth)
                    userViewModel.insertUser(newUser)
                }
            }

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Load user data
        userViewModel.getUserById(userId)
    }


    private fun observeUser() {
        userViewModel.user.observe(this, Observer { user ->
            user?.let {
                binding.etName.setText(it.name)
                binding.etWeight.setText(it.weight.toString())
                binding.etHeight.setText(it.height.toString())
                binding.etDateOfBirth.setText(it.dateOfBirth)
            }
        })
    }

    private fun formatDateInput() {
        binding.etDateOfBirth.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val yyyymmdd = "YYYYMMDD"

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    var clean = s.toString().replace("[^\\d.]|\\.".toRegex(), "")
                    val cleanC = current.replace("[^\\d.]|\\.".toRegex(), "")

                    val cl = clean.length
                    var sel = cl
                    if (clean == cleanC) sel--

                    if (clean.length < 8) {
                        clean += yyyymmdd.substring(clean.length)
                    } else {
                        var year = clean.substring(0, 4).toInt()
                        var mon = clean.substring(4, 6).toInt()
                        var day = clean.substring(6, 8).toInt()

                        if (mon > 12) mon = 12
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.MONTH, mon - 1)
                        year = if (year < 1900) 1900 else if (year > 2100) 2100 else year
                        cal.set(Calendar.YEAR, year)
                        day = if (day > cal.getActualMaximum(Calendar.DATE)) cal.getActualMaximum(Calendar.DATE) else day
                        clean = String.format("%04d%02d%02d", year, mon, day)
                    }

                    clean = String.format("%s-%s-%s", clean.substring(0, 4),
                        clean.substring(4, 6),
                        clean.substring(6, 8))

                    sel = if (sel < 0) 0 else sel
                    current = clean
                    binding.etDateOfBirth.setText(current)
                    binding.etDateOfBirth.setSelection(if (sel < current.length) sel else current.length)
                }
            }
        })
    }
}
