package fi.lbd.mobile.authorization;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.Scopes;

import java.io.IOException;

import fi.lbd.mobile.ApplicationDetails;
import fi.lbd.mobile.R;

/**
 * Provides static methods to help in authentication. "getToken" -method performs the login procedure
 * "resolveResult" -method should be invoked from the calling activity.
 *
 * Created by Tommi on 23.1.2015.
 */
public class Authorization {
//    private static String savedUserAccount;
    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;

    /**
     * Starts fetching for token. Listener is informed about the current state of the process and
     * also the resolved token is passed to listener.
     * @param authorizationListener
     */
    public static void getToken(@NonNull AuthorizationListener authorizationListener) {
        SharedPreferences settings = null;
        String userAccount = null;

        if (authorizationListener.getActivity() == null) {
            Log.e(Authorization.class.getSimpleName(), "Activity on authorization listener was null!");
            authorizationListener.onFatalException();
            return;
        }

        if (ApplicationDetails.get().getUserEmail() == null) {
            settings = authorizationListener.getActivity().getApplication().getApplicationContext()
                    .getSharedPreferences(authorizationListener.getActivity().getString(R.string.shared_preferences), Activity.MODE_PRIVATE);
            userAccount = authorizationListener.getActivity().getResources().getString(R.string.user_account);
            ApplicationDetails.get().setUserEmail(settings.getString(userAccount, null));
        }

        String email = Authorization.selectAccount(ApplicationDetails.get().getUserEmail(), authorizationListener, REQUEST_CODE_PICK_ACCOUNT);
        if (email != null) {
            if (settings == null) {
                settings = authorizationListener.getActivity().getApplication().getApplicationContext()
                        .getSharedPreferences(authorizationListener.getActivity().getString(R.string.shared_preferences), Activity.MODE_PRIVATE);
            }
            if (userAccount == null) {
                userAccount = authorizationListener.getActivity().getResources().getString(R.string.user_account);
            }
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(userAccount, email);
            editor.apply();
            Log.d(Authorization.class.getSimpleName(), "Stored user as default user: "+ email);

            ApplicationDetails.get().setUserEmail(email);
            Authorization.getTokenAsync(authorizationListener, ApplicationDetails.get().getUserEmail());
        }
    }

    /**
     * Resolves the results from the started activities if errors have occurred while fetching token.
     * @param authorizationListener
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public static void resolveResult(@NonNull AuthorizationListener authorizationListener, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT && resultCode == Activity.RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Log.d(Authorization.class.getSimpleName(), "Selected account from picker: " + accountName);

            SharedPreferences settings = authorizationListener.getActivity().getApplication().getApplicationContext()
                    .getSharedPreferences(authorizationListener.getActivity().getString(R.string.shared_preferences), Activity.MODE_PRIVATE);
            String userAccount = authorizationListener.getActivity().getResources().getString(R.string.user_account);

            SharedPreferences.Editor editor = settings.edit();
            editor.putString(userAccount, accountName);
            editor.apply();

            // Test if the chosen email works
            ApplicationDetails.get().setUserEmail(accountName);
            Authorization.getTokenAsync(authorizationListener, ApplicationDetails.get().getUserEmail());
            // When auth error is resolved => Try to get new token again.
        } else  if (requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR && resultCode ==  Activity.RESULT_OK) {
            // Test if the chosen email works
            Authorization.getTokenAsync(authorizationListener, ApplicationDetails.get().getUserEmail());
        } else {
            Log.d(Authorization.class.getSimpleName(), "onActivityResult: "+ resultCode);
            authorizationListener.onFatalException();
        }
    }

    /**
     * Selects an account from the accounts installed in the device. Chooses saved account if such
     * exists. Shows account picker dialog if phone has multiple accounts.
     * @param savedAccount
     * @param authorizationListener
     * @param requestCode
     * @return
     */
    private static String selectAccount(@Nullable String savedAccount, AuthorizationListener authorizationListener, int requestCode) {
        AccountManager accountManager = AccountManager.get(authorizationListener.getActivity().getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType("com.google");
        if (savedAccount != null) {
            for (Account account : accounts) {
                if (account.name.equals(savedAccount)) {
                    Log.d(Authorization.class.getSimpleName(), "Select account, picked savedAccount: "+ savedAccount);
                    return account.name;
                }
            }
        }
        if (accounts.length == 1) {
            Log.d(Authorization.class.getSimpleName(), "Select account, picked only existing account.");
            return accounts[0].name;
        }

        Log.d(Authorization.class.getSimpleName(), "Select account, show list.");
        authorizationListener.setCurrentRequestCode(requestCode);
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                false, null, null, null, null);
        authorizationListener.getActivity().startActivityForResult(intent, requestCode);
        return null;
    }

