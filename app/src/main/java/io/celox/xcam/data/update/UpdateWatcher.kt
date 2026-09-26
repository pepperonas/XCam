package io.celox.xcam.data.update

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.celox.xcam.BuildConfig
import io.celox.xcam.data.PreferencesManager
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * One release check, shared by app start and the background worker: fetch the newest release,
 * remember it (the Record screen shows a hint while it is newer than this build), and post a
 * notification for it — once per release and only while checks are switched on. A notification that
 * could not be shown (permission missing) is not recorded, so it appears once the permission is given.
 */
object UpdateWatcher {
    private const val WORK_NAME = "app_update_check"
    private const val INTERVAL_HOURS = 12L

    /** App start: at most every [INTERVAL_HOURS], so opening the app often costs no extra requests. */
    private const val START_THROTTLE_MS = INTERVAL_HOURS * 60 * 60 * 1000

    suspend fun check(
        context: Context,
        installedVersion: String = BuildConfig.VERSION_NAME,
    ): AppUpdate? {
        val prefs = PreferencesManager(context)
        if (!prefs.settings.first().updateChecks) return null
        val release = UpdateChecker.fetchLatest() ?: return null
        prefs.setKnownUpdate(release)
        prefs.setLastUpdateCheckMs(System.currentTimeMillis())
        val notified = prefs.notifiedUpdateVersion.first()
        if (AppVersions.shouldNotify(installedVersion, release, notified, enabled = true) &&
            UpdateNotifier.show(context, release)
        ) {
            prefs.setNotifiedUpdateVersion(release.version)
        }
        return release
    }

    suspend fun checkIfDue(context: Context) {
        val last = PreferencesManager(context).lastUpdateCheckMs.first()
        if (System.currentTimeMillis() - last >= START_THROTTLE_MS) check(context)
    }

    /** Keeps one periodic check scheduled. KEEP: calling it on every start must not reset the period. */
    fun schedule(context: Context) {
        val request =
            PeriodicWorkRequestBuilder<Worker>(INTERVAL_HOURS, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /** The background half: twice a day, only with a network, best-effort (no retry loop). */
    class Worker(
        context: Context,
        params: WorkerParameters,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            runCatching { check(applicationContext) }
            return Result.success()
        }
    }
}
