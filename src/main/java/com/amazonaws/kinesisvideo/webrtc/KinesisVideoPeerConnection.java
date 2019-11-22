package com.amazonaws.kinesisvideo.webrtc;

import android.util.Log;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;

public class KinesisVideoPeerConnection implements PeerConnection.Observer {

    private final static String TAG = "KVSPeerConnection";

    public KinesisVideoPeerConnection() {

    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

        Log.d(TAG, "onSignalingChange(): signalingState = [" + signalingState + "]");

    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

        Log.d(TAG, "onIceConnectionChange(): iceConnectionState = [" + iceConnectionState + "]");

    }

    @Override
    public void onIceConnectionReceivingChange(boolean connectionChange) {

        Log.d(TAG, "onIceConnectionReceivingChange(): connectionChange = [" + connectionChange + "]");

    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

        Log.d(TAG, "onIceGatheringChange(): iceGatheringState = [" + iceGatheringState + "]");

    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {

        Log.d(TAG, "onIceCandidate(): iceCandidate = [" + iceCandidate + "]");

    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

        Log.d(TAG, "onIceCandidatesRemoved(): iceCandidates Length = [" + iceCandidates.length + "]");

    }

    @Override
    public void onAddStream(MediaStream mediaStream) {

        Log.d(TAG, "onAddStream(): mediaStream = [" + mediaStream + "]");

    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {

        Log.d(TAG, "onRemoveStream(): mediaStream = [" + mediaStream + "]");

    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {

        Log.d(TAG, "onDataChannel(): dataChannel = [" + dataChannel + "]");

    }

    @Override
    public void onRenegotiationNeeded() {

        Log.d(TAG, "onRenegotiationNeeded():");

    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

        Log.d(TAG, "onAddTrack(): rtpReceiver = [" + rtpReceiver + "], " +
                "mediaStreams Length = [" + mediaStreams.length + "]");

    }
}
