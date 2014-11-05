package fi.lbd.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import fi.lbd.mobile.mapobjects.MapObject;


public class DetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Intent i = this.getIntent();
        MapObject o = (MapObject)i.getSerializableExtra("selectedObject");

        if(o != null) {
            TextView text = (TextView) findViewById(R.id.detailsText);
            text.setText("This is a detail view for object    " + o.getId());
        }
        else {
            TextView text = (TextView) findViewById(R.id.detailsText);
            text.setText("Not working");
        }
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
}
