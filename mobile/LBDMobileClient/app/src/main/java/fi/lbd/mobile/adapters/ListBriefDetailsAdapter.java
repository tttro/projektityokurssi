package fi.lbd.mobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
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
    private final int MAX_BRIEF_DETAILS = 2;
    private MapObject object;
    private ArrayList<Map.Entry<String,String>> additionalProperties;
    private Context context;
    private static boolean expandDetails = false;
    private static int visibleDetailsCounter = 2;

    public void setObject(MapObject mapObject){
        this.object = mapObject;
        if(mapObject != null) {
            Log.d(" number of additional properties _________", ((Integer)object.getAdditionalProperties().size()).toString());

            for (Map.Entry<String, String> entry : object.getAdditionalProperties().entrySet()) {
                Log.d("1. Lisätty additional property" + entry.getKey(), "_________");
                Log.d("2. Lisätty Additional property" + entry.getValue(), "_________");
                this.additionalProperties.add(entry);
            }
        }
    }

    public ListBriefDetailsAdapter(Context context, MapObject mapObject) {
        Log.d("*************", "rakentaja kutsuttu");
        this.context = context;
        this.additionalProperties = new ArrayList<Map.Entry<String,String>>();
        setObject(mapObject);
    }

    @Override
    public int getCount() {
        // Location, additional properties
        Log.d(Integer.toString(this.object.getAdditionalProperties().size() + 1), "_________");
        //return (this.object.getAdditionalProperties()).size() + 1;
        return visibleDetailsCounter;
    }

    @Override
    public Object getItem(int i) {
        if (i == 0){
            return object.getPointLocation();
        }
        //else if (i > 0 && i < getCount() - 1) {
        else if (i > 0 && i < visibleDetailsCounter) {
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
        else if (i > 0 && i < visibleDetailsCounter){
            Log.d("*************", "i on"+Integer.toString(i));
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

        ((View)viewGroup.getParent()).findViewById(R.id.moreButton).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleVisibleDetails(!expandDetails);
                ListView list = ((ListView)((View)(view.getParent())).findViewById(android.R.id.list));
                ViewGroup.LayoutParams params = list.getLayoutParams();
                if(expandDetails == true) {
                    params.height = 1000;
                }
                else {
                    params.height = 200;
                }
                list.setLayoutParams(params);
                list.requestLayout();
            }
        });
        return view;
    }

    public void setVisibleDetailsCounter(int visibleDetails){
        visibleDetailsCounter = visibleDetails;
    }

    public void toggleVisibleDetails(boolean isExpanded){
        this.expandDetails = isExpanded;
        if(expandDetails == true){
            setVisibleDetailsCounter(this.object.getAdditionalProperties().size() + 1);
            Log.d("*************", "visible details counter set to"+Integer.toString(getCount()));
        }
        else {
            setVisibleDetailsCounter(MAX_BRIEF_DETAILS);
            Log.d("*************", "else haara");
        }
    }
}
