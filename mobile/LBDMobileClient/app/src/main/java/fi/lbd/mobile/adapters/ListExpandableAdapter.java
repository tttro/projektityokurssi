package fi.lbd.mobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

import fi.lbd.mobile.R;
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

        // Get the group item
        MapObject object = this.objects.get(groupPosition);

        if(object != null) {
            TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
            textViewId.setText(object.getId());
            textViewId.setTag(object.getId());
        }
        return view;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {

        if (view == null) {
            LayoutInflater inflater = ((Activity) this.context).getLayoutInflater();
            view = inflater.inflate(R.layout.listview_expanded_row, parent, false);
        }

        // Tag links the expanded item to its location object
        view.setTag(getGroup(groupPosition));
        Log.d("TAG SET--------------------", ((MapObject)getGroup(groupPosition)).getId());

        if (getGroup(groupPosition) != null){
           final MapObject object = (MapObject)getGroup(groupPosition);
           final ListBriefDetailsAdapter adapter = new ListBriefDetailsAdapter(
                   this.context, object, MIN_ADDITIONAL_PROPERTIES, MIN_COORDINATES,
                   MIN_METADATA_PROPERTIES);

            ((ListView)view.findViewById(android.R.id.list)).setAdapter(adapter);
            adjustListHeight((ListView)view.findViewById(android.R.id.list));

            // Listener for "More info"/"Lisätietoja" button
            ((Button)(view.findViewById(R.id.moreButton))).setText(context
                    .getString(R.string.lisätietoja));
            (view.findViewById(R.id.moreButton)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ListView list = (ListView)((View)(view.getParent()))
                                    .findViewById(android.R.id.list);
                            Button button = (Button)view;

                            // Show all details in the expanded list
                            if(button.getText().equals(context.getString(R.string.lisätietoja))) {
                                ((ListBriefDetailsAdapter)list.getAdapter())
                                     .setAmountOfProperties(object.getAdditionalProperties().size(),
                                             1, object.getMetadataProperties().size());
                                ((ListBriefDetailsAdapter)list.getAdapter()).notifyDataSetChanged();
                                button.setText(context.getString(R.string.piilota));
                            }
                            // Show the minimum amount of details in the expanded list
                            else if (button.getText().equals(context.getString(R.string.piilota))){
                                ((ListBriefDetailsAdapter)list.getAdapter())
                                     .setAmountOfProperties(MIN_ADDITIONAL_PROPERTIES,
                                             MIN_COORDINATES, MIN_METADATA_PROPERTIES);
                                ((ListBriefDetailsAdapter)list.getAdapter()).notifyDataSetChanged();
                                button.setText(context.getString(R.string.lisätietoja));
                            }
                            adjustListHeight(list);
                        }
                    });
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    // Function to adjust the height of a listview according to its children
    public void adjustListHeight(ListView listView){
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