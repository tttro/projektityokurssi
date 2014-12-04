package fi.lbd.mobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

import fi.lbd.mobile.R;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Created by Ossi on 19.11.2014.
 */

public class ListDetailsAdapter extends BaseAdapter {
    private final static String EMPTY = "Empty";
    private final static String NOTES = "ADDITIONAL NOTES";
    private final static String LOCATION = "LOCATION";

    // Number of properties, metadata items and coordinate points contained in each object
    private int amountOfAdditionalProperties = 0;
    private boolean showMetaDataProperties = false;
    private boolean showCoordinates = false;

    private MapObject object;
    private ArrayList<Map.Entry<String,String>> additionalProperties;
    private ArrayList<Map.Entry<String,String>> metaDataProperties;
    private Context context;

    private void setObject(MapObject mapObject){
        this.object = mapObject;
        if(mapObject != null) {
            int i = 1;
            for (Map.Entry<String, String> entry : object.getAdditionalProperties().entrySet()) {
                if (i <= amountOfAdditionalProperties) {
                    this.additionalProperties.add(entry);
                }
            }
            if(object.getMetadataProperties().isEmpty() && showMetaDataProperties){
                Log.d("********", "Lisätään tyhjä");
               this.metaDataProperties.add(new AbstractMap.SimpleEntry<String, String>(null, null));
            }
            else if (!object.getMetadataProperties().isEmpty() && showMetaDataProperties){
                for (Map.Entry<String, String> entry : object.getMetadataProperties().entrySet()) {
                    this.metaDataProperties.add(entry);
                    break;
                }
            }
        }
    }

    private void setAmountOfProperties(MapObject object, int showAdditionalProperties,
                                       boolean showLocation, boolean showMetaData){
        if(object.getAdditionalProperties().size() >= showAdditionalProperties) {
            this.amountOfAdditionalProperties = showAdditionalProperties;
        }
        else {
            this.amountOfAdditionalProperties = object.getAdditionalProperties().size();
        }
        showMetaDataProperties = showMetaData;
        showCoordinates = showLocation;
    }

    public ListDetailsAdapter(Context context, MapObject mapObject, int additionalProperties,
                              boolean showCoordinates, boolean showMetaData) {
        this.context = context;
        this.additionalProperties = new ArrayList<Map.Entry<String,String>>();
        this.metaDataProperties = new ArrayList<Map.Entry<String,String>>();
        setAmountOfProperties(mapObject, additionalProperties, showCoordinates, showMetaData);
        setObject(mapObject);
    }

    @Override
    public int getCount() {
        int amountOfMetaDataProperties = showMetaDataProperties ? 1 : 0;
        int amountOfCoordinates = showCoordinates ? 1 : 0;
        return amountOfAdditionalProperties + amountOfMetaDataProperties + amountOfCoordinates;
    }

    // Not used
    @Override
    public Object getItem(int i) {
        return null;
    }

    // Not used
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
        TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
        TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);

        if (i >= 0 && i < amountOfAdditionalProperties){
            String key = additionalProperties.get(i).getKey();
            if (key == null || key.isEmpty()){
                key = EMPTY;
            }
            textViewId.setText(key);
            String value = additionalProperties.get(i).getValue();
            if (value == null || value.isEmpty()){
                value = EMPTY;
            }
            textViewLocation.setText(value);
        }
        else if (i == amountOfAdditionalProperties && showCoordinates){
            textViewId.setText(LOCATION);
            textViewLocation.setText(object.getPointLocation().toString());
        }
        else if(i >= amountOfAdditionalProperties && showMetaDataProperties &&
                i < getCount()){
            String key = metaDataProperties.get(0).getKey();
            if (key == null || key.isEmpty()){
                key = NOTES;
            }
            textViewId.setText(key);
            String value = metaDataProperties.get(0).getValue();
            if (value == null || value.isEmpty()){
                value = EMPTY;
            }
            textViewLocation.setText(value);
        }
        return view;
    }
}
