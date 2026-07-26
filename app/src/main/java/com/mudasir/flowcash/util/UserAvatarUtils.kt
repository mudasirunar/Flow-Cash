package com.mudasir.flowcash.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mudasir.flowcash.ui.theme.AvatarColors
import kotlin.math.abs

object UserAvatarUtils {

    fun getUserInitials(name: String?): String {
        if (name.isNullOrBlank()) return "FC"
        val parts = name.trim().split("\\s+".toRegex())
        return when {
            parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            parts.isNotEmpty() && parts[0].length >= 2 -> parts[0].take(2).uppercase()
            parts.isNotEmpty() -> parts[0].take(1).uppercase()
            else -> "FC"
        }
    }

    fun getAvatarColorHexForUser(email: String?, name: String? = null): String {
        val color = getAvatarColorForUser(name = name, email = email)
        return String.format("#%06X", (0xFFFFFF and color.toArgb()))
    }

    fun getAvatarColorForUser(name: String?, email: String? = null, customHex: String? = null): Color {
        if (!customHex.isNullOrBlank()) {
            try {
                return Color(android.graphics.Color.parseColor(customHex))
            } catch (_: Exception) {}
        }
        val identifier = email?.trim()?.lowercase()?.ifBlank { null }
            ?: name?.trim()?.lowercase()?.ifBlank { null }
            ?: "flowcash_user"
        val hash = abs(identifier.hashCode())
        val index = hash % AvatarColors.size
        return AvatarColors[index]
    }
}
