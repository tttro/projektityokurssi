package fi.lbd.mobile.messaging;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;

import fi.lbd.mobile.ActiveActivitiesTracker;
import fi.lbd.mobile.R;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.messaging.events.DeleteMessageEvent;
import fi.lbd.mobile.messaging.events.DeleteMessageFromListEvent;
import fi.lbd.mobile.messaging.events.DeleteMessageSucceededEvent;
import fi.lbd.mobile.messaging.events.RequestUserMessagesEvent;
import fi.lbd.mobile.messaging.messageobjects.StringMessageObject;


public class ReadMessageActivity extends Activity {
    private boolean deleteInProgress = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_message);

        StringMessageObject object = (StringMessageObject) MessageObjectSelectionManager.get()
                .getSelectedMessageObject();
        if(object != null) {
            View rootView = findViewById(android.R.id.content);
            String sender = object.getSender();
            String topic = object.getTopic();
            String message = object.getMessage();

            long timestamp = object.getTimestamp();
            Date date = new Date(timestamp*1000);
            String parsedDate = new SimpleDateFormat("dd.MM.yyyy', 'HH:mm").format(date);

            if(sender != null && topic != null && message != null && parsedDate != null){
                ((TextView) rootView.findViewById(R.id.textViewSender)).setText(sender);
                ((TextView) rootView.findViewById(R.id.textViewTopic)).setText(topic);
                ((TextView) rootView.findViewById(R.id.textViewDate)).setText(parsedDate);
                ((TextView) rootView.findViewById(R.id.textViewMessageContents)).setText(message);
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        BusHandler.getBus().register(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        BusHandler.getBus().unregister(this);
    }

    @Override
    public void onStart(){
        super.onStart();
        ActiveActivitiesTracker.activityStarted();
    }

    @Override
    public void onStop(){
        super.onStop();
        ActiveActivitiesTracker.activityStopped();
        this.deleteInProgress = false;
        if(progressDialog != null && progressDialog.isShowing()){
            this.progressDialog.dismiss();
        }
    }

    public void onDeleteClick(View view){
        if(!this.deleteInProgress) {
            StringMessageObject object = (StringMessageObject) MessageObjectSelectionManager.get()
                    .getSelectedMessageObject();
            if (object != null && object.getId() != null && object.getId().length() > 0) {
                this.deleteInProgress = true;
                progressDialog = ProgressDialog.show(this, "", "Deleting message...", true);
                progressDialog.setCancelable(false);
                DeleteMessageEvent deleteMessageEvent = new DeleteMessageEvent(object.getId());
                BusHandler.getBus().post(deleteMessageEvent);
            }
        }
    }

    public void onReplyClick(View view){
        Intent intent = new Intent(this, SendMessageActivity.class);
        intent.putExtra("Replying", true);
        startActivity(intent);
    }

    @Subscribe
    public void onEvent(DeleteMessageSucceededEvent event){
        Context context = getApplicationContext();
        CharSequence dialogText = "Message deleted";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, dialogText, duration).show();

        BusHandler.getBus().post(new DeleteMessageFromListEvent(event.getOriginalEvent().getMessageId()));

        this.deleteInProgress = false;
        if(progressDialog != null && progressDialog.isShowing()){
            this.progressDialog.dismiss();
        }
        onBackPressed();
    }

    @Subscribe
    public void onEvent(RequestFailedEvent event){
        if(event.getFailedEvent() instanceof DeleteMessageEvent) {
            this.deleteInProgress = false;
            if(progressDialog != null && progressDialog.isShowing()){
                this.progressDialog.dismiss();
            }
            Context context = getApplicationContext();
            CharSequence dialogText = "Failed to delete message";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, dialogText, duration).show();
        }
    }
}
