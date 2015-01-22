package fi.lbd.mobile.backendhandler;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;

import java.io.IOException;

/**
 * Provides authentication details from google.
 *
 * Created by Aki & Tommi.
 */
public class GoogleAuthProvider implements AuthProvider {
    private Context context;

    public GoogleAuthProvider(Context context) {
        this.context = context;
    }

    @Override
    public Pair<String, String> getIdToken() {
        AccountManager accountManager = AccountManager.get(this.context);
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        Log.d(this.getClass().getSimpleName(), "Current user account: "+ account.toString());
        String email = account.name;
        String token = "foo";
        String scope = "oauth2:" + Scopes.PROFILE;
        String gid = "empty";
        try {
            token = GoogleAuthUtil.getToken(context, email, scope);
            gid = GoogleAuthUtil.getAccountId(context, email);
            Log.d(this.getClass().getSimpleName(), token);
        } catch (UserRecoverableAuthException userRecoverableException) {
            // GooglePlayServices.apk is either old, disabled, or not present, which is
            // recoverable, so we need to show the user some UI through the activity.

//                startActivityForResult(new myActivity(), userRecoverableException.getIntent(),
//                        REQUEST_AUTHORIZATION, new Bundle());
            Log.d(this.getClass().getSimpleName(), userRecoverableException.toString()); // TODO!
        } catch (GoogleAuthException fatalException) {
            Log.e(this.getClass().getSimpleName(), "Fatal authorization exception!", fatalException);
        } catch (IOException io) {
            Log.e(this.getClass().getSimpleName(), "IOException", io);
        }
        Log.i(this.getClass().getSimpleName(), "USER ID: "+ gid);
        return new Pair<>(gid, token);
    }
}
