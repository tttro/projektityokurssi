package fi.lbd.mobile.fragments;

import android.app.ListFragment;
import android.graphics.Color;
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

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import fi.lbd.mobile.SelectionManager;
import fi.lbd.mobile.adapters.ListExpandableAdapter;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.R;
import fi.lbd.mobile.events.RequestNearObjectsEvent;
import fi.lbd.mobile.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.location.LocationHandler;
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

    private int firstVisiblePosition;
    private ArrayList<Boolean> groupExpandedArray;

    public static ObjectListFragment newInstance(LocationHandler locationHandler) {
        ObjectListFragment fragment = new ObjectListFragment();
        fragment.setLocationHandler(locationHandler);
        return fragment; // Constructor should not have additional parameters!
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

        // Requests the default map objects from the object repository through OTTO-bus. TODO: Parempi tapa tehdä?
        AsyncTask task = new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // TODO: Exception handling...
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                BusHandler.getBus().post(new RequestNearObjectsEvent(new ImmutablePointLocation(61.510988, 23.777366), 0.001, false)); // TODO: Actual location
            }
        }.execute();
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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
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
        }
        this.getListView().requestLayout();
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
        statusText.setText(R.string.showing_results);
        statusText.setBackgroundColor(getActivity().getResources().
                getColor(R.color.search_results_background));
    }

    // TODO: User location functionality
    public void showNearestObjects(){
        statusText.setText(R.string.showing_nearest);
        statusText.setBackgroundColor(getActivity().getResources().
                getColor(R.color.near_objects_background));
    }
 }


