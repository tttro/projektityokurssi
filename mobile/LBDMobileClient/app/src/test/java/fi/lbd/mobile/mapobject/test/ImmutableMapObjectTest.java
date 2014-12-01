package fi.lbd.mobile.mapobject.test;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowLog;

import java.util.HashMap;
import java.util.Map;

import fi.lbd.mobile.CustomRobolectricTestRunner;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.location.PointLocation;
import fi.lbd.mobile.mapobjects.ImmutableMapObject;
import fi.lbd.mobile.mapobjects.MapObject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Created by Tommi.
 */
@RunWith(CustomRobolectricTestRunner.class)
public class ImmutableMapObjectTest {
    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testConstructor() throws Exception {
        final String testName = "testConstructor";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");
        ImmutablePointLocation location = mock(ImmutablePointLocation.class);
        Map<String, String> properties = new HashMap<>();
        properties.put("propertyp1", "valuep1");
        properties.put("propertyp2", "valuep2");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("propertym1", "valuem1");
        metadata.put("propertym2", "valuem2");
        MapObject mapObject = new ImmutableMapObject(false, "1", location, properties, metadata);

        assertThat(mapObject.isMinimized()).isEqualTo(false);
        assertThat(mapObject.getId()).isEqualTo("1");
        assertThat(mapObject.getPointLocation()).isEqualTo(location);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            assertThat(mapObject.getAdditionalProperties()).containsEntry(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            assertThat(mapObject.getMetadataProperties()).containsEntry(entry.getKey(), entry.getValue());
        }

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testEquals() throws Exception {
        final String testName = "testEquals";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: testModelBasic");
        System.out.println("---------------------------------------------------------------------");
        final ImmutablePointLocation location = mock(ImmutablePointLocation.class);

        final Map<String, String> properties = new HashMap<>();
        properties.put("propertyp1", "valuep1");
        properties.put("propertyp2", "valuep2");
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("propertym1", "valuem1");
        metadata.put("propertym2", "valuem2");

        MapObject mapObject1 = new ImmutableMapObject(false, "1", location, properties, metadata);
        MapObject mapObject2 = new ImmutableMapObject(false, "2", location, properties, metadata);
        MapObject mapObject3 = new ImmutableMapObject(true, "1", location,
                new HashMap<String, String>(),
                new HashMap<String, String>());

        assertThat(mapObject1).isNotEqualTo(null);
        assertThat(mapObject1).isNotEqualTo(new MapObject() {
            @Override
            public String getId() {
                return "1";
            }

            @Override
            public PointLocation getPointLocation() {
                return location;
            }

            @Override
            public Map<String, String> getAdditionalProperties() {
                return properties;
            }

            @Override
            public Map<String, String> getMetadataProperties() {
                return metadata;
            }

            @Override
            public boolean isMinimized() {
                return false;
            }
        });
        assertThat(mapObject1).isEqualTo(mapObject1);
        assertThat(mapObject1).isNotEqualTo(mapObject2);
        assertThat(mapObject1).isEqualTo(mapObject3);
        assertThat(mapObject1.hashCode()).isEqualTo(mapObject3.hashCode());
        assertThat(mapObject1.hashCode()).isNotEqualTo(mapObject2.hashCode());

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }
}
