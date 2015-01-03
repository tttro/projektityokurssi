package fi.lbd.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import fi.lbd.mobile.messageobjects.MessageObject;


public class SendMessageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
    }

    @Override
    public void onResume(){
        super.onResume();

        if(getIntent().getSerializableExtra("Replying") != null &&
                (boolean)getIntent().getSerializableExtra("Replying") == true){
            MessageObject object = MessageObjectSelectionManager.get().getSelectedMessageObject();
            View rootView = findViewById(android.R.id.content);
            ((TextView)rootView.findViewById(R.id.textViewReceiver)).setText(object.getSender());
            ((TextView)rootView.findViewById(R.id.textViewTitle)).setText("RE: " + object.getTopic());
        }
    }

    public void onSelectReceiverClick(View view){

    }

    public void onSendClick(View view){

    }
}
