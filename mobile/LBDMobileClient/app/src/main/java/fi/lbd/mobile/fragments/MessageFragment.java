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
import android.widget.TextView;
import android.widget.Toast;

import com.android.debug.hv.ViewServer;
import com.squareup.otto.Subscribe;

import fi.lbd.mobile.messaging.MessageObjectSelectionManager;
import fi.lbd.mobile.R;
import fi.lbd.mobile.messaging.ReadMessageActivity;
import fi.lbd.mobile.messaging.SendMessageActivity;
import fi.lbd.mobile.adapters.MessageAdapter;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.messaging.events.RequestUserMessagesEvent;
import fi.lbd.mobile.messaging.events.UpdateCachedMessagesToListEvent;
import fi.lbd.mobile.messaging.messageobjects.MessageObject;

/**
 *
 * Fragment that shows user messages on a list view.
 *
 * Created by Ossi.
 */

public class MessageFragment extends ListFragment {
    private MessageAdapter adapter;
    private ProgressDialog progressDialog;

    public static MessageFragment newInstance() { return new MessageFragment();}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.adapter = new MessageAdapter(this.getActivity());
        setListAdapter(this.adapter);
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
                progressDialog = ProgressDialog.show(getActivity(), "", "Downloading messages...", true);
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(false);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        BusHandler.getBus().register(this);
        if(adapter.getCount() > 0 && getListView().getVisibility() == View.INVISIBLE){
            TextView noMessagesText = (TextView)((View)getListView().getParent())
                    .findViewById(R.id.noMessagesTextView);
            noMessagesText.setVisibility(View.GONE);
            getListView().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHandler.getBus().unregister(this);
        if(progressDialog != null && progressDialog.isShowing()){
            this.progressDialog.dismiss();
        }
    }

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        MessageObject object = this.adapter.get(position);
        if(object != null) {
            MessageObjectSelectionManager.get().setSelectedMessageObject(object);
            Intent intent = new Intent(getActivity(), ReadMessageActivity.class);
            startActivity(intent);
        }
	}

    @Subscribe
    public void onEvent(UpdateCachedMessagesToListEvent event) {
        TextView noMessagesText = (TextView)((View)getListView().getParent())
                .findViewById(R.id.noMessagesTextView);
        Log.d(getClass().toString(), " received UpdateMessagesEvent");

        if(event.getObjects() != null && event.getObjects().size() > 0) {
            noMessagesText.setVisibility(View.GONE);
            getListView().setVisibility(View.VISIBLE);
            this.adapter.clear();
            this.adapter.addAll(event.getObjects());
            this.adapter.notifyDataSetChanged();
            for (MessageObject message : event.getObjects()) {
                Log.d(this.getClass().getSimpleName(), "Message: " + message);
            }
        }
        else if(event.getObjects() != null && event.getObjects().size() == 0){
            this.adapter.clear();
            this.adapter.notifyDataSetChanged();
            getListView().setVisibility(View.INVISIBLE);
            noMessagesText.setVisibility(View.VISIBLE);
        }

        if (progressDialog != null && progressDialog.isShowing()) {
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