    /**
     * Tries to get token in async task.
     * @param authorizationListener
     * @param email
     */
    private static void getTokenAsync(@NonNull final AuthorizationListener authorizationListener, @NonNull final String email) {
        new AsyncTask<String, Integer, AuthResult>() {
            protected AuthResult doInBackground (String... email){
                return (Authorization.getAuthResult(authorizationListener, REQUEST_CODE_RECOVER_FROM_AUTH_ERROR, email[0]));
            }

            protected void onPostExecute(AuthResult result){
                // Got new token and id instantly from the provider => Move to next activity
                if (result.getStatus() == AuthResult.Status.SUCCESS) {
                    Log.d(Authorization.class.getSimpleName(), "Got id: "+ result.getId() + " token: "+ result.getToken());
                    authorizationListener.onTokenSuccess(result);

                    // If the provider opened an resolve exception activity, wait for result in another method.
                } else if (result.getStatus() == AuthResult.Status.WAIT_FOR_RESULT) {
                    Log.d(Authorization.class.getSimpleName(), "Waiting for result from resolving activity.");
                    authorizationListener.onWaitingForResult();

                    // If the provider got an unrecoverable exception
                } else if (result.getStatus() == AuthResult.Status.FATAL_EXCEPTION) {
                    Log.e(Authorization.class.getSimpleName(), "Fatal exception when trying to get token");
                    authorizationListener.onFatalException();
                }
            }
        }.execute(email);
    }

    /**
     * Tries to get token from google auth utils. If recoverable exception occurs, authorization listener
     * provides the activity which resolves the exception.
     * @param authorizationListener
     * @param requestCode
     * @param email
     * @return
     */
    private static AuthResult getAuthResult(@NonNull AuthorizationListener authorizationListener, int requestCode, @NonNull String email) {
        String token = null;
        String scope = "oauth2:" + Scopes.PROFILE;
        String gid = null;
        try {
            token = GoogleAuthUtil.getToken(authorizationListener.getActivity().getApplication().getApplicationContext(), email, scope);
            gid = GoogleAuthUtil.getAccountId(authorizationListener.getActivity().getApplication().getApplicationContext(), email);
        } catch (UserRecoverableAuthException userRecoverableException) {
            authorizationListener.setCurrentRequestCode(requestCode);
            authorizationListener.getActivity().startActivityForResult(userRecoverableException.getIntent(), requestCode);
            return new AuthResult(AuthResult.Status.WAIT_FOR_RESULT, null, null);
        } catch (GoogleAuthException fatalException) {
            Log.e(Authorization.class.getSimpleName(), "Fatal authorization exception!", fatalException);
            return new AuthResult(AuthResult.Status.FATAL_EXCEPTION, null, null);
        } catch (IOException io) {
            Log.e(Authorization.class.getSimpleName(), "IOException", io);
            return new AuthResult(AuthResult.Status.FATAL_EXCEPTION, null, null);
        }

        if (token == null || gid == null) {
            return new AuthResult(AuthResult.Status.UNKNOWN, gid, token);
        }
        return new AuthResult(AuthResult.Status.SUCCESS, gid, token);
    }


    /**
     * Result from the authentication.
     */
    public static class AuthResult {
        public static enum Status {SUCCESS, FATAL_EXCEPTION, WAIT_FOR_RESULT, UNKNOWN, SHOULD_RETRY}

        private final Status status;
        private final String id;
        private final String token;

        public AuthResult(Status status, String id, String token) {
            this.status = status;
            this.id = id;
            this.token = token;
        }

        public Status getStatus() {
            return status;
        }

        public String getToken() {
            return token;
        }

        public String getId() {
            return id;
        }
    }


}
