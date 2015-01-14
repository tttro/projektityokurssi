package fi.lbd.mobile;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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


public class EditNoteActivity extends Activity {
    private boolean acceptButtonPressed = false;

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
    }

    @Override
    public void onStart(){
        super.onStart();
        ActiveActivitiesTracker.activityStarted();
    }

    @Override
    public void onStop(){
        super.onStop();
        this.acceptButtonPressed = false;
        ActiveActivitiesTracker.activityStopped();
    }

    @Override
    public void onResume(){
        super.onResume();
        BusHandler.getBus().register(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        BusHandler.getBus().unregister(this);
    }

    @Subscribe
    public void onEvent(UpdateMapObjectSucceededEvent event){
        this.acceptButtonPressed = false;
        onBackPressed();
    }

    @Subscribe
    public void onEvent(RequestFailedEvent event){
        if(event.getFailedEvent() instanceof UpdateMapObjectEvent){
            this.acceptButtonPressed = false;
            makeShortToast("Saving content failed");
        }
    }

    public void onAcceptClick(View view){
        if(!this.acceptButtonPressed) {
            MapObject object = MapObjectSelectionManager.get().getSelectedMapObject();
            if (object != null) {
                this.acceptButtonPressed = true;
                EditText editText = (EditText) ((View) view.getParent().getParent()).findViewById(R.id.textViewEdit);
                String text = editText.getText().toString();
                if (text == null) {
                    text = "";
                }
                Map<String, String> metaData = new HashMap<>();
                metaData.put("status", "");
                metaData.put("info", text);
                Log.d("???????", "Posting new metadataevent");
                BusHandler.getBus().post(new UpdateMapObjectEvent(new ImmutableMapObject(object.isMinimized(),
                        object.getId(), (ImmutablePointLocation)object.getPointLocation(),
                        object.getAdditionalProperties(), metaData)));
            }
        }
    }

    public void onCancelClick(View view){
        onBackPressed();
    }

    private void makeShortToast(String message){
        if(message != null) {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, message, duration).show();
        }
    }
}
