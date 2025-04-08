package com.amazonaws.kinesisvideo.demoapp.activity;

import org.webrtc.RendererCommon;

public class VideoRendererEvents implements RendererCommon.RendererEvents {
    @Override
    public void onFirstFrameRendered() {
        System.out.println("Video is now playable");
    }

    @Override
    public void onFrameResolutionChanged(int videoWidth, int videoHeight, int rotation) {
        // Not needed for this use case
    }
}
