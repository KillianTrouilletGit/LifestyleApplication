package com.example.personallevelingsystem.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.personallevelingsystem.databinding.ActivityUserProfileBinding
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.viewmodel.UserViewModel
import com.example.personallevelingsystem.viewmodel.UserViewModelFactory
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.util.NotificationUtils
import com.example.personallevelingsystem.R

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding

    // Initialisation du ViewModel avec la Factory
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(AppDatabase.getDatabase(this).userDao(), applicationContext))
    }

    // Gestionnaire pour demander les permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("UserProfileActivity", "Notification permission granted")
            // Vous pouvez faire l'action nécessitant la permission ici
        } else {
            Log.d("UserProfileActivity", "Notification permission denied")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = ""

        // Créer le canal de notification
        NotificationUtils.createNotificationChannel(this)

        val userId = 1  // Id utilisateur courant, à récupérer dynamiquement selon votre logique

        // Observer pour obtenir les données de l'utilisateur
        observeUser()

        // Ajouter un bouton pour retourner en arrière
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Vérifiez et demandez la permission si nécessaire
        checkAndRequestNotificationPermission()

        // Charger les données utilisateur
        userViewModel.getUserById(userId)
    }

    private fun observeUser() {
        userViewModel.user.observe(this, Observer { user ->
            user?.let {
                Log.d("UserProfileActivity", "User data observed: $it")
                val xpForNextLevel = userViewModel.calculateXpForNextLevel(it.level)
                binding.tvUserName.text = "NAME: ${it.name.uppercase()}"
                binding.tvUserLevel.text = "LEVEL: ${it.level}"
                binding.tvUserXP.text = "XP: ${it.xp} / $xpForNextLevel"

                // Update progress bar
                binding.xpProgressBar.max = xpForNextLevel
                binding.xpProgressBar.progress = it.xp


            } ?: run {
                Log.d("UserProfileActivity", "User data is null")
                binding.tvUserName.text = "NAME: N/A"
                binding.tvUserLevel.text = "LEVEL: N/A"
                binding.tvUserXP.text = "XP: N/A"
                binding.xpProgressBar.max = 100
                binding.xpProgressBar.progress = 0
            }
        })
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkAndRequestNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Vous avez la permission
                Log.d("UserProfileActivity", "Notification permission already granted")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // Afficher une explication à l'utilisateur, puis demander la permission
                Log.d("UserProfileActivity", "Showing permission rationale")
                // Afficher un dialogue ou une vue pour expliquer pourquoi vous avez besoin de cette permission
                // Après l'explication, demandez la permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            else -> {
                // Demandez la permission pour la première fois
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
