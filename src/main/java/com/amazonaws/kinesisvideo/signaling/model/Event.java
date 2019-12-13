package com.amazonaws.kinesisvideo.signaling.model;

import android.util.Base64;
import android.util.Log;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.webrtc.IceCandidate;

public class Event {

    private static final String TAG = "Event";

    private String senderClientId;

    private String messageType;

    private String messagePayload;

    private String statusCode;

    private String body;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSenderClientId() {
        return senderClientId;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getMessagePayload() {
        return messagePayload;
    }

    public Event() {}

    public Event(String senderClientId, String messageType, String messagePayload) {
        this.senderClientId = senderClientId;
        this.messageType = messageType;
        this.messagePayload = messagePayload;
    }

    public static IceCandidate parseIceCandidate(Event event) {

        byte[] decode = Base64.decode(event.getMessagePayload(), Base64.DEFAULT);
        String candidateString = new String(decode);

        JsonObject jsonObject = JsonParser.parseString(candidateString).getAsJsonObject();

        String sdpMid = jsonObject.get("sdpMid").toString();
        if (sdpMid.length() > 2) {
            sdpMid = sdpMid.substring(1, sdpMid.length() - 1);
        }

        int sdpMLineIndex = 0;
        try {
            sdpMLineIndex = Integer.parseInt(jsonObject.get("sdpMLineIndex").toString());
        } catch (NumberFormatException e) {
            Log.e(TAG,  "Invalid sdpMLineIndex");
            return null;
        }

        String candidate = jsonObject.get("candidate").toString();
        if (candidate.length() > 2) {
            candidate = candidate.substring(1, candidate.length() - 1);
        }

        return new IceCandidate(sdpMid, sdpMLineIndex, candidate);
    }

    public static String parseSdpEvent(Event answerEvent) {

        String message = new String(Base64.decode(answerEvent.getMessagePayload().getBytes(), Base64.DEFAULT));
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        String type = jsonObject.get("type").toString();

        if (!type.equalsIgnoreCase("\"answer\"")) {
            Log.e(TAG, "Error in answer message");
        }

        String sdp = jsonObject.get("sdp").getAsString();
        Log.d(TAG, "SDP answer received from master:" + sdp);
        return sdp;
    }

    public static String parseOfferEvent(Event offerEvent) {
        String s = new String(Base64.decode(offerEvent.getMessagePayload(), Base64.DEFAULT));

        JsonObject jsonObject = JsonParser.parseString(s).getAsJsonObject();
        return jsonObject.get("sdp").getAsString();
    }

}
