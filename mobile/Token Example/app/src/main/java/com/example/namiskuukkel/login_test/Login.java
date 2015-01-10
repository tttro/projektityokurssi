package com.example.namiskuukkel.login_test;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;


public class Login extends Activity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;
    private static final int USER_AUTH_EX = 1;

    //Related to USER_AUTH_EX; Do not ask user to resolve that exception multiple times
    private boolean mSecondTry;

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

    private String token;
    private String id;
    private String error;

    public void onCreate(Bundle savedInstanceState) {

        token = "";
        id = "";
        error = "";
        mSecondTry = false;

        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();

    }


    public void onClick(View view) {

        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }

    }

    protected void onStart() {
        super.onStart();
        if(isOnline()) {
            mGoogleApiClient.connect();
        } else {
            TextView status = (TextView) findViewById(R.id.status);
            status.setText(R.string.network_error);
        }
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void finish() {
        Log.d("Finish", "Finishing");
        // Prepare data intent
        Intent data = new Intent();
        if (!error.equals("")) {
            //There was an error message. Pass it back to the caller to be showed to the user
            data.putExtra("error", error);
            setResult(RESULT_CANCELED, data);
            super.finish();
        }
        else {
            //Everything is okay. Return token and id.
            data.putExtra("token", token);
            data.putExtra("id", id);
            // Activity finished ok, return the data
            setResult(RESULT_OK, data);
            super.finish();
        }
    }

    public void onConnectionFailed(ConnectionResult result) {
        //Show the UI at this point. Before this user can be automatically logged in and doesn't
        //need to see anything
        setContentView(R.layout.sign_in_activity);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
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
        mSignInClicked = false;
        getAndUseAuthTokenInAsyncTask();

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
        } else if (requestCode == USER_AUTH_EX) {
            if (responseCode == RESULT_OK) {
                mSecondTry = true;
                getAndUseAuthTokenInAsyncTask();
            } else {
                error = "Authorization denied by user";
                finish();
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

    private void getAndUseAuthTokenInAsyncTask() {
        class PostTask extends AsyncTask<String, Integer, Void> {
            @Override
            protected Void doInBackground(String... params) {

                try {
                    //TODO: Check the scope! Should be the same everywhere.
                    //Now web is different from this
                    token = GoogleAuthUtil.getToken(getApplicationContext(),
                            Plus.AccountApi.getAccountName(mGoogleApiClient),
                            "oauth2:"+ Scopes.PLUS_ME);

                } catch (IOException transientEx) {
                    // network or server error, the call is expected to succeed if you try again later.
                    // Don't attempt to call again immediately - the request is likely to
                    // fail, you'll hit quotas or back-off.
                    error = "There was an error. Please try again.";
                    finish();
                    return null;
                } catch (UserRecoverableAuthException e) {
                    // TODO: Test this! Somehow...
                    //UserRecoverableAuthException should be caused by user not logged in or user
                    //not granting the app permissions
                    Log.d("PostTask", "UserRecoverableAuthException");
                    if (!mSecondTry) {
                        startActivityForResult(e.getIntent(), USER_AUTH_EX);
                    } else {
                        error = "Multiple approval attempts";
                        finish();
                        return null;
                    }
                } catch (GoogleAuthException authEx) {
                    // Failure. The call is not expected to ever succeed so it should not be
                    // retried.
                    error = "Error of type GoogleAuthException happened. Contact support personnel.";
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                //For some unknown reason this has sometimes been null...
                try {
                    Person me = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                    id = me.getId();
                    if (id.equals("")) {
                        error = "Google user ID could not be retrieved.";
                        finish();
                        return null;
                    }
                    finish();
                    return null;
                } catch (NullPointerException e) {

                    Log.d("Errr", e.toString());
                    error = "Could not resolve the current person logged in with Google API";
                    finish();
                    return null;
                }
            }
        };

        if (isOnline()) {
            new PostTask().execute((String) null);
        }
        else {
            TextView token = (TextView) findViewById(R.id.token);
            token.setText(R.string.network_error);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}