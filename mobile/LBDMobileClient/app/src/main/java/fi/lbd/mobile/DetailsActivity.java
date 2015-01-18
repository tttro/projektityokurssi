package fi.lbd.mobile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import fi.lbd.mobile.adapters.ListDetailsAdapter;
import fi.lbd.mobile.backendhandler.BackendHandlerService;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestCollectionsEvent;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.mapobjects.MapObjectSelectionManager;
import fi.lbd.mobile.mapobjects.events.RequestMapObjectEvent;
import fi.lbd.mobile.mapobjects.events.ReturnMapObjectEvent;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 *
 * Activity to show details of an individual object.
 *
 * Created by Ossi.
 */
public class DetailsActivity extends Activity {
    private ListDetailsAdapter adapter;
    private ProgressDialog progressDialog;
    private ServiceConnection backendHandlerConnection;
    private Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backendHandlerConnection = new BackendHandlerConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        BusHandler.getBus().register(this);
        activity.bindService(new Intent(activity, BackendHandlerService.class),
                backendHandlerConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHandler.getBus().unregister(this);

        // Try to unbind binded service to ensure no binds are leaked when going to pause
        try {
            activity.unbindService(backendHandlerConnection);
        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(), " onPause() trying to unbind a non-existing bind.");
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        ActiveActivitiesTracker.activityStarted(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        ActiveActivitiesTracker.activityStopped(this);
        if(progressDialog != null && progressDialog.isShowing()){
            this.progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Subscribe
    public void onEvent (ReturnMapObjectEvent event){
        final MapObject obj = event.getMapObject();

        if (obj != null) {
            // Update selected object to SelectionManager in case of new data was received
            MapObjectSelectionManager.get().setSelectedMapObject(event.getMapObject());
            this.adapter = new ListDetailsAdapter(this, obj, obj
                    .getAdditionalProperties().size(), 1, obj.getMetadataProperties().size());
            setContentView(R.layout.activity_details);
            ListView list = (ListView) findViewById(android.R.id.list);
            list.setAdapter(this.adapter);

            // Listen to edit button press
            findViewById(R.id.editNoteButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editNote();
                }
            });

            // Listen to copy button press
            findViewById(R.id.copyButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    copyId(obj.getId());
                }
            });
        }
        if(progressDialog != null && progressDialog.isShowing()){
            this.progressDialog.dismiss();
        }
    }

    @Subscribe
    public void onEvent(RequestFailedEvent event){
        if(event.getFailedEvent() instanceof RequestMapObjectEvent){
            if(progressDialog != null && progressDialog.isShowing()){
                this.progressDialog.dismiss();
            }
            onBackPressed();
        }
    }

    private void editNote(){
        Intent intent = new Intent(this, EditInfoActivity.class);
        startActivity(intent);
    }

    private void copyId(String id){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("ID", id);
        clipboard.setPrimaryClip(clip);

        CharSequence dialogText = "Object id copied to clipboard";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(this, dialogText, duration).show();
    }

    /**
     *  Connection that represents binding to BackendHandlerService.
     *  Only used to provide onServiceConnected callback.
     */
    private class BackendHandlerConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            progressDialog = ProgressDialog.show(activity, "", "Loading object...", true);
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            Log.d(getClass().toString(), " onServiceConnected, sending RequestObjectEvent, and unbinding");
            BusHandler.getBus().post(new RequestMapObjectEvent(MapObjectSelectionManager.get().getSelectedMapObject().getId()));
            activity.unbindService(this);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };
}
