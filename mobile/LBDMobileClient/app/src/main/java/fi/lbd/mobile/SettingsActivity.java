package fi.lbd.mobile;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import fi.lbd.mobile.backendhandler.BackendHandlerService;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestCollectionsEvent;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.events.ReturnCollectionsEvent;
import fi.lbd.mobile.messaging.events.RequestUserMessagesEvent;


public class SettingsActivity extends Activity {

    private String BACKEND_URL = "";
    private String OBJECT_COLLECTION = "";
    private RadioGroup radioGroup;
    private View rootView;
    private EditText urlText;
    private TextView selectCollectionText;
    private Button acceptButton;
    private ServiceConnection backendHandlerConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(this.getClass().getSimpleName(), "----- Creating SettingsActivity");
        setContentView(R.layout.activity_settings);
        BACKEND_URL = getResources().getString(R.string.backend_url);
        OBJECT_COLLECTION = getResources().getString(R.string.object_collection);

        this.rootView = this.findViewById(android.R.id.content);
        this.urlText = ((EditText)rootView.findViewById(R.id.backendUrlText));
        this.selectCollectionText = (TextView)rootView.findViewById(R.id.collectionText);
        this.radioGroup = (RadioGroup)rootView.findViewById(R.id.linearLayout3)
                .findViewById(R.id.scrollView).findViewById(R.id.radioGroup);
        this.acceptButton = ((Button)rootView.findViewById(R.id.linearLayout3).findViewById(R.id.linearLayout).
                findViewById(R.id.acceptButton));

        // When user changes the URL, hide accept button and "Select collection" text,
        // and remove all radiobuttons
        urlText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearRadioGroup();
                acceptButton.setVisibility(View.INVISIBLE);
                selectCollectionText.setVisibility(View.INVISIBLE);
            }
        });
        // When user clicks "Done" on keyboard, close the keyboard
        urlText.setOnKeyListener(onSoftKeyboardDonePress);

        backendHandlerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                BusHandler.getBus().post(new RequestCollectionsEvent(ApplicationDetails.get().getCurrentBaseApiUrl()));
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(this.getClass().getSimpleName(), "----- Resuming SettingsActivity");
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
        Log.d(this.getClass().getSimpleName(), "-----Pausing and unbinding SettingsActivity");
        super.onPause();
        // Unbind binded service to prevent from losing a bind while on pause
        try {
            unbindService(backendHandlerConnection);
        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(), "-----onDestroy trying to unbind a nonexisting service.");
        }
        BusHandler.getBus().unregister(this);
    }

    public void onLoadClick(View view){
        String url = urlText.getText().toString();
        if(url != null){
            // Stop and unbind old service if it exists
//            try {
//                unbindService(backendHandlerConnection);
//            } catch (Exception e){
//                Log.d("------", e.getMessage());
//               // Log.d("------", e.getCause().toString());
//                Log.d(this.getClass().getSimpleName(), "----onLoadClick trying to unbind a nonexisting service.");
//            }
//            ServiceManager.stopBackendService();

            // Attempt to create a new service with the new URL
            ApplicationDetails.get().setCurrentBaseApiUrl(url);
//            ServiceManager.startBackendService();
            bindService(new Intent(this, BackendHandlerService.class), backendHandlerConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void onAcceptClick(View view){
        String checkedCollection = null;

        RadioButton checkedButton = (RadioButton)this.radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
        if(checkedButton != null) {
            checkedCollection = checkedButton.getText().toString();

            String url = urlText.getText().toString();
            Log.d(getClass().getSimpleName(), " Saving settings to ApplicationDetails url: "+ url);
            ApplicationDetails.get().setCurrentCollection(checkedCollection);
            ApplicationDetails.get().setCurrentBaseApiUrl(url);

            Log.d(getClass().getSimpleName(), " Saving settings to SharedPreferences");
            SharedPreferences settings = getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(BACKEND_URL, url);
            editor.putString(OBJECT_COLLECTION, checkedCollection);
            editor.apply();

            BusHandler.getBus().post(new RequestUserMessagesEvent());
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
        }
        else {
            makeShortToast("Please select a collection");
        }
    }

    @Subscribe
    public void onEvent(ReturnCollectionsEvent event){
        Log.d("------", "ReturnCollectionsEvent");
        // Clear the radiogroup and load new radiobuttons to view
        clearRadioGroup();
        RadioGroup.LayoutParams layoutParams;
        int i = 0;
        for(String collection : event.getCollections()){
            RadioButton radioButton = new RadioButton(this);
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

        // Finally, set accept button and "Select object collection" text visible
        this.acceptButton.setVisibility(View.VISIBLE);
        this.selectCollectionText.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onEvent(RequestFailedEvent event){
        if(event.getFailedEvent() instanceof RequestCollectionsEvent){
            Log.d("--------", "RequestFailedEvent");
            makeShortToast("Please check URL");

            // Clear the radiogroup and add a radiobutton with "empty" text
            clearRadioGroup();
            addEmptyRadioButton();

            // Finally, set accept button and "Select object collection" text visible
            this.acceptButton.setVisibility(View.VISIBLE);
            this.selectCollectionText.setVisibility(View.VISIBLE);
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
            RadioButton radioButton = new RadioButton(this);
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
        InputMethodManager manager = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(urlText.getApplicationWindowToken(), 0);
    }

    private void makeShortToast(String message){
        if(message != null) {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, message, duration).show();
        }
    }
}
