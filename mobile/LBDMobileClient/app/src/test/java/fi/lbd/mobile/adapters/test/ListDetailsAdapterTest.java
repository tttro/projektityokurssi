package fi.lbd.mobile.adapters.test;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowTextView;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.lbd.mobile.CustomRobolectricTestRunner;
import fi.lbd.mobile.ListActivity;
import fi.lbd.mobile.R;
import fi.lbd.mobile.adapters.ListDetailsAdapter;
import fi.lbd.mobile.adapters.ListExpandableAdapter;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.location.LocationHandler;
import fi.lbd.mobile.location.PointLocation;
import fi.lbd.mobile.mapobjects.ImmutableMapObject;
import fi.lbd.mobile.mapobjects.MapObject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by Ossi.
 */

@RunWith(CustomRobolectricTestRunner.class)
public class ListDetailsAdapterTest {

    @Mock ImmutablePointLocation location;
   // @Mock Activity activity = mock(Activity.class);

    Activity activity;
    Map<String, String> propertiesTest1;
    Map<String, String> metadataTest1;
    MapObject mapObjectTest1;
    ListDetailsAdapter adapterTest1;
    ListDetailsAdapter adapterTest2;
    ListDetailsAdapter adapterTest3;
    ListDetailsAdapter adapterTest4;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        MockitoAnnotations.initMocks(this);

        propertiesTest1 = new HashMap<>();
        propertiesTest1.put("propertyp0", "valuep0");
        propertiesTest1.put("propertyp1", "valuep1");
        metadataTest1 = new HashMap<>();
        metadataTest1.put("propertym0", "valuem0");
        metadataTest1.put("propertym1", "valuem1");

        activity = new Activity();
        mapObjectTest1 = new ImmutableMapObject(false, "1", location, propertiesTest1, metadataTest1);

        adapterTest1 = new ListDetailsAdapter(activity, mapObjectTest1, propertiesTest1.size(),
                1,metadataTest1.size());
        adapterTest2 = new ListDetailsAdapter(activity, mapObjectTest1, propertiesTest1.size()+1,
                1,metadataTest1.size()+1);
        adapterTest3 = new ListDetailsAdapter(activity, mapObjectTest1, propertiesTest1.size(),
                0,metadataTest1.size());
        adapterTest4 = new ListDetailsAdapter(activity, mapObjectTest1, 0,0,0);
    }

    @Test
    public void testConstructor() {
        final String testName = "testConstructor";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        assertThat(adapterTest1.getCount() == 5);
        assertThat(adapterTest2.getCount() == 5);
        assertThat(adapterTest3.getCount() == 4);
        assertThat(adapterTest4.getCount() == 0);
    }

    /*
    @Test
    public void testGetView(){
        final String testName = "testGetView";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        View view = mock(View.class);
        TextView titleText = mock(TextView.class);
        TextView dataText = mock(TextView.class);

        ViewGroup viewGroup = mock(ViewGroup.class);
        LayoutInflater layoutInflater = mock(LayoutInflater.class);

        when(activity.getLayoutInflater()).thenReturn(layoutInflater);
        when(layoutInflater.inflate(R.layout.listview_double_row, viewGroup, false)).thenReturn(view);
        when(view.findViewById(R.id.textViewObjectId)).thenReturn(titleText);
        when(view.findViewById(R.id.textViewObjectLocation)).thenReturn(dataText);

        for(int i = 0; i < 2; ++i){
            adapterTest1.getView(i, null, viewGroup);
            if(i == 0) {
              //  assertThat(titleText.getText().equals(String.format("propertyp%d", i)));
              //  assertThat(dataText.getText().equals(String.format("valuep%d", i)));
            }
        }
    }
    */
}
