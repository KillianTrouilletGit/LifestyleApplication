package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.model.User
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.ui.compose.theme.ProtocolCyan
import com.example.personallevelingsystem.ui.compose.theme.ProtocolGreen
import com.example.personallevelingsystem.viewmodel.UserViewModel

@Composable
fun UserProfileScreen(
    viewModel: UserViewModel,
    onBackClick: () -> Unit,
    onModifyClick: () -> Unit
) {
    val user by viewModel.user.observeAsState()
    var maxXp by remember { mutableIntStateOf(100) }

    LaunchedEffect(user) {
        user?.let {
            // Assuming this is a quick calculation or we can offload
            // Since we can't easily call suspend functions here without scope or knowledge of repo
            // We will just use the viewmodel function synchronously if it's not suspend, 
            // but the viewmodel definition showed it was just a function returning Int.
            maxXp = viewModel.calculateXpForNextLevel(it.level)
        }
    }

    // Trigger load if user is null (optional, depends on app flow)
    LaunchedEffect(Unit) {
        // Hardcoded ID 1 for single user app context usually
        if (user == null) {
            viewModel.getUserById(1)
        }
    }

    UserProfileContent(
        user = user,
        maxXp = maxXp,
        onBackClick = onBackClick,
        onModifyClick = onModifyClick
    )
}

@Composable
fun UserProfileContent(
    user: User?,
    maxXp: Int,
    onBackClick: () -> Unit,
    onModifyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OperatorHeader(subtitle = "Identity", title = "Operator Profile")
        
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, ProtocolCyan, CircleShape)
                .background(Color.Gray)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_user_placeholder),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = user?.name ?: "Unknown Operator",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Level
        Text(
            text = "Level: ${user?.level ?: 1}",
            style = MaterialTheme.typography.titleLarge,
            color = ProtocolGreen
        )

        Spacer(modifier = Modifier.height(24.dp))

        JuicyButton(
            onClick = onModifyClick,
            text = "MODIFY INFORMATION",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // XP Bar
        val currentXp = user?.xp ?: 0
        val progress = if (maxXp > 0) currentXp.toFloat() / maxXp.toFloat() else 0f
        
        Text(
            text = "XP: $currentXp / $maxXp",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(MaterialTheme.shapes.small),
            color = ProtocolCyan,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(modifier = Modifier.weight(1f))

        JuicyButton(
            onClick = onBackClick,
            text = "BACK",
            modifier = Modifier.align(Alignment.Start)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfilePreview() {
    PersonalLevelingSystemTheme {
        UserProfileContent(
            user = User(
                id = 1,
                name = "Hunter Name",
                xp = 450,
                level = 5,
                weight = 70f,
                height = 180f,
                dateOfBirth = "2000-01-01"
            ),
            maxXp = 1000,
            onBackClick = {},
            onModifyClick = {}
        )
    }
}
