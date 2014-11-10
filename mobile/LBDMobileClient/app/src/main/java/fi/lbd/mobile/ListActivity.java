package fi.lbd.mobile;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.view.View;

import com.google.android.gms.maps.GoogleMapOptions;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.SelectMapObjectEvent;
import fi.lbd.mobile.fragments.GoogleMapFragment;
import fi.lbd.mobile.fragments.InboxFragment;
import fi.lbd.mobile.fragments.ListClickListener;
import fi.lbd.mobile.fragments.ObjectListFragment;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.repository.MapObjectRepositoryService;


public class ListActivity extends Activity {
    // Keeps loaded fragments in memory
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;

    // Declare constants for tab UI
    private static final int START_TAB = 1;
    private static final int MSG_TAB = 0;
    private static final int OBJ_TAB = 1;
    private static final int MAP_TAB = 2;
    private static final int TAB_COUNT = 3;

    // Data structure to save previous tabs in order to restore them when back button is pressed.
    private ArrayDeque<Integer> pageStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BusHandler.getBus().register(this);

        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_list);

        // Start the object repository service. // TODO: Missä käynnistys?
        startService(new Intent(this, MapObjectRepositoryService.class));

        pageStack = new ArrayDeque<Integer>();
        pageStack.push(START_TAB);

        this.sectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        this.viewPager = (ViewPager) findViewById(R.id.pager);
        this.viewPager.setAdapter(this.sectionsPagerAdapter);
        viewPager.setCurrentItem(START_TAB);

        // http://stackoverflow.com/questions/13185476/how-to-handle-back-button-using-view-pager
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int i) {
                pageStack.push(i);
            }
            @Override
            public void onPageScrolled(int i, float v, int j) {}
            @Override
            public void onPageScrollStateChanged(int i) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == MSG_TAB) {
                InboxFragment frag = InboxFragment.newInstance();
                return frag;
            } else if (position == OBJ_TAB) {
                ObjectListFragment frag = ObjectListFragment.newInstance();
                return frag;
            } else if (position == MAP_TAB){
                GoogleMapFragment frag = GoogleMapFragment.newInstance();
                return frag;
            }
            return null;
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case MSG_TAB:
                    return getString(R.string.title_tab_messages).toUpperCase(l);
                case OBJ_TAB:
                    return getString(R.string.title_tab_objects).toUpperCase(l);
                case MAP_TAB:
                    return getString(R.string.title_tab_map).toUpperCase(l);
            }
            return null;
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MapObjectRepositoryService.class)); // TODO: Missä pysäytys?
        BusHandler.getBus().unregister(this);
    }

    public void onDetailsClick(View view){
        // Retrieve object for which the button was pressed
        MapObject object = null;
        try {
            object = (MapObject)((View)(view.getParent()).getParent()).getTag();
        } catch (Exception e){
            e.printStackTrace();
            Log.d("NO OBJECT TAG RECEIVED", "-----ON DETAILS CLICK");
        }
        if (object != null) {
            SelectionManager.get().setSelection(object);
            Intent intent = new Intent(this, DetailsActivity.class);
            startActivity(intent);
        }
    }
    public void onMapClick(View view){
        // Retrieve object for which the button was pressed
        MapObject object = null;
        try {
            object = (MapObject)((View)(view.getParent()).getParent()).getTag();
        } catch (Exception e){
            e.printStackTrace();
            Log.d("NO OBJECT TAG RECEIVED", "-----ON MAP CLICK");
        }
        if (object != null){
            SelectionManager.get().setSelection(object);
            BusHandler.getBus().post(new SelectMapObjectEvent());
            viewPager.setCurrentItem(MAP_TAB);
        }
    }

    // http://stackoverflow.com/questions/13185476/how-to-handle-back-button-using-view-pager
    @Override
    public void onBackPressed() {
        if(pageStack.size() > 1) {
            pageStack.pop();
            viewPager.setCurrentItem(pageStack.peek(), true);
        } else {
            pageStack.clear();
            super.onBackPressed(); // This will pop the Activity from the stack.
        }
    }
}
