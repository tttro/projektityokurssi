package fi.lbd.mobile;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;


public class StreetviewActivity extends FragmentActivity implements StreetViewPanorama.OnStreetViewPanoramaChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streetview);
        StreetViewPanoramaFragment streetview = (StreetViewPanoramaFragment)getFragmentManager()
                .findFragmentById(R.id.streetviewpanorama);

    }


}
