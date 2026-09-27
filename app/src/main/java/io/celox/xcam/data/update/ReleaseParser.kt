package io.celox.xcam.data.update

import org.json.JSONObject

/** Pure parsers for the two release sources, so their shape assumptions are pinned by tests. */
object ReleaseParser {
    /**
     * The product page's `latest.json` (written from GitHub Releases by the site's timer):
     * `{"version":"v3.1.0","notes":"https://github.com/…/releases/tag/v3.1.0",…}`. Only published
     * releases reach that file. The notes link is later opened in a browser, so only https is accepted.
     */
    fun parseSite(body: String): AppUpdate? =
        runCatching {
            val obj = JSONObject(body)
            val version = obj.text("version")?.takeIf { it.isNotBlank() }
            val notes = obj.text("notes")?.takeIf { it.startsWith("https://") }
            if (version == null || notes == null) null else AppUpdate(version, notes)
        }.getOrNull()

    /** GitHub's `releases/latest` response. Drafts and prereleases are never offered to users. */
    fun parseGitHub(body: String): AppUpdate? =
        runCatching {
            val obj = JSONObject(body)
            if (obj.optBoolean("draft") || obj.optBoolean("prerelease")) return@runCatching null
            val tag = obj.text("tag_name")?.takeIf { it.isNotBlank() }
            val url = obj.text("html_url")?.takeIf { it.startsWith("https://") }
            if (tag == null || url == null) null else AppUpdate(tag, url)
        }.getOrNull()

    /**
     * The value when it is a JSON string, else null. Android's `optString` turns a JSON `null` into
     * the text "null", which would pass as a release version.
     */
    private fun JSONObject.text(key: String): String? = opt(key) as? String
}
