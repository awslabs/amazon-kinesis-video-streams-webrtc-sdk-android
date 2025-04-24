package com.amazonaws.kinesisvideo.demoapp.util;

import com.amazonaws.kinesisvideo.demoapp.KinesisVideoWebRtcDemoApp;
import com.amazonaws.services.kinesisvideo.AWSKinesisVideoClient;
import com.amazonaws.regions.Region;

import android.util.Log;


public class KvsClientFactory {
    private static final String TAG = KvsClientFactory.class.getSimpleName();

    private static final String DUAL_STACK_CONTROL_PLANE_ENDPOINT_FORMAT = "kinesisvideo.%s.api.aws";
    private static final String DUAL_STACK_CONTROL_PLANE_ENDPOINT_FORMAT_CN = "kinesisvideo.%s.api.amazonwebservices.com.cn";

    private static String generateDualStackEndpoint(String region) {
        if (region == null || region.isEmpty()) {
            Log.w(TAG, "AWS region is null or empty, will use legacy control-plane endpoint.");
            return null;
        }

        if (region.startsWith("cn-")) {
            return String.format(DUAL_STACK_CONTROL_PLANE_ENDPOINT_FORMAT_CN, region);
        }

        return String.format(DUAL_STACK_CONTROL_PLANE_ENDPOINT_FORMAT, region);
    }
    
    public static AWSKinesisVideoClient getAwsKinesisVideoClient(final String region, final boolean useDualStack) {
        final AWSKinesisVideoClient awsKinesisVideoClient = new AWSKinesisVideoClient(
                KinesisVideoWebRtcDemoApp.getCredentialsProvider().getCredentials());
        awsKinesisVideoClient.setRegion(Region.getRegion(region));
        awsKinesisVideoClient.setSignerRegionOverride(region);
        awsKinesisVideoClient.setServiceNameIntern("kinesisvideo");

        if (useDualStack) {
            awsKinesisVideoClient.setEndpoint(generateDualStackEndpoint(region));
        }

        return awsKinesisVideoClient;
    }
    
}
