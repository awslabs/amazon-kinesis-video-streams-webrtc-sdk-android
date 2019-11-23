# Running KinesisVideoWebRTCDempApp Sample

## 1. Download the WebRTC SDK for Android

To download the WebRTC SDK in Android, run the following command:

`git clone https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-android.git`
  
## 2. Create a user pool
  * Go to https://console.aws.amazon.com/cognito/
  * Click `Manage your User Pools`
  * Click `Create a user pool`
  * Fill-in `Pool name`
  * Click `Review defaults`
  * Click `Create user pool`
  * Copy `Pool Id` :clipboard:
  * Select `App clients` in the left nav.
  * Click `Add an app client`
  * Fill-in `App client name`
  * Click `Create app client`
  * Click `Show details` and copy `App client id` and `App client secret` :clipboard:
    * ![Shows show details button](screenshots/click_show_details.png) `-->` ![](screenshots/copy_app_client_id_and_secret.png)

## 3. Create an identity pool
  * Go to https://console.aws.amazon.com/cognito/
  * Click `Manage Federated Identities`
  * Click `Create new identity pool`
  * Fill-in `Identity pool name`
    * ![Shows field for inputting identity pool name](screenshots/pool_name.png)
  * Under the heading `Authentication providers`, in the `Cognito` tab, fill-in the `User Pool Id` and `App client id` from the user pools step.
    ![Shows field for inputting identity pool name](screenshots/fill_in_user_pool.png)
  * Click `Create create`
  * There will be details for 2 roles. Look at the one for `authenticated identities` and click `Edit` next to the policy document and your policy should look like this:
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

 ## 4. Build and run the demo applicaiton using Android Studio
 
 1.  Import the downloaded SDK into the Android Studio integrated development environment by opening the amazon-kinesis-video-streams-webrtc-sdkandroid/build.gradle with Open as Project.
 2.  You will need all the information from the above steps copied in clipboard, then paste them into this file on your local file [awsconfiguration.json](src/main/res/raw/awsconfiguration.json).
 3.  Click gradle `Sync` or `Build`
 4.  Run the demo application in simulator or in Android device (connected through USB)
 
 ## 5. Peer to Peer Streaming

  On your Android device, open AWSKinesisVideoWebRTCDemoApp and login using the AWS user credentials from Set Up an AWS Account and Create an Administrator.
  
  Note : Cognito settings can be tuned through your Cognito User Pool in the AWS management Console.

 Once login is successful, 
  1. Use a  unique channel name (must be unique within an AWS region and account).
  2. Choose a region and whether you want to send audio or video data, or both.
  3.  Optionally, choose a unique Client Id if you want to connect to this channel as a viewer. Client ID is required only if multiple viewers are connected to a channel. This helps channel's master identify respective viewers.

  To verify peer to peer streaming, do any of the following. Ensure that in all the cases, the signaling channel name, region, viewer ID, and the AWS account ID are the same.

  ##### Peer to Peer Streaming between two Android device: Master and Viewer
  
 *  Start one Android device in Master mode for starting a new session. Remote peer will be joining as viewer to this master. There should only be one master for any given channel
 *  Use another Android device (or JS client) to connect to the same channel name (started up in the above step set up as a master). Starting in viewer mode connects to an existing session (channel) where a master is connected. Note: Please ensure that the master is already streaming before connecting the viewers

   ##### Peer to Peer Streaming between Embedded SDK master and Android device

  * Run KVS [WebRTC embedded SDK](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-c) in master mode on a camera device.
  * Start the Android device in viewer mode - you should be able to check the video (and audio if selected both in embedded SDK) showing up in the Android device from the camera. Reconnect viewer button if you are not seeing the remote video in the bottom half of the screen.

  ##### Peer to Peer Streaming between Android device as master and Web browser as viewer

  *  Start one Android device in Master mode for starting a new session.
  * Start the Web Browser using the [Javacript SDK](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-js) and start it as viewer.
  * Verify media showing up from the Android device to the browser

