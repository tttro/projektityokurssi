package fi.lbd.mobile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.MapObjectSelectionManager;


public class StreetviewActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streetview);
    }

    // TODO: Check if Streetview exists for a given location
    @Override
    public void onStart(){
        super.onStart();
        ActiveActivitiesTracker.activityStarted();
        StreetViewPanoramaFragment streetview = (StreetViewPanoramaFragment)getFragmentManager()
                .findFragmentById(R.id.streetviewpanorama);
        StreetViewPanorama panorama = streetview.getStreetViewPanorama();
        MapObject object = MapObjectSelectionManager.get().getSelectedMapObject();
        if(panorama != null && object != null) {
            panorama.setPosition(new LatLng(object.getPointLocation().getLatitude(),
                    object.getPointLocation().getLongitude()));
            String urlBase = "http://maps.googleapis.com/maps/api/streetview?size=400x400&";
            String urlEnd = "&fov=90&heading=235&pitch=10&sensor=false";
            String location = "location="+Double.toString(object.getPointLocation().getLatitude())+","+
                    Double.toString(object.getPointLocation().getLongitude());
            String url = urlBase + location + urlEnd;
            new StreetViewTask().execute(url);
        }
        else {
            onBackPressed();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        ActiveActivitiesTracker.activityStopped();
    }

    private class StreetViewTask extends AsyncTask<String, Void, Boolean>{
        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(urls[0]).openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    Log.d(getClass().getSimpleName(), " Response code != 200");
                }
                else {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                }
            } catch(MalformedURLException e) {
                Log.d(getClass().getSimpleName(), e.getMessage());
            } catch (IOException e){
                Log.d(getClass().getSimpleName(), e.getMessage());
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

        }

    }
}
