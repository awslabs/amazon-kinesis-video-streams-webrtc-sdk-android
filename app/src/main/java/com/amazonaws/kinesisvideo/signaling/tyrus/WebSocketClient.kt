package com.amazonaws.kinesisvideo.signaling.tyrus

import android.util.Log
import com.amazonaws.kinesisvideo.signaling.SignalingListener
import com.amazonaws.kinesisvideo.utils.Constants
import org.awaitility.Awaitility
import org.glassfish.tyrus.client.ClientManager
import org.glassfish.tyrus.client.ClientProperties
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import jakarta.websocket.ClientEndpointConfig
import jakarta.websocket.CloseReason
import jakarta.websocket.DeploymentException
import jakarta.websocket.Endpoint
import jakarta.websocket.EndpointConfig
import jakarta.websocket.HandshakeResponse
import jakarta.websocket.Session


/**
 * A JSR356 based websocket client.
 */
internal class WebSocketClient(
    uri: String?,
    clientManager:
    ClientManager,
    signalingListener: SignalingListener,
    private val executorService: ExecutorService
) {
    private var session: Session? = null

    init {
        val cec = ClientEndpointConfig.Builder.create()
            .configurator(object : ClientEndpointConfig.Configurator() {
                override fun beforeRequest(headers: MutableMap<String, List<String>>) {
                    super.beforeRequest(headers)
                    val agent = System.getProperty("http.agent")
                    val userAgent = "${Constants.APP_NAME}/${Constants.VERSION} $agent"
                    headers["User-Agent"] = listOf(userAgent.trim { it <= ' ' })
                }

                override fun afterResponse(hr: HandshakeResponse) {
                    super.afterResponse(hr)
                    hr.headers.forEach { (key: String, values: List<String?>) ->
                        Log.d(TAG, "header - $key: $values")
                    }
                }
            })
            .build()
        clientManager.properties[ClientProperties.LOG_HTTP_UPGRADE] = true
        val endpoint: Endpoint = object : Endpoint() {
            override fun onOpen(session: Session, endpointConfig: EndpointConfig) {
                Log.d(TAG, "Registering message handler")
                session.addMessageHandler(signalingListener.messageHandler)
            }

            override fun onClose(session: Session, closeReason: CloseReason) {
                super.onClose(session, closeReason)
                Log.d(
                    TAG, "Session " + session.requestURI + " closed with reason " +
                            closeReason.reasonPhrase
                )
            }

            override fun onError(session: Session, thr: Throwable) {
                super.onError(session, thr)
                Log.w(TAG, thr)
            }
        }
        executorService.submit {
            try {
                session = clientManager.connectToServer(endpoint, cec, URI(uri))
            } catch (e: DeploymentException) {
                signalingListener.onException(e)
            } catch (e: IOException) {
                signalingListener.onException(e)
            } catch (e: URISyntaxException) {
                signalingListener.onException(e)
            }
        }
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until { isOpen() }
    }

    fun isOpen(): Boolean {
        Log.d(TAG, "isOpen: " + session?.isOpen)
        return session?.isOpen == true
    }

    fun send(message: String) {
        if (!isOpen()) {
            Log.e(TAG, "Connection isn't open!")
            return
        }
        try {
            session?.basicRemote?.sendText(message)
        } catch (e: IOException) {
            Log.e(TAG, "Exception sending message: " + e.message)
        }
    }

    fun disconnect() {
        if (!isOpen()) {
            Log.w(TAG, "Connection already closed for " + session?.requestURI)
            return
        }
        try {
            session?.close()
            executorService.shutdownNow()
            Log.i(TAG, "Disconnected from " + session?.requestURI + " successfully!")
        } catch (e: IOException) {
            Log.e(TAG, "Exception closing: " + e.message)
        }
    }

    companion object {
        private const val TAG = "WebSocketClient"
    }
}