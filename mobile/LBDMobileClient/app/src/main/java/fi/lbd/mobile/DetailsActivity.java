package fi.lbd.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import fi.lbd.mobile.adapters.ListBriefDetailsAdapter;
import fi.lbd.mobile.adapters.ListDetailsAdapter;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestMapObjectEvent;
import fi.lbd.mobile.events.ReturnMapObjectEvent;
import fi.lbd.mobile.mapobjects.MapObject;


public class DetailsActivity extends Activity {

    private ListDetailsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusHandler.getBus().register(this);
        BusHandler.getBus().post(new RequestMapObjectEvent(SelectionManager.get().getSelectedObject().getId()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusHandler.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHandler.getBus().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Subscribe
    public void onEvent (ReturnMapObjectEvent event){
            MapObject obj = event.getMapObject();
            if (obj != null){
                this.adapter = new ListDetailsAdapter(this, obj);
                //adapter.setObject(obj);
                setContentView(R.layout.activity_details);
                ListView list = (ListView)findViewById(android.R.id.list);
                list.setAdapter(this.adapter);
            }
    }

    public void onFinderClick(View view){
        Intent intent = new Intent(this, FinderActivity.class);
        startActivity(intent);
    }
}
