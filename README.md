# Amazon Kinesis Video Streams Android WebRTC SDK
[![Build Status](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android/actions/workflows/ci.yml/badge.svg)](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android/actions/workflows/ci.yml)
[![Coverage Status](https://codecov.io/gh/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android/branch/master/graph/badge.svg)](https://codecov.io/gh/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android)

## Running KinesisVideoWebRTCDempApp Sample

### 1. Download the WebRTC SDK for Android

 To download the Kinesis Video Streams WebRTC SDK in Android, run the following command:

 `git clone https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android.git`


### 2. Create a user pool

 Follow the instructions [here](https://docs.aws.amazon.com/kinesisvideostreams/latest/dg/producersdk-android-prerequisites.html#set-up-user-pool) to set up an Amazon Cognito user pool for secure login.

### 3. Create an identity pool

 Follow the instructions [here](https://docs.aws.amazon.com/kinesisvideostreams/latest/dg/producersdk-android-prerequisites.html#set-up-identity-pool) to set up an Amazon Cognito identity pool.

## 4. Build and run the demo application using Android Studio

 1.  Import the downloaded SDK into the Android Studio integrated development environment by opening the **amazon-kinesis-video-streams-webrtc-sdkandroid/build.gradle** with `Open an existing Android Studio project`.
 2.  You will need all the information from the above steps copied in clipboard, then paste them into this file on your local file [awsconfiguration.json](app/src/main/res/raw/awsconfiguration.json). Your completed awsconfiguration.json should look something like this:
 ```json
 {
  "Version": "1.0",
  "CredentialsProvider": {
    "CognitoIdentity": {
      "Default": {
        "PoolId": "us-west-2:01234567-89ab-cdef-0123-456789abcdef",
        "Region": "us-west-2"
      }
    }
  },
  "IdentityManager": {
    "Default": {}
  },
  "CognitoUserPool": {
    "Default": {
      "AppClientSecret": "abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmno",
      "AppClientId": "0123456789abcdefghijklmnop",
      "PoolId": "us-west-2_qRsTuVwXy",
      "Region": "us-west-2"
    }
  }
}
 ```
 3. Import Google WebRTC (aka libwebrtc). Choose one of the following:

    a. Build libwebrtc yourself, following https://webrtc.github.io/webrtc-org/native-code/android/#getting-the-code. After it's been built, place the `.aar` file in [libs](app/libs).
    
    b. Acquire a pre-built binary of libwebrtc and import it. 
    
 5. Click gradle __Sync__ and __Build__
 6. Run the demo application in simulator or in Android device (connected through USB).


## 5. Peer to Peer Streaming

  On your Android device, open AWSKinesisVideoWebRTCDemoApp and sign up with `Create New Account` or sign in with existing accounts.

Note: This account information is stored in your Cognito User Pool and is not your AWS Console user name/password.

Once login is successful, you will entering the following channel information to start peer to peer streaming.

  1. Enter a channel name: e.g. `demo-channel`
  2. Enter AWS region: e.g. `us-west-2`
  3. Select `audio` if you would like to send both audio or video data.
  4. Optionally, when using it in `viewer` mode, you can enter a unique `Client Id` . Client ID is required only if multiple viewers are connected to a channel. This helps channel's master identify respective viewers.

 To verify peer to peer streaming, do any of the following setup. In these setup, ensure that  the _signaling channel name_, _region_, _viewer ID_,  and the AWS account ID are the __same__.

  ### 5.1 Peer to Peer Streaming between two Android device: Master and Viewer

 *  Start one Android device in `master` mode for starting a new session. Remote peer will be joining as viewer to this master. There should be only one master for any given channel.
 *  Use another Android device to connect to the same channel name (started up in the step above as `master`)  in `viewer` mode; this will connect to an existing session (channel) where a master is connected.
  * Verify media showing up in both Android devices.

   ### 5.2 Peer to Peer Streaming between Embedded SDK as master and Android device as viewer

  * Run Kinesis Video Streams [WebRTC embedded SDK](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-c/tree/master/samples) in `master` mode on a camera device.
  * Start the Android device in `viewer` mode - you should be able to check the video (and audio if selected both in embedded SDK) showing up in the Android device from the camera.
  * Verify media showing up from the Embedded SDK to the Android.

  ### 5.3 Peer to Peer Streaming between Android device as master and Web browser as viewer

  * Start one Android device in `master` mode for starting a new session.
  * Start the web browser using the [Javascript SDK](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-js) and start it as `viewer`.
  * Verify media showing up from the Android device to the browser.

## 6. ICE Candidate Trickling

Candidate trickling is a technique through which a caller may incrementally provide candidates to the callee after the initial offer has been dispatched; the semantics of "Trickle ICE" are defined in [RFC8838].

Trickle ICE is the default behavior within the Kinesis Video Stream WebRTC SDK for Android since it reduces the time it takes for the ICE negotiation process.
However, in the case that it needs to be disabled, locate the RTCConfiguration property called ContinualGatheringPolicy and change it to GATHER_ONCE instead of GATHER_CONTINUALLY (default).

```java
PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration();
rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
```
## 7. Testing
This SDK has been tested with Java 11, 17 to build the Gradle dependencies and Java 8, 11, and, 17 in the compile options in build.gradle

```agsl
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```
For more details about the different Java versions, please check [Java versions in Android builds](https://developer.android.com/build/jdks#jdk-config-in-studio)

## License

This library is licensed under the [Apache 2.0 License](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android/blob/master/LICENSE).
