package com.amazonaws.kinesisvideo.signaling;


import com.amazonaws.kinesisvideo.signaling.model.Event;


public interface Signaling {

    void onSdpOffer(Event event);

    void onSdpAnswer(Event event);

    void onIceCandidate(Event event);

    void onError(Event event);

    void onException(Exception e);
}
