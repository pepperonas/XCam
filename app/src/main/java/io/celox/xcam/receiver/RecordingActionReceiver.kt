package io.celox.xcam.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.celox.xcam.service.RecordingService
import io.celox.xcam.util.Constants

/** Handles the notification's "Stop" action. */
class RecordingActionReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == Constants.ACTION_STOP_RECORDING) {
            RecordingService.stopRecording(context)
        }
    }
}
