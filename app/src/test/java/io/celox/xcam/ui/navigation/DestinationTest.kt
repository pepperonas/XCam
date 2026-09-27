package io.celox.xcam.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DestinationTest {
    @Test
    fun `routes and labels are unique`() {
        val entries = Destination.entries
        assertEquals(entries.size, entries.map { it.route }.toSet().size)
        assertEquals(entries.size, entries.map { it.labelRes }.toSet().size)
    }

    @Test
    fun `tabIndexOf finds each tab and nothing else`() {
        Destination.entries.forEachIndexed { i, d -> assertEquals(i, Destination.tabIndexOf(d.route)) }
        assertEquals(-1, Destination.tabIndexOf(null))
        assertEquals(-1, Destination.tabIndexOf(""))
        assertEquals(-1, Destination.tabIndexOf(Routes.PLAYER))
        assertEquals(-1, Destination.tabIndexOf("RECORD"))
    }

    @Test
    fun `record is the first tab, the fixed root of back navigation`() {
        assertEquals(Destination.RECORD, Destination.entries.first())
    }

    @Test
    fun `each tab has a distinct selected and resting icon`() {
        Destination.entries.forEach { d ->
            assertNotSame("${d.name} uses the same icon twice", d.icon(), d.selectedIcon())
            assertTrue(d.icon().name != d.selectedIcon().name)
        }
    }

    @Test
    fun `the player route fills the id argument of its pattern`() {
        assertEquals("player/{id}", Routes.PLAYER)
        assertEquals("player/42", Routes.player(42))
        assertEquals("player/-1", Routes.player(-1))
        assertEquals(Routes.PLAYER.substringBefore('{'), Routes.player(7).substringBefore('7'))
    }

    @Test
    fun `no tab route collides with the player prefix`() {
        Destination.entries.forEach { assertTrue(!it.route.startsWith("player")) }
    }
}
