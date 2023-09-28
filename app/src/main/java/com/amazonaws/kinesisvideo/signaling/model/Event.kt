package com.amazonaws.kinesisvideo.signaling.model

import android.util.Base64
import android.util.Log
import com.google.common.base.Charsets
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.webrtc.IceCandidate
import java.util.Optional

/**
 * A class representing the Event object. All response messages are asynchronously delivered
 * to the recipient as events (for example, an SDP offer or SDP answer delivery).
 *
 * @see [Event](https://docs.aws.amazon.com/kinesisvideostreams-webrtc-dg/latest/devguide/kvswebrtc-websocket-apis-7.html)
 */
class Event(senderClientId: String, messageType: String, messagePayload: String) {

    @JvmField var senderClientId: String? = senderClientId
    @JvmField var messageType: String? = messageType
    @JvmField var messagePayload: String? = messagePayload

    companion object {
        /**
         * Attempts to convert an `ICE_CANDIDATE` [Event] into an [IceCandidate].
         *
         * @return an [IceCandidate] from the [Event]. `null` if the IceCandidate wasn't
         * able to be constructed.
         */
        @JvmStatic
        fun parseIceCandidate(event: Event): IceCandidate? {
            if (!"ICE_CANDIDATE".equals(event.messageType, ignoreCase = true)) {
                Log.e("parseIceCandidate", "$this is not an ICE_CANDIDATE type!")
                return null
            }
            val decode = Base64.decode(event.messagePayload, Base64.DEFAULT)
            val candidateString = String(decode, Charsets.UTF_8)
            if (candidateString == "null") {
                Log.w("parseIceCandidate", "Received null IceCandidate!")
                return null
            }
            val jsonObject = JsonParser.parseString(candidateString).asJsonObject
            val sdpMid = Optional.ofNullable(jsonObject["sdpMid"])
                .map { obj: JsonElement -> obj.toString() } // Remove quotes
                .map { sdpMidStr: String ->
                    if (sdpMidStr.length > 2) sdpMidStr.substring(
                        1,
                        sdpMidStr.length - 1
                    ) else sdpMidStr
                }
                .orElse("")
            var sdpMLineIndex = -1
            try {
                sdpMLineIndex = jsonObject["sdpMLineIndex"].toString().toInt()
            } catch (e: NumberFormatException) {
                Log.e("parseIceCandidate", "Invalid sdpMLineIndex")
            }

            // Ice Candidate needs one of these two to be present
            if (sdpMid.isEmpty() && sdpMLineIndex == -1) {
                return null
            }
            val candidate = Optional.ofNullable(jsonObject["candidate"])
                .map { obj: JsonElement -> obj.toString() } // Remove quotes
                .map { candidateStr: String ->
                    if (candidateStr.length > 2) candidateStr.substring(
                        1,
                        candidateStr.length - 1
                    ) else candidateStr
                }
                .orElse("")
            return IceCandidate(sdpMid, if (sdpMLineIndex == -1) 0 else sdpMLineIndex, candidate)
        }

        @JvmStatic
        fun parseSdpEvent(answerEvent: Event): String {
            val message = String(Base64.decode(answerEvent.messagePayload?.toByteArray(), Base64.DEFAULT))
            val jsonObject = JsonParser.parseString(message).asJsonObject
            val type = jsonObject["type"].toString()
            if (!type.equals("\"answer\"", ignoreCase = true)) {
                Log.e("parseSdpEvent", "Error in answer message")
            }
            val sdp = jsonObject["sdp"].asString
            Log.d("parseSdpEvent", "SDP answer received from master: $sdp")
            return sdp
        }

        @JvmStatic
        fun parseOfferEvent(offerEvent: Event): String {
            val s = String(Base64.decode(offerEvent.messagePayload, Base64.DEFAULT))
            return Optional.of(JsonParser.parseString(s))
                .filter { obj: JsonElement -> obj.isJsonObject }
                .map { obj: JsonElement -> obj.asJsonObject }
                .map { jsonObject: JsonObject -> jsonObject["sdp"] }
                .map { obj: JsonElement -> obj.asString }
                .orElse("")
        }
    }

    override fun toString(): String {
        return "Event(" +
                "senderClientId='" + senderClientId + '\'' +
                ", messageType='" + messageType + '\'' +
                ", messagePayload='" + messagePayload + '\'' +
                ')'
    }
}