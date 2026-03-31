package com.amazonaws.kinesisvideo.webrtc;

import android.util.Log;

import org.webrtc.CandidatePairChangeEvent;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;

/**
 * Listener for Peer connection events. Prints event info to the logs at debug level.
 */
public class KinesisVideoPeerConnection implements PeerConnection.Observer {

    private final static String TAG = "KVSPeerConnection";

    public KinesisVideoPeerConnection() {

    }

    /**
     * Triggered when the SignalingState changes.
     */
    @Override
    public void onSignalingChange(final PeerConnection.SignalingState signalingState) {

        Log.d(TAG, "onSignalingChange(): signalingState = [" + signalingState + "]");

    }

    /**
     * Triggered when the IceConnectionState changes.
     */
    @Override
    public void onIceConnectionChange(final PeerConnection.IceConnectionState iceConnectionState) {

        Log.d(TAG, "onIceConnectionChange(): iceConnectionState = [" + iceConnectionState + "]");

    }

    /**
     * Triggered when the ICE connection receiving status changes.
     */
    @Override
    public void onIceConnectionReceivingChange(final boolean connectionChange) {

        Log.d(TAG, "onIceConnectionReceivingChange(): connectionChange = [" + connectionChange + "]");

    }

    /**
     * Triggered when the IceGatheringState changes.
     */
    @Override
    public void onIceGatheringChange(final PeerConnection.IceGatheringState iceGatheringState) {
        Log.i(TAG, "ICE gathering state: " + iceGatheringState);
        if (iceGatheringState == PeerConnection.IceGatheringState.COMPLETE) {
            Log.i(TAG, "ICE gathering complete — if no srflx candidates were logged, STUN binding failed");
        }
    }

    /**
     * Triggered when a new ICE candidate has been found.
     */
    @Override
    public void onIceCandidate(final IceCandidate iceCandidate) {
        final String sdp = iceCandidate.sdp;
        final String mid = iceCandidate.sdpMid;

        String type = "unknown";
        if (sdp.contains("typ host")) type = "host";
        else if (sdp.contains("typ srflx")) type = "srflx (STUN binding success)";
        else if (sdp.contains("typ relay")) type = "relay (TURN)";
        else if (sdp.contains("typ prflx")) type = "prflx";

        String protocol = "unknown";
        if (sdp.contains(" udp ") || sdp.contains(" UDP ")) protocol = "UDP";
        else if (sdp.contains(" tcp ") || sdp.contains(" TCP ")) protocol = "TCP";

        Log.i(TAG, "ICE candidate gathered: mid=" + mid
                + " type=" + type
                + " protocol=" + protocol
                + " sdp=[" + sdp + "]");
    }

    /**
     * Triggered when some ICE candidates have been removed.
     */
    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] iceCandidates) {

        Log.d(TAG, "onIceCandidatesRemoved(): iceCandidates Length = [" + iceCandidates.length + "]");

    }

    /**
     * Triggered when the ICE candidate pair is changed.
     */
    @Override
    public void onSelectedCandidatePairChanged(final CandidatePairChangeEvent event) {
        Log.i(TAG, "ICE candidate pair selected:"
                + " local=[" + event.local.sdp + "]"
                + " remote=[" + event.remote.sdp + "]"
                + " reason=" + event.reason);

        final String localSdp = event.local.sdp;
        if (localSdp.contains(" udp ") || localSdp.contains(" UDP ")) {
            Log.i(TAG, "Selected transport: UDP (DTLS-SRTP for encryption)");
        } else if (localSdp.contains(" tcp ") || localSdp.contains(" TCP ")) {
            Log.i(TAG, "Selected transport: TCP (TLS for encryption)");
        }

        if (localSdp.contains("typ relay")) {
            Log.i(TAG, "Candidate type: TURN relay");
        } else if (localSdp.contains("typ srflx")) {
            Log.i(TAG, "Candidate type: server-reflexive (STUN)");
        } else if (localSdp.contains("typ host")) {
            Log.i(TAG, "Candidate type: host");
        }
    }

    /**
     * Triggered when media is received on a new stream from remote peer.
     */
    @Override
    public void onAddStream(final MediaStream mediaStream) {

        Log.d(TAG, "onAddStream(): mediaStream = [" + mediaStream + "]");

    }

    /**
     * Triggered when a remote peer close a stream.
     */
    @Override
    public void onRemoveStream(final MediaStream mediaStream) {

        Log.d(TAG, "onRemoveStream(): mediaStream = [" + mediaStream + "]");

    }

    /**
     * Triggered when a remote peer opens a DataChannel.
     */
    @Override
    public void onDataChannel(final DataChannel dataChannel) {

        Log.d(TAG, "onDataChannel(): dataChannel = [" + dataChannel + "]");

    }

    /**
     * Triggered when renegotiation is necessary.
     */
    @Override
    public void onRenegotiationNeeded() {

        Log.d(TAG, "onRenegotiationNeeded():");

    }

    /**
     * Triggered when a new track is signaled by the remote peer, as a result of setRemoteDescription.
     */
    @Override
    public void onAddTrack(final RtpReceiver rtpReceiver, final MediaStream[] mediaStreams) {

        Log.d(TAG, "onAddTrack(): rtpReceiver = [" + rtpReceiver + "], " +
                "mediaStreams Length = [" + mediaStreams.length + "]");

    }
}
