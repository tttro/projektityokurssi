package fi.lbd.mobile;

import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import fi.lbd.mobile.fragments.GoogleMapFragment;
import fi.lbd.mobile.fragments.InboxFragment;
import fi.lbd.mobile.fragments.ListClickListener;
import fi.lbd.mobile.fragments.ObjectListFragment;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.repository.MapObjectRepositoryService;


public class ListActivity extends Activity implements ListClickListener<MapObject> {
    // Keeps loaded fragments in memory
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_list);

        // Start the object repository service. // TODO: Missä käynnistys?
        startService(new Intent(this, MapObjectRepositoryService.class));

        this.sectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        this.viewPager = (ViewPager) findViewById(R.id.pager);
        this.viewPager.setAdapter(this.sectionsPagerAdapter);
        viewPager.setCurrentItem(1);
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
            if (position == 0) {
                InboxFragment frag = InboxFragment.newInstance();
                return frag;
            } else if (position == 1) {
                ObjectListFragment frag = ObjectListFragment.newInstance();
                frag.addListClickListener(ListActivity.this);
                return frag;
            } else if (position == 2){
                GoogleMapFragment frag = GoogleMapFragment.newInstance();
                return frag;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_tab_messages).toUpperCase(l);
                case 1:
                    return getString(R.string.title_tab_objects).toUpperCase(l);
                case 2:
                    return getString(R.string.title_tab_map).toUpperCase(l);
            }
            return null;
        }

        public int getPageIcon(int position) {
            switch (position) {
                case 0:
                    return android.R.drawable.ic_menu_search;
                case 1:
                    return android.R.drawable.sym_action_email;
            }
            return 0;
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MapObjectRepositoryService.class)); // TODO: Missä pysäytys?
    }

    @Override
    public void onClick(MapObject object) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("selectedObject", object);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
}
