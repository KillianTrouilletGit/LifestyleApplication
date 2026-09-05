package com.example.personallevelingsystem.ui.compose.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import com.example.personallevelingsystem.ui.compose.theme.AccentViolet
import com.example.personallevelingsystem.ui.compose.theme.BorderGradient
import com.example.personallevelingsystem.ui.compose.theme.BorderSubtle
import com.example.personallevelingsystem.ui.compose.theme.CardGradient
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.DeepViolet
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.GlassFill
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.Motion
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary
import com.example.personallevelingsystem.ui.compose.theme.TextTertiary
import com.example.personallevelingsystem.ui.compose.theme.tabular
import com.example.personallevelingsystem.util.hapticTap

/** Near-opaque card over the ambient wash, hairline gradient border, optional left accent bar. */
@Composable
fun ArcCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(DesignSystem.Radius.card),
    contentPadding: PaddingValues = PaddingValues(DesignSystem.Padding),
    accent: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val view = LocalView.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.98f else 1f,
        animationSpec = Motion.Press,
        label = "arcCardScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(CardGradient)
            .drawBehind {
                if (accent != null) {
                    drawRect(color = accent, size = Size(3.dp.toPx(), size.height))
                }
            }
            .border(1.dp, BorderGradient, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(interactionSource = interaction, indication = null) {
                        view.hapticTap()
                        onClick()
                    }
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/** Violet overline used to introduce a group of cards or rows. */
@Composable
fun ArcSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    trailing: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = AccentViolet,
            letterSpacing = 1.5.sp,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(text = trailing, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

/** Tappable row inside a card: icon tile, title, optional subtitle, chevron. */
@Composable
fun ArcListRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    tint: Color = CrimsonRed,
    showChevron: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    ArcCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(DesignSystem.Radius.chip))
                        .background(tint.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = HologramText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            trailing?.invoke()
            if (showChevron) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/** Label + big tabular number. */
@Composable
fun ArcStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = HologramText,
    valueStyle: TextStyle = MaterialTheme.typography.headlineMedium.tabular
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Thin rounded progress bar (no Material stop dot). */
@Composable
fun ArcProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = CrimsonRed,
    trackColor: Color = color.copy(alpha = 0.15f),
    height: Dp = 5.dp
) {
    val shape = RoundedCornerShape(height / 2)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(color, shape)
        )
    }
}

/** Two-to-four way pill switch with a sliding gradient indicator. */
@Composable
fun ArcSegmentedTabs(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pill = RoundedCornerShape(50)
    BoxWithConstraints(
        modifier = modifier
            .height(44.dp)
            .clip(pill)
            .background(GlassFill)
            .border(1.dp, BorderSubtle, pill)
            .padding(4.dp)
    ) {
        val segmentWidth = maxWidth / options.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selected,
            animationSpec = Motion.standard(),
            label = "segmentedIndicator"
        )
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(pill)
                .background(Brush.horizontalGradient(listOf(CrimsonRed, DeepViolet)))
        )
        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, label ->
                val interaction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(interactionSource = interaction, indication = null) { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (index == selected) Color.White else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
