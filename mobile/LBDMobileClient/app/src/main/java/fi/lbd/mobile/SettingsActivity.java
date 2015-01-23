package fi.lbd.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import fi.lbd.mobile.authorization.Authorization;
import fi.lbd.mobile.authorization.AuthorizedEventHandler;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AuthorizedEventHandler.processResults(requestCode, resultCode, data);
    }
}
