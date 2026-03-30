package com.amazonaws.kinesisvideo.utils;

import com.amazonaws.kinesisvideo.demoapp.BuildConfig;

public class Constants {
    /**
     * SDK identifier
     */
    public static final String APP_NAME = "aws-kvs-webrtc-android-client";
    /**
     * SDK version identifier
     */
    public static final String VERSION = BuildConfig.VERSION_NAME;

    /**
     * Query parameter for Channel ARN. Used for calling Kinesis Video Websocket APIs.
     */
    public static final String CHANNEL_ARN_QUERY_PARAM = "X-Amz-ChannelARN";

    /**
     * Query parameter for Client Id. Only used for viewers. Used for calling Kinesis Video Websocket APIs.
     */
    public static final String CLIENT_ID_QUERY_PARAM = "X-Amz-ClientId";

    /** FIPS control-plane endpoint format for GovCloud (e.g. kinesisvideo-fips.us-gov-west-1.amazonaws.com) */
    public static final String FIPS_CONTROL_PLANE_ENDPOINT_FORMAT = "kinesisvideo-fips.%s.amazonaws.com";

    public static boolean isGovCloudRegion(final String region) {
        return region != null && region.startsWith("us-gov-");
    }
}
