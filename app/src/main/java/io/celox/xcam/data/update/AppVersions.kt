package io.celox.xcam.data.update

/** A published release: its tag (e.g. `v3.1.0`) and the release-notes page. */
data class AppUpdate(val version: String, val notesUrl: String)

/**
 * Compares release version strings like `3.0.1`, `v3.1` or `3.0.0-debug` (a leading `v` and any
 * non-numeric suffix are ignored). Pure, so the "is this release newer than the installed build?"
 * decision is unit-tested instead of failing silently in a background worker.
 */
object AppVersions {
    /** True when [candidate] is strictly newer than [installed]. Unparseable input is never newer. */
    fun isNewer(
        installed: String,
        candidate: String,
    ): Boolean {
        val a = parts(installed)
        val b = parts(candidate)
        if (a.isEmpty() || b.isEmpty()) return false
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return y > x
        }
        return false
    }

    /**
     * One notification per release: checks are on, the release is newer than the installed build and
     * newer than the last release a notification was already posted for.
     */
    fun shouldNotify(
        installed: String,
        known: AppUpdate?,
        notified: String?,
        enabled: Boolean,
    ): Boolean =
        enabled && known != null && isNewer(installed, known.version) &&
            (notified == null || isNewer(notified, known.version))

    private fun parts(version: String): List<Int> =
        version
            .trim()
            .removePrefix("v")
            .removePrefix("V")
            .takeWhile { it.isDigit() || it == '.' }
            .split('.')
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
}
