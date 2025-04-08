package com.amazonaws.kinesisvideo.demoapp.activity;

import static org.webrtc.ContextUtils.getApplicationContext;

import android.util.Log;

import org.webrtc.RendererCommon;
public class VideoRendererCallbacks implements RendererCommon.RendererEvents{

    private static final String TAG = "VideoRendererCallbacks";

    private long offerSentTime;

    public void setOfferSentTime(long time) {
        offerSentTime = time;
    }

    @Override
    public void onFirstFrameRendered() {
        long firstFrameReceivedTime = System.nanoTime();

        Log.d(TAG, "[Testing] Offer to first frame received time (callback) (ms): " + ((firstFrameReceivedTime - offerSentTime) / 1000000));
        FileHelper.appendLineToFile(getApplicationContext(), "Offer to First Frame Time: " + ((firstFrameReceivedTime - offerSentTime) / 1000000.0));

    }

    @Override
    public void onFrameResolutionChanged(int i, int i1, int i2) {

    }
}