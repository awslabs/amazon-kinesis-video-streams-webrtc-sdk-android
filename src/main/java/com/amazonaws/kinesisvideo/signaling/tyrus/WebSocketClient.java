package com.amazonaws.kinesisvideo.signaling.tyrus;

import android.util.Log;
import com.amazonaws.kinesisvideo.signaling.SignalingListener;
import com.amazonaws.kinesisvideo.signaling.model.Event;

import com.google.gson.Gson;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * A JSR356 based websocket client.
 */

class WebSocketClient {

    private static final String TAG = "WebSocketClient";

    private Session session;

    private final SignalingListener signalingListener;

    private final Gson gson = new Gson();

    private ExecutorService executorService;

    WebSocketClient(final String uri, final ClientManager clientManager,
                    final SignalingListener signalingListener,
                    final ExecutorService executorService) {

        this.signalingListener = signalingListener;
        this.executorService = executorService;

        final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

        clientManager.getProperties().put(ClientProperties.LOG_HTTP_UPGRADE, true);


        Endpoint endpoint = new Endpoint() {

            @Override
            public void onOpen(final Session session, final EndpointConfig endpointConfig) {

                Log.d(TAG, "Registering message handler");

                session.addMessageHandler(signalingListener.getMessageHandler());

            }

            @Override
            public void onClose(final Session session, final CloseReason closeReason) {

                super.onClose(session, closeReason);

                Log.d(TAG, "Session " + session.getRequestURI() + " closed with reason " +
                        closeReason.getReasonPhrase());
            }

            @Override
            public void onError(final Session session, final Throwable thr) {

                super.onError(session, thr);

                Log.w(TAG, thr);

            }
        };

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    session = clientManager.connectToServer(endpoint, cec, new URI(uri));
                } catch (final DeploymentException e) {
                    Log.e(TAG, "Exception " + e.getMessage());
                } catch (final IOException e) {
                    Log.e(TAG, "Exception " + e.getMessage());
                } catch (final URISyntaxException e) {
                    Log.e(TAG, "Exception " + e.getMessage());
                }
            }
        });


        await().atMost(10, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return WebSocketClient.this.isOpen();
            }
        });
    }

    boolean isOpen() {

        if (session != null) {

            Log.d(TAG, " isOpen " + session.isOpen());

            return session.isOpen();

        } else {

            return false;
        }
    }

    void send(final String message) {

        try {

            session.getBasicRemote().sendText(message);

        } catch (final IOException e) {

            Log.e(TAG, "Exception" + e.getMessage());
        }
    }

    void disconnect() {

        if (session.isOpen()) {

            try {

                session.close();

            } catch (final IOException e) {

                Log.e(TAG, "Exception" + e.getMessage());
            }
        } else {

            Log.w(TAG, "Connection already closed for " + session.getRequestURI());
        }
    }
}
