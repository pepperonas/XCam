package io.celox.xcam.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.celox.xcam.service.RecordingService
import io.celox.xcam.util.Constants

class RecordingActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Constants.ACTION_STOP_RECORDING -> {
                RecordingService.stopRecording(context)
            }
        }
    }
}
