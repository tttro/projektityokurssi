package fi.lbd.mobile.adapters;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import fi.lbd.mobile.R;
import fi.lbd.mobile.mapobjects.MapObject;
/**
 * Adapter to handle showing details of a MapObject on a ListView.
 * The amount of details (properties, metadata items, and coordinate points) to be shown can be determined.
 *
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
    private String objectId;
    private ArrayList<Map.Entry<String,String>> additionalProperties;
    private ArrayList<Map.Entry<String,String>> metaDataProperties;
    private Context context;

    public ListDetailsAdapter(Context context, MapObject mapObject, int additionalProperties,
                              int coordinates, int metaDataProperties) {
        this.context = context;
        this.additionalProperties = new ArrayList<Map.Entry<String,String>>();
        this.metaDataProperties = new ArrayList<Map.Entry<String,String>>();
        setAmountOfProperties(mapObject, additionalProperties, coordinates, metaDataProperties);
        setObject(mapObject);
    }

    private void setObject(MapObject mapObject){
        this.object = mapObject;
        if(mapObject != null) {
            this.objectId = object.getId();

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
            if(this.metaDataProperties.isEmpty() && amountOfMetaDataProperties == 1){
                this.metaDataProperties.add(new AbstractMap.SimpleEntry<String, String>(null, null));
            }
        }
    }

    private void setAmountOfProperties(MapObject object, int additionalProperties,
                                       int coordinateObjects, int metaDataProperties){
        if(object.getAdditionalProperties().size() >= additionalProperties) {
            this.amountOfAdditionalProperties = additionalProperties;
        }
        else {
            this.amountOfAdditionalProperties = object.getAdditionalProperties().size();
        }

        if(metaDataProperties <= 0){
            this.amountOfMetaDataProperties = 0;
        }
        else if(object.getMetadataProperties().size() >= metaDataProperties){
            this.amountOfMetaDataProperties = metaDataProperties;
        }
        else if(object.getMetadataProperties().size() > 0) {
            this.amountOfMetaDataProperties = object.getMetadataProperties().size();
        }
        else if (object.getMetadataProperties().size() == 0){
            this.amountOfMetaDataProperties = 1;
        }

        if(coordinateObjects != 1){
            this.amountOfCoordinates = 0;
        }
        else {
            this.amountOfCoordinates = 1;
        }
        Log.d(this.getClass().toString(), String.format("amount of metadataproperties set to " +
                "%d",this.amountOfMetaDataProperties));
    }

    // Count = amount of: object id + additional properties + metadata properties + coordinates
    @Override
    public int getCount() {
        return 1 + amountOfAdditionalProperties + amountOfMetaDataProperties + amountOfCoordinates;
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

    /*
    //  Object details are listed in the following order:
    //
    //  1. Object ID
    //  2. Additional properties
    //  3. Coordinates (currently an object can contain at most only 1 coordinate point)
    //  4. Metadata properties
    //
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            LayoutInflater inflater = ((Activity) this.context).getLayoutInflater();
            view = inflater.inflate(R.layout.listview_double_row, viewGroup, false);
        }

        TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
        TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);

        if(i == 0){
            String key = "OBJECT ID";
            String value = objectId;
            textViewId.setText(key);
            if(value == null || value.isEmpty()){
                value = EMPTY;
            }
            textViewLocation.setText(value);
        }
        else if (i >= 1 && i <= amountOfAdditionalProperties){
            String key = additionalProperties.get(i-1).getKey();
            if (key == null || key.isEmpty()){
                key = EMPTY;
            }
            textViewId.setText(key);
            String value = additionalProperties.get(i-1).getValue();
            if (value == null || value.isEmpty()){
                value = EMPTY;
            }
            textViewLocation.setText(value);
        }
        else if (i == amountOfAdditionalProperties + 1 && amountOfCoordinates == 1){
            textViewId.setText(LOCATION);
            textViewLocation.setText(object.getPointLocation().toString());
        }
        else if(i >= amountOfAdditionalProperties && i-amountOfAdditionalProperties
                -amountOfCoordinates <= amountOfMetaDataProperties){
            int metaDataIndex = i-amountOfAdditionalProperties-amountOfCoordinates-1;
            String key = metaDataProperties.get(metaDataIndex).getKey();
            if (key == null || key.isEmpty()){
                key = EMPTY;
            }
            textViewId.setText(key);
            String value = metaDataProperties.get(metaDataIndex).getValue();
            if (value == null || value.isEmpty()){
                value = EMPTY;
            }
            else if(key.equals("modified")){
                Date date = new Date(Long.parseLong(value)*1000);
                value = new SimpleDateFormat("'Date: 'dd.MM.yyyy', Time: 'HH:mm").format(date);
            }
            textViewLocation.setText(value);
        }
        return view;
    }
}