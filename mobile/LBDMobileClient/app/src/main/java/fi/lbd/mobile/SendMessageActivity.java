package fi.lbd.mobile;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.events.RequestUsersEvent;
import fi.lbd.mobile.events.ReturnUsersEvent;
import fi.lbd.mobile.messageobjects.MessageObject;
import fi.lbd.mobile.messageobjects.SelectReceiverDialogFragment;
import fi.lbd.mobile.messageobjects.StringMessageObject;
import fi.lbd.mobile.messageobjects.events.SelectReceiverEvent;
import fi.lbd.mobile.messageobjects.events.SendMessageEvent;
import fi.lbd.mobile.messageobjects.events.SendMessageSucceededEvent;


public class SendMessageActivity extends Activity {

    private DialogFragment selectReceiverDialog = null;

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
        BusHandler.getBus().post(new RequestUsersEvent());
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
            Log.d("********", "Sending message");
            Log.d("******** Receiver", receiver);
            Log.d("******** Title", title);
            Log.d("******** Message", message);
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
        else if(event.getFailedEvent() instanceof RequestUsersEvent){
            Context context = getApplicationContext();
            CharSequence dialogText = "Loading users failed";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, dialogText, duration).show();
        }
    }

    @Subscribe
    public void onEvent(ReturnUsersEvent event){
        List<String> users = event.getUsers();
        if(users != null && !users.isEmpty()){
            showDialog(users);
        }
        else {
            Context context = getApplicationContext();
            CharSequence dialogText = "No users found";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, dialogText, duration).show();
        }
    }

    private void showDialog(List<String> users) {

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("Select receiver");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        selectReceiverDialog = SelectReceiverDialogFragment.newInstance(users);
        selectReceiverDialog.show(ft, "Select receiver");
    }

    @Subscribe
    public void onEvent(SelectReceiverEvent event){
        if(selectReceiverDialog != null && selectReceiverDialog.isVisible()){
            selectReceiverDialog.dismiss();
        }
        String receiver = event.getReceiver();
        if(receiver != null && !receiver.isEmpty()){
            View rootView = findViewById(android.R.id.content);
            ((TextView)rootView.findViewById(R.id.textViewReceiver)).setText(receiver);
        }
    }
}
