package com.example.namiskuukkel.login_test;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends Activity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;

    /* Track whether the sign-in button has been clicked so that we know to resolve
 * all issues preventing sign-in without waiting.
 */
    private boolean mSignInClicked;

    /* Store the connection result from onConnectionFailed callbacks so that we can
     * resolve them when the user clicks sign-in.
     */
    private ConnectionResult mConnectionResult;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_activity);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        //The following two have to be implemented somewhere cause of Google+ developer policies
        findViewById(R.id.revoke_access_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();

    }


    public void onClick(View view) {
        TextView status = (TextView) findViewById(R.id.status);
        switch (view.getId()){
            case R.id.sign_in_button:
                if (!mGoogleApiClient.isConnecting()) {
                    mSignInClicked = true;
                    resolveSignInError();
                }
                break;

            case R.id.sign_out_button:
                if (mGoogleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                    mGoogleApiClient.connect();
                }

                status.setText("Not Connected");
                break;
            case R.id.revoke_access_button:
                if (mGoogleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                            .setResultCallback(new ResultCallback<Status>() {

                                public void onResult(Status stat) {
                                    // mGoogleApiClient is now disconnected and access has been revoked.
                                    // Trigger app logic to comply with the developer policies
                                }

                            });
                    status.setText("Not Connected");
                }

                break;
        }

    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.d("onStart", "hip");
    }

    protected void onStop() {
        super.onStop();
        Log.d("onStop", "hip");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    public void onConnectionFailed(ConnectionResult result) {
        Log.d("onFailed", "hip");
        if (!mIntentInProgress) {
            // Store the ConnectionResult so that we can use it later when the user clicks
            // 'sign-in'.
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("onConnected", "hip");
        mSignInClicked = false;
        TextView status = (TextView) findViewById(R.id.status);
        status.setText("Connected");
        getAndUseAuthTokenInAsyncTask();

        /*while(result == "") {
            android.os.SystemClock.sleep(1000);
            result = settings.getString("token", "");
        }*/
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    public void onConnectionSuspended(int cause) {
       mGoogleApiClient.connect();
    }

    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (mConnectionResult != null && mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    void getAndUseAuthTokenInAsyncTask() {
        class PostTask extends AsyncTask<String, Integer, String> {
            @Override
            protected String doInBackground(String... params) {
                String accessToken = null;
                try {
                    mGoogleApiClient.connect();
                    accessToken = GoogleAuthUtil.getToken(getApplicationContext(),
                            Plus.AccountApi.getAccountName(mGoogleApiClient),
                            "oauth2:"+Scopes.PLUS_ME);
                    return accessToken;
                } catch (IOException transientEx) {
                    // network or server error, the call is expected to succeed if you try again later.
                    // Don't attempt to call again immediately - the request is likely to
                    // fail, you'll hit quotas or back-off.
                    //TODO:
                    return "IOException";
                } catch (UserRecoverableAuthException e) {
                    // Recover
                    return "UserRecoverableAuthException";
                } catch (GoogleAuthException authEx) {
                    // Failure. The call is not expected to ever succeed so it should not be
                    // retried.
                    return "GoogleAuthException";
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                TextView token = (TextView) findViewById(R.id.token);
                token.setText(result);
            }
        };

        new PostTask().execute((String)null);
    }
}

