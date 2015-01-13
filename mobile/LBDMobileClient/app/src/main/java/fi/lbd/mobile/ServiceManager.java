package fi.lbd.mobile;

import android.content.Context;
import android.content.Intent;

import fi.lbd.mobile.backendhandler.BackendHandlerService;
import fi.lbd.mobile.messaging.MessageUpdateService;

/**
 * Created by Ossi on 11.1.2015.
 *
 * Static class to manage connections to services.
 */
public final class ServiceManager {
    private static Context serviceContext;

    public static void initialize(Context context){
        serviceContext = context;
    }

    private ServiceManager(){}

    public static void startMessageService(){
        serviceContext.startService(new Intent(serviceContext, MessageUpdateService.class));
    }

    public static void startBackendService(){
        serviceContext.startService(new Intent(serviceContext, BackendHandlerService.class));
    }

    public static void stopBackendService(){
        serviceContext.stopService(new Intent(serviceContext, BackendHandlerService.class));
    }

    public static void stopMessageService(){
        serviceContext.stopService(new Intent(serviceContext, MessageUpdateService.class));
    }
}
