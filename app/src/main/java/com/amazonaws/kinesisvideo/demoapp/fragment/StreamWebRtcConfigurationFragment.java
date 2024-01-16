package com.amazonaws.kinesisvideo.demoapp.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.amazonaws.kinesisvideo.demoapp.KinesisVideoWebRtcDemoApp;
import com.amazonaws.kinesisvideo.demoapp.R;
import com.amazonaws.kinesisvideo.demoapp.activity.SimpleNavActivity;
import com.amazonaws.kinesisvideo.demoapp.activity.WebRtcActivity;
import com.amazonaws.regions.Region;
import com.amazonaws.services.kinesisvideo.AWSKinesisVideoClient;
import com.amazonaws.services.kinesisvideo.model.ChannelRole;
import com.amazonaws.services.kinesisvideo.model.CreateSignalingChannelRequest;
import com.amazonaws.services.kinesisvideo.model.CreateSignalingChannelResult;
import com.amazonaws.services.kinesisvideo.model.DescribeMediaStorageConfigurationRequest;
import com.amazonaws.services.kinesisvideo.model.DescribeMediaStorageConfigurationResult;
import com.amazonaws.services.kinesisvideo.model.DescribeSignalingChannelRequest;
import com.amazonaws.services.kinesisvideo.model.DescribeSignalingChannelResult;
import com.amazonaws.services.kinesisvideo.model.GetSignalingChannelEndpointRequest;
import com.amazonaws.services.kinesisvideo.model.GetSignalingChannelEndpointResult;
import com.amazonaws.services.kinesisvideo.model.ResourceEndpointListItem;
import com.amazonaws.services.kinesisvideo.model.ResourceNotFoundException;
import com.amazonaws.services.kinesisvideo.model.SingleMasterChannelEndpointConfiguration;
import com.amazonaws.services.kinesisvideosignaling.AWSKinesisVideoSignalingClient;
import com.amazonaws.services.kinesisvideosignaling.model.GetIceServerConfigRequest;
import com.amazonaws.services.kinesisvideosignaling.model.GetIceServerConfigResult;
import com.amazonaws.services.kinesisvideosignaling.model.IceServer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StreamWebRtcConfigurationFragment extends Fragment {
    private static final String TAG = StreamWebRtcConfigurationFragment.class.getSimpleName();

    private static final String KEY_CHANNEL_NAME = "channelName";
    public static final String KEY_CLIENT_ID = "clientId";
    public static final String KEY_REGION = "region";
    public static final String KEY_CHANNEL_ARN = "channelArn";
    public static final String KEY_STREAM_ARN = "streamArn";
    public static final String KEY_WSS_ENDPOINT = "wssEndpoint";
    public static final String KEY_WEBRTC_ENDPOINT = "webrtcEndpoint";
    public static final String KEY_IS_MASTER = "isMaster";
    public static final String KEY_ICE_SERVER_USER_NAME = "iceServerUserName";
    public static final String KEY_ICE_SERVER_PASSWORD = "iceServerPassword";
    public static final String KEY_ICE_SERVER_TTL = "iceServerTTL";
    public static final String KEY_ICE_SERVER_URI = "iceServerUri";
    public static final String KEY_MEDIA_SOURCE = "MediaSource";
    public static final String KEY_MEDIA_URL = "MediaURL";

    public static final String MEDIA_SOURCE_CAMERA_FRONT = "Front Camera";
    public static final String MEDIA_SOURCE_CAMERA_BACK = "Back Camera";
    public static final String MEDIA_SOURCE_STREAM = "Stream URL";

    private static final String KEY_SEND_VIDEO = "sendVideo";
    public static final String KEY_SEND_AUDIO = "sendAudio";

    private static final String[] WEBRTC_OPTIONS = {
            "Send Video",
            "Send Audio",
    };

    private static final String[] KEY_OF_OPTIONS = {
            KEY_SEND_VIDEO,
            KEY_SEND_AUDIO,
    };


    private EditText mChannelName;
    private EditText mClientId;
    private EditText mRegion;
    private Spinner mSources;
    private EditText mStreamURL;
    private CheckBox mIngestMedia;
    private final List<ResourceEndpointListItem> mEndpointList = new ArrayList<>();
    private final List<IceServer> mIceServerList = new ArrayList<>();
    private String mChannelArn = null;
    private String mStreamArn = null;
    private ListView mOptions;

    private SimpleNavActivity navActivity;

    public static StreamWebRtcConfigurationFragment newInstance(SimpleNavActivity navActivity) {
        StreamWebRtcConfigurationFragment s = new StreamWebRtcConfigurationFragment();
        s.navActivity = navActivity;
        return s;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        if (getActivity() != null) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 9393);
            }

            getActivity().setTitle(getActivity().getString(R.string.title_fragment_channel));
        }

        return inflater.inflate(R.layout.fragment_stream_webrtc_configuration, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Button mStartMasterButton = view.findViewById(R.id.start_master);
        mStartMasterButton.setOnClickListener(startMasterActivityWhenClicked());
        Button mStartViewerButton = view.findViewById(R.id.start_viewer);
        mStartViewerButton.setOnClickListener(startViewerActivityWhenClicked());

        mChannelName = view.findViewById(R.id.channel_name);
        mClientId = view.findViewById(R.id.client_id);
        mRegion = view.findViewById(R.id.region);
        mIngestMedia = view.findViewById(R.id.ingest_media);
        setRegionFromCognito();

        mOptions = view.findViewById(R.id.webrtc_options);
        mOptions.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, WEBRTC_OPTIONS) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_multiple_choice, null);

                    final CheckedTextView ctv = v.findViewById(android.R.id.text1);
                    ctv.setText(WEBRTC_OPTIONS[position]);

                    // Send video is enabled by default and cannot uncheck
                    if (position == 0) {
                        ctv.setEnabled(false);
                        ctv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ctv.setChecked(true);
                            }
                        });
                    }
                    return v;
                }

                return convertView;
            }
        });
        mOptions.setItemsCanFocus(false);
        mOptions.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mOptions.setItemChecked(0, true);

        mSources = view.findViewById(R.id.source_spinner);

        final List<String> sourceList = Arrays.asList(
                MEDIA_SOURCE_CAMERA_FRONT,
                MEDIA_SOURCE_CAMERA_BACK,
                MEDIA_SOURCE_STREAM
        );

        if (getContext() != null) {
            mSources.setAdapter(new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    sourceList));
        }

        mStreamURL = view.findViewById(R.id.url);
        mSources.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mStreamURL.setVisibility(sourceList.get(position).equals(MEDIA_SOURCE_STREAM)? View.VISIBLE: View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mStreamURL.setVisibility(View.GONE);
            }
        });
    }

    private void setRegionFromCognito() {
        String region = KinesisVideoWebRtcDemoApp.getRegion();
        if (region != null) {
            mRegion.setText(region);
        }
    }

    private View.OnClickListener startMasterActivityWhenClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                startMasterActivity();
            }
        };
    }

    private void startMasterActivity() {

        if (mIngestMedia.isChecked()) {
            // Check that the "Send Audio" and "Send Video" boxes are enabled.
            final SparseBooleanArray checked = mOptions.getCheckedItemPositions();
            for (int i = 0; i < mOptions.getCount(); i++) {
                if (!checked.get(i)) {
                    new AlertDialog.Builder(getActivity())
                            .setPositiveButton("OK", null)
                            .setMessage("Audio and video must be sent to ingest media!")
                            .create()
                            .show();
                    return;
                }
            }
        }

        if (!updateSignalingChannelInfo(mRegion.getText().toString(),
                mChannelName.getText().toString(),
                ChannelRole.MASTER)) {
            return;
        }

        if (mChannelArn != null) {
            Bundle extras = setExtras(true);
            Intent intent = new Intent(getActivity(), WebRtcActivity.class);
            intent.putExtras(extras);
            startActivity(intent);
        }
    }

    private View.OnClickListener startViewerActivityWhenClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                startViewerActivity();
            }
        };
    }

    private void startViewerActivity() {
        if (!updateSignalingChannelInfo(mRegion.getText().toString(),
                mChannelName.getText().toString(),
                ChannelRole.VIEWER)) {
            return;
        }

        if (mChannelArn != null) {
            Bundle extras = setExtras(false);
            Intent intent = new Intent(getActivity(), WebRtcActivity.class);
            intent.putExtras(extras);
            startActivity(intent);
        }
    }

    private Bundle setExtras(boolean isMaster) {
        final Bundle extras = new Bundle();
        final String channelName = mChannelName.getText().toString();
        final String clientId = mClientId.getText().toString();
        final String region = mRegion.getText().toString();

        extras.putString(KEY_CHANNEL_NAME, channelName);
        extras.putString(KEY_CLIENT_ID, clientId);
        extras.putString(KEY_REGION, region);
        extras.putString(KEY_REGION, region);
        extras.putString(KEY_CHANNEL_ARN, mChannelArn);
        extras.putString(KEY_STREAM_ARN, mStreamArn);
        extras.putBoolean(KEY_IS_MASTER, isMaster);

        if (mIceServerList.size() > 0) {
            ArrayList<String> userNames = new ArrayList<>(mIceServerList.size());
            ArrayList<String> passwords = new ArrayList<>(mIceServerList.size());
            ArrayList<Integer> ttls = new ArrayList<>(mIceServerList.size());
            ArrayList<List<String>> urisList = new ArrayList<>();
            for (final IceServer iceServer : mIceServerList) {
                userNames.add(iceServer.getUsername());
                passwords.add(iceServer.getPassword());
                ttls.add(iceServer.getTtl());
                urisList.add(iceServer.getUris());
            }
            extras.putStringArrayList(KEY_ICE_SERVER_USER_NAME, userNames);
            extras.putStringArrayList(KEY_ICE_SERVER_PASSWORD, passwords);
            extras.putIntegerArrayList(KEY_ICE_SERVER_TTL, ttls);
            extras.putSerializable(KEY_ICE_SERVER_URI, urisList);
        } else {
            extras.putStringArrayList(KEY_ICE_SERVER_USER_NAME, null);
            extras.putStringArrayList(KEY_ICE_SERVER_PASSWORD, null);
            extras.putIntegerArrayList(KEY_ICE_SERVER_TTL, null);
            extras.putSerializable(KEY_ICE_SERVER_URI, null);
        }

        for (ResourceEndpointListItem endpoint : mEndpointList) {
            if (endpoint.getProtocol().equals("WSS")) {
                extras.putString(KEY_WSS_ENDPOINT, endpoint.getResourceEndpoint());
            } else if (endpoint.getProtocol().equals("WEBRTC")) {
                extras.putString(KEY_WEBRTC_ENDPOINT, endpoint.getResourceEndpoint());
            }
        }

        final SparseBooleanArray checked = mOptions.getCheckedItemPositions();
        for (int i = 0; i < mOptions.getCount(); i++) {
            extras.putBoolean(KEY_OF_OPTIONS[i], checked.get(i));
        }

        extras.putString(KEY_MEDIA_SOURCE, mSources.getSelectedItem().toString());
        extras.putString(KEY_MEDIA_URL, mStreamURL.getText().toString());

        return extras;
    }

    private AWSKinesisVideoClient getAwsKinesisVideoClient(final String region) {
        final AWSKinesisVideoClient awsKinesisVideoClient = new AWSKinesisVideoClient(
                KinesisVideoWebRtcDemoApp.getCredentialsProvider().getCredentials());
        awsKinesisVideoClient.setRegion(Region.getRegion(region));
        awsKinesisVideoClient.setSignerRegionOverride(region);
        awsKinesisVideoClient.setServiceNameIntern("kinesisvideo");
        return awsKinesisVideoClient;
    }

    private AWSKinesisVideoSignalingClient getAwsKinesisVideoSignalingClient(final String region, final String endpoint) {
        final AWSKinesisVideoSignalingClient client = new AWSKinesisVideoSignalingClient(
                KinesisVideoWebRtcDemoApp.getCredentialsProvider().getCredentials());
        client.setRegion(Region.getRegion(region));
        client.setSignerRegionOverride(region);
        client.setServiceNameIntern("kinesisvideo");
        client.setEndpoint(endpoint);
        return client;
    }

    /**
     * Fetches info needed to connect to the Amazon Kinesis Video Streams Signaling channel.
     *
     * @param region      The region the Signaling channel is located in.
     * @param channelName The name of the Amazon Kinesis Video Streams Signaling channel.
     * @param role        The signaling channel role (master or viewer).
     * @return {@code true} on success. {@code false} if unsuccessful.
     */
    private boolean updateSignalingChannelInfo(final String region, final String channelName, final ChannelRole role) {
        mEndpointList.clear();
        mIceServerList.clear();
        mChannelArn = null;
        final UpdateSignalingChannelInfoTask task = new UpdateSignalingChannelInfoTask(this);

        String errorMessage = null;
        try {
            errorMessage = task.execute(region, channelName, role).get();
        } catch (Exception e) {
            Log.e(TAG, "Failed to wait for response of UpdateSignalingChannelInfoTask", e);
        }

        if (errorMessage != null) {
            Log.e(TAG, "updateSignalingChannelInfo() encountered an error: " + errorMessage);
        }
        return errorMessage == null;
    }

    /**
     * Makes backend calls to KVS in order to obtain info needed to start the WebRTC session.
     * <p>
     * The task returns {@code null} upon success, otherwise, it returns an error message.
     */
    static class UpdateSignalingChannelInfoTask extends AsyncTask<Object, String, String> {
        final WeakReference<StreamWebRtcConfigurationFragment> mFragment;

        UpdateSignalingChannelInfoTask(final StreamWebRtcConfigurationFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        protected String doInBackground(final Object... objects) {
            final String region = (String) objects[0];
            final String channelName = (String) objects[1];
            final ChannelRole role = (ChannelRole) objects[2];

            // Step 1. Create Kinesis Video Client
            final AWSKinesisVideoClient awsKinesisVideoClient;
            try {
                awsKinesisVideoClient = mFragment.get().getAwsKinesisVideoClient(region);
            } catch (Exception e) {
                return "Create client failed with " + e.getLocalizedMessage();
            }

            // Step 2. Use the Kinesis Video Client to call DescribeSignalingChannel API.
            //         If that fails with ResourceNotFoundException, the channel does not exist.
            //         If we are connecting as Master, if it doesn't exist, we attempt to create
            //         it by calling CreateSignalingChannel API.
            try {
                final DescribeSignalingChannelResult describeSignalingChannelResult = awsKinesisVideoClient.describeSignalingChannel(
                        new DescribeSignalingChannelRequest()
                                .withChannelName(channelName));

                Log.i(TAG, "Channel ARN is " + describeSignalingChannelResult.getChannelInfo().getChannelARN());
                mFragment.get().mChannelArn = describeSignalingChannelResult.getChannelInfo().getChannelARN();
            } catch (final ResourceNotFoundException e) {
                if (role.equals(ChannelRole.MASTER)) {
                    try {
                        CreateSignalingChannelResult createSignalingChannelResult = awsKinesisVideoClient.createSignalingChannel(
                                new CreateSignalingChannelRequest()
                                        .withChannelName(channelName));

                        mFragment.get().mChannelArn = createSignalingChannelResult.getChannelARN();
                    } catch (Exception ex) {
                        return "Create Signaling Channel failed with Exception " + ex.getLocalizedMessage();
                    }
                } else {
                    return "Signaling Channel " + channelName + " doesn't exist!";
                }
            } catch (Exception ex) {
                return "Describe Signaling Channel failed with Exception " + ex.getLocalizedMessage();
            }

            // Step 3. If we are ingesting media, we need to check if the Signaling Channel has a Kinesis Video
            //         Stream configured to write media to. We can call the DescribeMediaStorageConfiguration API
            //         to determine this.
            if (role == ChannelRole.MASTER && mFragment.get().mIngestMedia.isChecked()) {
                try {
                    final DescribeMediaStorageConfigurationResult describeMediaStorageConfigurationResult = awsKinesisVideoClient.describeMediaStorageConfiguration(
                            new DescribeMediaStorageConfigurationRequest()
                                    .withChannelARN(mFragment.get().mChannelArn));

                    if (!"ENABLED".equalsIgnoreCase(describeMediaStorageConfigurationResult.getMediaStorageConfiguration().getStatus())) {
                        Log.e(TAG, "Media storage is not enabled for this channel!");
                        return "Media Storage is DISABLED for this channel!";
                    }
                    mFragment.get().mStreamArn = describeMediaStorageConfigurationResult.getMediaStorageConfiguration().getStreamARN();
                } catch (Exception ex) {
                    return "Describe Media Storage Configuration failed with Exception " + ex.getLocalizedMessage();
                }
            }

            final String[] protocols;
            if (mFragment.get().mIngestMedia.isChecked()) {
                protocols = new String[]{"WSS", "HTTPS", "WEBRTC"};
            } else {
                protocols = new String[]{"WSS", "HTTPS"};
            }

            // Step 4. Use the Kinesis Video Client to call GetSignalingChannelEndpoint.
            //         Each signaling channel is assigned an HTTPS and WSS endpoint to connect
            //         to for data-plane operations, which we fetch using the GetSignalingChannelEndpoint API,
            //         and a WEBRTC endpoint to for storage data-plane operations.
            //         Attempting to obtain the WEBRTC endpoint if the signaling channel is not configured
            //         will result in an InvalidArgumentException.
            try {
                final GetSignalingChannelEndpointResult getSignalingChannelEndpointResult = awsKinesisVideoClient.getSignalingChannelEndpoint(
                        new GetSignalingChannelEndpointRequest()
                                .withChannelARN(mFragment.get().mChannelArn)
                                .withSingleMasterChannelEndpointConfiguration(
                                        new SingleMasterChannelEndpointConfiguration()
                                                .withProtocols(protocols)
                                                .withRole(role)));

                Log.i(TAG, "Endpoints " + getSignalingChannelEndpointResult.toString());
                mFragment.get().mEndpointList.addAll(getSignalingChannelEndpointResult.getResourceEndpointList());
            } catch (Exception e) {
                return "Get Signaling Endpoint failed with Exception " + e.getLocalizedMessage();
            }

            String dataEndpoint = null;
            for (ResourceEndpointListItem endpoint : mFragment.get().mEndpointList) {
                if (endpoint.getProtocol().equals("HTTPS")) {
                    dataEndpoint = endpoint.getResourceEndpoint();
                }
            }

            // Step 5. Construct the Kinesis Video Signaling Client. The HTTPS endpoint from the
            //         GetSignalingChannelEndpoint response above is used with this client. This
            //         client is just used for getting ICE servers, not for actual signaling.
            // Step 6. Call GetIceServerConfig in order to obtain TURN ICE server info.
            //         Note: the STUN endpoint will be `stun:stun.kinesisvideo.${region}.amazonaws.com:443`
            try {
                final AWSKinesisVideoSignalingClient awsKinesisVideoSignalingClient = mFragment.get().getAwsKinesisVideoSignalingClient(region, dataEndpoint);
                GetIceServerConfigResult getIceServerConfigResult = awsKinesisVideoSignalingClient.getIceServerConfig(
                        new GetIceServerConfigRequest().withChannelARN(mFragment.get().mChannelArn).withClientId(role.name()));
                mFragment.get().mIceServerList.addAll(getIceServerConfigResult.getIceServerList());
            } catch (Exception e) {
                return "Get Ice Server Config failed with Exception " + e.getLocalizedMessage();
            }

            return null;
        }

        /**
         * Shows a Dialog box if any errors were returned in {@link #doInBackground(Object...)}.
         *
         * @param result This will be displayed in the Dialog box.
         */
        @Override
        protected void onPostExecute(final String result) {
            if (result != null) {
                new AlertDialog.Builder(mFragment.get().getContext())
                        .setPositiveButton("OK", null)
                        .setMessage(result)
                        .create()
                        .show();
            }
        }
    }
}