package fi.lbd.mobile.adapters.test;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fi.lbd.mobile.CustomRobolectricTestRunner;
import fi.lbd.mobile.ListActivity;
import fi.lbd.mobile.R;
import fi.lbd.mobile.adapters.ListExpandableAdapter;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.location.LocationHandler;
import fi.lbd.mobile.mapobjects.ImmutableMapObject;
import fi.lbd.mobile.mapobjects.MapObject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by Ossi.
 */
@RunWith(CustomRobolectricTestRunner.class)
public class ListExpandableAdapterTest {

    @Mock ImmutablePointLocation location1;
    @Mock ImmutablePointLocation location2;
    @Mock ListActivity mockActivity;

    ListExpandableAdapter adapter;
    Map<String, String> propertiesTest1;
    Map<String, String> propertiesTest2;
    Map<String, String> metadataTest1;
    Map<String, String> metadataTest2;
    MapObject mapObjectTest1;
    MapObject mapObjectTest2;
    ArrayList<MapObject> list;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        MockitoAnnotations.initMocks(this);

        adapter = new ListExpandableAdapter(mockActivity);
        propertiesTest1 = new HashMap<>();
        propertiesTest2 = new HashMap<>();
        metadataTest1 = new HashMap<>();
        metadataTest2 = new HashMap<>();
        propertiesTest1.put("propertyp1_test1", "valuep1_test1");
        propertiesTest1.put("propertyp2_test1", "valuep2_test1");
        metadataTest1.put("propertym1_test1", "valuem1_test1");
        metadataTest1.put("propertym2_test1", "valuem2_test1");
        propertiesTest2.put("propertyp1_test2", "valuep1_test2");
        propertiesTest2.put("propertyp2_test2", "valuep2_test2");
        metadataTest2.put("propertym1_test2", "valuem1_test2");
        metadataTest2.put("propertym2_test2", "valuem2_test2");
        mapObjectTest1 = new ImmutableMapObject(false, "1", location1, propertiesTest1, metadataTest1);
        mapObjectTest2 = new ImmutableMapObject(false, "2", location2, propertiesTest2, metadataTest2);
        list = new ArrayList<MapObject>();
        list.add(mapObjectTest1);
        list.add(mapObjectTest2);

        LocationHandler testLocationHandler = Mockito.mock(LocationHandler.class);
        doNothing().when(testLocationHandler).updateCachedLocation();
        when(testLocationHandler.getCachedLocation())
                .thenReturn((ImmutablePointLocation)mapObjectTest2.getPointLocation());
        when(testLocationHandler.getLastLocation())
                .thenReturn((ImmutablePointLocation)mapObjectTest2.getPointLocation());

        when(mockActivity.getLocationHandler()).thenReturn(testLocationHandler);
        adapter.addAll(list);
    }

    @Test
    public void testAddAll() {
        final String testName = "testAddAll";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        adapter.addAll(new ArrayList<MapObject>());
        assertThat(adapter.getGroupCount() == 0);
        assertThat(adapter.getGroup(0) == null);
        adapter.addAll(list);
        adapter.clear();
        assertThat(adapter.getGroupCount() == 0);
        assertThat(adapter.getGroup(0) == null);

        adapter.addAll(list);
        assertThat(adapter.getGroupCount() == 2);
        assertThat(adapter.getGroup(0).equals(mapObjectTest2));
        assertThat(adapter.getGroup(1).equals(mapObjectTest1));
        assertThat(adapter.getGroup(2) == null);
        assertThat(adapter.getGroup(-1) == null);
    }

    @Test
    public void testGetGroupView(){

        final String testName = "testGetGroupView";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        LayoutInflater layoutInflater = Robolectric.buildActivity(Activity.class).create().get().getLayoutInflater();
        LayoutInflater mockLayoutInflater = mock(LayoutInflater.class);
        ViewGroup mockViewGroup = mock(ViewGroup.class);

        LinearLayout parent = new LinearLayout(Robolectric.application);
        LinearLayout linearLayout = (LinearLayout)layoutInflater.inflate(R.layout.listview_single_row, parent);
        TextView idText = (TextView)linearLayout.findViewById(R.id.textViewObjectId);
        TextView distanceText = (TextView)linearLayout.findViewById(R.id.textViewDistance);

        when(mockActivity.getLayoutInflater()).thenReturn(mockLayoutInflater);
        when(mockLayoutInflater.inflate(R.layout.listview_single_row, mockViewGroup, false)).thenReturn(mockViewGroup);
        when(mockViewGroup.findViewById(R.id.textViewObjectId)).thenReturn(idText);
        when(mockViewGroup.findViewById(R.id.textViewDistance)).thenReturn(distanceText);

        for(int i = -1; i < 3; ++i){
            distanceText.setText("Text");
            idText.setText("Text");

            adapter.getGroupView(i, false, null, mockViewGroup);
            if(i == -1){
                assertThat(idText.getText().equals("Text"));
                assertThat(distanceText.getText().equals("Text"));
            }
            else if(i == 0) {
                assertThat(idText.getText().equals(mapObjectTest2.getId()));
                assertThat(distanceText.getText().equals("0m"));
            }
            else if(i == 1){
                assertThat(idText.getText().equals(mapObjectTest1.getId()));
            }
            else {
                assertThat(idText.getText().equals("Text"));
                assertThat(distanceText.getText().equals("Text"));
            }
        }
    }
}