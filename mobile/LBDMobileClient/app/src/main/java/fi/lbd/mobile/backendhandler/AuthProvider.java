package fi.lbd.mobile.backendhandler;

import android.util.Pair;

/**
 * Interface for defining auth providers.
 *
 * Created by Tommi on 20.1.2015.
 */
public interface AuthProvider {
    /**
     * Returns a pair which contains user id and an authentication token.
     * @return
     */
    Pair<String, String> getIdToken();
}
