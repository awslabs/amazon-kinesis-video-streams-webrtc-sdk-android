package com.amazonaws.kinesisvideo.demoapp;

import android.app.Application;
import android.util.Log;

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

    /**
     * Parse awsconfiguration.json and extract the region from it.
     *
     * @return The region in String form. {@code null} if not.
     * @throws IllegalStateException if awsconfiguration.json is not properly configured.
     */
    public static String getRegion() {
        final AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
        if (configuration == null) {
            throw new IllegalStateException("awsconfiguration.json has not been properly configured!");
        }

        final JSONObject jsonObject = configuration.optJsonObject("CredentialsProvider");

        String region = null;
        try {
            region = (String) ((JSONObject) (((JSONObject) jsonObject.get("CognitoIdentity")).get("Default"))).get("Region");
        } catch (final JSONException e) {
            Log.e(TAG, "Got exception when extracting region from cognito setting.", e);
        }
        return region;
    }

}
