package fi.lbd.mobile.mapobject.test;

import com.squareup.otto.Subscribe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fi.lbd.mobile.CustomRobolectricTestRunner;
import fi.lbd.mobile.mapobjects.MapObjectSelectionManager;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.mapobjects.events.CacheObjectsInAreaEvent;
import fi.lbd.mobile.mapobjects.events.RequestObjectsInAreaEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.ImmutableMapObject;
import fi.lbd.mobile.mapobjects.MapModelController;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.MarkerProducer;
import fi.lbd.mobile.mapobjects.ProgressListener;
import fi.lbd.mobile.utils.DummyMarker;
import fi.lbd.mobile.utils.Area;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for MapModelController.
 *
 * Created by Tommi.
 */
@RunWith(CustomRobolectricTestRunner.class)
public class MapModelControllerTest {
    private List<DummyMarker> createdMarkers;
    private List<DummyMarker> removedMarkers;
    private List<DummyMarker> eventedMarkers;
    private List<Area> requestedAreas;
    private List<Area> cachedAreas;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        this.createdMarkers = new ArrayList<>();
        this.removedMarkers = new ArrayList<>();
        this.eventedMarkers = new ArrayList<>();
        this.requestedAreas = new ArrayList<>();
        this.cachedAreas = new ArrayList<>();
        BusHandler.getBus().register(this);
        BusHandler.getBus().setTestMode(true);
    }


    @Test
    public void testMarkerCreateDeleteFind() throws Exception {
        final String testName = "testMarkerCreateAndDelete";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        clearListsAndEvents();
        MapModelController<DummyMarker> controller = initializeController(12);
        controller.cameraChanged(14,
                1.0, 1.0,
                1.0+MapModelController.DIVIDE_GRID_LAT-MapModelController.DIVIDE_GRID_LAT*0.1,
                1.0+MapModelController.DIVIDE_GRID_LON-MapModelController.DIVIDE_GRID_LON*0.1);
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.
        assertThat(this.requestedAreas).containsExactly(new Area(1.0, 1.0,
                1.0+MapModelController.DIVIDE_GRID_LAT,
                1.0+MapModelController.DIVIDE_GRID_LON));

        ImmutableMapObject mapObject = new ImmutableMapObject(false, "TestID",
                new ImmutablePointLocation(1.05, 1.05),
                new HashMap<String,String>(),
                new HashMap<String,String>());
        List<MapObject> objects = new ArrayList<>();
        objects.add(mapObject);
        controller.processObjects(objects, 1.0, 1.0);

        assertThat(this.createdMarkers).hasSize(1);
        DummyMarker createdMarker = this.createdMarkers.get(0);
        assertThat(createdMarker.getId()).isEqualTo("TestID");
        assertThat(createdMarker.getLatitude()).isEqualTo(1.05);
        assertThat(createdMarker.getLongitude()).isEqualTo(1.05);
        assertThat(createdMarker.getEvent(MarkerProducer.Event.INACTIVE)).isEqualTo(true);
        this.requestedAreas.clear();
        this.createdMarkers.clear();

        assertThat(controller.findMapObject(createdMarker)).isEqualTo(mapObject);
        assertThat(controller.findMarker(mapObject)).isEqualTo(createdMarker);

        controller.cameraChanged(14,
                2.0, 2.0,
                2.0+MapModelController.DIVIDE_GRID_LAT-MapModelController.DIVIDE_GRID_LAT*0.1,
                2.0+MapModelController.DIVIDE_GRID_LON-MapModelController.DIVIDE_GRID_LON*0.1);
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.

        assertThat(this.removedMarkers).containsExactly(createdMarker);
        assertThat(this.requestedAreas).containsExactly(new Area(2.0, 2.0,
                2.0+MapModelController.DIVIDE_GRID_LAT,
                2.0+MapModelController.DIVIDE_GRID_LON));


        assertThat(controller.findMapObject(createdMarker)).isNull();
        assertThat(controller.findMarker(mapObject)).isNull();

        System.out.println("FINISHED: " + testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testMarkerProgress() throws Exception {
        final String testName = "testMarkerProgress";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        TestProgressListener listener = new TestProgressListener();

        clearListsAndEvents();
        MapModelController<DummyMarker> controller = initializeController(12);
        controller.addProgressListener(listener);

        controller.cameraChanged(14,
                1.0, 1.0,
                1.0+MapModelController.DIVIDE_GRID_LAT-MapModelController.DIVIDE_GRID_LAT*0.1,
                1.0+MapModelController.DIVIDE_GRID_LON-MapModelController.DIVIDE_GRID_LON*0.1);
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.
        assertThat(this.requestedAreas).containsExactly(new Area(1.0, 1.0,
                1.0+MapModelController.DIVIDE_GRID_LAT,
                1.0+MapModelController.DIVIDE_GRID_LON));

        assertThat(listener.isLoading()).isEqualTo(true);

        ImmutableMapObject mapObject = new ImmutableMapObject(false, "TestID",
                new ImmutablePointLocation(1.05, 1.05),
                new HashMap<String,String>(),
                new HashMap<String,String>());
        List<MapObject> objects = new ArrayList<>();
        objects.add(mapObject);
        controller.processObjects(objects, 1.0, 1.0);

        assertThat(this.createdMarkers).hasSize(1);
        DummyMarker createdMarker = this.createdMarkers.get(0);
        assertThat(createdMarker.getId()).isEqualTo("TestID");
        assertThat(createdMarker.getLatitude()).isEqualTo(1.05);
        assertThat(createdMarker.getLongitude()).isEqualTo(1.05);
        assertThat(createdMarker.getEvent(MarkerProducer.Event.INACTIVE)).isEqualTo(true);
        this.requestedAreas.clear();
        this.createdMarkers.clear();

        assertThat(listener.isLoading()).isEqualTo(false);
        controller.removeProgressListener(listener);

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testMarkerZoom() throws Exception {
        final String testName = "testMarkerZoom";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        clearListsAndEvents();
        MapModelController<DummyMarker> controller = initializeController(12);

        controller.cameraChanged(14,
                1.0, 1.0,
                1.0+MapModelController.DIVIDE_GRID_LAT-MapModelController.DIVIDE_GRID_LAT*0.1,
                1.0+MapModelController.DIVIDE_GRID_LON-MapModelController.DIVIDE_GRID_LON*0.1);
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.
        assertThat(this.requestedAreas).containsExactly(new Area(1.0, 1.0,
                1.0 + MapModelController.DIVIDE_GRID_LAT,
                1.0 + MapModelController.DIVIDE_GRID_LON));

        ImmutableMapObject mapObject = new ImmutableMapObject(false, "TestID",
                new ImmutablePointLocation(1.05, 1.05),
                new HashMap<String,String>(),
                new HashMap<String,String>());
        List<MapObject> objects = new ArrayList<>();
        objects.add(mapObject);
        controller.processObjects(objects, 1.0, 1.0);

        assertThat(this.createdMarkers).hasSize(1);
        DummyMarker createdMarker = this.createdMarkers.get(0);
        assertThat(createdMarker.getId()).isEqualTo("TestID");
        assertThat(createdMarker.getLatitude()).isEqualTo(1.05);
        assertThat(createdMarker.getLongitude()).isEqualTo(1.05);
        assertThat(createdMarker.getEvent(MarkerProducer.Event.INACTIVE)).isEqualTo(true);
        this.requestedAreas.clear();
        this.createdMarkers.clear();

        controller.cameraChanged(10,
                1.0, 1.0,
                1.0+MapModelController.DIVIDE_GRID_LAT-MapModelController.DIVIDE_GRID_LAT*0.1,
                1.0+MapModelController.DIVIDE_GRID_LON-MapModelController.DIVIDE_GRID_LON*0.1);
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.

        assertThat(createdMarker.getEvent(MarkerProducer.Event.HIDE)).isEqualTo(true);

        controller.cameraChanged(10,
                1.0, 1.0,
                1.0+MapModelController.DIVIDE_GRID_LAT-MapModelController.DIVIDE_GRID_LAT*0.1,
                1.0+MapModelController.DIVIDE_GRID_LON-MapModelController.DIVIDE_GRID_LON*0.1);
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.

        assertThat(createdMarker.getEvent(MarkerProducer.Event.HIDE)).isEqualTo(true);

        controller.cameraChanged(14,
                1.0, 1.0,
                1.0+MapModelController.DIVIDE_GRID_LAT-MapModelController.DIVIDE_GRID_LAT*0.1,
                1.0+MapModelController.DIVIDE_GRID_LON-MapModelController.DIVIDE_GRID_LON*0.1);
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.

        assertThat(createdMarker.getEvent(MarkerProducer.Event.SHOW)).isEqualTo(true);

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testActiveMarkers() throws Exception {
        final String testName = "testActiveMarkers";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        clearListsAndEvents();
        MapModelController<DummyMarker> controller = initializeController(12);

        ImmutableMapObject selectedObject = new ImmutableMapObject(false, "TestID",
                new ImmutablePointLocation(1.05, 1.05),
                new HashMap<String,String>(),
                new HashMap<String,String>());

        MapObjectSelectionManager.get().setSelectedMapObject(selectedObject);
        controller.cameraChanged(14,
                1.0, 1.0,
                1.0+MapModelController.DIVIDE_GRID_LAT-MapModelController.DIVIDE_GRID_LAT*0.1,
                1.0+MapModelController.DIVIDE_GRID_LON-MapModelController.DIVIDE_GRID_LON*0.1);
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.
        assertThat(this.requestedAreas).containsExactly(new Area(1.0, 1.0,
                1.0+MapModelController.DIVIDE_GRID_LAT,
                1.0+MapModelController.DIVIDE_GRID_LON));

        List<MapObject> objects = new ArrayList<>();
        objects.add(selectedObject);
        controller.processObjects(objects, 1.0, 1.0);

        assertThat(this.createdMarkers).hasSize(1);
        DummyMarker createdMarker = this.createdMarkers.get(0);
        assertThat(createdMarker.getId()).isEqualTo("TestID");
        assertThat(createdMarker.getLatitude()).isEqualTo(1.05);
        assertThat(createdMarker.getLongitude()).isEqualTo(1.05);
        assertThat(createdMarker.getEvent(MarkerProducer.Event.SHOW_INFO)).isEqualTo(true);
        assertThat(createdMarker.getEvent(MarkerProducer.Event.ACTIVE)).isEqualTo(true);

        this.requestedAreas.clear();
        this.createdMarkers.clear();

        createdMarker.resetEvents();
        controller.clearActiveMarker();
        assertThat(createdMarker.getEvent(MarkerProducer.Event.ACTIVE)).isEqualTo(false);
        assertThat(createdMarker.getEvent(MarkerProducer.Event.INACTIVE)).isEqualTo(true);


        System.out.println("FINISHED: " + testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testNullValues() throws Exception {
        final String testName = "testActiveMarkers";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        clearListsAndEvents();
        MapModelController<DummyMarker> controller = initializeController(12);

        ImmutableMapObject selectedObject = new ImmutableMapObject(false, "TestID",
                new ImmutablePointLocation(1.05, 1.05),
                new HashMap<String,String>(),
                new HashMap<String,String>());

        controller.cameraChanged(14,
                1.0, 1.0,
                1.0+MapModelController.DIVIDE_GRID_LAT-MapModelController.DIVIDE_GRID_LAT*0.1,
                1.0+MapModelController.DIVIDE_GRID_LON-MapModelController.DIVIDE_GRID_LON*0.1);
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.
        assertThat(this.requestedAreas).containsExactly(new Area(1.0, 1.0,
                1.0+MapModelController.DIVIDE_GRID_LAT,
                1.0+MapModelController.DIVIDE_GRID_LON));

        controller.processObjects(null, 1.0, 1.0);

        controller.clearActiveMarker();

        assertThat(controller.findMapObject(null)).isNull();
        assertThat(controller.findMarker(null)).isNull();

        System.out.println("FINISHED: " + testName);
        System.out.println("_____________________________________________________________________");
    }

    @Subscribe
    public void onEvent(final RequestObjectsInAreaEvent event) {
        System.out.println("onEvent: RequestObjectsInAreaEvent");
        this.requestedAreas.add(new Area(event.getSouthWest().getLatitude(),
                event.getSouthWest().getLongitude(),
                event.getNorthEast().getLatitude(),
                event.getNorthEast().getLongitude()));
    }

    @Subscribe
    public void onEvent(final CacheObjectsInAreaEvent event) {
        System.out.println("onEvent: CacheObjectsInAreaEvent");
        this.cachedAreas.add(new Area(event.getSouthWest().getLatitude(),
                event.getSouthWest().getLongitude(),
                event.getNorthEast().getLatitude(),
                event.getNorthEast().getLongitude()));
    }

    private void clearListsAndEvents() {
        this.createdMarkers.clear();
        this.removedMarkers.clear();
        this.eventedMarkers.clear();
        this.requestedAreas.clear();
        this.cachedAreas.clear();
    }

    private MapModelController<DummyMarker> initializeController(int maxZoom) {
        MapModelController<DummyMarker> modelController = new MapModelController<>(new MarkerProducer<DummyMarker>() {
                    @Override
                    public DummyMarker produce(String id, double latitude, double longitude) {
                        DummyMarker marker = new DummyMarker(id, latitude, longitude);
                        createdMarkers.add(marker);
                        return marker;
                    }

                    @Override
                    public void remove(DummyMarker marker) {
                        removedMarkers.add(marker);
                    }

                    @Override
                    public void event(DummyMarker obj, Event evt) {
                        obj.setEvent(evt);
                        eventedMarkers.add(obj);
                    }
                },
                maxZoom);
        return modelController;
    }

    @After
    public void tearDown() {
        BusHandler.getBus().unregister(this);
    }

    private class TestProgressListener implements ProgressListener {
        private boolean isLoading = false;

        public TestProgressListener() {}

        @Override
        public void startLoading() {
            this.isLoading = true;
        }

        @Override
        public void finishLoading() {
            this.isLoading = false;
        }

        public boolean isLoading() {
            return isLoading;
        }

        public void setLoading(boolean isLoading) {
            this.isLoading = isLoading;
        }
    }
}
