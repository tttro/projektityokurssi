package fi.lbd.mobile.authorization;

import android.app.Activity;
import android.util.Pair;

/**
 * Listens for actions which are happening during authentication.
 * Created by Tommi on 23.1.2015.
 */
public interface AuthorizationListener {
    /**
     * Returns an activity which should be used to resolve issues.
     * @return
     */
    Activity getActivity();

    /**
     * Invoked when token is successfully acquired.
     * @param token
     */
    void onTokenSuccess(Authorization.AuthResult token);

    /**
     * Invoked when the process is waiting for another activity.
     */
    void onWaitingForResult();

    /**
     * Something unexpected has occurred.
     */
    void onFatalException();

    /**
     * The latest thing that has been requested from another activity.
     * @param currentRequestCode
     */
    void setCurrentRequestCode(int currentRequestCode);
}
