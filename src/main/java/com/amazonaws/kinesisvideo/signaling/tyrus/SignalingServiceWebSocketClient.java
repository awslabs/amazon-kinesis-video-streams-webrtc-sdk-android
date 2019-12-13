package com.amazonaws.kinesisvideo.signaling.tyrus;

import android.util.Base64;
import android.util.Log;
import com.amazonaws.kinesisvideo.signaling.SignalingListener;

import com.amazonaws.kinesisvideo.signaling.model.Message;
import com.google.gson.Gson;
import org.glassfish.tyrus.client.ClientManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Signaling service client based on websocket.
 */

public class SignalingServiceWebSocketClient {

    private static final String TAG = "SignalingServiceWebSocketClient";

    private final WebSocketClient websocketClient;

    private final ExecutorService executorService;

    private final Gson gson = new Gson();

    public SignalingServiceWebSocketClient(final String uri, final SignalingListener signalingListener,
                                           final ExecutorService executorService) {
        Log.d(TAG, "Connecting to URI " + uri + " as master");
        websocketClient = new WebSocketClient(uri, new ClientManager(), signalingListener, executorService);
        this.executorService = executorService;
    }

    public boolean isOpen() {
        return websocketClient.isOpen();
    }

    public void sendSdpOffer(final Message offer) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if (offer.getAction().equalsIgnoreCase("SDP_OFFER")) {

                    Log.d(TAG, "Sending Offer");

                    send(offer);
                }
            }
        });
    }

    public void sendSdpAnswer(final Message answer) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if (answer.getAction().equalsIgnoreCase("SDP_ANSWER")) {

                    Log.d(TAG, "Answer sent " + new String(Base64.decode(answer.getMessagePayload().getBytes(),
                            Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE)));

                    send(answer);
                }
            }
        });
    }

    public void sendIceCandidate(final Message candidate) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if (candidate.getAction().equalsIgnoreCase("ICE_CANDIDATE")) {

                    send(candidate);
                }

                Log.d(TAG, "Sent Ice candidate message");
            }
        });
    }

    public void disconnect() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                websocketClient.disconnect();
            }
        });
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Error in disconnect");
        }
    }

    private void send(final Message message) {
        String jsonMessage = gson.toJson(message);
        Log.d(TAG, "Sending JSON Message= " + jsonMessage);
        websocketClient.send(jsonMessage);
        Log.d(TAG, "Sent JSON Message= " + jsonMessage);
    }

}
