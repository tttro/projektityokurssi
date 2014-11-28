package fi.lbd.mobile.fragments;

import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.TreeMap;

import fi.lbd.mobile.ListActivity;
import fi.lbd.mobile.SelectionManager;
import fi.lbd.mobile.adapters.ListExpandableAdapter;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.R;
import fi.lbd.mobile.events.RequestNearObjectsEvent;
import fi.lbd.mobile.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.location.LocationHandler;
import fi.lbd.mobile.location.LocationUtils;
import fi.lbd.mobile.location.PointLocation;
import fi.lbd.mobile.mapobjects.MapObject;


/**
 * The map object list view. On click causes view change event to OTTO-bus.
 * Receives MapObjects from ObjectRepositoryService via OTTO-bus.
 *
 * Created by tommi on 19.10.2014.
 */
public class ObjectListFragment extends ListFragment {
    private ListExpandableAdapter adapter;
    private ExpandableListView expandableListView;
    private int lastExpanded;
    private EditText searchText;
    private LinearLayout dummyView;
    private TextView statusText;

    private LocationHandler locationHandler;
    // Lock and boolean variables used to prevent users from creating more than 1 locationtask
    // at a time, even if they tap the location button repeatedly.
    private static final Object LOCK = new Object();
    private static Boolean locationInProgress = new Boolean(false);
    LocationTask activeTask = null;

    private int firstVisiblePosition;
    private ArrayList<Boolean> groupExpandedArray;

    public static ObjectListFragment newInstance(LocationHandler locationHandler) {
        ObjectListFragment fragment = new ObjectListFragment();
        fragment.setLocationHandler(locationHandler);
        return fragment;
    }

    public void setLocationHandler(LocationHandler locationHandler) {
        this.locationHandler = locationHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.adapter = new ListExpandableAdapter(this.getActivity());
        this.groupExpandedArray = new ArrayList<Boolean>();
        this.firstVisiblePosition = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listview_search_fragment, container, false);

        // TODO: siirrä expandable-nuoli oikealle puolelle ettei mene tekstin päälle:
        // http://stackoverflow.com/questions/5800426/expandable-list-view-move-group-icon-indicator-to-right
        this.expandableListView = (ExpandableListView) view.findViewById(android.R.id.list);
        this.expandableListView.setOnGroupExpandListener(new ExpandListener());
        this.expandableListView.setOnGroupCollapseListener(new CollapseListener());
        this.lastExpanded = -1;
        this.searchText = (EditText)view.findViewById(R.id.searchText);
        this.dummyView = (LinearLayout)view.findViewById(R.id.dummyView);
        this.statusText = (TextView)view.findViewById(R.id.view_status_text);

