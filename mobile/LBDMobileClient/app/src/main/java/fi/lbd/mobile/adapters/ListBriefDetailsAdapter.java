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
    private final static String METADATA = "metadata";
    private final static String TYHJA = "Tyhjä";
    private final static String LISATIETOJA = "LISÄTIETOJA";
    private final static String SIJAINTI = "SIJAINTI";

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

    public ListBriefDetailsAdapter(Context context, MapObject mapObject, int additionalProperties,
                                   int coordinates, int metaDataProperties) {
        this.context = context;
        this.additionalProperties = new ArrayList<Map.Entry<String,String>>();
        this.metaDataProperties = new ArrayList<Map.Entry<String,String>>();
        setAmountOfProperties(additionalProperties, coordinates, metaDataProperties);
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
                key = TYHJA;
            }
            TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
            textViewId.setText(key);

            String value = additionalProperties.get(i).getValue();
            if (value == null || value.isEmpty()){
                value = TYHJA;
            }
            TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
            textViewLocation.setText(value);
        }
        else if (i == amountOfAdditionalProperties && amountOfCoordinates==1){
            TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
            textViewId.setText(SIJAINTI);

            TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
            textViewLocation.setText(object.getPointLocation().toString());
        }
        else if(i >= amountOfAdditionalProperties && i-amountOfAdditionalProperties
                -amountOfCoordinates < amountOfMetaDataProperties){
            int metaDataIndex = i-amountOfAdditionalProperties-amountOfCoordinates;

            String key = metaDataProperties.get(metaDataIndex).getKey();
            if (key == null || key.isEmpty()){
                key = TYHJA;
            }
            TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
            textViewId.setText(key);

            String value = additionalProperties.get(metaDataIndex).getValue();
            if (value == null || value.isEmpty()){
                value = TYHJA;
            }
            TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
            textViewLocation.setText(value);
        }
        return view;
    }

    public void setAmountOfProperties(int additionalProperties, int coordinateObjects,
                                      int metaDataProperties){
        this.amountOfAdditionalProperties = additionalProperties;
        this.amountOfMetaDataProperties = metaDataProperties;
        if(coordinateObjects != 1){
            this.amountOfCoordinates = 0;
        }
        else {
            this.amountOfCoordinates = 1;
        }
    }
}
