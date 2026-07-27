package com.mudasir.flowcash.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun TransactionFab(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Fast spring response for responsive tap feedback
    val fastSpringSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )

    // 1. Press Squeeze Scale
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.84f else 1.0f,
        animationSpec = fastSpringSpec,
        label = "PressSqueezeScale"
    )

    // 2. Corner Morphing
    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 28.dp else 18.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "CornerMorph"
    )

    // 3. Icon Rotation & Scale
    val iconRotation by animateFloatAsState(
        targetValue = if (visible) 0f else -90f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "IconRotation"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 0.80f else 1.0f,
        animationSpec = fastSpringSpec,
        label = "IconScale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut(animationSpec = tween(150)) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .size(56.dp)
                .clip(RoundedCornerShape(cornerRadius))
                // Translucent fill matching chips & dashboard tiles
                .background(
                    primaryColor.copy(
                        alpha = if (isPressed) 0.38f else 0.20f
                    )
                )
                // Crisp 1.5dp Solid Primary Border
                .border(
                    width = 1.5.dp,
                    color = primaryColor,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Transaction",
                tint = primaryColor, // Crisp primary icon that pops
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer {
                        rotationZ = iconRotation
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )
        }
    }
}