package com.mudasir.flowcash.util

import androidx.compose.ui.graphics.Color
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

    fun getAvatarColorForUser(name: String?, email: String?): Color {
        val key = "${name.orEmpty()}_${email.orEmpty()}"
        val hash = abs(key.hashCode())
        val index = hash % AvatarColors.size
        return AvatarColors[index]
    }
}
