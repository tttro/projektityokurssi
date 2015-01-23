package fi.lbd.mobile.messaging;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.List;

import fi.lbd.mobile.ActiveActivitiesTracker;
import fi.lbd.mobile.GlobalToastMaker;
import fi.lbd.mobile.R;
import fi.lbd.mobile.authorization.AuthorizedEventHandler;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.events.RequestUsersEvent;
import fi.lbd.mobile.events.ReturnUsersEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.ImmutableMapObject;
import fi.lbd.mobile.messaging.messageobjects.MessageObject;
import fi.lbd.mobile.messaging.events.SelectReceiverEvent;
import fi.lbd.mobile.messaging.events.SendMessageEvent;
import fi.lbd.mobile.messaging.events.SendMessageSucceededEvent;

/**
 *
 * Activity for sending a message.
 *
 * Created by Ossi.
 */
public class SendMessageActivity extends Activity {

    private ProgressDialog sendingMessageDialog;
    private ProgressDialog loadReceiversDialog;
    private DialogFragment selectReceiverDialog = null;
    private boolean sendInProgress = false;
    private boolean selectReceiverInProgress = false;

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
                (boolean)getIntent().getSerializableExtra("Replying")){
            MessageObject object = MessageObjectSelectionManager.get().getSelectedMessageObject();
            if(object != null) {
                View rootView = findViewById(android.R.id.content);
                ((TextView) rootView.findViewById(R.id.textViewReceiver)).setText(object.getSender());
                ((TextView) rootView.findViewById(R.id.textViewTitle)).setText("RE: " + object.getTopic());
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        BusHandler.getBus().unregister(this);
    }

    @Override
    public void onStart(){
        super.onStart();
        ActiveActivitiesTracker.activityStarted(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        ActiveActivitiesTracker.activityStopped(this);
        this.selectReceiverInProgress = false;
        this.sendInProgress = false;
        if(sendingMessageDialog != null && sendingMessageDialog.isShowing()){
            this.sendingMessageDialog.dismiss();
        }
        if(loadReceiversDialog != null && loadReceiversDialog.isShowing()){
            this.loadReceiversDialog.dismiss();
        }
    }

    public void onSelectReceiverClick(View view){
        if(!this.selectReceiverInProgress) {
            this.selectReceiverInProgress = true;
            loadReceiversDialog = ProgressDialog.show(this, "", "Loading users...", true);
            loadReceiversDialog.setCancelable(true);
            loadReceiversDialog.setCanceledOnTouchOutside(false);
            BusHandler.getBus().post(new RequestUsersEvent());
        }
    }

    public void onSendClick(View view){
        View rootView = findViewById(android.R.id.content);
        String receiver = (String)(((TextView)rootView.findViewById(R.id.textViewReceiver)).getText());
        String title = ((EditText)rootView.findViewById(R.id.textViewTitle)).getText().toString();
        String message = ((EditText)rootView.findViewById(R.id.textViewMessage)).getText().toString();

        if(!this.sendInProgress) {
            if (receiver != null && title != null && message != null && receiver.length() > 0
                    && title.length() > 0 && message.length() > 0) {
                this.sendInProgress = true;
                sendingMessageDialog = ProgressDialog.show(this, "", "Sending message...", true);
                sendingMessageDialog.setCancelable(true);
                sendingMessageDialog.setCanceledOnTouchOutside(false);
                SendMessageEvent<String> sendMessageEvent = new SendMessageEvent<>(receiver, title, message,
                        new ImmutableMapObject(false, "Empty", new ImmutablePointLocation(10, 10),
                                new HashMap<String, String>(), new HashMap<String, String>()));
                BusHandler.getBus().post(sendMessageEvent);
            }
            else {
                String dialogText = "Please check message parameters";
                makeShortToast(dialogText);
            }
        }
    }

    @Subscribe
    public void onEvent(SendMessageSucceededEvent event){
        String dialogText = "Message sent";
        GlobalToastMaker.makeShortToast(dialogText);
        this.sendInProgress = false;
        if(sendingMessageDialog != null && sendingMessageDialog.isShowing()){
            this.sendingMessageDialog.dismiss();
        }
        onBackPressed();
    }

    @Subscribe
    public void onEvent(RequestFailedEvent event){
        if (event.getFailedEvent() instanceof SendMessageEvent) {
            String dialogText = "Failed to send message";
            GlobalToastMaker.makeShortToast(dialogText);
            this.sendInProgress = false;
            if(sendingMessageDialog != null && sendingMessageDialog.isShowing()){
                this.sendingMessageDialog.dismiss();
            }
        }
        else if(event.getFailedEvent() instanceof RequestUsersEvent){
            this.selectReceiverInProgress = false;
            if(loadReceiversDialog != null && loadReceiversDialog.isShowing()){
                this.loadReceiversDialog.dismiss();
            }
            String dialogText = "Loading users failed";
            makeShortToast(dialogText);
        }
    }

    @Subscribe
    public void onEvent(ReturnUsersEvent event){
        List<String> users = event.getUsers();
        if(users != null && !users.isEmpty()){
            showDialog(users);
        }
        else {
            this.selectReceiverInProgress = false;
            if(loadReceiversDialog != null && loadReceiversDialog.isShowing()){
                this.loadReceiversDialog.dismiss();
            }
            String dialogText = "No users found";
            makeShortToast(dialogText);
        }
    }

    private void showDialog(List<String> users) {

        // Remove any currently showing dialog
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

    protected void setSelectReceiverInProgress(boolean isInProgress){
        this.selectReceiverInProgress = isInProgress;
        if(loadReceiversDialog != null && loadReceiversDialog.isShowing()){
            this.loadReceiversDialog.dismiss();
        }
    }

    private void makeShortToast(String message){
        if(message != null) {
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(this, message, duration).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AuthorizedEventHandler.processResults(requestCode, resultCode, data);
    }
}
