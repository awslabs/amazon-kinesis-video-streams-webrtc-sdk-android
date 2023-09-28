package com.amazonaws.kinesisvideo.utils

object Constants {
    /**
     * SDK identifier
     */
    const val APP_NAME = "aws-kvs-webrtc-android-client"

    /**
     * SDK version identifier
     */
    const val VERSION = "1.0.0"

    /**
     * Query parameter for Channel ARN. Used for calling Kinesis Video Websocket APIs.
     */
    const val CHANNEL_ARN_QUERY_PARAM = "X-Amz-ChannelARN"

    /**
     * Query parameter for Client Id. Only used for viewers. Used for calling Kinesis Video Websocket APIs.
     */
    const val CLIENT_ID_QUERY_PARAM = "X-Amz-ClientId"
}