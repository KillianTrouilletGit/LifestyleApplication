package com.example.personallevelingsystem.ui.compose.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.ui.compose.theme.BorderGradient
import com.example.personallevelingsystem.ui.compose.theme.BouncySpring
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.GlassGradient
import com.example.personallevelingsystem.ui.compose.theme.PrimaryAccent
import com.example.personallevelingsystem.ui.compose.theme.SpaceBlack

@Composable
fun JuicyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Spring Physics Scaling
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = BouncySpring,
        label = "buttonScale"
    )

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier.scale(scale),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = PrimaryAccent, // Neon Text
            disabledContainerColor = Color.Transparent
        ),
        border = BorderStroke(1.5.dp, com.example.personallevelingsystem.ui.compose.theme.PrimaryGradient),
        shape = RoundedCornerShape(8.dp) // Technical "cut" look
    ) {
        Text(
            text = text.uppercase(), // HUD style
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun JuicyCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(DesignSystem.CornerRadius),
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = BouncySpring,
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .border(
                BorderStroke(1.dp, BorderGradient),
                shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black, // Ensure opaque black background behind glass/border
        ),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(GlassGradient)
                .padding(DesignSystem.Padding)
                .fillMaxSize(), // Fill available space (height mainly)
            contentAlignment = Alignment.Center // Center content
        ) {
            content()
        }
    }
}

@Composable
fun OperatorHeader(
    subtitle: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = subtitle.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = PrimaryAccent,
            letterSpacing = 2.sp
        )
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth(0.4f)
                .background(BorderGradient)
        )
    }
}

@Composable
fun JuicyInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            singleLine = true,
            readOnly = readOnly,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (onClick != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onClick)
            )
        }
    }
}
