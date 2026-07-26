package com.mudasir.flowcash.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.mudasir.flowcash.util.UserAvatarUtils

@Composable
fun UserProfileAvatar(
    name: String?,
    email: String? = null,
    profilePicUrl: String? = null,
    avatarColorHex: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    borderWidth: Dp = 2.dp,
    borderColor: Color = Color.White.copy(alpha = 0.35f)
) {
    val initials = remember(name) { UserAvatarUtils.getUserInitials(name) }
    val avatarBgColor = remember(name, email, avatarColorHex) {
        UserAvatarUtils.getAvatarColorForUser(name, email, avatarColorHex)
    }

    val resolvedPhotoUrl = remember(profilePicUrl) {
        profilePicUrl?.ifBlank { null } ?: FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
    }

    var isLoading by remember { mutableStateOf(!resolvedPhotoUrl.isNullOrBlank()) }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(avatarBgColor)
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!resolvedPhotoUrl.isNullOrBlank() && !isError) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(resolvedPhotoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "User Profile Picture",
                contentScale = ContentScale.Crop,
                onState = { state ->
                    when (state) {
                        is AsyncImagePainter.State.Loading -> {
                            isLoading = true
                            isError = false
                        }
                        is AsyncImagePainter.State.Success -> {
                            isLoading = false
                            isError = false
                        }
                        is AsyncImagePainter.State.Error -> {
                            isLoading = false
                            isError = true
                        }
                        else -> {}
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(size * 0.4f),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = (size.value * 0.38f).sp
                )
            )
        }
    }
}
