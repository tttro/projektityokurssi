package fi.lbd.mobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import fi.lbd.mobile.R;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Created by Ossi on 19.11.2014.
 */

public class ListBriefDetailsAdapter extends BaseAdapter {
    private final String METADATA = "metadata";
    private final String TYHJA = "Tyhjä";
    private final String LISATIETOJA = "LISÄTIETOJA";
    private final String SIJAINTI = "SIJAINTI";
    private int maxBriefDetails;
    private MapObject object;
    private ArrayList<Map.Entry<String,String>> additionalProperties;
    private Context context;

    public void setObject(MapObject mapObject){
        this.object = mapObject;
        if(mapObject != null) {
            Log.d(" number of additional properties _________", ((Integer)object.getAdditionalProperties().size()).toString());
            int i = 1;
            for (Map.Entry<String, String> entry : object.getAdditionalProperties().entrySet()) {
                if (i < maxBriefDetails) {
                    this.additionalProperties.add(entry);
                }
            }
          // this.additionalProperties.add(object.getAdditionalProperties();
        }
    }

    public ListBriefDetailsAdapter(Context context, MapObject mapObject, int maxDetails) {
        this.context = context;
        this.additionalProperties = new ArrayList<Map.Entry<String,String>>();
        maxBriefDetails = maxDetails;
        setObject(mapObject);
    }

    @Override
    public int getCount() {
        // Location, additional properties
        //return (this.object.getAdditionalProperties()).size() + 1;
        return maxBriefDetails;
    }

    @Override
    public Object getItem(int i) {
        if (i == 0){
            return object.getPointLocation();
        }
        else if (i > 0 && i < getCount() - 1) {
            return additionalProperties.get(i).getValue();
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            LayoutInflater inflater = ((Activity) this.context).getLayoutInflater();
            view = inflater.inflate(R.layout.listview_double_row, viewGroup, false);
        }
        if (i == 0) {
            TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
            textViewId.setText(SIJAINTI);

            TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
            textViewLocation.setText(object.getPointLocation().toString());
        }
        else if (i > 0 && i < getCount()){
            String key = additionalProperties.get(i-1).getKey();
            if (key == METADATA){
                key = LISATIETOJA;
            }
            TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
            textViewId.setText(key);

            String value = additionalProperties.get(i-1).getValue();
            if (value == null || value.isEmpty()){
                value = TYHJA;
            }
            TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
            textViewLocation.setText(value);
        }
        return view;
    }

    public void setMaxBriefDetails(int maxDetails){
        this.maxBriefDetails = maxDetails;
    }
}
