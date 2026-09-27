package io.celox.xcam.res

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** Manifest properties the recording-with-screen-off promise depends on. */
class ManifestTest {
    private val ns = "http://schemas.android.com/apk/res/android"
    private val doc =
        DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            .newDocumentBuilder().parse(File("src/main/AndroidManifest.xml"))

    private fun elements(tag: String) =
        doc.getElementsByTagName(tag).let { list -> (0 until list.length).map { list.item(it) as Element } }

    private fun Element.android(attr: String) = getAttributeNS(ns, attr)

    private val permissions = elements("uses-permission").map { it.android("name") }.toSet()

    @Test
    fun `camera, microphone and their foreground-service types are requested`() {
        listOf(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.FOREGROUND_SERVICE",
            "android.permission.FOREGROUND_SERVICE_CAMERA",
            "android.permission.FOREGROUND_SERVICE_MICROPHONE",
            "android.permission.WAKE_LOCK",
            "android.permission.POST_NOTIFICATIONS",
        ).forEach { assertTrue("missing $it", it in permissions) }
    }

    @Test
    fun `no storage write permission - recordings go through MediaStore`() {
        assertTrue("android.permission.WRITE_EXTERNAL_STORAGE" !in permissions)
        assertTrue("android.permission.MANAGE_EXTERNAL_STORAGE" !in permissions)
    }

    @Test
    fun `the recording service is private and typed camera plus microphone`() {
        val service = elements("service").single { it.android("name").endsWith("RecordingService") }
        assertEquals("false", service.android("exported"))
        assertEquals(setOf("camera", "microphone"), service.android("foregroundServiceType").split('|').toSet())
    }

    @Test
    fun `the stop receiver cannot be triggered by other apps`() {
        val receiver = elements("receiver").single { it.android("name").endsWith("RecordingActionReceiver") }
        assertEquals("false", receiver.android("exported"))
    }

    @Test
    fun `exactly one exported activity, the launcher, single top`() {
        val exported = elements("activity").filter { it.android("exported") == "true" }
        assertEquals(1, exported.size)
        assertTrue(exported.single().android("name").endsWith("MainActivity"))
        assertEquals("singleTop", exported.single().android("launchMode"))
        val actions = exported.single().getElementsByTagName("action")
        assertEquals("android.intent.action.MAIN", (actions.item(0) as Element).android("name"))
    }

    @Test
    fun `predictive back, per-app languages and the application class are wired`() {
        val application = elements("application").single()
        assertEquals("true", application.android("enableOnBackInvokedCallback"))
        assertEquals("@xml/locales_config", application.android("localeConfig"))
        assertEquals(".XCamApplication", application.android("name"))
        assertEquals("@mipmap/ic_launcher_round", application.android("roundIcon"))
    }

    @Test
    fun `a camera is required, autofocus is not`() {
        val features = elements("uses-feature").associate { it.android("name") to it.android("required") }
        assertEquals("true", features["android.hardware.camera"])
        assertEquals("false", features["android.hardware.camera.autofocus"])
    }
}
