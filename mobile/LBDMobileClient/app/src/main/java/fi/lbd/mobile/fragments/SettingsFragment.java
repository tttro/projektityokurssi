package fi.lbd.mobile.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import fi.lbd.mobile.ActiveActivitiesTracker;
import fi.lbd.mobile.ApplicationDetails;
import fi.lbd.mobile.ListActivity;
import fi.lbd.mobile.R;
import fi.lbd.mobile.backendhandler.BackendHandlerService;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestCollectionsEvent;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.events.ReturnCollectionsEvent;
import fi.lbd.mobile.messaging.events.RequestUserMessagesEvent;

public class SettingsFragment extends Fragment {
    private String BACKEND_URL = "";
    private String OBJECT_COLLECTION = "";
    private RadioGroup radioGroup;
    private View rootView;
    private EditText urlText;
    private TextView selectCollectionText;
    private Button acceptButton;
    private Button loadButton;
    private ServiceConnection backendHandlerConnection;

    private ProgressDialog progressDialog;

    private boolean isAcceptClicked = false;
    private boolean isLoadCollectionsClicked = false;

    public static SettingsFragment newInstance(){
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate UI
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Find UI elements
        this.rootView = view;
        this.urlText = ((EditText)rootView.findViewById(R.id.backendUrlText));
        this.selectCollectionText = (TextView)rootView.findViewById(R.id.collectionText);
        this.radioGroup = (RadioGroup)rootView.findViewById(R.id.linearLayout3)
                .findViewById(R.id.scrollView).findViewById(R.id.radioGroup);
        this.acceptButton = ((Button)rootView.findViewById(R.id.linearLayout3).findViewById(R.id.linearLayout).
                findViewById(R.id.acceptButton));
        this.loadButton = ((Button)rootView.findViewById(R.id.linearLayout2).findViewById(R.id.loadButton));

        if(getActivity() instanceof ListActivity) {
            rootView.findViewById(R.id.textView2).setVisibility(View.INVISIBLE);
        }

        acceptButton.setOnClickListener(new AcceptListener());
        loadButton.setOnClickListener(new LoadListener());

        // When user changes the URL, hide accept button and "Select collection" text,
        // remove all radiobuttons, and set "Load Collections" and "Accept" buttons clickable.
        urlText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearRadioGroup();
                acceptButton.setVisibility(View.INVISIBLE);
                selectCollectionText.setVisibility(View.INVISIBLE);
                isLoadCollectionsClicked = false;
                isAcceptClicked = false;
            }
        });

        // When user clicks "Done" on keyboard, close the keyboard
        urlText.setOnKeyListener(onSoftKeyboardDonePress);

        /*
        //  Connection that represents binding to BackendHandlerService.
        //  Only used to provide onServiceConnected callback.
         */
        backendHandlerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                progressDialog = ProgressDialog.show(getActivity(), "", "Downloading collections...", true);
                progressDialog.setCancelable(false);
                Log.d("--------", "onServiceConnected, sending RequestCollectionsEvent, and unbinding");
                BusHandler.getBus().post(new RequestCollectionsEvent(ApplicationDetails.get().getCurrentBaseApiUrl()));
                getActivity().unbindService(this);
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(this.getClass().getSimpleName(), "----- Creating SettingsFragment");
        BACKEND_URL = getResources().getString(R.string.backend_url);
        OBJECT_COLLECTION = getResources().getString(R.string.object_collection);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(this.getClass().getSimpleName(), "----- Resuming SettingsFragment");
        BusHandler.getBus().register(this);
        hideSoftKeyboard();

        // Restore settings that were last selected
        String url = ApplicationDetails.get().getCurrentBaseApiUrl();
        if(url != null){
            urlText.setText(url);
            BusHandler.getBus().post(new RequestCollectionsEvent(url));
        }
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
    }

    @Override
    public void onPause(){
        super.onPause();
        BusHandler.getBus().unregister(this);

        // Try to unbind binded service to ensure no binds are leaked when going to pause
        try {
            this.getActivity().unbindService(backendHandlerConnection);
        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(), "-----onPause trying to unbind a nonexisting bind.");
        }
    }

    /*
     //  Clicking the load button updates new url for BackendHandlerService.
     //
     //  Also binds to BackendHandlerService to get a callback when the Service is connected with
     //  its new URL.
      */
    private class LoadListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            if(!isLoadCollectionsClicked) {
                isLoadCollectionsClicked = true;
                String url = urlText.getText().toString();
                if (url != null) {
                    ApplicationDetails.get().setCurrentBaseApiUrl(url);
                    getActivity().bindService(new Intent(getActivity(), BackendHandlerService.class),
                            backendHandlerConnection, Context.BIND_AUTO_CREATE);
                }
            }
        }
    }

    /*
    //  Clicking the Accept button saves new settings and starts ListActivity
    //
     */
    private class AcceptListener implements Button.OnClickListener{
        @Override
        public void onClick(View view){
            if(!isAcceptClicked) {
                isAcceptClicked = true;
                String checkedCollection = null;

                RadioButton checkedButton = (RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
                if (checkedButton != null) {
                    checkedCollection = checkedButton.getText().toString();

                    String url = urlText.getText().toString();
                    Log.d(getClass().getSimpleName(), " Saving settings to ApplicationDetails url: " + url);
                    ApplicationDetails.get().setCurrentCollection(checkedCollection);
                    ApplicationDetails.get().setCurrentBaseApiUrl(url);

                    Log.d(getClass().getSimpleName(), " Saving settings to SharedPreferences");
                    SharedPreferences settings = getActivity().getSharedPreferences(getString(R.string.shared_preferences),
                            Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(BACKEND_URL, url);
                    editor.putString(OBJECT_COLLECTION, checkedCollection);
                    editor.apply();

                    BusHandler.getBus().post(new RequestUserMessagesEvent());
                    Intent intent = new Intent(getActivity(), ListActivity.class);
                    startActivity(intent);
                } else {
                    makeShortToast("Please select a collection");
                }
            }
        }
    }

    @Subscribe
    public void onEvent(ReturnCollectionsEvent event){
        // Clear the radiogroup and load new radiobuttons to view
        Log.d("------", "ReturnCollectionsEvent");
        clearRadioGroup();
        RadioGroup.LayoutParams layoutParams;
        int i = 0;
        for(String collection : event.getCollections()){
            RadioButton radioButton = new RadioButton(this.getActivity());
            radioButton.setText(collection);
            radioButton.setId(i);
            layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            radioGroup.addView(radioButton, layoutParams);
            ++i;
        }

        // If a radiobutton was selected before, select it
        String collection = ApplicationDetails.get().getCurrentCollection();
        if(collection != null) {
            int count = radioGroup.getChildCount();
            for (int j=0;j<count;j++) {
                View o = radioGroup.getChildAt(j);
                if (o instanceof RadioButton && ((RadioButton)o).getText().equals(collection)) {
                    radioGroup.check((o).getId());
                    break;
                }
            }
        }

        // Finally, set accept button and "Select object collection" text visible, and dismiss dialog
        this.acceptButton.setVisibility(View.VISIBLE);
        this.selectCollectionText.setVisibility(View.VISIBLE);
        if(progressDialog != null && progressDialog.isShowing()){
            this.progressDialog.dismiss();
        }
    }

    @Subscribe
    public void onEvent(RequestFailedEvent event){
        if(event.getFailedEvent() instanceof RequestCollectionsEvent){
            Log.d("--------", "RequestFailedEvent");
            makeShortToast("Please check URL");

            // Clear the radiogroup and add a radiobutton with "empty" text
            clearRadioGroup();
            addEmptyRadioButton();

            // Finally, set accept button and "Select object collection" text visible, and dismiss dialog
            this.acceptButton.setVisibility(View.VISIBLE);
            this.selectCollectionText.setVisibility(View.VISIBLE);
            if(progressDialog != null && progressDialog.isShowing()){
                this.progressDialog.dismiss();
            }
        }
    }

    // Clear the radiogroup, including checked button
    private void clearRadioGroup(){
        if(this.radioGroup != null) {
            RadioButton checkedButton = (RadioButton) this.radioGroup.findViewById(this.radioGroup.getCheckedRadioButtonId());
            if (checkedButton != null) {
                this.radioGroup.clearCheck();
            }
            this.radioGroup.removeAllViews();
        }
    }

    // Add an empty radio button to the radiogroup
    private void addEmptyRadioButton() {
        if (this.radioGroup != null) {
            RadioGroup.LayoutParams layoutParams;
            RadioButton radioButton = new RadioButton(this.getActivity());
            radioButton.setText("Empty (you won't see any objects)");
            radioButton.setId(0);
            layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            radioGroup.addView(radioButton, layoutParams);
            radioGroup.check(radioButton.getId());
        }
    }

    // Listener to close the soft keyboard when user presses "Done"
    private View.OnKeyListener onSoftKeyboardDonePress=new View.OnKeyListener(){
        public boolean onKey(View v, int keyCode, KeyEvent event){
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                hideSoftKeyboard();
            }
            return false;
        }
    };

    private void hideSoftKeyboard(){
        InputMethodManager manager = (InputMethodManager) this.getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(urlText.getApplicationWindowToken(), 0);
    }

    private void makeShortToast(String message){
        if(message != null) {
            Context context = this.getActivity().getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, message, duration).show();
        }
    }
}
