package io.celox.xcam.docs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The README, the FAQ and the Settings text promise that the update check is XCam's **only** network
 * access. This pins that promise to the code: every URL in the app's sources is one of the known ones,
 * nothing is fetched over plain http, and the permissions are exactly what the promise allows.
 */
class NetworkPolicyTest {
    private val sources =
        File("src/main/java").walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

    @Test
    fun `the app talks only to its own site and GitHub`() {
        val urls =
            sources.flatMap { f -> Regex("""https?://[^"\s)]+""").findAll(f.readText()).map { it.value }.toList() }
                .map { it.substringAfter("://").substringBefore('/') }
                .toSet()
        assertEquals(setOf("x-cam.celox.io", "api.github.com", "github.com"), urls - setOf("schemas.android.com"))
    }

    @Test
    fun `nothing uses plain http`() {
        sources.forEach { f ->
            assertTrue("${f.name} uses http://", !Regex("""["']http://""").containsMatchIn(f.readText()))
        }
    }

    @Test
    fun `the manifest asks for internet but for no location, contacts or accounts`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android.permission.INTERNET"))
        listOf("ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION", "READ_CONTACTS", "GET_ACCOUNTS", "READ_PHONE_STATE")
            .forEach { assertTrue("unexpected $it", !manifest.contains(it)) }
    }

    @Test
    fun `the settings text names the domain the checker really uses`() {
        val checker = File("src/main/java/io/celox/xcam/data/update/UpdateChecker.kt").readText()
        val domain = Regex("""SITE_LATEST_URL = "https://([^/]+)/""").find(checker)!!.groupValues[1]
        listOf("values", "values-de").forEach { dir ->
            val hint = Regex("""name="setting_update_checks_hint">([^<]+)<""").find(File("src/main/res/$dir/strings.xml").readText())!!.groupValues[1]
            assertTrue("$dir hint does not name $domain", hint.contains(domain))
        }
    }
}
