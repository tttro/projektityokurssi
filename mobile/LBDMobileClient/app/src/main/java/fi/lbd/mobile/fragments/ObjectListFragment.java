package fi.lbd.mobile.fragments;

import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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

import fi.lbd.mobile.adapters.ListExpandableAdapter;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.R;
import fi.lbd.mobile.events.RequestNearObjectsEvent;
import fi.lbd.mobile.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.location.LocationHandler;

/**
 * Fragment that shows objects using an expandable list view.
 *
 * Receives MapObjects from ObjectRepositoryService via OTTO-bus.
 * Uses LocationHandler to get user location.
 *
 * Created by Tommi & Ossi
 */
public class ObjectListFragment extends ListFragment {
    // Declare strings used in the status bar.
    // Defined in onCreateView(), since strings.xml cannot be accessed here.
    private final String EMPTY = "";
    private String LOADING;
    private String LOCATION_FAILED;
    private String SHOWING_NEAREST;
    private String NO_NEAREST;
    private String SEARCH_RESULTS;

    // Declare background colors used in the status bar.
    // Defined in onCreateView(), since colors.xml cannot be accessed here.
    private int LOCATION_BACKGROUND;
    private int SEARCH_BACKGROUND;

    private ListExpandableAdapter adapter;
    private ExpandableListView expandableListView;
    private int lastExpanded;
    private EditText searchText;
    private LinearLayout dummyView;
    private TextView statusText;
    private String lastStatusText = EMPTY;
    private int lastStatusBackground = 0;

    private LocationHandler locationHandler;
    // Lock and boolean variables used to prevent users from creating more than one LocationTask
    // at a time, even if they tap the location button repeatedly.
    private static final Object LOCK = new Object();
    private static Boolean locationInProgress = false;
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

        LOADING = getActivity().getString(R.string.loading);
        LOCATION_FAILED = getActivity().getString(R.string.location_failed);
        SHOWING_NEAREST = getActivity().getString(R.string.showing_nearest);
        NO_NEAREST = getActivity().getString(R.string.no_nearest_found);
        SEARCH_RESULTS = getActivity().getString(R.string.showing_results);
        LOCATION_BACKGROUND = getActivity().getResources().getColor(R.color
                .near_objects_background);
        SEARCH_BACKGROUND = getActivity().getResources().getColor(R.color
                .search_results_background);
        lastStatusBackground = LOCATION_BACKGROUND;

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

        // Listen to location button press
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

        // Listen to onConnected callback on LocationHandler
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

        // Return states of the expanded list elements as they were
        for (int i = 0; i < groupExpandedArray.size(); ++i){
            if (groupExpandedArray.get(i))
                expandableListView.expandGroup(i);
        }
        this.expandableListView.setSelection(firstVisiblePosition);

        // Return text and background of the status bar as they were
        if(statusText.getText().equals(EMPTY)){
            statusText.setText(lastStatusText);
            statusText.setBackgroundColor(lastStatusBackground);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHandler.getBus().unregister(this);
        this.locationHandler.stop();

        // Set active location task as finished, clear the status bar text
        synchronized (LOCK){
            locationInProgress = false;
            activeTask = null;
        }
        statusText.setText(EMPTY);

        // Save expandable list view item states
        int numberOfGroups = adapter.getGroupCount();
        this.groupExpandedArray.clear();
        for (int i=0;i<numberOfGroups;i++){
            this.groupExpandedArray.add(this.expandableListView.isGroupExpanded(i));
        }
        this.firstVisiblePosition = this.expandableListView.getFirstVisiblePosition();
        hideCursor();
        hideKeyBoard();
    }

    /**
     *  Receive nearest objects from BackendHandler by OTTO bus.
     *  Updates the list view and status bar accordingly.
     */
    @Subscribe
    public void onEvent(ReturnNearObjectsEvent event) {
        this.adapter.clear();

        statusText.setBackgroundColor(LOCATION_BACKGROUND);
        lastStatusBackground = LOCATION_BACKGROUND;
        if (event.getMapObjects() != null) {
            this.adapter.addAll(event.getMapObjects());
            statusText.setText(String.format(SHOWING_NEAREST, event.getMapObjects().size()));
            lastStatusText = String.format(SHOWING_NEAREST, event.getMapObjects().size());
        }
        else {
            statusText.setText(NO_NEAREST);
            lastStatusText = NO_NEAREST;
        }
        this.getListView().requestLayout();

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

    // When expanding a list element: collapse old expanded element and scroll to correct position
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
            statusText.setText(String.format(SEARCH_RESULTS, 0));
            lastStatusText = String.format(SEARCH_RESULTS, 0);
            statusText.setBackgroundColor(SEARCH_BACKGROUND);
            lastStatusBackground = SEARCH_BACKGROUND;
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

    /**
     *  AsyncTask that is created when user presses the location button.
     *
     *  Updates the user location by LocationHandler, then asks for a new set of
     *  near objects from BackendHandler using an OTTO bus event.
     *
     *  @return Returns true if user location was successful and OTTO event was successfully sent.
     *  @return Returns false if user location was unsuccessful during a 5s timeout, or if the task
     *  is cancelled.
     */
    private class LocationTask extends AsyncTask<Void, Void, Boolean>{
        @Override
        protected void onPreExecute(){
            synchronized (LOCK){
                locationInProgress = true;
            }
            statusText.setText(LOADING);
            statusText.setBackgroundColor(LOCATION_BACKGROUND);
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
                statusText.setText(LOCATION_FAILED);
                lastStatusText = LOCATION_FAILED;
                lastStatusBackground = LOCATION_BACKGROUND;
            }
        }
    }
 }


