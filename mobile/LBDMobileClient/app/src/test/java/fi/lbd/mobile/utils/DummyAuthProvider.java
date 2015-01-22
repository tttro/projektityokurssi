package fi.lbd.mobile.utils;

import android.util.Pair;

import fi.lbd.mobile.backendhandler.AuthProvider;

/**
 * Dummy class for providing authorization for the test classes.
 * Created by Tommi on 20.1.2015.
 */
public class DummyAuthProvider implements AuthProvider {
    @Override
    public Pair<String, String> getIdToken() {
        return new Pair<>("", "");
    }
}
