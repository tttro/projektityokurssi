package com.example.namiskuukkel.login_test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Starter extends Activity implements View.OnClickListener{
    //Ints to identidy startActivityForResult requests
    static final int SOME_POSITIVE_INTEGER_TO_IDENTIFY_THE_REQUEST = 42;
    static final int ANOTHER_INT = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.disconnect).setOnClickListener(this);
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                //NOTENOTENOTENOTE
                //Starting the token and id providing activity here
                Intent i = new Intent(this, Login.class);
                // set the request code to any code you like,
                // you can identify the callback via this code
                startActivityForResult(i, SOME_POSITIVE_INTEGER_TO_IDENTIFY_THE_REQUEST);
                break;
            case R.id.disconnect:
                //This is here for testing purposes.
                //In this activity you can disconnect the app and revoke the access to the app
                //Accoding to the Google Developer policies these SHOULD BE INCLUDED SOMEWHERE
                //TODO: Include them in the app
                Intent j = new Intent(this, Disconnect.class);
                // set the request code to any code you like,
                // you can identify the callback via this code
                startActivityForResult(j, ANOTHER_INT);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SOME_POSITIVE_INTEGER_TO_IDENTIFY_THE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                //On a succesful login attempt, the activity returns token and id in an intent
                if (data.hasExtra("token")) {
                    TextView token = (TextView) findViewById(R.id.token);
                    token.setText(data.getExtras().getString("token"));
                }
                if (data.hasExtra("id")) {
                    TextView id = (TextView) findViewById(R.id.id);
                    id.setText(data.getExtras().getString("id"));
                }
            }
            //RESULT_CANCELED
            else {
                //On a failed attempt, the activity returns a human readable
                // error message in an intent
                if (data.hasExtra("error")) {
                    TextView error = (TextView) findViewById(R.id.error);
                    error.setText(data.getExtras().getString("error"));
                }
            }
        }
    }
}
