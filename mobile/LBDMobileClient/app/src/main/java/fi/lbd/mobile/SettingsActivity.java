package fi.lbd.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestCollectionsEvent;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.events.ReturnCollectionsEvent;


public class SettingsActivity extends Activity {

    private String BACKEND_URL = "";
    private String OBJECT_COLLECTION = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        BusHandler.getBus().register(this);
        BACKEND_URL = getResources().getString(R.string.backend_url);
        OBJECT_COLLECTION = getResources().getString(R.string.object_collection);
    }

    @Override
    public void onResume(){
        super.onResume();
        View rootView = this.findViewById(android.R.id.content);

        // Restore settings that were last selected
        String url = ApplicationDetails.get().getCurrentBackendUrl();
        if(url != null){
            ((EditText)rootView.findViewById(R.id.backendUrlText)).setText(url);
            BusHandler.getBus().post(new RequestCollectionsEvent(url));
        }
    }

    public void onLoadClick(View view){
        String url = ((EditText)((View)(view.getParent().getParent())).findViewById(R.id.backendUrlText)).
                getText().toString();
        if(url != null){
            BusHandler.getBus().post(new RequestCollectionsEvent(url));
        }
    }

    public void onAcceptClick(View view){
        View rootView = this.findViewById(android.R.id.content);
        String checkedCollection = null;
        final RadioGroup radioGroup = (RadioGroup)rootView.findViewById(R.id.linearLayout3)
                .findViewById(R.id.scrollView).findViewById(R.id.radioGroup);

        RadioButton checkedButton = (RadioButton)radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
        if(checkedButton != null) {
            checkedCollection = checkedButton.getText().toString();
        }
        String url = ((EditText)((View)(view.getParent().getParent().getParent())).findViewById(R.id.backendUrlText)).
                getText().toString();

        Log.d(getClass().getSimpleName(), " Saving settings to ApplicationDetails");
        if(checkedCollection != null && url != null) {

            // TODO: selvitä
            if (checkedCollection.equals("Ring around the rosie")) {
                checkedCollection = "Playgrounds";
            } else if (checkedCollection.equals("Tampere Streetlights")) {
                checkedCollection = "Streetlights";
            }
        }
        ApplicationDetails.get().setCurrentCollection(checkedCollection);
        ApplicationDetails.get().setCurrentBackendUrl(url);

        Log.d(getClass().getSimpleName(), " Saving settings to SharedPreferences");
        SharedPreferences settings = getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(BACKEND_URL, url);
        editor.putString(OBJECT_COLLECTION, checkedCollection);
        editor.apply();

        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    @Subscribe
    public void onEvent(ReturnCollectionsEvent event){

        // Clear the radiogroup and load new radiobuttons to view
        View rootView = this.findViewById(android.R.id.content);
        final RadioGroup radioGroup = (RadioGroup)rootView.findViewById(R.id.linearLayout3)
                .findViewById(R.id.scrollView).findViewById(R.id.radioGroup);
        RadioGroup.LayoutParams layoutParams;
        RadioButton checkedButton = (RadioButton)radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
        if(checkedButton != null) {
            radioGroup.clearCheck();
        }
        radioGroup.removeAllViews();
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
    }

    @Subscribe
    public void onEvent(RequestFailedEvent event){
        if(event.getFailedEvent() instanceof RequestCollectionsEvent){
            Context context = getApplicationContext();
            CharSequence dialogText = "Please check URL";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, dialogText, duration).show();

            // Clear the radiogroup
            View rootView = this.findViewById(android.R.id.content);
            final RadioGroup radioGroup = (RadioGroup)rootView.findViewById(R.id.linearLayout3)
                    .findViewById(R.id.scrollView).findViewById(R.id.radioGroup);
            RadioGroup.LayoutParams layoutParams;
            RadioButton checkedButton = (RadioButton)radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
            if(checkedButton != null) {
                radioGroup.clearCheck();
            }
            radioGroup.removeAllViews();

            RadioButton radioButton = new RadioButton(this);
            radioButton.setText("Empty");
            radioButton.setId(0);
            layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            radioGroup.addView(radioButton, layoutParams);
        }
    }
}
