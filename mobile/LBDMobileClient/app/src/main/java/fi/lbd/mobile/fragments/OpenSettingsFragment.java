package fi.lbd.mobile.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import fi.lbd.mobile.R;
import fi.lbd.mobile.SettingsActivity;

/**
 * Created by Ossi on 14.1.2015.
 */
public class OpenSettingsFragment extends Fragment{
    public static OpenSettingsFragment newInstance() {
        return new OpenSettingsFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate UI
        View view = inflater.inflate(R.layout.fragment_open_settings, container, false);

        view.findViewById(R.id.openSettingsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
