package com.example.personallevelingsystem.ui.compose.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.ui.compose.theme.ArcStyle
import com.example.personallevelingsystem.ui.compose.theme.BorderGradient
import com.example.personallevelingsystem.ui.compose.theme.BorderStrong
import com.example.personallevelingsystem.ui.compose.theme.BorderSubtle
import com.example.personallevelingsystem.ui.compose.theme.ButtonStyle
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.DeepViolet
import com.example.personallevelingsystem.ui.compose.theme.GlassFill
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.Motion
import com.example.personallevelingsystem.ui.compose.theme.SheenGradient
import com.example.personallevelingsystem.ui.compose.theme.SurfaceElevated
import com.example.personallevelingsystem.ui.compose.theme.SurfaceHigh
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary
import com.example.personallevelingsystem.util.hapticTap

private val Pill = RoundedCornerShape(50)

/**
 * Primary action. Renders in the style chosen in the Style Lab (gradient glow by default).
 * Give it an icon, a subtitle and a chevron for a "furnished" call to action, or just text.
 */
@Composable
fun ArcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    showChevron: Boolean = subtitle != null,
    enabled: Boolean = true,
    compact: Boolean = false,
    style: ButtonStyle = ArcStyle.buttonStyle
) {
    val view = LocalView.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = Motion.Press,
        label = "arcButtonScale"
    )

    val rich = icon != null || subtitle != null || showChevron
    val height = when {
        compact -> 44.dp
        subtitle != null -> 64.dp
        else -> 54.dp
    }
    val contentColor = when {
        !enabled -> TextSecondary
        style == ButtonStyle.Glass -> HologramText
        else -> Color.White
    }
    val subtitleColor = if (style == ButtonStyle.Glass) TextSecondary else Color.White.copy(alpha = 0.72f)

    Row(
        modifier = modifier
            .scale(scale)
            .height(height)
            .buttonSurface(style, enabled)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled) {
                view.hapticTap()
                onClick()
            }
            .padding(horizontal = if (compact) 14.dp else 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (rich) Arrangement.Start else Arrangement.Center
    ) {
        if (icon != null) {
            IconBadge(icon = icon, style = style, enabled = enabled, compact = compact)
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = if (rich) Modifier.weight(1f) else Modifier) {
            Text(
                text = text,
                style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (showChevron) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/** Secondary / destructive-adjacent action: outlined pill, no fill. */
@Composable
fun ArcGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = HologramText,
    enabled: Boolean = true,
    compact: Boolean = false
) {
    val view = LocalView.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = Motion.Press,
        label = "arcGhostScale"
    )
    val color = if (enabled) tint else TextSecondary

    Row(
        modifier = modifier
            .scale(scale)
            .height(if (compact) 44.dp else 50.dp)
            .clip(Pill)
            .border(1.dp, if (enabled) BorderStrong else BorderSubtle, Pill)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled) {
                view.hapticTap()
                onClick()
            }
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun IconBadge(icon: ImageVector, style: ButtonStyle, enabled: Boolean, compact: Boolean) {
    val (background, tint) = when {
        !enabled -> SurfaceHigh to TextSecondary
        style == ButtonStyle.Glass -> CrimsonRed.copy(alpha = 0.18f) to CrimsonRed
        else -> Color.White.copy(alpha = 0.18f) to Color.White
    }
    Box(
        modifier = Modifier
            .size(if (compact) 24.dp else 32.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(if (compact) 15.dp else 18.dp)
        )
    }
}

private fun Modifier.buttonSurface(style: ButtonStyle, enabled: Boolean): Modifier {
    if (!enabled) {
        return clip(Pill).background(SurfaceElevated).border(1.dp, BorderSubtle, Pill)
    }
    return when (style) {
        ButtonStyle.GradientGlow -> this
            .shadow(
                elevation = 14.dp,
                shape = Pill,
                clip = false,
                ambientColor = CrimsonRed.copy(alpha = 0.45f),
                spotColor = DeepViolet.copy(alpha = 0.7f)
            )
            .clip(Pill)
            .background(Brush.horizontalGradient(listOf(CrimsonRed, DeepViolet)))
            .sheen()

        ButtonStyle.FlatShadow -> this
            .shadow(
                elevation = 10.dp,
                shape = Pill,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.7f)
            )
            .clip(Pill)
            .background(CrimsonRed)

        ButtonStyle.Glass -> this
            .clip(Pill)
            .background(GlassFill)
            .border(1.dp, BorderGradient, Pill)
            .sheen()
    }
}

private fun Modifier.sheen(): Modifier = drawBehind {
    drawRect(brush = SheenGradient, size = Size(size.width, size.height * 0.55f))
}
