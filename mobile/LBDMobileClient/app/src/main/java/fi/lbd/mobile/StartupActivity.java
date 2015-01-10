package fi.lbd.mobile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import fi.lbd.mobile.backendhandler.BackendHandlerService;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.messaging.MessageUpdateService;


public class StartupActivity extends Activity {
    private String BACKEND_URL = "";
    private String OBJECT_COLLECTION = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start the object repository service. // TODO: Missä käynnistys?
        //startService(new Intent(this, BackendHandlerService.class));
       // startService(new Intent(this, MessageUpdateService.class));

        BACKEND_URL = getResources().getString(R.string.backend_url);
        OBJECT_COLLECTION = getResources().getString(R.string.object_collection);

        SharedPreferences settings = getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
        String url = settings.getString(BACKEND_URL, null);
        String collection = settings.getString(OBJECT_COLLECTION, null);

        // If no previous settings were found, move the user to SettingsActivity
        if(url == null || collection == null){
            finish();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        // Else go straight to ListActivity
        else {
            finish();
            ApplicationDetails.get().setCurrentCollection(collection);
            ApplicationDetails.get().setCurrentBackendUrl(url);
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
