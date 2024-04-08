package com.amazonaws.kinesisvideo.signaling;

import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import com.google.gson.Gson;

import com.amazonaws.kinesisvideo.signaling.model.Event;

public abstract class SignalingListener implements Signaling {

    private final static String TAG = "CustomMessageHandler";

    private final Gson gson = new Gson();

    private final WebSocketListener messageHandler = new WebSocketListener() {

        @Override
        public void onMessage(@NonNull WebSocket webSocket, String text) {
            if (text.isEmpty()) {
                return;
            }

            Log.d(TAG, "Received message: " + text);

            if (!text.contains("messagePayload")) {
                return;
            }

            final Event evt = gson.fromJson(text, Event.class);

            if (evt == null || evt.getMessageType() == null || evt.getMessagePayload().isEmpty()) {
                return;
            }

            switch (evt.getMessageType().toUpperCase()) {
                case "SDP_OFFER":
                    Log.d(TAG, "Offer received: SenderClientId=" + evt.getSenderClientId());
                    Log.d(TAG, new String(Base64.decode(evt.getMessagePayload(), 0)));

                    onSdpOffer(evt);
                    break;
                case "SDP_ANSWER":
                    Log.d(TAG, "Answer received: SenderClientId=" + evt.getSenderClientId());

                    onSdpAnswer(evt);
                    break;
                case "ICE_CANDIDATE":
                    Log.d(TAG, "Ice Candidate received: SenderClientId=" + evt.getSenderClientId());
                    Log.d(TAG, new String(Base64.decode(evt.getMessagePayload(), 0)));

                    onIceCandidate(evt);
                    break;
                default:
                    break;
            }
        }
    };

    public WebSocketListener getMessageHandler() {
        return messageHandler;
    }
}