        // Listen to keyboard search button press
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    hideKeyBoard();
                    hideCursor();
                    return true;
                }
                return false;
            }
        });

        view.findViewById(R.id.locationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNearestObjects();
                hideKeyBoard();
                hideCursor();
            }
        });

        // Hide keyboard and blinking cursor when "enter" or "back" key is pressed on soft keyboard
        this.searchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    hideKeyBoard();
                    hideCursor();
                    return true;
                }
                else if(keyCode == KeyEvent.KEYCODE_BACK) {
                    hideCursor();
                    hideKeyBoard();
                    return true;
                }
                else {
                    return false;
                }
            }
        });

        this.locationHandler.addListener( new GooglePlayServicesClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                synchronized (LOCK) {
                    if (!locationInProgress) {
                        activeTask = new LocationTask();
                        activeTask.execute();
                    }
                }
                locationHandler.removeListener(this);
            }
            @Override
            public void onDisconnected() {}
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.locationHandler.start();
        this.expandableListView.setAdapter(this.adapter);
        BusHandler.getBus().register(this);

        for (int i=0; i<groupExpandedArray.size() ;i++){
            if (groupExpandedArray.get(i))
                expandableListView.expandGroup(i);
        }
        this.expandableListView.setSelection(firstVisiblePosition);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHandler.getBus().unregister(this);
        this.locationHandler.stop();

        synchronized (LOCK){
            locationInProgress = false;
            activeTask = null;
        }
        statusText.setText("");

        int numberOfGroups = adapter.getGroupCount();
        this.groupExpandedArray.clear();
        for (int i=0;i<numberOfGroups;i++){
            this.groupExpandedArray.add(this.expandableListView.isGroupExpanded(i));
        }
        this.firstVisiblePosition = this.expandableListView.getFirstVisiblePosition();
        hideCursor();
        hideKeyBoard();
    }

    @Subscribe
    public void onEvent(ReturnNearObjectsEvent event) {
        this.adapter.clear();

        if (event.getMapObjects() != null) {
            this.adapter.addAll(event.getMapObjects());

            statusText.setText(String.format(getResources().getString(R.string.showing_nearest),
                    event.getMapObjects().size()));
        }
        else {
            statusText.setText(getResources().getString(R.string.no_nearest_found));
        }
        this.getListView().requestLayout();
        statusText.setBackgroundColor(getActivity().getResources().
                getColor(R.color.near_objects_background));


        synchronized (LOCK){
            this.activeTask = null;
            locationInProgress = false;
        }
    }

    public void hideKeyBoard() {
        if (this.searchText != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.searchText.getWindowToken(), 0);
        }
    }

    public void hideCursor(){
        if(this.searchText != null) {
            this.dummyView.setFocusable(true);
            this.dummyView.setFocusableInTouchMode(true);
            this.dummyView.requestFocus();
            this.searchText.setFocusable(false);
            this.searchText.setFocusableInTouchMode(true);
        }
    }

    // Collapse old expanded group and scroll to correct position
    public class ExpandListener implements ExpandableListView.OnGroupExpandListener {
        @Override
        public void onGroupExpand(int groupPosition) {
                if (lastExpanded >= 0 && lastExpanded != groupPosition){
                    expandableListView.collapseGroup(lastExpanded);
                }
              lastExpanded = groupPosition;
              expandableListView.smoothScrollToPositionFromTop(groupPosition, 0);
            }
        }

    public class CollapseListener implements ExpandableListView.OnGroupCollapseListener {
        @Override
        public void onGroupCollapse(int groupPosition) {
            if(lastExpanded == groupPosition){
                lastExpanded = -1;
            }
        }
    }

    // TODO: search functionality
    public void performSearch(){
        if (this.locationHandler != null && this.locationHandler.getLocationClient().isConnected()) {
            this.locationHandler.updateCachedLocation();
            statusText.setText(String.format(getResources().getString(R.string.showing_results), 0));
            statusText.setBackgroundColor(getActivity().getResources().
                    getColor(R.color.search_results_background));
        }
    }

    public void showNearestObjects() {

        synchronized (LOCK){
            if(!locationInProgress) {
                this.activeTask = new LocationTask();
                activeTask.execute();
            }
        }
    }

    private class LocationTask extends AsyncTask<Void, Void, Boolean>{
        @Override
        protected void onPreExecute(){

            synchronized (LOCK){
                locationInProgress = true;
            }
            statusText.setText(getResources().getString(R.string.loading));
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            for (int i = 1; i < 5; ++i) {
                if (locationHandler != null && locationHandler.getLocationClient().isConnected()) {
                    locationHandler.updateCachedLocation();
                    if (locationHandler.getCachedLocation() != null) {
                        BusHandler.getBus().post(new RequestNearObjectsEvent(new ImmutablePointLocation(
                                locationHandler.getCachedLocation().getLatitude(),
                                locationHandler.getCachedLocation().getLongitude()), 0.001, false));
                        return true;
                    }
                }
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException exception){
                    Log.d("*****", "Cancelled location task in interruptException.");
                    synchronized (LOCK){
                        locationInProgress = false;
                        activeTask = null;
                        statusText.setText(getResources().getString(R.string.location_failed));
                    }
                    return false;
                }
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(!result) {
                synchronized (LOCK){
                    locationInProgress = false;
                    activeTask = null;
                }
                statusText.setText(getResources().getString(R.string.location_failed));
            }
        }
    }

 }


