package fi.lbd.mobile.fragments;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fi.lbd.mobile.MessageObjectSelectionManager;
import fi.lbd.mobile.R;
import fi.lbd.mobile.ReadMessageActivity;
import fi.lbd.mobile.SendMessageActivity;
import fi.lbd.mobile.adapters.MessageAdapter;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.ImmutableMapObject;
import fi.lbd.mobile.messageobjects.events.DeleteMessageEvent;
import fi.lbd.mobile.messageobjects.events.RequestUserMessagesEvent;
import fi.lbd.mobile.messageobjects.events.ReturnUserMessagesEvent;
import fi.lbd.mobile.messageobjects.MessageObject;
import fi.lbd.mobile.messageobjects.StringMessageObject;
import fi.lbd.mobile.messageobjects.events.SendMessageEvent;


public class MessageFragment extends ListFragment {
    private MessageAdapter adapter;
    private ProgressDialog progressDialog;

    public static MessageFragment newInstance() { return new MessageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.adapter = new MessageAdapter(this.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listview_message_fragment, container, false);

        View newMessageButton = view.findViewById(R.id.newMessageButton);
        newMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SendMessageActivity.class);
                startActivity(intent);
            }
        });

        View refreshButton = view.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusHandler.getBus().post(new RequestUserMessagesEvent());
                progressDialog = ProgressDialog.show(getActivity(), "", "Downloading messages", true);
            }
        });

        BusHandler.getBus().post(new RequestUserMessagesEvent());

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

    @Subscribe
    public void onEvent(ReturnUserMessagesEvent event) {
        this.adapter.clear();
        this.adapter.addAll(event.getMessageObjects());
        for (MessageObject message : event.getMessageObjects()) {
            Log.d(this.getClass().getSimpleName(), "Message: "+ message);
        }
        this.adapter.notifyDataSetChanged();
        if(progressDialog != null && progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
    }

    @Subscribe
    public void onEvent(RequestFailedEvent event){
        if(event.getFailedEvent() instanceof RequestUserMessagesEvent) {
            Context context = getActivity().getApplicationContext();
            CharSequence dialogText = "Failed to download messages";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, dialogText, duration).show();
            if(progressDialog != null && progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }
        }
    }
} 
