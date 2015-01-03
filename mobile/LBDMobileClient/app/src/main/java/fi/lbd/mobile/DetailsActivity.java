package fi.lbd.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import fi.lbd.mobile.adapters.ListDetailsAdapter;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.mapobjects.events.RequestMapObjectEvent;
import fi.lbd.mobile.mapobjects.events.ReturnMapObjectEvent;
import fi.lbd.mobile.mapobjects.MapObject;


public class DetailsActivity extends Activity {

    private ListDetailsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BusHandler.getBus().register(this);
        BusHandler.getBus().post(new RequestMapObjectEvent(MapObjectSelectionManager.get().getSelectedMapObject().getId()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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
                this.adapter = new ListDetailsAdapter(this, obj, obj
                        .getAdditionalProperties().size(), 1, 1);
                setContentView(R.layout.activity_details);
                ListView list = (ListView)findViewById(android.R.id.list);
                list.setAdapter(this.adapter);
            }
    }
}
