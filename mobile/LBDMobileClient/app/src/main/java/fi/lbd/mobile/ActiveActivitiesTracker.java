package fi.lbd.mobile;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Ossi on 11.1.2015.
 *
 * Class that tracks whether the whole application is moved to background or foreground, and
 * starts and stops services accordingly.
 */
public class ActiveActivitiesTracker {
    private static int activeActivities = 0;

    public static Activity getLatestActiveActivity() {
        return latestActiveActivity;
    }

    private static Activity latestActiveActivity;

    public static void activityStarted(Activity activity)
    {
        latestActiveActivity = activity;
        if( activeActivities == 0 ) {
            Log.d("Activity: " + activity.getClass().toString() + " going to foreground", " starting services...");

            String backend_url = activity.getResources().getString(R.string.backend_url);
            String object_collection = activity.getResources().getString(R.string.object_collection);
            SharedPreferences settings = activity.getApplication().getApplicationContext()
                    .getSharedPreferences(activity.getString(R.string.shared_preferences), Activity.MODE_PRIVATE);
            String url = settings.getString(backend_url, null);
            String collection = settings.getString(object_collection, null);

            // If previous settings were found, update them to ApplicationDetails
            if(url != null && collection != null){
                Log.d("---------------", "Updated applicationdetails" + "url " + url + "collection " + collection);
                ApplicationDetails.get().setCurrentBaseApiUrl(url);
                ApplicationDetails.get().setCurrentCollection(collection);
            }

            ServiceManager.startMessageService();
            ServiceManager.startBackendService();
        }
        activeActivities++;
       // Log.d("Number of active activities: ", Integer.toString(activeActivities));
    }

    public static void activityStopped(Activity activity){
        activeActivities--;
        if( activeActivities == 0 ){
            Log.d("Activity: " + activity.getClass().toString() + " going to background", " stopping services...");
            ServiceManager.stopMessageService();
            ServiceManager.stopBackendService();
        }
        if (latestActiveActivity == activity) {
            latestActiveActivity = null;
        }
        //Log.d("Number of active activities: ", Integer.toString(activeActivities));
    }
}