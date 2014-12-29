package fi.lbd.mobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
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
import fi.lbd.mobile.messageobjects.MessageObject;

/**
 * Created by tommi on 20.10.2014.
 */

// TODO: http://www.codeofaninja.com/2013/09/android-viewholder-pattern-example.html
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
        textViewId.setText(object.getTopic());
       // textViewId.setTag(object.getId());

        TextView textViewLocation = (TextView) view.findViewById(R.id.textViewObjectLocation);
        textViewLocation.setText(object.getSender());
      //  textViewLocation.setTag(object.getId());

        return view;
    }
}
