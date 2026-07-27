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

    // 1. Elastic Liquid Squish Physics on Press
    val squishX by animateFloatAsState(
        targetValue = if (isPressed) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SquishX"
    )
    val squishY by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SquishY"
    )

    // 2. Corner Morphing
    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 24.dp else 18.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "CornerMorph"
    )

    // 3. Icon Rotation on Show / Hide
    val iconRotation by animateFloatAsState(
        targetValue = if (visible) 0f else -90f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "IconRotation"
    )

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
                    scaleX = squishX
                    scaleY = squishY
                }
                .size(56.dp)
                .clip(RoundedCornerShape(cornerRadius))
                // Glass-tinted background (matches app surface variant & primary tint for high visibility)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                // 1.5.dp Solid Primary Border
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary,
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
                tint = MaterialTheme.colorScheme.primary, // Primary color matching border
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer {
                        rotationZ = iconRotation
                    }
            )
        }
    }
}