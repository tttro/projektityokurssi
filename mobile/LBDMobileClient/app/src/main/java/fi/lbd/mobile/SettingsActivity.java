package fi.lbd.mobile;

import android.app.Activity;
import android.os.Bundle;

import com.android.debug.hv.ViewServer;

import fi.lbd.mobile.fragments.SettingsFragment;

/**
 *  Activity to provide settings functionality.
 *
 *  Created by Ossi.
 */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }
        ViewServer.get(this).addWindow(this);
    }

    @Override
    public void onStart(){
        super.onStart();
        ActiveActivitiesTracker.activityStarted(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        ActiveActivitiesTracker.activityStopped(this);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        ViewServer.get(this).removeWindow(this);
    }
    @Override
    public void onResume() {
        super.onResume();
        ViewServer.get(this).setFocusedWindow(this);
    }
}
