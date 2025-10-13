package com.amazonaws.kinesisvideo.demoapp;

import android.app.Application;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

public class KinesisVideoWebRtcDemoApp extends Application {
    private static final String TAG = KinesisVideoWebRtcDemoApp.class.getSimpleName();

    public static AWSCredentialsProvider getCredentialsProvider() {
        // Check if custom credentials are available from .env
        if (hasEnvCredentials()) {
            return new CustomCredentialsProvider();
        }
        return AWSMobileClient.getInstance();
    }

    public static boolean hasEnvCredentials() {
        try {
            String accessKeyId = BuildConfig.AWS_ACCESS_KEY_ID;
            String secretAccessKey = BuildConfig.AWS_SECRET_ACCESS_KEY;
            return accessKeyId != null && !accessKeyId.isEmpty() && !"null".equals(accessKeyId) &&
                   secretAccessKey != null && !secretAccessKey.isEmpty() && !"null".equals(secretAccessKey);
        } catch (Exception e) {
            return false;
        }
    }

    private static class CustomCredentialsProvider implements AWSCredentialsProvider {
        @Override
        public AWSCredentials getCredentials() {
            try {
                String accessKeyId = BuildConfig.AWS_ACCESS_KEY_ID;
                String secretAccessKey = BuildConfig.AWS_SECRET_ACCESS_KEY;
                String sessionToken = BuildConfig.AWS_SESSION_TOKEN;
                
                if (!hasEnvCredentials()) {
                    throw new RuntimeException("AWS credentials not available from .env");
                }
                
                if (sessionToken != null && !sessionToken.isEmpty() && !"null".equals(sessionToken)) {
                    return new BasicSessionCredentials(accessKeyId, secretAccessKey, sessionToken);
                } else {
                    return new BasicAWSCredentials(accessKeyId, secretAccessKey);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to get credentials from .env: " + e.getMessage(), e);
            }
        }

        @Override
        public void refresh() {
            // No-op for static credentials
        }
    }

    /**
     * Parse awsconfiguration.json and extract the region from it.
     * If using .env credentials, return a null region.
     *
     * @return The region in String form. {@code null} if not.
     * @throws IllegalStateException if awsconfiguration.json is not properly configured.
     */
    public static String getRegion() {
        // If using .env credentials, return null (user will manually enter region)
        if (hasEnvCredentials()) {
            return null;
        }
        
        try {
            final AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
            if (configuration == null) {
                Log.w(TAG, "awsconfiguration.json not found, returning null region");
                return null; // Return null instead of throwing exception
            }

            final JSONObject jsonObject = configuration.optJsonObject("CredentialsProvider");
            if (jsonObject == null) {
                Log.w(TAG, "CredentialsProvider not found in awsconfiguration.json");
                return null;
            }

            String region = null;
            try {
                region = (String) ((JSONObject) (((JSONObject) jsonObject.get("CognitoIdentity")).get("Default"))).get("Region");
            } catch (final JSONException e) {
                Log.e(TAG, "Got exception when extracting region from cognito setting.", e);
            }
            return region;
        } catch (Exception e) {
            Log.w(TAG, "Failed to get region from awsconfiguration.json: " + e.getMessage());
            return null; // Return null on any error
        }
    }

}
