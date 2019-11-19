package com.amazonaws.kinesisvideo.demoapp.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.amazonaws.kinesisvideo.demoapp.R;
import com.amazonaws.kinesisvideo.demoapp.fragment.StreamWebRtcConfigurationFragment;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.google.android.material.navigation.NavigationView;

public class SimpleNavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = SimpleNavActivity.class.getSimpleName();

    private Fragment streamFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState != null) {
            streamFragment = (Fragment) getSupportFragmentManager().findFragmentByTag(StreamWebRtcConfigurationFragment.class.getName());
        }
        // Video only
        this.startConfigFragment();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            AWSMobileClient.getInstance().signOut();
            AWSMobileClient.getInstance().showSignIn(this,
                    SignInUIOptions.builder()
                            .logo(R.drawable.kinesisvideo_logo)
                            .backgroundColor(Color.WHITE)
                            .nextActivity(SimpleNavActivity.class)
                            .build(),
                    new Callback<UserStateDetails>() {
                        @Override
                        public void onResult(UserStateDetails result) {
                            Log.d(TAG, "onResult: User sign-in " + result.getUserState());
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "onError: User sign-in", e);
                        }
                    });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void startFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_simple, fragment, StreamWebRtcConfigurationFragment.class.getName()).commit();
    }

    public void startConfigFragment() {
        try {
            if (streamFragment == null) {
                streamFragment = StreamWebRtcConfigurationFragment.newInstance(this);
                this.startFragment(streamFragment);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to go back to configure stream.");
            e.printStackTrace();
        }
    }
}
