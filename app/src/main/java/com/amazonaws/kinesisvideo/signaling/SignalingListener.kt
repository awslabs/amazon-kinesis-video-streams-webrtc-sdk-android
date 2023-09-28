package com.amazonaws.kinesisvideo.signaling

import android.util.Base64
import android.util.Log
import com.amazonaws.kinesisvideo.signaling.model.Event
import com.google.gson.Gson
import jakarta.websocket.MessageHandler
import java.util.Locale



abstract class SignalingListener : Signaling {

    private val gson = Gson()
    private val TAG = "SignalingListener"

    @JvmField
    val messageHandler: MessageHandler = object : MessageHandler.Whole<String> {
        override fun onMessage(message: String) {
            if (message.isEmpty()) {
                return
            }
            Log.d(TAG, "Received message: $message")
            if (!message.contains("messagePayload")) {
                return
            }

            val evt = gson.fromJson(message, Event::class.java)

            if (evt?.messageType == null || evt.messagePayload!!.isEmpty()) {
                return
            }
            when (evt.messageType!!.uppercase(Locale.getDefault())) {
                "SDP_OFFER" -> {
                    Log.d(TAG, "Offer received: SenderClientId=" + evt.senderClientId)
                    Log.d(TAG, String(Base64.decode(evt.messagePayload, 0)))
                    onSdpOffer(evt)
                }

                "SDP_ANSWER" -> {
                    Log.d(TAG, "Answer received: SenderClientId=" + evt.senderClientId)
                    onSdpAnswer(evt)
                }

                "ICE_CANDIDATE" -> {
                    Log.d(TAG, "Ice Candidate received: SenderClientId=" + evt.senderClientId)
                    Log.d(TAG, String(Base64.decode(evt.messagePayload, 0)))
                    onIceCandidate(evt)
                }

                else -> {}
            }
        }
    }
}