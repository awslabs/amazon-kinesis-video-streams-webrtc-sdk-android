package com.amazonaws.kinesisvideo.signaling.model;

import android.util.Base64;
import org.webrtc.SessionDescription;

public class Message {

    private String action;

    private String recipientClientId;

    private String senderClientId;

    private String messagePayload;


    public Message() {}

    public Message(String action, String recipientClientId, String senderClientId, String messagePayload) {

        this.action = action;
        this.recipientClientId = recipientClientId;
        this.senderClientId = senderClientId;
        this.messagePayload = messagePayload;

    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRecipientClientId() {
        return recipientClientId;
    }

    public void setRecipientClientId(String recipientClientId) {
        this.recipientClientId = recipientClientId;
    }

    public String getSenderClientId() {
        return senderClientId;
    }

    public void setSenderClientId(String senderClientId) {
        this.senderClientId = senderClientId;
    }

    public String getMessagePayload() {
        return messagePayload;
    }

    public void setMessagePayload(String messagePayload) {
        this.messagePayload = messagePayload;
    }


    /**
     * @param sessionDescription SDP description to be converted & sent to signaling service
     * @param master true if local is set to be the master
     * @param recipientClientId - has to be set to null if this is set as viewer
     * @return SDP Answer message to be sent to signaling service
     */
    public static  Message createAnswerMessage(SessionDescription sessionDescription, boolean master, String recipientClientId) {

        String description = sessionDescription.description;

        String answerPayload = "{\"type\":\"answer\",\"sdp\":\"" + description.replace("\r\n", "\\r\\n") + "\"}";

        String encodedString = new String(Base64.encode(answerPayload.getBytes(), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));

        // SenderClientId should always be "" for master creating answer case
        return new Message("SDP_ANSWER", recipientClientId, "", encodedString);
    }


    /**
     * @param sessionDescription SDP description to be converted  as Offer Message & sent to signaling service
     * @param clientId Client Id to mark this viewer in signaling service
     * @return SDP Offer message to be sent to signaling service
     */
    public static Message createOfferMessage(SessionDescription sessionDescription, String clientId) {

        String description = sessionDescription.description;

        String offerPayload = "{\"type\":\"offer\",\"sdp\":\"" + description.replace("\r\n", "\\r\\n") + "\"}";

        String encodedString = new String(Base64.encode(offerPayload.getBytes(), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));

        return new Message("SDP_OFFER", "", clientId, encodedString);
    }

}
