package com.amazonaws.kinesisvideo.demoapp.activity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.kinesisvideo.client.KinesisVideoClient;
import com.amazonaws.kinesisvideo.demoapp.KinesisVideoWebRtcDemoApp;
import com.amazonaws.kinesisvideo.demoapp.R;
import com.amazonaws.kinesisvideo.demoapp.util.ActivityUtils;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.kinesisvideo.client.AndroidKinesisVideoClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesisvideo.AWSKinesisVideoClient;

import java.util.concurrent.CountDownLatch;

public class StartUpActivity extends AppCompatActivity {
    private static final String TAG = StartUpActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.setProperty("javax.net.debug", "ssl");

        final AWSMobileClient auth = AWSMobileClient.getInstance();
        initializeMobileClient(auth);

        final AppCompatActivity thisActivity = this;
        supportFinishAfterTransition();

        AsyncTask.execute(() -> {
            if (auth.isSignedIn()) {

                AWSKinesisVideoClient kvsClient = new AWSKinesisVideoClient(
                        KinesisVideoWebRtcDemoApp.getCredentialsProvider().getCredentials());
                kvsClient.setRegion(Region.getRegion(Regions.CN_NORTH_1));
//                kvsClient.setEndpoint("https://kinesisvideo.cn-north-1.amazonaws.com.cn");
                System.out.println(kvsClient.getEndpoint());

                ActivityUtils.startActivity(thisActivity, SimpleNavActivity.class);
            } else {
                auth.showSignIn(thisActivity,
                        SignInUIOptions.builder()
                                .logo(R.mipmap.kinesisvideo_logo)
                                .backgroundColor(Color.WHITE)
                                .nextActivity(SimpleNavActivity.class)
                                .build(),
                        new Callback<UserStateDetails>() {
                            @Override
                            public void onResult(UserStateDetails result) {
                                Log.d(TAG, "onResult: User signed-in " + result.getUserState());
                            }

                            @Override
                            public void onError(final Exception e) {
                                runOnUiThread(() -> {
                                    Log.e(TAG, "onError: User sign-in error", e);
                                    Toast.makeText(StartUpActivity.this, "User sign-in error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                            }
                        });
            }
        });
    }

    private void initializeMobileClient(AWSMobileClient client) {
        final CountDownLatch latch = new CountDownLatch(1);
        client.initialize(getApplicationContext(), new Callback<UserStateDetails>() {
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
