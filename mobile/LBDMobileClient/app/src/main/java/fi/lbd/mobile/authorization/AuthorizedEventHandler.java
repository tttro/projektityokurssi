package fi.lbd.mobile.authorization;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import fi.lbd.mobile.ActiveActivitiesTracker;
import fi.lbd.mobile.GlobalToastMaker;
import fi.lbd.mobile.events.BusHandler;

/**
 * Adds authentication information to events which are posted to OTTO-bus. Posted event must extend
 * AuthorizedEvent class and every activity must call "processResults" -method.
 *
 * Created by Tommi on 23.1.2015.
 */
public class AuthorizedEventHandler {
    private static final Set<UnresolvedAuthorizedEvent> unresolvedAuthorizedEvents = Sets.newConcurrentHashSet();

    /**
     * !!!SHOULD NOT BE CALLED FROM ANYWHERE ELSE THAN WrappedBus -class!!!
     * @param event
     */
    public static void process(AuthorizedEvent event) {
        UnresolvedAuthorizedEvent unresolvedAuthorizedEvent = new UnresolvedAuthorizedEvent(event);
        unresolvedAuthorizedEvents.add(unresolvedAuthorizedEvent);
        if (unresolvedAuthorizedEvent.getState() == UnresolvedAuthorizedEvent.State.EXCEPTION) {

        }
    }

    /**
     * Should be called from every activities "onActivityResult" -method.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public static void processResults(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.e(AuthorizedEventHandler.class.getSimpleName(), "User cancelled dialog! requestCode: "+ requestCode);
            GlobalToastMaker.makeLongToast("Certain features of the application are disabled until a proper account is selected.");
        }
        if (data == null) {
            return;
        }

        Iterator<UnresolvedAuthorizedEvent> iter = unresolvedAuthorizedEvents.iterator();
        while (iter.hasNext()) {
            UnresolvedAuthorizedEvent unresolvedAuthorizedEvent = iter.next();
            if (unresolvedAuthorizedEvent.getCurrentRequestCode() == requestCode) {
                Authorization.resolveResult(unresolvedAuthorizedEvent.getAuthorizationListener(), requestCode, resultCode, data);
                if (unresolvedAuthorizedEvent.getState() == UnresolvedAuthorizedEvent.State.FINISHED) {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Used inside AuthorizedEventHandler to keep track of events which have been processed and
     * which are still unresolved.
     */
    private static class UnresolvedAuthorizedEvent {
        private enum State {NOT_READY, FINISHED, WAITING_RESULT, EXCEPTION}

        private final AuthorizationListener authorizationListener;
        private AuthorizedEvent authorizedEvent;
        private State state = State.NOT_READY;

        private int requestCode;

        public UnresolvedAuthorizedEvent(AuthorizedEvent event) {
            this.authorizedEvent = event;
            this.authorizationListener = new AuthorizationListener() {
                @Override
                public Activity getActivity() {
                    return ActiveActivitiesTracker.getLatestActiveActivity();
                }

                @Override
                public void onTokenSuccess(Authorization.AuthResult result) {
                    authorizedEvent.setHeaders(result.getId(), result.getToken());
                    BusHandler.getBus().post(authorizedEvent, false);
                    state = State.FINISHED;
                }

                @Override
                public void onWaitingForResult() {
                    state = State.WAITING_RESULT;
                }

                @Override
                public void onFatalException() {
                    state = State.EXCEPTION;
                }

                @Override
                public void setCurrentRequestCode(int currentRequestCode) {
                    requestCode = currentRequestCode;
                }

            };
            Authorization.getToken(this.authorizationListener);
        }

        public int getCurrentRequestCode() {
            return requestCode;
        }

        public AuthorizationListener getAuthorizationListener() {
            return authorizationListener;
        }

        public State getState() {
            return this.state;
        }
    }
}
