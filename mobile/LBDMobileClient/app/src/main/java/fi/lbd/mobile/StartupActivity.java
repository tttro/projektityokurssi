package fi.lbd.mobile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static android.support.v4.app.ActivityCompat.startActivityForResult;


public class StartupActivity extends Activity {
    private String BACKEND_URL = "";
    private String OBJECT_COLLECTION = "";
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    static final int REQUEST_AUTHORIZATION = 1138;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    static final String TAG = "GCMDemo";
    String SENDER_ID = "Your-Sender-ID";

    String regid;

    private String mEmail;
    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("******", "StartupActivity onCreate()");

        BACKEND_URL = getResources().getString(R.string.backend_url);
        OBJECT_COLLECTION = getResources().getString(R.string.object_collection);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                AccountManager accountManager = AccountManager.get(getBaseContext());
                Account[] accounts = accountManager.getAccountsByType("com.google");
                Account account;
                if (accounts.length > 0) {
                    account = accounts[0];
                } else {
                    account = null;
                }
                Log.d("******", account.toString());
                String email = account.name;
                String foo = "foo";
                String scope = "oauth2:" + Scopes.PROFILE;
                try {
                    foo = GoogleAuthUtil.getToken(getApplicationContext(), email, scope);
                } catch (UserRecoverableAuthException userRecoverableException) {
                    // GooglePlayServices.apk is either old, disabled, or not present, which is
                    // recoverable, so we need to show the user some UI through the activity.

                    startActivityForResult(userRecoverableException.getIntent(), REQUEST_AUTHORIZATION);
                    Log.d("******", userRecoverableException.toString());
                } catch (GoogleAuthException fatalException) {
                    Log.d("******", fatalException.toString());
                    Log.d("******", fatalException.getMessage());
                } catch (IOException io) {
                    Log.d("******", "Blah3");
                }
                Log.d("******", foo);
            }
        });
        t.start();

        SharedPreferences settings = getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
        String url = settings.getString(BACKEND_URL, null);
        String collection = settings.getString(OBJECT_COLLECTION, null);

        context = getApplicationContext();

        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

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
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
//        Check if app was updated; if so, it must clear the registration ID
//        since the existing regID is not guaranteed to work with the new
//        app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
    }

    private void registerInBackground() {
        new AsyncTask() {

            @Override
            protected String doInBackground(Object[] params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }
//
//            @Override
//            protected void onPostExecute(String msg) {
//                Log.d("******", msg);
//            }

        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
}
