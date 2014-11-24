package fi.lbd.mobile.location;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

/**
 * Class which handles getting the users current location. Checking if the google play services
 * are available and displays error dialog if they are missing.
 *
 * Created by Tommi.
 */
public class LocationHandler implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private Activity activity;
    private LocationClient locationClient;

    /**
     * Checks if the google play services are available and if they are creates a location client.
     *
     * @param activity
     */
    public LocationHandler(Activity activity) {
        this.activity = activity;
        if (checkPlayServices()) {
            Log.d(this.getClass().getSimpleName(), "Google Play services is available.");
            this.locationClient = new LocationClient(this.activity, this, this);
        } else {
            Log.e(this.getClass().getSimpleName(), "Google Play services are not available.");
        }
    }

    /**
     * Checks if the google play services are available and if not, presents an error dialog.
     *
     * @return  True if services are available. False otherwise.
     */
    protected boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this.activity,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
            return false;
        }
        return true;
    }

    /**
     * Starts the location client.
     */
    public void start() {
        if (this.locationClient != null) {
            this.locationClient.connect();
        }
    }

    /**
     * Stops the location client.
     */
    public void stop() {
        if (this.locationClient != null) {
            this.locationClient.disconnect();
        }
    }

    /**
     * Gets the users current location or null if the location can't be resolved.
     *
     * @return
     */
    public ImmutablePointLocation getLastLocation() {
        if (this.locationClient == null) {
            return null;
        }
        Location location = this.locationClient.getLastLocation();
        return (location != null) ? new ImmutablePointLocation(location.getLatitude(), location.getLongitude()) : null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(this.getClass().getSimpleName(), "Location client connected.");
    }

    @Override
    public void onDisconnected() {
        Log.d(this.getClass().getSimpleName(), "Location client disconnected.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            Log.e(this.getClass().getSimpleName(), "Google Play services connection failed. Trying to resolve errors.");
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this.activity,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e(this.getClass().getSimpleName(), "Error when trying to resolve errors. ", e);
            }
        } else {
            Log.e(this.getClass().getSimpleName(), "Google Play services encountered an error which could not be resolved.");
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this.activity,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
        }
    }
}
