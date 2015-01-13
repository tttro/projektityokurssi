package fi.lbd.mobile;

import android.util.Log;

/**
 * Created by Ossi on 11.1.2015.
 *
 * Class that tracks whether the whole application is moved to background or foreground, and
 * starts and stops services accordingly.
 */
public class ActiveActivitiesTracker {
    private static int activeActivities = 0;

    public static void activityStarted()
    {
        if( activeActivities == 0 )
        {
            Log.d("Going to foreground", " starting services...");
            ServiceManager.startMessageService();
            ServiceManager.startBackendService();
        }
        activeActivities++;
       // Log.d("Number of active activities: ", Integer.toString(activeActivities));
    }

    public static void activityStopped()
    {
        activeActivities--;
        if( activeActivities == 0 )
        {
            Log.d("Going to background", " stopping services...");
            ServiceManager.stopMessageService();
            ServiceManager.stopBackendService();
        }
        //Log.d("Number of active activities: ", Integer.toString(activeActivities));
    }
}