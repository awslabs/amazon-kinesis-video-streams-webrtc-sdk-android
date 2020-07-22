# Amazon Kinesis Video Streams Android WebRTC SDK
[![Build Status](https://travis-ci.org/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android.svg?branch=master)](https://travis-ci.org/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android)
[![Coverage Status](https://codecov.io/gh/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android/branch/master/graph/badge.svg)](https://codecov.io/gh/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android)

## Running KinesisVideoWebRTCDempApp Sample

### 1. Download the WebRTC SDK for Android

 To download the Kinesis Video Streams WebRTC SDK in Android, run the following command:

 `git clone https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android.git`


### 2. Create a user pool

 Setup user pool for secure login using Cognito:

 * Go to https://console.aws.amazon.com/cognito/
 * Click `Manage User Pools`
 * Click `Create a user pool`
 * Fill-in `Pool name`
 * Click `Review defaults`
 * Click `Create user pool`
 * Copy `Pool Id`
 * Select `App clients` in the left navigation.
 * Click `Add an app client`
 * Fill-in `App client name`
 * Click `Create app client`
 * Click `Show details` and copy `App client id` and `App client secret`  

### 3. Create an identity pool

 * Go to https://console.aws.amazon.com/cognito/
 * Click `Manage Identity Pools`
 * Click `Create new identity pool`
 * Fill-in `Identity pool name`
 * Under the heading `Authentication providers`, in the `Cognito` tab, fill-in the `User Pool Id` and  `App client id` from the user pools step.
 * Click `Create Pool`
 * There will be details for 2 roles. Look at the one for `authenticated identities` and click `Edit` next to the policy document and your policy should look like this after editing:

```
    {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "cognito-identity:*",
              "kinesisvideo:*"
            ],
            "Resource": [
              "*"
            ]
          }
        ]
      }
```
* Click `Allow`
* Copy the `Identity Pool Id` from the code snippets on the screen.  

## 4. Build and run the demo application using Android Studio

 1.  Import the downloaded SDK into the Android Studio integrated development environment by opening the **amazon-kinesis-video-streams-webrtc-sdkandroid/build.gradle** with `Open an existing Android Studio project`.
 2.  You will need all the information from the above steps copied in clipboard, then paste them into this file on your local file [awsconfiguration.json](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android/blob/master/src/main/res/raw/awsconfiguration.json).
 3.  Click gradle __Sync__ and __Build__
 4.  Run the demo application in simulator or in Android device (connected through USB).


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

## License

This library is licensed under the [Apache 2.0 License](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android/blob/master/LICENSE).
