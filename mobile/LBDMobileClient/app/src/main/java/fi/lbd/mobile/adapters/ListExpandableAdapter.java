package fi.lbd.mobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fi.lbd.mobile.R;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Created by tommi on 20.10.2014.
 */

// TODO: http://www.codeofaninja.com/2013/09/android-viewholder-pattern-example.html
public class ListExpandableAdapter extends BaseExpandableListAdapter {
    private ArrayList<MapObject> objects;
    private Context context;

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
                view = inflater.inflate(R.layout.listview_row, parent, false);
        }

        // Get the group item
        MapObject obj = this.objects.get(groupPosition);

        TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
        textViewId.setText(obj.getId());
        textViewId.setTag(obj.getId());

        TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
        textViewLocation.setText(obj.getPointLocation().toString());
        textViewLocation.setTag(obj.getId());

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {

        if (view == null) {
            LayoutInflater inflater = ((Activity) this.context).getLayoutInflater();
            view = inflater.inflate(R.layout.listview_expanded_row, parent, false);
        }
        // Tag links the expanded item to its location object
        view.setTag(getGroup(groupPosition));
        Log.d("TAG SET--------------------", ((MapObject)getGroup(groupPosition)).getId());

        /*
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.BLUE);
            }
        });*/

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}