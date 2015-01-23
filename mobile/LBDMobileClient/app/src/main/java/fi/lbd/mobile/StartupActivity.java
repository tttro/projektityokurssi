package fi.lbd.mobile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import fi.lbd.mobile.authorization.Authorization;
import fi.lbd.mobile.authorization.AuthorizationListener;
import fi.lbd.mobile.authorization.AuthorizedEventHandler;

/**
 * The first activity which is launched when the application is started. Tries to get one token from
 * google to check if the user has given access to details.
 */
public class StartupActivity extends Activity {

    private AuthorizationListener authListener = new AuthorizationListener() {
        @Override
        public Activity getActivity() {
            return StartupActivity.this;
        }

        @Override
        public void onTokenSuccess(Authorization.AuthResult token) {
            Log.d(this.getClass().getSimpleName(), "Got auth result in StartupActivity, id: " + token.getId() + " token: " + token.getToken());
            moveToNextActivity();
        }

        @Override
        public void onWaitingForResult() {}

        @Override
        public void onFatalException() {
            Log.e(this.getClass().getSimpleName(), "Failed to get authorization token in StartupActivity!");
//            StartupActivity.this.finish();
            moveToNextActivity();
        }

        @Override
        public void setCurrentRequestCode(int currentRequestCode) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        Authorization.getToken(this.authListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        ActiveActivitiesTracker.activityStarted(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
//        ActiveActivitiesTracker.activityStopped(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Authorization.resolveResult(this.authListener, requestCode, resultCode, data);
        AuthorizedEventHandler.processResults(requestCode, resultCode, data);
    }

    /**
     * When the execution gets to this point, user email has been chosen and a test token has been
     * successfully requested.
     */
    private void moveToNextActivity() {

        String backend_url = this.getResources().getString(R.string.backend_url);
        String object_collection = this.getResources().getString(R.string.object_collection);
        SharedPreferences settings = this.getApplication().getApplicationContext()
                .getSharedPreferences(this.getString(R.string.shared_preferences), Activity.MODE_PRIVATE);
        String url = settings.getString(backend_url, null);
        String collection = settings.getString(object_collection, null);

        // If no previous settings were found, move the user to SettingsActivity
        if(url == null || collection == null){
            finish();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            this.overridePendingTransition(0, 0);
        }
        // Else go straight to ListActivity
        else {
            finish();
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
            this.overridePendingTransition(0, 0);
        }
    }
}
