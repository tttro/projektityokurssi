package fi.lbd.mobile;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.messageobjects.MessageObject;
import fi.lbd.mobile.messageobjects.StringMessageObject;
import fi.lbd.mobile.messageobjects.events.SendMessageEvent;
import fi.lbd.mobile.messageobjects.events.SendMessageSucceededEvent;


public class SendMessageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
    }

    @Override
    public void onResume(){
        super.onResume();
        BusHandler.getBus().register(this);

        if(getIntent().getSerializableExtra("Replying") != null &&
                (boolean)getIntent().getSerializableExtra("Replying") == true){
            MessageObject object = MessageObjectSelectionManager.get().getSelectedMessageObject();
            View rootView = findViewById(android.R.id.content);
            ((TextView)rootView.findViewById(R.id.textViewReceiver)).setText(object.getSender());
            ((TextView)rootView.findViewById(R.id.textViewTitle)).setText("RE: " + object.getTopic());
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        BusHandler.getBus().unregister(this);
    }

    public void onSelectReceiverClick(View view){

    }

    public void onSendClick(View view){
        View rootView = findViewById(android.R.id.content);
        String receiver = (String)(((TextView)rootView.findViewById(R.id.textViewReceiver)).getText());
        String title = ((EditText)rootView.findViewById(R.id.textViewTitle)).getText().toString();
        String message = ((EditText)rootView.findViewById(R.id.textViewMessage)).getText().toString();

        if(receiver != null && title != null && message != null && receiver.length()>0
             && title.length()>0 && message.length() > 0){
            SendMessageEvent<String> sendMessageEvent = new SendMessageEvent<>(receiver, title, message, null);
            BusHandler.getBus().post(sendMessageEvent);
        }
        else {
            Context context = getApplicationContext();
            CharSequence dialogText = "Please check message parameters";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, dialogText, duration).show();
        }
    }

    @Subscribe
    public void onEvent(SendMessageSucceededEvent event){
        Context context = getApplicationContext();
        CharSequence dialogText = "Message sent";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, dialogText, duration).show();
    }

    @Subscribe
    public void onEvent(RequestFailedEvent event){
        if (event.getFailedEvent() instanceof SendMessageEvent) {
            Context context = getApplicationContext();
            CharSequence dialogText = "Failed to send message";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, dialogText, duration).show();
        }
    }
}
