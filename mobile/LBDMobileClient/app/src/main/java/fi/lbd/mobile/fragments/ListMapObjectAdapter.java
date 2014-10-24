package fi.lbd.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fi.lbd.mobile.R;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Created by tommi on 20.10.2014.
 */

// TODO: http://www.codeofaninja.com/2013/09/android-viewholder-pattern-example.html
public class ListMapObjectAdapter extends BaseAdapter {
    private List<MapObject> objects;
    private Context context;


    public ListMapObjectAdapter(Context context) {
        this.context = context;
        this.objects = new ArrayList<MapObject>();
    }

    @Override
    public int getCount() {
        return this.objects.size();
    }

    public MapObject get(int i) {
        return this.objects.get(i);
    }

    @Override
    public Object getItem(int i) {
        return this.objects.get(i);
    }

    public void clear() {
        this.objects.clear();
    }

    public void addAll(Collection<MapObject> objects) {
        this.objects.addAll(objects);
    }

    @Override
    public long getItemId(int i) {
        return this.objects.indexOf(getItem(i));
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            LayoutInflater inflater = ((Activity) this.context).getLayoutInflater();
            view = inflater.inflate(R.layout.listview_row, viewGroup, false);
        }
        MapObject obj = this.objects.get(i);

        TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
        textViewId.setText(obj.getId());
        textViewId.setTag(obj.getId());

        TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
        textViewLocation.setText(obj.getPointLocation().toString());
        textViewLocation.setTag(obj.getId());

        return view;
    }
}
