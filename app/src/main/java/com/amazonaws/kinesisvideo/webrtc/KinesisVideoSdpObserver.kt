package com.amazonaws.kinesisvideo.webrtc

import android.util.Log
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class KinesisVideoSdpObserver : SdpObserver {
    private val TAG = KinesisVideoSdpObserver::class.java.simpleName
    override fun onCreateSuccess(sessionDescription: SessionDescription) {
        Log.d(TAG, "onCreateSuccess(): SDP=" + sessionDescription.description)
    }

    override fun onSetSuccess() {
        Log.d(TAG, "onSetSuccess(): SDP")
    }

    override fun onCreateFailure(error: String) {
        Log.e(TAG, "onCreateFailure(): Error=$error")
    }

    override fun onSetFailure(error: String) {
        Log.e(TAG, "onSetFailure(): Error=$error")
    }
}