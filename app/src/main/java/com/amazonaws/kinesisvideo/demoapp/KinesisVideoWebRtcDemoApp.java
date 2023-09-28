package com.amazonaws.kinesisvideo.demoapp;

import android.app.Application;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.kinesisvideo.common.logging.LogLevel;
import com.amazonaws.kinesisvideo.common.logging.OutputChannel;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.kinesisvideo.util.AndroidLogOutputChannel;

import org.json.JSONException;
import org.json.JSONObject;

public class KinesisVideoWebRtcDemoApp extends Application {
    private static final String TAG = KinesisVideoWebRtcDemoApp.class.getSimpleName();

    public static AWSCredentialsProvider getCredentialsProvider() {
        final OutputChannel outputChannel = new AndroidLogOutputChannel();
        final com.amazonaws.kinesisvideo.common.logging.Log log =
                new com.amazonaws.kinesisvideo.common.logging.Log(outputChannel, LogLevel.VERBOSE, TAG);
        return AWSMobileClient.getInstance();
    }

    public static AWSCredentials getCredentials() {
        return new AWSCredentials() {

            @Override
            public String getAWSAccessKeyId() {
                return "AWSAccessKeyId";
            }

            @Override
            public String getAWSSecretKey() {
                return "AWSSecretKey";
            }
        };
    }

    public static String getRegion() {
        return "ca-central-1";
    }

}
