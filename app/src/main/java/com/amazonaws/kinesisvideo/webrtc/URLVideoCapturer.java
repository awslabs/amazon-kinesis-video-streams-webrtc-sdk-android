package com.amazonaws.kinesisvideo.webrtc;

import android.content.Context;
import android.net.Uri;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;
import org.webrtc.CapturerObserver;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class implements a VideoCapturer for media streams. Similar to how camera capturers work, this
 * class also leverages the SurfaceTextureHelper to do the heavy lifting of grabbing and encoding video
 * frame using an intermediate OpenGL texture. It also uses libVLC(User must add) to playback streams on the texture.
 */
public class URLVideoCapturer implements VideoCapturer, VideoSink {
    private String url;
    private String[] options;
    private String aspectRatio;
    private SurfaceTextureHelper surfaceTextureHelper;
    private Context context;
    private CapturerObserver capturerObserver;

    /**
     * Public constructor that accepts the stream url.
     * @param url Media stream url.
     */
    public URLVideoCapturer(String url, String[] options, String aspectRatio){
        this.url = url;
        this.options = options;
        this.aspectRatio = aspectRatio;
    }

    @Override
    public void initialize(SurfaceTextureHelper surfaceTextureHelper, Context context, CapturerObserver capturerObserver) {
        this.surfaceTextureHelper = surfaceTextureHelper;
        this.context = context;
        this.capturerObserver = capturerObserver;

        surfaceTextureHelper.startListening(this);
    }

    @Override
    public void onFrame(VideoFrame videoFrame) {
        capturerObserver.onFrameCaptured(videoFrame);
    }

    @Override
    public void startCapture(int width, int height, int framerate) {
        capturerObserver.onCapturerStarted(true);
        surfaceTextureHelper.setTextureSize(width, height);

        // Use libVLC to play the stream onto the texture.
        LibVLC libVlc = new LibVLC(context, new ArrayList<>(Arrays.asList(options)));

        MediaPlayer mediaPlayer = new MediaPlayer(libVlc);
        IVLCVout vOut = mediaPlayer.getVLCVout();
        vOut.setWindowSize(width, height);
        vOut.setVideoSurface(surfaceTextureHelper.getSurfaceTexture());
        vOut.attachViews();

        Media videoMedia = new Media (libVlc, Uri.parse(url));
        mediaPlayer.setMedia(videoMedia);
        mediaPlayer.setAspectRatio(aspectRatio);
        mediaPlayer.play();
    }

    @Override
    public void stopCapture()  {
        capturerObserver.onCapturerStopped();
    }

    @Override
    public void changeCaptureFormat(int width, int height, int framerate) {
        stopCapture();
        startCapture(width, height, framerate);
    }

    @Override
    public void dispose() {
        surfaceTextureHelper.dispose();

        surfaceTextureHelper = null;
        context = null;
        capturerObserver = null;
    }

    @Override
    public boolean isScreencast() {
        return false;
    }
}
