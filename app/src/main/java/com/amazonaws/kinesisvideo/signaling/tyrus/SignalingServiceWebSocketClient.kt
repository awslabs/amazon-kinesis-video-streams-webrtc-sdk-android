package com.amazonaws.kinesisvideo.signaling.tyrus

import android.util.Base64
import android.util.Log
import com.amazonaws.kinesisvideo.signaling.SignalingListener
import com.amazonaws.kinesisvideo.signaling.model.Message
import com.google.gson.Gson
import org.glassfish.tyrus.client.ClientManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

/**
 * Signaling service client based on websocket.
 */
class SignalingServiceWebSocketClient(
    uri: String,
    signalingListener: SignalingListener?,
    executorService: ExecutorService
) {
    private val websocketClient: WebSocketClient
    private val executorService: ExecutorService
    private val gson = Gson()
    private val TAG = "SignalingServiceWebSocketClient"

    init {
        Log.d(TAG, "Connecting to URI $uri as master")
        websocketClient = WebSocketClient(uri, ClientManager(), signalingListener!!, executorService)
        this.executorService = executorService
    }

    val isOpen = websocketClient.isOpen()

    fun sendSdpOffer(offer: Message) {
        executorService.submit {
            if (offer.action.equals("SDP_OFFER", ignoreCase = true)) {
                Log.d(TAG, "Sending Offer")
                send(offer)
            }
        }
    }

    fun sendSdpAnswer(answer: Message) {
        executorService.submit {
            if (answer.action.equals("SDP_ANSWER", ignoreCase = true)) {
                Log.d(
                    TAG, "Answer sent " + String(
                        Base64.decode(
                            answer.messagePayload!!.toByteArray(),
                            Base64.NO_WRAP or Base64.URL_SAFE
                        )
                    )
                )
                send(answer)
            }
        }
    }

    fun sendIceCandidate(candidate: Message) {
        executorService.submit {
            if (candidate.action.equals("ICE_CANDIDATE", ignoreCase = true)) {
                send(candidate)
            }
            Log.d(TAG, "Sent Ice candidate message")
        }
    }

    fun disconnect() {
        executorService.submit { websocketClient.disconnect() }
        try {
            executorService.shutdown()
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                executorService.shutdownNow()
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "Error in disconnect")
        }
    }

    private fun send(message: Message) {
        val jsonMessage = gson.toJson(message)
        Log.d(TAG, "Sending JSON Message= $jsonMessage")
        websocketClient.send(jsonMessage)
        Log.d(TAG, "Sent JSON Message= $jsonMessage")
    }
}