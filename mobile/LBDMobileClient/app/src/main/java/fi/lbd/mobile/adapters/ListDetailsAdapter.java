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

public class ListDetailsAdapter extends BaseAdapter {
    private final static String EMPTY = "Empty";
    private final static String LOCATION = "LOCATION";

    // Number of properties, metadata items and coordinate points contained in each object
    private int amountOfAdditionalProperties = 0;
    private int amountOfMetaDataProperties = 0;
    private int amountOfCoordinates = 0;

    private MapObject object;
    private ArrayList<Map.Entry<String,String>> additionalProperties;
    private ArrayList<Map.Entry<String,String>> metaDataProperties;
    private Context context;

    public void setObject(MapObject mapObject){
        this.object = mapObject;
        if(mapObject != null) {
            int i = 1;
            for (Map.Entry<String, String> entry : object.getAdditionalProperties().entrySet()) {
                if (i <= amountOfAdditionalProperties) {
                    this.additionalProperties.add(entry);
                }
            }
            i = 1;
            for (Map.Entry<String, String> entry : object.getMetadataProperties().entrySet()) {
                if (i <= amountOfMetaDataProperties) {
                        this.metaDataProperties.add(entry);
                }
            }
        }
    }

    public ListDetailsAdapter(Context context, MapObject mapObject, int additionalProperties,
                              int coordinates, int metaDataProperties) {
        this.context = context;
        this.additionalProperties = new ArrayList<Map.Entry<String,String>>();
        this.metaDataProperties = new ArrayList<Map.Entry<String,String>>();
        setAmountOfProperties(mapObject, additionalProperties, coordinates, metaDataProperties);
        setObject(mapObject);
    }

    @Override
    public int getCount() {
        return amountOfAdditionalProperties + amountOfMetaDataProperties + amountOfCoordinates;
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

        if (i >= 0 && i < amountOfAdditionalProperties){
            String key = additionalProperties.get(i).getKey();
            if (key == null || key.isEmpty()){
                key = EMPTY;
            }
            TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
            textViewId.setText(key);

            String value = additionalProperties.get(i).getValue();
            if (value == null || value.isEmpty()){
                value = EMPTY;
            }
            TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
            textViewLocation.setText(value);
        }
        else if (i == amountOfAdditionalProperties && amountOfCoordinates==1){
            TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
            textViewId.setText(LOCATION);

            TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
            textViewLocation.setText(object.getPointLocation().toString());
        }
        else if(i >= amountOfAdditionalProperties && i-amountOfAdditionalProperties
                -amountOfCoordinates < amountOfMetaDataProperties){
            int metaDataIndex = i-amountOfAdditionalProperties-amountOfCoordinates;

            String key = metaDataProperties.get(metaDataIndex).getKey();
            if (key == null || key.isEmpty()){
                key = EMPTY;
            }
            TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
            textViewId.setText(key);

            String value = additionalProperties.get(metaDataIndex).getValue();
            if (value == null || value.isEmpty()){
                value = EMPTY;
            }
            TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
            textViewLocation.setText(value);
        }
        return view;
    }

    public void setAmountOfProperties(MapObject object, int additionalProperties,
                                      int coordinateObjects, int metaDataProperties){
        if(object.getAdditionalProperties().size() >= additionalProperties) {
            this.amountOfAdditionalProperties = additionalProperties;
        }
        else {
            this.amountOfAdditionalProperties = object.getAdditionalProperties().size();
        }
        if(object.getMetadataProperties().size() >= metaDataProperties){
            this.amountOfMetaDataProperties = metaDataProperties;
        }
        else {
            this.amountOfMetaDataProperties = object.getMetadataProperties().size();
        }
        if(coordinateObjects != 1){
            this.amountOfCoordinates = 0;
        }
        else {
            this.amountOfCoordinates = 1;
        }
    }
}
