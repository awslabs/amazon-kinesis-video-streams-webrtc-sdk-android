package com.amazonaws.kinesisvideo.demoapp;

import android.app.Application;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.kinesisvideo.common.logging.LogLevel;
import com.amazonaws.kinesisvideo.common.logging.OutputChannel;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.kinesisvideo.util.AndroidLogOutputChannel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

public class KinesisVideoWebRtcDemoApp extends Application {
    private static final String TAG = KinesisVideoWebRtcDemoApp.class.getSimpleName();

    public static AWSCredentialsProvider getCredentialsProvider() {
        final OutputChannel outputChannel = new AndroidLogOutputChannel();
        final com.amazonaws.kinesisvideo.common.logging.Log log =
                new com.amazonaws.kinesisvideo.common.logging.Log(outputChannel, LogLevel.VERBOSE, TAG);
        return AWSMobileClient.getInstance();
    }

    public static String getRegion() {
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
        JSONObject jsonObject = configuration.optJsonObject("CredentialsProvider");
        String region = null;
        try {
            region = (String) ((JSONObject) (((JSONObject) jsonObject.get("CognitoIdentity")).get("Default"))).get("Region");
        } catch (JSONException e) {
            Log.e(TAG, "Got exception when extracting region from cognito setting.", e);
        }
        return region;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final CountDownLatch latch = new CountDownLatch(1);
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                Log.d(TAG, "onResult: user state: " + result.getUserState());
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: Initialization error of the mobile client", e);
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
