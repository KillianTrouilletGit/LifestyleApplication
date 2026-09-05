package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.model.User
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.ui.compose.components.ArcButton
import com.example.personallevelingsystem.ui.compose.components.ArcCard
import com.example.personallevelingsystem.ui.compose.components.ArcProgressBar
import com.example.personallevelingsystem.ui.compose.components.ArcStat
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.ui.compose.theme.PrimaryGradient
import com.example.personallevelingsystem.ui.compose.theme.SignalCyan
import com.example.personallevelingsystem.ui.compose.theme.SurfaceHigh
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary
import com.example.personallevelingsystem.ui.compose.theme.tabular
import com.example.personallevelingsystem.viewmodel.UserViewModel

@Composable
fun UserProfileScreen(
    viewModel: UserViewModel,
    onModifyClick: () -> Unit
) {
    val user by viewModel.user.observeAsState()
    var maxXp by remember { mutableIntStateOf(100) }

    LaunchedEffect(user) {
        user?.let { maxXp = viewModel.calculateXpForNextLevel(it.level) }
    }

    LaunchedEffect(Unit) {
        if (user == null) {
            viewModel.getUserById(UserRepository.DEFAULT_USER_ID)
        }
    }

    UserProfileContent(
        user = user,
        maxXp = maxXp,
        onModifyClick = onModifyClick
    )
}

@Composable
fun UserProfileContent(
    user: User?,
    maxXp: Int,
    onModifyClick: () -> Unit
) {
    val currentXp = user?.xp ?: 0
    val progress = if (maxXp > 0) currentXp.toFloat() / maxXp else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DesignSystem.Padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ArcCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .border(2.dp, PrimaryGradient, CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(SurfaceHigh)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_user_placeholder),
                        contentDescription = "Profile image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.name?.takeIf { it.isNotBlank() } ?: "Unnamed",
                        style = MaterialTheme.typography.headlineSmall,
                        color = HologramText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .background(CrimsonRed.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                            .border(1.dp, CrimsonRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "LEVEL ${user?.level ?: 1}",
                            style = MaterialTheme.typography.labelMedium.tabular,
                            color = CrimsonRed,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Text(
                    text = "XP TO NEXT LEVEL",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "%,d / %,d".format(currentXp, maxXp),
                    style = MaterialTheme.typography.labelSmall.tabular,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            ArcProgressBar(progress = progress, color = CrimsonRed, height = 6.dp)
        }

        ArcCard(modifier = Modifier.fillMaxWidth()) {
            val statStyle = MaterialTheme.typography.titleMedium.tabular
            Row {
                ArcStat(
                    label = "Weight",
                    value = user?.weight?.takeIf { it > 0f }?.let { formatMeasure(it, "kg") } ?: "—",
                    valueColor = SignalCyan,
                    valueStyle = statStyle,
                    modifier = Modifier.weight(1f)
                )
                ArcStat(
                    label = "Height",
                    value = user?.height?.takeIf { it > 0f }?.let { formatMeasure(it, "cm") } ?: "—",
                    valueStyle = statStyle,
                    modifier = Modifier.weight(1f)
                )
                ArcStat(
                    label = "Born",
                    value = user?.dateOfBirth?.takeIf { it.isNotBlank() } ?: "—",
                    valueStyle = statStyle,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }

        ArcButton(
            text = "Edit profile",
            subtitle = "Name, weight, height, birth date",
            icon = Icons.Rounded.Edit,
            onClick = onModifyClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun formatMeasure(value: Float, unit: String): String =
    if (value % 1f == 0f) "${value.toInt()} $unit" else "%.1f %s".format(value, unit)

@Preview(showBackground = true, backgroundColor = 0xFF0B0A10)
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
            onModifyClick = {}
        )
    }
}
