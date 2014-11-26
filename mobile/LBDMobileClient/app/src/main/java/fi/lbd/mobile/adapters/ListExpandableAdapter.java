package fi.lbd.mobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
    private ArrayList<MapObject> objects;
    private Context context;

    // How many additional properties, coordinates and metadata properties are shown
    // before "lisää"/"more" button is pressed
    private final static int MIN_ADDITIONAL_PROPERTIES = 3;
    private final static int MIN_COORDINATES = 0;
    private final static int MIN_METADATA_PROPERTIES = 1;
    private final static int ADDITIONAL_PADDING = 20;

    public ListExpandableAdapter(Context context) {
        this.context = context;
        this.objects = new ArrayList<MapObject>();
    }

    @Override
    public int getGroupCount() {
        return this.objects.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.objects.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.objects.get(groupPosition);
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

    public void addAll(Collection<MapObject> objects) {
        if(objects != null){
            this.objects.addAll(objects);
        }
        else Log.d("ListExpandableAdapter", "No objects to add after ReturnObjectsInAreaEvent.");
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

        MapObject object = this.objects.get(groupPosition);
        if(object != null) {
            TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
            textViewId.setText(object.getId());

            // Use the last cached user location to calculate distance to object
            TextView textViewDistance = (TextView) view.findViewById(R.id.textViewDistance);
            PointLocation location = ((ListActivity)this.context)
                    .getLocationHandler().getCachedLocation();
            if (location != null) {
                int distance = ((int) LocationUtils.distanceBetween(
                        object.getPointLocation().getLatitude(), object.getPointLocation().getLongitude(),
                        location.getLatitude(), location.getLongitude()));
                textViewDistance.setText(LocationUtils.formatDistance(distance));
            } else {
                textViewDistance.setText(context.getResources().getString(R.string.unknown));
            }
        }
        return view;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = ((Activity) this.context).getLayoutInflater();
            view = inflater.inflate(R.layout.listview_expanded_row, parent, false);
        }
        if (getGroup(groupPosition) != null){

            // Tag links the expanded item to its location object
            view.setTag(getGroup(groupPosition));
            Log.d("TAG SET--------------------", ((MapObject)getGroup(groupPosition)).getId());


           final MapObject object = (MapObject)getGroup(groupPosition);
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
}