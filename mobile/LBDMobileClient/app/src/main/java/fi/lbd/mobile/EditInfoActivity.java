package fi.lbd.mobile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.debug.hv.ViewServer;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.ImmutableMapObject;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.MapObjectSelectionManager;
import fi.lbd.mobile.mapobjects.events.UpdateMapObjectEvent;
import fi.lbd.mobile.mapobjects.events.UpdateMapObjectSucceededEvent;

/**
 *
 * Activity to allow editing the "Info" field of an individual object
 *
 * Created by Ossi.
 */
public class EditInfoActivity extends Activity {
    private boolean acceptButtonPressed = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        MapObject object = MapObjectSelectionManager.get().getSelectedMapObject();
        if (object != null) {
            String oldInfo = object.getMetadataProperties().get("info");
            if(oldInfo != null && !oldInfo.isEmpty()) {
                ((EditText) findViewById(android.R.id.content).findViewById(R.id.textViewEdit)).setText(oldInfo);
            }
        }
        ViewServer.get(this).addWindow(this);
    }

    @Override
    public void onStart(){
        super.onStart();
        ActiveActivitiesTracker.activityStarted(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        this.acceptButtonPressed = false;
        ActiveActivitiesTracker.activityStopped(this);
        if(progressDialog != null && progressDialog.isShowing()){
            this.progressDialog.dismiss();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        BusHandler.getBus().register(this);
        ViewServer.get(this).setFocusedWindow(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ViewServer.get(this).removeWindow(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        BusHandler.getBus().unregister(this);
    }

    @Subscribe
    public void onEvent(UpdateMapObjectSucceededEvent event){
        this.acceptButtonPressed = false;
        if(progressDialog != null && progressDialog.isShowing()){
            this.progressDialog.dismiss();
        }
        onBackPressed();
    }

    @Subscribe
    public void onEvent(RequestFailedEvent event){
        if(event.getFailedEvent() instanceof UpdateMapObjectEvent){
            this.acceptButtonPressed = false;
            if(progressDialog != null && progressDialog.isShowing()){
                this.progressDialog.dismiss();
            }
            GlobalToastMaker.makeShortToast("Saving content failed");
        }
    }

    public void onAcceptClick(View view){
        if(!this.acceptButtonPressed) {
            MapObject object = MapObjectSelectionManager.get().getSelectedMapObject();
            if (object != null) {
                this.acceptButtonPressed = true;
                progressDialog = ProgressDialog.show(this, "", "Saving...", true);
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(false);
                EditText editText = (EditText) ((View) view.getParent().getParent()).findViewById(R.id.textViewEdit);
                String text = editText.getText().toString();
                if (text == null) {
                    text = "";
                }
                Map<String, String> metaData = new HashMap<>();
                metaData.put("status", "");
                metaData.put("info", text);
                BusHandler.getBus().post(new UpdateMapObjectEvent(new ImmutableMapObject(object.isMinimized(),
                        object.getId(), (ImmutablePointLocation)object.getPointLocation(),
                        object.getAdditionalProperties(), metaData)));
            }
        }
    }

    public void onCancelClick(View view){
        onBackPressed();
    }
}
