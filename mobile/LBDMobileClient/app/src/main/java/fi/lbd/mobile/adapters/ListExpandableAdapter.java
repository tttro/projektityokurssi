package fi.lbd.mobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import fi.lbd.mobile.ListActivity;
import fi.lbd.mobile.R;
import fi.lbd.mobile.location.LocationUtils;
import fi.lbd.mobile.location.PointLocation;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Created by Ossi on 20.10.2014.
 */

// TODO: http://www.codeofaninja.com/2013/09/android-viewholder-pattern-example.html
public class ListExpandableAdapter extends BaseExpandableListAdapter {
    private ArrayList<Pair<Integer, MapObject>> objects;
    private Context context;

    // Number of additional properties, coordinates and metadata properties that are shown in the
    // expanded list
    private final static int MIN_ADDITIONAL_PROPERTIES = 2;
    private final static int MIN_COORDINATES = 0;
    private final static int MIN_METADATA_PROPERTIES = 1;
    private final static int ADDITIONAL_PADDING = 20;

    public ListExpandableAdapter(Context context) {
        this.context = context;
        this.objects = new ArrayList<>();
    }

    @Override
    public int getGroupCount() {
        return this.objects.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        MapObject object = (MapObject)getGroup(groupPosition);
        if (object != null){
            if(object.getAdditionalProperties().size() >= MIN_ADDITIONAL_PROPERTIES){
                if(object.getMetadataProperties().size() >= MIN_METADATA_PROPERTIES){
                    return MIN_ADDITIONAL_PROPERTIES + MIN_METADATA_PROPERTIES + MIN_COORDINATES;
                }
                else {
                    return MIN_ADDITIONAL_PROPERTIES + object.getMetadataProperties().size() +
                            MIN_COORDINATES;
                }
            }
            else {
                if(object.getMetadataProperties().size() >= MIN_METADATA_PROPERTIES){
                    return object.getAdditionalProperties().size() +
                            MIN_METADATA_PROPERTIES + MIN_COORDINATES;
                }
                else {
                    return object.getAdditionalProperties().size()
                            + object.getMetadataProperties().size() + MIN_COORDINATES;
                }
            }
        }
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        if( groupPosition >= 0 && groupPosition < getGroupCount()){
            return this.objects.get(groupPosition);
        }
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if( groupPosition >= 0 && groupPosition < getGroupCount()){
            return this.objects.get(groupPosition);
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    public void clear() {
        this.objects.clear();
    }

    // Adds all objects and sorts them by distance from nearest to furthest
    public void addAll(Collection<MapObject> newObjects) {

        if(newObjects != null && !newObjects.isEmpty()){
            for (MapObject object : newObjects){
                PointLocation location = ((ListActivity)this.context)
                        .getLocationHandler().getCachedLocation();
                int distance = -1;
                if (location != null) {
                    distance = ((int) LocationUtils.distanceBetween(
                            object.getPointLocation().getLatitude(), object.getPointLocation().getLongitude(),
                            location.getLatitude(), location.getLongitude()));
                }
                objects.add(new Pair<Integer, MapObject>(distance, object));
            }
            Collections.sort(objects, new DistanceComparator());
        }
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
        if (view == null) {
                LayoutInflater inflater = ((Activity) this.context).getLayoutInflater();
                view = inflater.inflate(R.layout.listview_single_row, parent, false);
        }
        // Color the background
        if (isExpanded){
            view.setBackgroundColor(context.getResources().getColor(R.color.exp_background));
        }
        else {
            view.setBackgroundColor(Color.WHITE);
        }

        int distance = this.objects.get(groupPosition).first;
        MapObject object = this.objects.get(groupPosition).second;

        if(object != null) {
            TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
            textViewId.setText(object.getId());

            TextView textViewDistance = (TextView) view.findViewById(R.id.textViewDistance);
            if(distance >= 0){
                textViewDistance.setText(LocationUtils.formatDistance(distance));
            }
            else {
                textViewDistance.setText(context.getResources().getString(R.string.unknown));
            }
        }
        return view;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild,
                             View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = ((Activity) this.context).getLayoutInflater();
            view = inflater.inflate(R.layout.listview_expanded_row, parent, false);
        }
        if (getGroup(groupPosition) != null &&
                ((Pair<Integer, MapObject>)getGroup(groupPosition)).second != null){

            // Tag links the expanded item to its location object
            view.setTag(((Pair<Integer, MapObject>)getGroup(groupPosition)).second);
            Log.d("TAG SET--------------------",
                    (((Pair<Integer, MapObject>)getGroup(groupPosition)).second).getId());

           final MapObject object = ((Pair<Integer, MapObject>)getGroup(groupPosition)).second;
           final ListDetailsAdapter adapter = new ListDetailsAdapter(
                   this.context, object, MIN_ADDITIONAL_PROPERTIES, MIN_COORDINATES,
                   MIN_METADATA_PROPERTIES);

            ((ListView)view.findViewById(android.R.id.list)).setAdapter(adapter);
            adjustListHeight((ListView)view.findViewById(android.R.id.list));
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    // Function to adjust the height of a listview according to its children
    private static void adjustListHeight(ListView listView){
        int newHeight = 0;
        Adapter adapter = listView.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            newHeight = newHeight + listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = newHeight + (listView.getDividerHeight() * (adapter.getCount() - 1)
        + listView.getPaddingRight() + listView.getPaddingLeft()
        + listView.getPaddingBottom() + listView.getPaddingTop() + ADDITIONAL_PADDING);
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private class DistanceComparator implements Comparator<Pair<Integer, MapObject>> {
        @Override
        public int compare(Pair<Integer, MapObject> o1, Pair<Integer, MapObject> o2) {
            return o1.first.compareTo(o2.first);
        }
    }
}