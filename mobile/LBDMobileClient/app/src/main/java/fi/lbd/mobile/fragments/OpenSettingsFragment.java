package fi.lbd.mobile.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;

import java.io.IOException;

import fi.lbd.mobile.R;
import fi.lbd.mobile.SettingsActivity;

/**
 * Fragment that only contains a button to open SettingsActivity.
 *
 * Created by Ossi on 14.1.2015.
 */
public class OpenSettingsFragment extends Fragment{
    public static OpenSettingsFragment newInstance() {
        return new OpenSettingsFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_open_settings, container, false);

        view.findViewById(R.id.openSettingsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.invalidateOauthToken).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AccountManager accountManager = AccountManager.get(getActivity().getBaseContext());
                        Account[] accounts = accountManager.getAccountsByType("com.google");
                        Account account;
                        if (accounts.length > 0) {
                            account = accounts[0];
                        } else {
                            account = null;
                        }
                        Log.d("******", account.toString());
                        String email = account.name;
                        String token = "";
                        String scope = "oauth2:" + Scopes.PROFILE;
                        String gid = "";
                        try {
                            token = GoogleAuthUtil.getToken(getActivity().getApplicationContext(), email, scope);
                            Log.d("******", token);
                            GoogleAuthUtil.clearToken(getActivity().getApplicationContext(), token);
                        } catch (UserRecoverableAuthException userRecoverableException) {
                            // GooglePlayServices.apk is either old, disabled, or not present, which is
                            // recoverable, so we need to show the user some UI through the activity.

        //                startActivityForResult(new myActivity(), userRecoverableException.getIntent(),
        //                        REQUEST_AUTHORIZATION, new Bundle());
                            Log.d("******", userRecoverableException.toString());
                        } catch (GoogleAuthException fatalException) {
                            Log.d("******", fatalException.toString());
                            Log.d("******", fatalException.getMessage());
                        } catch (IOException io) {
                            Log.d("******", "Blah3");
                        }
                    }
                });
                t.start();
            }
        });

        return view;
    }
}
