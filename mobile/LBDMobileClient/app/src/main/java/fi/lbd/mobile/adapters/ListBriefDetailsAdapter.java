package fi.lbd.mobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
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
    private final int MAX_BRIEF_DETAILS = 3;
    private static int fullListHeight = -1;
    private static int briefListHeight = -1;
    private MapObject object;
    private ArrayList<Map.Entry<String,String>> additionalProperties;
    private Context context;
    private boolean expandDetails = false;

    public void setObject(MapObject mapObject){
        this.object = mapObject;
        if(mapObject != null) {
            Log.d(" number of additional properties _________", ((Integer)object.getAdditionalProperties().size()).toString());

            for (Map.Entry<String, String> entry : object.getAdditionalProperties().entrySet()) {
                this.additionalProperties.add(entry);
            }
        }
    }

    public ListBriefDetailsAdapter(Context context, MapObject mapObject) {
        this.context = context;
        this.additionalProperties = new ArrayList<Map.Entry<String,String>>();
        setObject(mapObject);
    }

    @Override
    public int getCount() {
        // Location, additional properties
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
            ListView list = (ListView)((View)viewGroup.getParent()).findViewById(android.R.id.list);
            list.setLayoutParams(new LinearLayout.LayoutParams(2000,2000));
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

        ((View)viewGroup.getParent()).findViewById(R.id.moreButton).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandDetails = !expandDetails;
                ListView list = ((ListView)((View)(view.getParent())).findViewById(android.R.id.list));
                ViewGroup.LayoutParams params = list.getLayoutParams();
                if(expandDetails == true) {
                    params.height = calculateFullListHeight(list);
                }
                else {
                    params.height = calculateBriefListHeight(list);
                }
                list.setLayoutParams(params);
                list.requestLayout();
            }
        });
        return view;
    }

    public int calculateBriefListHeight(ListView listView){
        Log.d("22222222222 brieflisttHeight on ", Integer.toString(briefListHeight));
        Log.d("222222222 calculatebrieflistissä, expandDetails on  ", Boolean.toString(expandDetails));

        if(briefListHeight == -1) {
            View firstVisible = listView.getChildAt(listView.getFirstVisiblePosition());
            int height = firstVisible.getHeight();
            int lastElementPosition = listView.getFirstVisiblePosition() + listView.getCount() - 1;

            for (int i = 1; i < MAX_BRIEF_DETAILS; ++i) {
                int wantedChild = listView.getFirstVisiblePosition() + i;
                if (wantedChild > 0 && wantedChild < lastElementPosition) {
                    height = height + listView.getChildAt(i).getHeight();
                } else {
                    briefListHeight = height;
                    return briefListHeight;
                }
            }
            briefListHeight = height;
        }
        return briefListHeight;
    }

    public int calculateFullListHeight(ListView listView){

        Log.d("22222222222 fullListHeight on ", Integer.toString(fullListHeight));
        Log.d("22222222222 calculatefulllistissä, expandDetails on  ", Boolean.toString(expandDetails));

        if(fullListHeight == -1) {
            View firstVisible = listView.getChildAt(listView.getFirstVisiblePosition());
            int height = firstVisible.getHeight();
            int startPosition = listView.getFirstVisiblePosition() + 1;
            int endPosition = listView.getFirstVisiblePosition() + listView.getCount();
            for (int i = startPosition; i < endPosition; ++i) {
                height = height + listView.getChildAt(i).getHeight();
            }
            fullListHeight = height;
        }
        return fullListHeight;
    }
}
