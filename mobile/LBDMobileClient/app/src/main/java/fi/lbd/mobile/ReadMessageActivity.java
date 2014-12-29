package fi.lbd.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import fi.lbd.mobile.messageobjects.StringMessageObject;


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

    // TODO: Viestin deletointi
    public void onDeleteClick(View view){
        onBackPressed();
    }

    public void onReplyClick(View view){
        Intent intent = new Intent(this, SendMessageActivity.class);
        intent.putExtra("Replying", true);
        startActivity(intent);
    }
}
