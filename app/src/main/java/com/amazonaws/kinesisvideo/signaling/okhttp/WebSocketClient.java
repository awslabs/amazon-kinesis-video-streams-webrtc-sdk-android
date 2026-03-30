package com.amazonaws.kinesisvideo.signaling.okhttp;

import static org.awaitility.Awaitility.await;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import com.amazonaws.kinesisvideo.signaling.SignalingListener;
import com.amazonaws.kinesisvideo.utils.Constants;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * An OkHttp based WebSocket client.
 * Supports custom trust store with Amazon root CAs for GovCloud/FIPS.
 */
class WebSocketClient {

    private static final String TAG = "WebSocketClient";

    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
    private static final int GOVCLOUD_CONNECT_TIMEOUT_SECONDS = 30;

    private final WebSocket webSocket;
    private volatile boolean isOpen = false;

    WebSocketClient(@NonNull final Context context, @NonNull final String uri,
                    @NonNull final SignalingListener signalingListener) {
        this(context, uri, signalingListener, false);
    }

    WebSocketClient(@NonNull final Context context, @NonNull final String uri,
                    @NonNull final SignalingListener signalingListener,
                    final boolean isGovCloud) {

        final int connectTimeout = isGovCloud ? GOVCLOUD_CONNECT_TIMEOUT_SECONDS : DEFAULT_CONNECT_TIMEOUT_SECONDS;

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(connectTimeout, TimeUnit.SECONDS)
                .writeTimeout(connectTimeout, TimeUnit.SECONDS);

        try {
            final KeyStore keyStore = buildTrustStore(context);
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            final TrustManager[] trustManagers = tmf.getTrustManagers();

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);
            clientBuilder.sslSocketFactory(sslContext.getSocketFactory(),
                    (X509TrustManager) trustManagers[0]);
        } catch (Exception e) {
            Log.w(TAG, "Failed to configure custom trust store, falling back to default SSL", e);
        }

        OkHttpClient client = clientBuilder.build();

        String userAgent = (Constants.APP_NAME + "/" + Constants.VERSION + " " + System.getProperty("http.agent")).trim();

        Log.d(TAG, "User agent: " + userAgent);
        Log.d(TAG, "Connect timeout: " + connectTimeout + "s, GovCloud: " + isGovCloud);

        Request request = new Request.Builder()
                .url(uri)
                .addHeader("User-Agent", userAgent)
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

        await().atMost(connectTimeout, TimeUnit.SECONDS).until(WebSocketClient.this::isOpen);
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
            if (webSocket.close(1000, "Disconnect requested")) {
                Log.d(TAG, "Websocket successfully disconnected.");
            } else {
                Log.d(TAG, "Websocket could not disconnect in a graceful shutdown. Going to cancel it to release resources.");
                webSocket.cancel();
            }
        } else {
            Log.d(TAG, "Cannot close the websocket as it is not open.");
        }
    }

    boolean isOpen() {
        return isOpen;
    }

    /**
     * Builds a KeyStore containing system CAs plus Amazon root CAs from res/raw/.
     */
    private static KeyStore buildTrustStore(@NonNull final Context context) throws Exception {
        final TrustManagerFactory defaultTmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        defaultTmf.init((KeyStore) null);

        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);

        for (final TrustManager tm : defaultTmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                for (final java.security.cert.X509Certificate cert :
                        ((X509TrustManager) tm).getAcceptedIssuers()) {
                    keyStore.setCertificateEntry(cert.getSubjectX500Principal().getName(), cert);
                }
            }
        }

        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final String[] caNames = {"amazon_root_ca1", "amazon_root_ca2", "amazon_root_ca3", "amazon_root_ca4"};
        for (final String name : caNames) {
            final int resId = context.getResources().getIdentifier(name, "raw", context.getPackageName());
            if (resId != 0) {
                try (InputStream is = context.getResources().openRawResource(resId)) {
                    final Certificate ca = cf.generateCertificate(is);
                    keyStore.setCertificateEntry(name, ca);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to load CA resource " + name, e);
                }
            }
        }

        return keyStore;
    }
}
