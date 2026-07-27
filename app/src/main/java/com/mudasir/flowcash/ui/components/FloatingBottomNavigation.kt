package com.mudasir.flowcash.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudasir.flowcash.R
import kotlinx.coroutines.delay

sealed class BottomNavItem(val route: String, val title: String, @DrawableRes val iconRes: Int) {
    data object Dashboard : BottomNavItem("dashboard", "Dashboard", R.drawable.ic_home)
    data object Transactions : BottomNavItem("transactions", "History", R.drawable.ic_history)
    data object Analytics : BottomNavItem("analytics", "Analytics", R.drawable.ic_analytics)
    data object Settings : BottomNavItem("settings", "Settings", R.drawable.ic_settings)
}

@Composable
fun FloatingBottomNavigation(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val isDark = isSystemInDarkTheme()

    val containerBg = if (isDark) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    } else {
        Color.White.copy(alpha = 0.88f)
    }

    val shadowColor = if (isDark) {
        Color.Black.copy(alpha = 0.5f)
    } else {
        Color.Black.copy(alpha = 0.18f)
    }

    val containerBorder = if (isDark) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    } else {
        Color(0xFFE2E8F0)
    }

    // Liquid Stretch State on Movement
    var targetStretchScale by remember { mutableFloatStateOf(1.0f) }
    LaunchedEffect(selectedIndex) {
        targetStretchScale = 1.16f
        delay(130)
        targetStretchScale = 1.0f
    }

    val animatedPillStretch by animateFloatAsState(
        targetValue = targetStretchScale,
        animationSpec = spring(
            dampingRatio = 0.48f, // elastic liquid rebound physics
            stiffness = Spring.StiffnessLow
        ),
        label = "PillLiquidStretch"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(28.dp))
            .background(containerBg)
            .border(
                1.dp,
                containerBorder,
                RoundedCornerShape(28.dp)
            )
            .padding(5.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val widthPerItem = maxWidth / items.size

            val indicatorOffset by animateDpAsState(
                targetValue = widthPerItem * selectedIndex,
                animationSpec = spring(
                    dampingRatio = 0.52f, // ultra fluid liquid spring glide
                    stiffness = Spring.StiffnessLow
                ),
                label = "FloatingNavPillOffset"
            )

            // Ultra Liquid Sliding Highlight Pill Indicator with dynamic stretch & snap-back
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(widthPerItem)
                    .height(50.dp)
                    .graphicsLayer {
                        scaleX = animatedPillStretch
                    }
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = if (isDark) 0.22f else 0.15f))
            )

            // Row of Navigation Items
            Row(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index

                    val animatedScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.25f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = 0.58f, // liquid spring bounce for icon
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "IconScaleAnimation"
                    )

                    val animatedAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1.0f else 0.45f,
                        animationSpec = spring(
                            dampingRatio = 0.8f,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "IconAlphaAnimation"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onItemSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.title,
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer {
                                        scaleX = animatedScale
                                        scaleY = animatedScale
                                        alpha = animatedAlpha
                                    }
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        primaryColor
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
