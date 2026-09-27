package io.celox.xcam.docs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * The badges at the top of the README state facts — version, test count, size of the codebase. A badge
 * that quietly goes stale is worse than no badge, so each one is measured against what it claims.
 * Only the line counts may drift a little (every commit moves them); the check is that they stay
 * roughly true.
 */
class ReadmeBadgesTest {
    private val readme = File("../README.md").readText()
    private val buildFile = File("build.gradle.kts").readText()

    /** shields.io static badge value: `img.shields.io/badge/<label>-<value>-<colour>` ("--" = literal dash). */
    private fun badgeValue(label: String): String {
        val m = Regex("""img\.shields\.io/badge/$label-([^?)]+)""").find(readme)
        assertTrue("no '$label' badge in the README", m != null)
        val parts = m!!.groupValues[1].split(Regex("""(?<!-)-(?!-)"""))
        return parts.dropLast(1).joinToString("-").replace("--", "-")
    }

    private fun kotlinLines(dir: String) =
        File(dir).walkTopDown().filter { it.isFile && it.extension == "kt" }.sumOf { it.readLines().size }

    private fun kCount(value: String) = value.removeSuffix("k").toDouble() * 1000

    @Test
    fun `the version badge matches the version the app is built with`() {
        val version = Regex("""versionName = "([^"]+)"""").find(buildFile)!!.groupValues[1]
        assertEquals(version, badgeValue("version"))
    }

    @Test
    fun `the test badge counts the tests that actually exist`() {
        val counted =
            File("src/test").walkTopDown().filter { it.isFile && it.extension == "kt" }
                .sumOf { Regex("""^\s*@Test\b""", RegexOption.MULTILINE).findAll(it.readText()).count() }
        assertEquals(counted, badgeValue("unit%20tests").toInt())
    }

    @Test
    fun `the lines-of-code badge is still roughly true`() {
        val claimed = kCount(badgeValue("lines%20of%20code"))
        val actual = kotlinLines("src/main/java").toDouble()
        assertTrue("LoC badge says $claimed, code has $actual", kotlin.math.abs(actual - claimed) <= claimed * 0.10)
    }

    @Test
    fun `the test-code badge is still roughly true`() {
        val claimed = kCount(badgeValue("test%20code"))
        val actual = kotlinLines("src/test/java").toDouble()
        assertTrue("test-code badge says $claimed, tests have $actual", kotlin.math.abs(actual - claimed) <= claimed * 0.10)
    }

    @Test
    fun `the SDK and JDK badges match the build`() {
        listOf("minSdk" to "min%20SDK", "targetSdk" to "target%20SDK", "compileSdk" to "compile%20SDK").forEach { (key, label) ->
            val actual = Regex("""$key = (\d+)""").find(buildFile)!!.groupValues[1]
            assertEquals(label, actual, badgeValue(label))
        }
        val ci = File("../.github/workflows/release.yml").readText()
        val jdk = Regex("""java-version: '(\d+)'""").find(ci)!!.groupValues[1]
        assertEquals(jdk, badgeValue("JDK"))
    }

    @Test
    fun `the APK-size badge is still roughly true`() {
        // Only measurable against a local release build; skipped (not faked) where none exists.
        val apk =
            File("build/outputs/apk/release").listFiles { f -> f.extension == "apk" }?.maxByOrNull { it.lastModified() }
        assumeTrue("needs a local release build", apk != null)
        val actualMb = apk!!.length() / 1024.0 / 1024.0
        val claimedMb = badgeValue("APK").removeSuffix("%20MB").toDouble()
        assertTrue("APK badge $claimedMb MB, build $actualMb MB", kotlin.math.abs(actualMb - claimedMb) <= claimedMb * 0.10)
    }

    @Test
    fun `the ABI badge names the only ABI the build produces`() {
        val include = Regex("""include\(([^)]*)\)""").find(buildFile)!!.groupValues[1]
        val built = Regex(""""([^"]+)"""").findAll(include).map { it.groupValues[1] }.toList()
        assertEquals(listOf(badgeValue("ABI").substringBefore("%20")), built)
    }

    @Test
    fun `the headline badges come first and everything they claim sits above the first heading`() {
        val order = listOf("version-", "unit%20tests-", "lines%20of%20code-").map { readme.indexOf(it) }
        assertTrue("missing headline badge", order.none { it < 0 })
        assertEquals(order.sorted(), order)
        assertTrue(readme.indexOf("\n## ") > order.max())
    }

    @Test
    fun `the donate button goes to the author's own PayPal, in euro, for this app`() {
        assertTrue(readme.contains("paypal.com/donate/?business=martin.pfeffer@celox.io&currency_code=EUR&item_name=XCam"))
    }

    @Test
    fun `the website and download buttons point at the product page`() {
        assertTrue(readme.contains("](https://x-cam.celox.io)") || readme.contains("href=\"https://x-cam.celox.io\""))
        assertTrue(readme.contains("https://x-cam.celox.io/download"))
    }

    @Test
    fun `every image the README shows exists in the repository`() {
        val local = Regex("""(?:src="|\]\()((?:docs)/[^")\s]+)""").findAll(readme).map { it.groupValues[1] }.toSet()
        assertTrue("README shows no local images", local.isNotEmpty())
        local.forEach { assertTrue("missing $it", File("../$it").isFile) }
    }
}
