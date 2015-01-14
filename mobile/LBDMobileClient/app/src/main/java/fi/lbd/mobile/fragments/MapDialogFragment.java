package fi.lbd.mobile.fragments;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.util.List;

import fi.lbd.mobile.DetailsActivity;
import fi.lbd.mobile.R;
import fi.lbd.mobile.StreetviewActivity;

/**
 * Created by Ossi on 4.1.2015.
 */
public class MapDialogFragment extends DialogFragment{
    private Button streetViewButton;
    private Button infoButton;

    public static MapDialogFragment newInstance() {
        MapDialogFragment fragment = new MapDialogFragment();
        return fragment;
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onStart(){
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.20f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
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
        View v = inflater.inflate(R.layout.fragment_map_dialog, container, false);

        // Watch for button clicks.
        this.infoButton = (Button)v.findViewById(R.id.infoButton);
        this.streetViewButton = (Button)v.findViewById(R.id.streetviewButton);

        infoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0); // Hides the transition animation
            }
        });

        streetViewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), StreetviewActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0); // Hides the transition animation
            }
        });

        return v;
    }
}

