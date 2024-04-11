package com.amazonaws.kinesisvideo.signaling.okhttp;

import static org.awaitility.Awaitility.await;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import android.util.Log;
import androidx.annotation.NonNull;

import com.amazonaws.kinesisvideo.signaling.SignalingListener;
import com.amazonaws.kinesisvideo.utils.Constants;

import java.util.concurrent.TimeUnit;

/**
 * An OkHttp based WebSocket client.
 */
class WebSocketClient {

    private static final String TAG = "WebSocketClient";
    private final WebSocket webSocket;
    private volatile boolean isOpen = false;

    WebSocketClient(final String uri, final SignalingListener signalingListener) {

        OkHttpClient client = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url(uri)
                .addHeader("User-Agent", Constants.APP_NAME + "/" + Constants.VERSION
                        + " " + System.getProperty("http.agent"))
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                Log.d(TAG, "WebSocket connection opened");
                isOpen = true;
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String message) {
                Log.d(TAG, "Websocket received a message: " + message);
                signalingListener.getWebsocketListener().onMessage(webSocket, message);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "WebSocket connection closed: " + reason);
                isOpen = false;
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                Log.e(TAG, "WebSocket connection failed", t);
                isOpen = false;
                signalingListener.onException((Exception) t);
            }
        });

        // Await WebSocket connection
        await().atMost(10, TimeUnit.SECONDS).until(WebSocketClient.this::isOpen);
    }

    void send(String message) {
        if (isOpen) {
            if (webSocket.send(message)) {
                Log.d(TAG, "Successfully sent " + message);
            } else {
                Log.d(TAG, "Could not send " + message + " as the connection may have closing, closed, or canceled.");
            }
        } else {
            Log.d(TAG, "Cannot send the websocket message as it is not open.");
        }
    }

    void disconnect() {
        if (isOpen) {
            if (!webSocket.close(1000, "Disconnect requested")) {
                Log.d(TAG, "Websocket could not disconnect in a graceful shutdown. Going to cancel it to release resources.");
                webSocket.cancel();
            } else {
                Log.d(TAG, "Websocket successfully disconnected.");
            }
        } else {
            Log.d(TAG, "Cannot close the websocket as it is not open.");
        }
    }

    boolean isOpen() {
        return isOpen;
    }
}
