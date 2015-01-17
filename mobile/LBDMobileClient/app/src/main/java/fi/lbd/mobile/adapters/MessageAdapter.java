package fi.lbd.mobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import fi.lbd.mobile.R;
import fi.lbd.mobile.messaging.messageobjects.MessageObject;

/**
 *
 * Adapter to handle showing messages on a list view.
 *
 * Created by Ossi on 20.12.2014.
 */

public class MessageAdapter extends BaseAdapter {
    private List<MessageObject> objects;
    private Context context;

    public MessageAdapter(Context context) {
        this.context = context;
        this.objects = new ArrayList<MessageObject>();
    }

    @Override
    public int getCount() {
        return this.objects.size();
    }

    public MessageObject get(int i) {
        return this.objects.get(i);
    }

    @Override
    public Object getItem(int i) {
        return this.objects.get(i);
    }

    public void clear() {
        this.objects.clear();
    }

    public void addAll(Collection<MessageObject> objects) {
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
            view = inflater.inflate(R.layout.listview_double_row, viewGroup, false);
        }
        MessageObject object = this.objects.get(i);
        view.setTag(object.getId());

        TextView textViewId = (TextView) view.findViewById(R.id.textViewObjectId);
        textViewId.setText("TITLE: " + object.getTopic());

        TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
        textViewLocation.setText("FROM: " + object.getSender());

        return view;
    }

    public List<MessageObject> getObjects(){
        return this.objects;
    }
}
