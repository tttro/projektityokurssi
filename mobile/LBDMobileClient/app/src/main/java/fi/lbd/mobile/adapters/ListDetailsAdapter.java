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
import fi.lbd.mobile.SelectionManager;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Created by Ossi on 20.10.2014.
 */

public class ListDetailsAdapter extends BaseAdapter {
    private MapObject object;
    private ArrayList<Map.Entry<String,String>> additionalProperties;
    private Context context;

    public void setObject(MapObject mapObject){
        this.object = mapObject;
        if(mapObject != null) {
            Log.d(mapObject.getId(), "_________");

            for (Map.Entry<String, String> entry : object.getAdditionalProperties().entrySet()) {
                Log.d("1. " + entry.getKey(), "_________");
                Log.d("2. " + entry.getValue(), "_________");
                additionalProperties.add(entry);
            }
        }
    }

    public ListDetailsAdapter(Context context, MapObject mapObject) {
        this.context = context;
        additionalProperties = new ArrayList<Map.Entry<String,String>>();
        setObject(mapObject);
    }

    @Override
    public int getCount() {
        // ID, Location, additional properties
        Log.d(Integer.toString(this.object.getAdditionalProperties().size() + 1), "_________");
        return (this.object.getAdditionalProperties()).size() + 1;
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
            textViewId.setText(object.getId());
            textViewId.setTag(object.getId());

            TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
            textViewLocation.setText(object.getPointLocation().toString());
            textViewLocation.setTag(object.getId());
        }
        else if (i > 0){
            TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
            textViewId.setText(additionalProperties.get(i-1).getKey());

            TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
            textViewLocation.setText(additionalProperties.get(i-1).getValue());
        }
        return view;
    }
}

