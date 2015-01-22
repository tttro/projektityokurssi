package fi.lbd.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import fi.lbd.mobile.backendhandler.GoogleAuthProvider;

/**
 * The first activity which is launched when the application is started. Tries to get one token from
 * google to check if the user has given access to details.
 */
public class StartupActivity extends Activity {
    private String BACKEND_URL = "";
    private String OBJECT_COLLECTION = "";
//    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
//    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
//    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
//    static final int REQUEST_AUTHORIZATION = 1138;

//    private String mEmail;
//    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("******", "StartupActivity onCreate()");

        BACKEND_URL = getResources().getString(R.string.backend_url);
        OBJECT_COLLECTION = getResources().getString(R.string.object_collection);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
            (new GoogleAuthProvider(getApplicationContext())).getIdToken(); // TODO: Jotenki se errorin handlaus...
            }
        });
        t.start();

        SharedPreferences settings = getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
        String url = settings.getString(BACKEND_URL, null);
        String collection = settings.getString(OBJECT_COLLECTION, null);

        // If no previous settings were found, move the user to SettingsActivity
        if(url == null || collection == null){
            finish();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        // Else go straight to ListActivity
        else {
            finish();
            ApplicationDetails.get().setCurrentCollection(collection);
            ApplicationDetails.get().setCurrentBaseApiUrl(url);
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
