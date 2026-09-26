package io.celox.xcam.data.update

import io.celox.xcam.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fetches the newest published release. Best-effort: any network or parse problem returns null — an
 * update hint is a convenience, never worth an error.
 *
 * The product page's `latest.json` comes first: it mirrors GitHub Releases every 15 minutes and, unlike
 * the GitHub API, has no 60-requests-per-hour limit per IP (many phones behind one carrier NAT would
 * hit it). GitHub is the fallback while the site is down. Nothing but the request itself is sent.
 */
object UpdateChecker {
    const val SITE_LATEST_URL = "https://x-cam.celox.io/latest.json"
    const val GITHUB_LATEST_URL = "https://api.github.com/repos/pepperonas/XCam/releases/latest"

    /** What a tap on the notification opens: the product page with download button and changelog. */
    const val WEBSITE_URL = "https://x-cam.celox.io"

    private const val TIMEOUT_MS = 10_000
    private const val MAX_BODY_BYTES = 256 * 1024

    suspend fun fetchLatest(): AppUpdate? =
        withContext(Dispatchers.IO) {
            get(SITE_LATEST_URL, null)?.let(ReleaseParser::parseSite)
                ?: get(GITHUB_LATEST_URL, "application/vnd.github+json")?.let(ReleaseParser::parseGitHub)
        }

    private fun get(
        url: String,
        accept: String?,
    ): String? =
        runCatching {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "XCam/${BuildConfig.VERSION_NAME} (Android)")
                accept?.let { connection.setRequestProperty("Accept", it) }
                if (connection.responseCode != HttpURLConnection.HTTP_OK) return@runCatching null
                // A release manifest is a few hundred bytes; never read an unbounded body.
                connection.inputStream.use { stream ->
                    String(stream.readNBytesCompat(MAX_BODY_BYTES), Charsets.UTF_8)
                }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()

    private fun java.io.InputStream.readNBytesCompat(limit: Int): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (out.size() < limit) {
            val read = read(buffer, 0, minOf(buffer.size, limit - out.size()))
            if (read < 0) break
            out.write(buffer, 0, read)
        }
        return out.toByteArray()
    }
}
