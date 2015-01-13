package fi.lbd.mobile.messaging;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.List;

import fi.lbd.mobile.R;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.messaging.events.SelectReceiverEvent;

/**
 * Created by Ossi on 4.1.2015.
 */
public class SelectReceiverDialogFragment extends DialogFragment{
    private List<String> users;

    public static SelectReceiverDialogFragment newInstance(List<String> users) {
        SelectReceiverDialogFragment fragment = new SelectReceiverDialogFragment();

        // Supply arguments
        fragment.setArguments(users);
        return fragment;
    }

    public void setArguments(List<String> args){
        this.users = args;
    }

    @Override
    public void onStop(){
        super.onStop();
        ((SendMessageActivity)getActivity()).setSelectReceiverInProgress(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int style = DialogFragment.STYLE_NORMAL, theme = android.R.style.Theme_Holo_Light_Dialog_NoActionBar;
        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.select_message_receiver_fragment, container, false);

        final RadioGroup radioGroup = (RadioGroup)v.findViewById(R.id.radioGroup);
        RadioGroup.LayoutParams layoutParams;
        for(int i=0; i<users.size(); i++){
            RadioButton radioButton = new RadioButton(this.getActivity());
            radioButton.setText(users.get(i));
            radioButton.setId(i);
            layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            radioGroup.addView(radioButton, layoutParams);
        }

        // Watch for button clicks.
        Button button = (Button)v.findViewById(R.id.selectButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity.
                String selectedUser = null;
                int checkedIndex = radioGroup.getCheckedRadioButtonId();
                if(checkedIndex != -1) {
                    selectedUser = users.get(checkedIndex);
                }
                BusHandler.getBus().post(new SelectReceiverEvent(selectedUser));
            }
        });

        return v;
    }
}
