package com.mudasir.flowcash

import com.mudasir.flowcash.util.UserAvatarUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserAvatarUtilsTest {

    @Test
    fun testGetUserInitials_twoWords_returnsFirstLetters() {
        val initials = UserAvatarUtils.getUserInitials("Mudasir Unar")
        assertEquals("MU", initials)
    }

    @Test
    fun testGetUserInitials_singleWord_returnsFirstTwoLetters() {
        val initials = UserAvatarUtils.getUserInitials("FlowCash")
        assertEquals("FL", initials)
    }

    @Test
    fun testGetUserInitials_nullOrBlank_returnsDefault() {
        assertEquals("FC", UserAvatarUtils.getUserInitials(null))
        assertEquals("FC", UserAvatarUtils.getUserInitials("   "))
    }

    @Test
    fun testGetAvatarColorForUser_sameEmail_returnsIdenticalColor() {
        val email = "user@flowcash.com"
        val color1 = UserAvatarUtils.getAvatarColorForUser(name = "User One", email = email)
        val color2 = UserAvatarUtils.getAvatarColorForUser(name = "User Two", email = email)

        assertEquals("Same email must yield identical avatar color regardless of screen or name", color1, color2)
    }

    @Test
    fun testGetAvatarColorHexForUser_returnsValidFormattedHex() {
        val hex = UserAvatarUtils.getAvatarColorHexForUser("john.doe@example.com", "John Doe")
        assertNotNull(hex)
        assertTrue("Hex must start with #", hex.startsWith("#"))
        assertEquals("Hex string length should be 7", 7, hex.length)
    }

    @Test
    fun testGetAvatarColorForUser_validCustomHex_parsesCorrectly() {
        val customHex = "#4F46E5"
        val color = UserAvatarUtils.getAvatarColorForUser(name = "Test", email = "test@example.com", customHex = customHex)
        assertNotNull(color)
    }
}
