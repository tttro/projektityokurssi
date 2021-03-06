package fi.lbd.mobile.fragments;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import fi.lbd.mobile.ListActivity;
import fi.lbd.mobile.R;
import fi.lbd.mobile.adapters.ListExpandableAdapter;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.location.LocationHandler;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.events.RequestNearObjectsEvent;
import fi.lbd.mobile.mapobjects.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.mapobjects.events.ReturnSearchResultEvent;
import fi.lbd.mobile.mapobjects.events.SearchObjectsEvent;

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
    private final String EMPTY = "No objects to show";
    private String LOCATION_FAILED;
    private String SHOWING_NEAREST;
    private String NO_NEAREST;
    private String SEARCH_FAILED;
    private String SEARCH_RESULTS;
    private String NO_RESULTS;
    private String MAX_RESULTS;
    private String MORE_CHARACTERS;
    private final Integer MAX_RESULTS_AMOUNT = 20;

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
    private int lastStatusBackground = LOCATION_BACKGROUND;
    private ProgressDialog progressDialog;

    // Lock and boolean variables used to prevent users from flooding the backend with searches,
    // even if they tap the location button or search button repeatedly.
    private static final Object LOCK = new Object();
    private static Boolean searchInProgress = false;
    private LocationHandler locationHandler;

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
        Log.d(this.getClass().getSimpleName(), " onCreateView");
        View view = inflater.inflate(R.layout.listview_search_fragment, container, false);

        if (this.getActivity() instanceof ListActivity) {
            ListActivity listActivity = (ListActivity)this.getActivity();
            this.setLocationHandler(listActivity.getLocationHandler());
        }

        LOCATION_FAILED = getActivity().getString(R.string.location_failed);
        SHOWING_NEAREST = getActivity().getString(R.string.showing_nearest);
        NO_NEAREST = getActivity().getString(R.string.no_nearest_found);
        SEARCH_FAILED = getActivity().getString(R.string.search_failed);
        SEARCH_RESULTS = getActivity().getString(R.string.showing_results);
        NO_RESULTS = getActivity().getString(R.string.no_results);
        MAX_RESULTS = getActivity().getString(R.string.max_results);
        MORE_CHARACTERS = getActivity().getString(R.string.more_characters);
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
                    performSearch(v.getText());
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
                showNearestObjects();
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
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHandler.getBus().unregister(this);
        this.locationHandler.stop();

        // Set active location/search task as finished, clear the status bar text
        synchronized (LOCK){
            dismissDialog();
            Log.d(getClass().toString(), " onPause(). Releasing lock.");
            searchInProgress = false;
        }

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
     *
     *  Receives nearest objects from BackendHandler by OTTO bus.
     *
     *  Updates the list view and status bar accordingly.
     *
     */
    @Subscribe
    public void onEvent(ReturnNearObjectsEvent event) {
        if (event.getMapObjects() != null && event.getMapObjects().size() > 0) {
            this.adapter.clear();
            statusText.setBackgroundColor(LOCATION_BACKGROUND);
            lastStatusBackground = LOCATION_BACKGROUND;

            if(event.getMapObjects().size() > MAX_RESULTS_AMOUNT){
                ArrayList<MapObject> mapObjects = new ArrayList<MapObject>();
                for(MapObject object : event.getMapObjects()){
                    mapObjects.add(object);
                    if(mapObjects.size() == MAX_RESULTS_AMOUNT){
                        break;
                    }
                }
                this.adapter.addAll(mapObjects);
                statusText.setText(String.format(SHOWING_NEAREST, MAX_RESULTS_AMOUNT));
                lastStatusText = String.format(SHOWING_NEAREST, MAX_RESULTS_AMOUNT);
            }
            else {
                this.adapter.addAll(event.getMapObjects());
                statusText.setText(String.format(SHOWING_NEAREST, event.getMapObjects().size()));
                lastStatusText = String.format(SHOWING_NEAREST, event.getMapObjects().size());
            }
        }
        else {
            makeLongToast(NO_NEAREST);
        }
        this.getListView().requestLayout();
        if(lastExpanded >=0 && lastExpanded < adapter.getGroupCount()) {
            expandableListView.collapseGroup(lastExpanded);
        }
        this.adapter.notifyDataSetChanged();

        synchronized (LOCK){
            dismissDialog();
            Log.d("__________","Locationtask results received. Releasing lock.");
            searchInProgress = false;
        }
    }

    /*
     *  Receives search results from BackendHandler by OTTO bus.
     *
     *  Updates the list view and status bar accordingly.
     */
    @Subscribe
    public void onEvent (ReturnSearchResultEvent event){
        if (event.getMapObjects() != null && event.getMapObjects().size() > 0) {
            statusText.setBackgroundColor(SEARCH_BACKGROUND);
            lastStatusBackground = SEARCH_BACKGROUND;
            this.adapter.clear();
            if(event.getMapObjects().size() == MAX_RESULTS_AMOUNT) {
                statusText.setText(String.format(MAX_RESULTS, MAX_RESULTS_AMOUNT));
            }
            else {
                statusText.setText(String.format(SEARCH_RESULTS, event.getMapObjects().size()));
            }
            lastStatusText = statusText.getText().toString();
            this.adapter.addAll(event.getMapObjects());
            if(lastExpanded >=0 && lastExpanded < adapter.getGroupCount()) {
                expandableListView.collapseGroup(lastExpanded);
            }
            this.getListView().requestLayout();
            this.adapter.notifyDataSetChanged();
        }
        else {
            makeLongToast(NO_RESULTS);
        }

        synchronized (LOCK){
            dismissDialog();
            Log.d("__________","Search results received. Releasing lock.");
            searchInProgress = false;
        }
    }

    /*
     *  If BackendService was unable to fetch new objects from the backend,
     *  update status text and release lock.
     */
    @Subscribe
    public void onEvent(RequestFailedEvent event) {
        statusText.setBackgroundColor(lastStatusBackground);

        if (event.getFailedEvent() instanceof RequestNearObjectsEvent) {
            makeLongToast(LOCATION_FAILED);
        }
        else if (event.getFailedEvent() instanceof SearchObjectsEvent) {
            makeLongToast(SEARCH_FAILED);
        }
        this.getListView().requestLayout();
        synchronized (LOCK){
            dismissDialog();
            Log.d(getClass().toString(),"Error received. Releasing lock.");
            searchInProgress = false;
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

    // When expanding a list element, collapses old expanded element and scrolls to correct position
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

    /*
     *  Performs search from backend using OTTO bus event "SearchObjectsEvent".
     *
     *   Only searches from the "id" field.
     */
    public void performSearch(CharSequence searchParameter){
        synchronized (LOCK) {
            if(!searchInProgress) {
                if(searchParameter != null && searchParameter.length() > 0) {
                    progressDialog = ProgressDialog.show(getActivity(), "", "Searching objects...", true);
                    progressDialog.setCancelable(true);
                    progressDialog.setCanceledOnTouchOutside(false);
                    Log.d("********", "New search started");
                    searchInProgress = true;
                    ArrayList list = new ArrayList<String>();
                    list.add("id");
                    BusHandler.getBus().post(new SearchObjectsEvent(list, searchParameter.toString(),
                            MAX_RESULTS_AMOUNT, false));
                }
                else {
                    makeLongToast(MORE_CHARACTERS);
                }
            }
        }
    }

    public void showNearestObjects() {
        synchronized (LOCK){
            if(!searchInProgress) {
                progressDialog = ProgressDialog.show(getActivity(), "", "Locating nearest objects...",
                        true, true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        dismissDialog();
                        Log.d(getClass().toString() + "showNearestObjects()",
                                " Releasing the lock dialog onCancel.");
                        searchInProgress = false;
                    }
                });
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(false);
                searchInProgress = true;
                LocationTask activeTask = new LocationTask();
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
     *  @return Returns false if user location was unsuccessful during a 3s timeout, or if the task
     *  is cancelled.
     */
    private class LocationTask extends AsyncTask<Void, Void, Boolean>{
        @Override
        protected void onPreExecute(){
            Log.d("________", "New locationtask started");
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            for (int i = 1; i < 3; ++i) {
                if (locationHandler != null && locationHandler.getLocationClient().isConnected()) {
                    locationHandler.updateCachedLocation();
                    if (locationHandler.getCachedLocation() != null) {
                        BusHandler.getBus().post(new RequestNearObjectsEvent(new ImmutablePointLocation(
                                locationHandler.getCachedLocation().getLatitude(),
                                locationHandler.getCachedLocation().getLongitude()), 0.002, false));
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
                synchronized (LOCK) {
                    dismissDialog();
                    Log.d("________", "Couldn't connect to locationclient. Releasing lock.");
                    searchInProgress = false;
                    makeLongToast(LOCATION_FAILED);
                }
            }
        }
    }

    private void dismissDialog(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    private void makeLongToast(String message){
        if(message != null) {
            int duration = Toast.LENGTH_LONG;
            Toast.makeText(getActivity(), message, duration).show();
        }
    }
}


