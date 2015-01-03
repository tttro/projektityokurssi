package fi.lbd.mobile.fragments;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import fi.lbd.mobile.MessageObjectSelectionManager;
import fi.lbd.mobile.R;
import fi.lbd.mobile.ReadMessageActivity;
import fi.lbd.mobile.SendMessageActivity;
import fi.lbd.mobile.adapters.MessageAdapter;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestUserMessagesEvent;
import fi.lbd.mobile.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.events.ReturnUserMessagesEvent;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.messageobjects.MessageObject;
import fi.lbd.mobile.messageobjects.StringMessageObject;


public class MessageFragment extends ListFragment {
    private MessageAdapter adapter;

    public static MessageFragment newInstance() { return new MessageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.adapter = new MessageAdapter(this.getActivity());

        List<MessageObject> objects = new ArrayList<>();
        objects.add(new StringMessageObject("no_id", "pekka1", "jaakko1", "testitopic1", false, "testimessage1"));
        objects.add(new StringMessageObject("no_id", "pekka2", "jaakko2", "testitopic2", false, "testimessage2"));
        objects.add(new StringMessageObject("no_id", "pekka3", "jaakko3", "testitopic3", false, "testimessage3"));
        objects.add(new StringMessageObject("no_id", "pekka4", "jaakko4", "testitopic4", false, "testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4" +
                "testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4" +
                "testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4" +
                "testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4" +
                "testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4" +
        "testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4" +
                "testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4" +
                "testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4" +
                "testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4" +
                "testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4" +
                "testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4" +
                "testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4testimessage4" +
                "testimessage4testimessage4testimessage4 LOPPU"));
        objects.add(new StringMessageObject("no_id", "pekka5", "jaakko5", "testitopic5", false, "testimessage5"));
        objects.add(new StringMessageObject("no_id", "pekka6", "jaakko6", "testitopic6", false, "testimessage6"));
        objects.add(new StringMessageObject("no_id", "pekka7", "jaakko7", "testitopic7", false, "testimessage7"));
        objects.add(new StringMessageObject("no_id", "pekka8", "jaakko8", "testitopic8", false, "testimessage8"));
        objects.add(new StringMessageObject("no_id", "pekka9", "jaakko9", "testitopic9", false, "testimessage9"));
        objects.add(new StringMessageObject("no_id", "pekka10", "jaakko10", "testitopic10", false, "testimessage10"));

        this.adapter.addAll(objects);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listview_message_fragment, container, false);

        View newMessageButton = view.findViewById(R.id.newMessageButton);
        newMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusHandler.getBus().post(new RequestUserMessagesEvent()); // TODO: FIXME: Oikee paikka
//                Intent intent = new Intent(getActivity(), SendMessageActivity.class);
//                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setListAdapter(this.adapter);
        BusHandler.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHandler.getBus().unregister(this);
    }

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        MessageObjectSelectionManager.get().setSelectedMessageObject(this.adapter.get(position));
        Intent intent = new Intent(getActivity(), ReadMessageActivity.class);
        startActivity(intent);
	}

    public void onNewMessageClick(){

    }


    @Subscribe
    public void onEvent(ReturnUserMessagesEvent event) {
        this.adapter.clear();
        this.adapter.addAll(event.getMessageObjects());
        for (MessageObject message : event.getMessageObjects()) {
            Log.d(this.getClass().getSimpleName(), "Message: "+ message);
        }
        this.adapter.notifyDataSetChanged();
    }
} 
