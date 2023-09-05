package com.amazonaws.kinesisvideo.utils;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Constants {
    /**
     * SDK identifier
     */
    public static final String APP_NAME = "aws-kvs-webrtc-android-client";
    /**
     * SDK version identifier
     */
    public static final String VERSION = "1.0.0";

    /**
     * Query parameter for Channel ARN. Used for calling Kinesis Video Websocket APIs.
     */
    public static final String CHANNEL_ARN_QUERY_PARAM = "X-Amz-ChannelARN";

    /**
     * Query parameter for Client Id. Only used for viewers. Used for calling Kinesis Video Websocket APIs.
     */
    public static final String CLIENT_ID_QUERY_PARAM = "X-Amz-ClientId";

    /**
     * Regions for WebRTC Ingestion & Storage feature.
     */
    public static final Set<Region> INGESTION_PREVIEW_REGIONS = Sets.newHashSet(Region.getRegion("us-west-2"));

    public static final int MAX_CONNECTION_FAILURES_WITHIN_INTERVAL_FOR_JOIN_STORAGE_SESSION_RETRIES = 5;

    public static final long JOIN_STORAGE_SESSION_RETRIES_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(15);

    public static final long WEBSOCKET_MESSAGE_DELIVERY_TIMEOUT_MILLISECONDS = TimeUnit.SECONDS.toMillis(6);

    /**
     * Interval (in seconds) in which to print the peer connection stats.
     * 0 to disable.
     */
    public static final int LOG_STATS_INTERVAL_SECONDS = 30;

    public static final long EXPONENTIAL_BACKOFF_CAP_MILLISECONDS = TimeUnit.SECONDS.toMillis(10);

    public static final long EXPONENTIAL_BACKOFF_COEFFICIENT_MILLISECONDS = TimeUnit.MILLISECONDS.toMillis(200);

    public static final int TIMEOUT_TO_ESTABLISH_CONNECTION_WITH_MEDIA_SERVER_SECONDS = 5;
}
