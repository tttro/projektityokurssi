package fi.lbd.mobile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import fi.lbd.mobile.adapters.ListDetailsAdapter;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.mapobjects.MapObjectSelectionManager;
import fi.lbd.mobile.mapobjects.events.RequestMapObjectEvent;
import fi.lbd.mobile.mapobjects.events.ReturnMapObjectEvent;
import fi.lbd.mobile.mapobjects.MapObject;


public class DetailsActivity extends Activity {
    private ListDetailsAdapter adapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        progressDialog = ProgressDialog.show(this, "", "Loading object...", true);
        progressDialog.setCancelable(false);
        BusHandler.getBus().register(this);
        BusHandler.getBus().post(new RequestMapObjectEvent(MapObjectSelectionManager.get().getSelectedMapObject().getId()));
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHandler.getBus().unregister(this);
    }

    @Override
    public void onStart(){
        super.onStart();
        ActiveActivitiesTracker.activityStarted();
    }

    @Override
    public void onStop(){
        super.onStop();
        ActiveActivitiesTracker.activityStopped();
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
            // Update selected object in case of new data
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

        Context context = getApplicationContext();
        CharSequence dialogText = "Object id copied to clipboard";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, dialogText, duration).show();
    }
}
