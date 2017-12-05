package com.amazonaws.kinesisvideo.demoapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.amazonaws.kinesisvideo.demoapp.R;
import com.amazonaws.kinesisvideo.demoapp.fragment.StreamConfigurationFragment;
import com.amazonaws.kinesisvideo.demoapp.fragment.StreamingFragment;
import com.amazonaws.mobile.auth.core.DefaultSignInResultHandler;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.IdentityProvider;
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration;
import com.amazonaws.mobile.auth.ui.SignInActivity;

public class SimpleNavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.simple_nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            try {
                startConfigFragment();
            } catch (Exception e) {
                Log.e("", "Failed to initialize streaming demo fragment.");
                e.printStackTrace();
            }
        } else if (id == R.id.nav_logout) {
            IdentityManager.getDefaultIdentityManager().signOut();
            IdentityManager.getDefaultIdentityManager().setUpToAuthenticate(this, new DefaultSignInResultHandler() {
                @Override
                public void onSuccess(Activity callingActivity, IdentityProvider provider) {
                    startConfigFragment();
                }

                @Override
                public boolean onCancel(Activity callingActivity) {
                    return false;
                }
            });
            SignInActivity.startSignInActivity(this, new AuthUIConfiguration.Builder().userPools(true).build());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void startFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_simple, fragment).commit();
    }

    public void startStreamingFragment(Bundle extras) {
        try {
            Fragment streamFragment = StreamingFragment.newInstance(this);
            streamFragment.setArguments(extras);
            this.startFragment(streamFragment);
        } catch (Exception e) {
            Log.e("", "Failed to start streaming fragment.");
            e.printStackTrace();
        }
    }

    public void startConfigFragment() {
        try {
            Fragment streamFragment = StreamConfigurationFragment.newInstance(this);
            this.startFragment(streamFragment);
        } catch (Exception e) {
            Log.e("", "Failed to go back to configure stream.");
            e.printStackTrace();
        }
    }
}
