package fi.lbd.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.messageobjects.StringMessageObject;
import fi.lbd.mobile.messageobjects.events.DeleteMessageEvent;
import fi.lbd.mobile.messageobjects.events.DeleteMessageSucceededEvent;
import fi.lbd.mobile.messageobjects.events.RequestUserMessagesEvent;


public class ReadMessageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_message);

        StringMessageObject object = (StringMessageObject) MessageObjectSelectionManager.get()
                .getSelectedMessageObject();
        View rootView = findViewById(android.R.id.content);
        ((TextView)rootView.findViewById(R.id.textViewSender)).setText(object.getSender());
        ((TextView)rootView.findViewById(R.id.textViewTopic)).setText(object.getTopic());
        ((TextView)rootView.findViewById(R.id.textViewMessageContents)).setText(object.getMessage());
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

    public void onDeleteClick(View view){
        StringMessageObject object = (StringMessageObject) MessageObjectSelectionManager.get()
                .getSelectedMessageObject();
        if(object != null && object.getId() != null && object.getId().length()>0) {
            DeleteMessageEvent deleteMessageEvent = new DeleteMessageEvent(object.getId());
            BusHandler.getBus().post(deleteMessageEvent);
        }
    }

    public void onReplyClick(View view){
        Intent intent = new Intent(this, SendMessageActivity.class);
        intent.putExtra("Replying", true);
        startActivity(intent);
    }

    @Subscribe
    public void onEvent(DeleteMessageSucceededEvent event){
        BusHandler.getBus().post(new RequestUserMessagesEvent());
        Context context = getApplicationContext();
        CharSequence dialogText = "Message deleted";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, dialogText, duration).show();
        onBackPressed();
    }

    @Subscribe
    public void onEvent(RequestFailedEvent event){
        if(event.getFailedEvent() instanceof DeleteMessageEvent) {
            Context context = getApplicationContext();
            CharSequence dialogText = "Failed to delete message";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, dialogText, duration).show();
        }
    }
}
