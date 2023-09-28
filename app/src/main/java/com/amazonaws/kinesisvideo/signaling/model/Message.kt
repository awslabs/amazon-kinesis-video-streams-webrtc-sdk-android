package com.amazonaws.kinesisvideo.signaling.model

import android.util.Base64
import org.webrtc.SessionDescription

class Message {
    @JvmField
    var action: String? = null
    @JvmField
    var recipientClientId: String? = null
    @JvmField
    var senderClientId: String? = null
    @JvmField
    var messagePayload: String? = null

    constructor()
    constructor(
        action: String?,
        recipientClientId: String?,
        senderClientId: String?,
        messagePayload: String?
    ) {
        this.action = action
        this.recipientClientId = recipientClientId
        this.senderClientId = senderClientId
        this.messagePayload = messagePayload
    }

    companion object {
        /**
         * @param sessionDescription SDP description to be converted & sent to signaling service
         * @param master             true if local is set to be the master
         * @param recipientClientId  - has to be set to null if this is set as viewer
         * @return SDP Answer message to be sent to signaling service
         */
        @JvmStatic
        fun createAnswerMessage(
            sessionDescription: SessionDescription,
            master: Boolean,
            recipientClientId: String?
        ): Message {
            val description = sessionDescription.description
            val answerPayload =
                "{\"type\":\"answer\",\"sdp\":\"" + description.replace("\r\n", "\\r\\n") + "\"}"
            val encodedString = String(
                Base64.encode(
                    answerPayload.toByteArray(),
                    Base64.URL_SAFE or Base64.NO_WRAP
                )
            )

            // SenderClientId should always be "" for master creating answer case
            return Message("SDP_ANSWER", recipientClientId, "", encodedString)
        }

        /**
         * @param sessionDescription SDP description to be converted as Offer Message & sent to signaling service
         * @param clientId           Client Id to mark this viewer in signaling service
         * @return SDP Offer message to be sent to signaling service
         */
        @JvmStatic
        fun createOfferMessage(sessionDescription: SessionDescription, clientId: String?): Message {
            val description = sessionDescription.description
            val offerPayload =
                "{\"type\":\"offer\",\"sdp\":\"" + description.replace("\r\n", "\\r\\n") + "\"}"
            val encodedString = String(
                Base64.encode(
                    offerPayload.toByteArray(),
                    Base64.URL_SAFE or Base64.NO_WRAP
                )
            )
            return Message("SDP_OFFER", "", clientId, encodedString)
        }
    }
}