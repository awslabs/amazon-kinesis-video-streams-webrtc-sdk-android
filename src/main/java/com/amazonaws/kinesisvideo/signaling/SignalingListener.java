package com.amazonaws.kinesisvideo.signaling;


import android.util.Base64;
import android.util.Log;
import com.amazonaws.kinesisvideo.signaling.model.Event;
import com.google.gson.Gson;
import javax.websocket.MessageHandler;

public abstract class SignalingListener implements Signaling {

    private final static String TAG = "CustomMessageHandler";

    private final Gson gson = new Gson();

    private final MessageHandler messageHandler = new MessageHandler.Whole<String>() {

        @Override
        public void onMessage(String message) {

            Log.d(TAG, "Received message" + message);

            if (!message.isEmpty() && message.contains("messagePayload")) {

                Event evt = gson.fromJson(message, Event.class);

                if(evt != null && evt.getMessageType() != null && !evt.getMessagePayload().isEmpty()){

                    if (evt.getMessageType().equalsIgnoreCase("SDP_OFFER")) {

                        Log.d(TAG, "Offer received: SenderClientId="  + evt.getSenderClientId());

                        byte[] decode = Base64.decode(evt.getMessagePayload(), 0);

                        Log.d(TAG, new String(decode));

                        onSdpOffer(evt);
                    }

                    if (evt.getMessageType().equalsIgnoreCase("SDP_ANSWER")) {

                        Log.d(TAG, "Answer received: SenderClientId="  + evt.getSenderClientId());

                        onSdpAnswer(evt);
                    }

                    if (evt.getMessageType().equalsIgnoreCase("ICE_CANDIDATE")) {

                        Log.d(TAG, "Ice Candidate received: SenderClientId="  + evt.getSenderClientId());

                        byte[] decode = Base64.decode(evt.getMessagePayload(), 0);

                        Log.d(TAG, new String(decode));

                        onIceCandidate(evt);
                    }
                }
            }

        }
    };

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }
}
