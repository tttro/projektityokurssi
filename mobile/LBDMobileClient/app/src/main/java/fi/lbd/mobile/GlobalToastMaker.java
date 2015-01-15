package fi.lbd.mobile;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Ossi on 14.1.2015.
 */
public final class GlobalToastMaker {
    private static Context context;

    private GlobalToastMaker(){}

    public static void initialize(Context appContext){
        context = appContext;
    }

    public static void makeShortToast(String message){
        if(message != null) {
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, message, duration).show();
        }
    }

    public static void makeLongToast(String message){
        if(message != null) {
            int duration = Toast.LENGTH_LONG;
            Toast.makeText(context, message, duration).show();
        }
    }
}
