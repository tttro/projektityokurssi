package fi.lbd.mobile.adapters.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fi.lbd.mobile.CustomRobolectricTestRunner;
import fi.lbd.mobile.ListActivity;
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
    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testAddAll() {
        final String testName = "testAddAll";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        ListActivity activity = mock(ListActivity.class);
        ListExpandableAdapter adapter = new ListExpandableAdapter(activity);

        ImmutablePointLocation location = mock(ImmutablePointLocation.class);
        Map<String, String> propertiesTest1 = new HashMap<>();
        propertiesTest1.put("propertyp1", "valuep1");
        propertiesTest1.put("propertyp2", "valuep2");
        Map<String, String> metadataTest1 = new HashMap<>();
        metadataTest1.put("propertym1", "valuem1");
        metadataTest1.put("propertym2", "valuem2");
        MapObject mapObjectTest1 = new ImmutableMapObject(false, "1", location, propertiesTest1, metadataTest1);
        Map<String, String> propertiesTest2 = new HashMap<>();
        propertiesTest2.put("propertyp1", "valuep1");
        propertiesTest2.put("propertyp2", "valuep2");
        Map<String, String> metadataTest2 = new HashMap<>();
        metadataTest2.put("propertym1", "valuem1");
        metadataTest2.put("propertym2", "valuem2");
        MapObject mapObjectTest2 = new ImmutableMapObject(false, "1", location, propertiesTest2, metadataTest2);

        ArrayList<MapObject> list = new ArrayList<MapObject>();
        list.add(mapObjectTest1);
        list.add(mapObjectTest2);

        LocationHandler testLocationHandler = Mockito.mock(LocationHandler.class);
        doNothing().when(testLocationHandler).updateCachedLocation();
        when(testLocationHandler.getCachedLocation())
                .thenReturn((ImmutablePointLocation)mapObjectTest2.getPointLocation());
        when(testLocationHandler.getLastLocation())
                .thenReturn((ImmutablePointLocation)mapObjectTest2.getPointLocation());

        when(activity.getLocationHandler()).thenReturn(testLocationHandler);

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
}