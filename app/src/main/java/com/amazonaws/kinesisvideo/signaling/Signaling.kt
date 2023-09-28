package com.amazonaws.kinesisvideo.signaling

import com.amazonaws.kinesisvideo.signaling.model.Event

interface Signaling {
    fun onSdpOffer(event: Event?)
    fun onSdpAnswer(event: Event?)
    fun onIceCandidate(event: Event?)
    fun onError(event: Event?)
    fun onException(e: Exception?)
}