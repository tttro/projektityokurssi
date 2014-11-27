package fi.lbd.mobile.mapobject.test;


import android.util.Log;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;
import java.util.List;

import fi.lbd.mobile.CustomRobolectricTestRunner;
import fi.lbd.mobile.R;
import fi.lbd.mobile.mapobjects.ImmutableMapObject;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.MapTableModel;
import fi.lbd.mobile.mapobjects.MapTableModelListener;
import fi.lbd.mobile.utils.Area;

// REQUIRED STATIC IMPORTS!
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.*;


@RunWith(CustomRobolectricTestRunner.class)
public class MapTableModelTest {
    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testModelBasic() throws Exception {
        final String testName = "testModelBasic";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: testModelBasic");
        System.out.println("---------------------------------------------------------------------");

        final List<MapObject> removedObjects = new ArrayList<>();
        final List<Area> requestedAreas = new ArrayList<>();
        final List<Area> requestedCacheAreas = new ArrayList<>();
        MapTableModel<MapObject> model = initModel(testName, removedObjects, requestedAreas, requestedCacheAreas);

        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testModelMoveUp() throws Exception {
        final String testName = "testModelMoveUp";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        final List<MapObject> removedObjects = new ArrayList<>();
        final List<Area> requestedAreas = new ArrayList<>();
        final List<Area> requestedCacheAreas = new ArrayList<>();
        MapTableModel<MapObject> model = initModel(testName, removedObjects, requestedAreas, requestedCacheAreas);
        Area seekArea;

        // After init there is one cell waiting for objects (1.0, 1.0 & 1.5, 1.5)
        // Add objects to the cell
        List<MapObject> addObjects = new ArrayList<>();
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        model.addObjects(1.0, 1.0, addObjects );

        // Remove the area after adding elements to it
        seekArea = new Area(1.0, 1.0, 1.5, 1.5);
        requestedAreas.remove(seekArea);

        // Cell should contain objects
        boolean gridCellEmpty = model.isEmpty(1.0, 1.0);
        assertThat(gridCellEmpty).isEqualTo(false);

        // After adding objects, model should not be waiting for more objects
        boolean waitingForObjects = model.hasGridCellsWaitingForObjects();
        assertThat(waitingForObjects).isEqualTo(false);

        // Lists should not contain any objects
        assertThat(removedObjects).isEmpty();
        assertThat(requestedAreas).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();

        // Update table
        System.out.println(testName+": update model (1.1, 1.0, 1.35, 1.25)");
        model.updateTable(1.1, 1.0, 1.35, 1.25);
        Area cachedArea = new Area(1.5, 1.0, 2.0, 1.5);
        // Latest table update should have requested an area for caching
        assertThat(requestedCacheAreas).contains(cachedArea);
        assertThat(requestedCacheAreas).hasSize(1);
        assertThat(removedObjects).isEmpty();
        assertThat(requestedAreas).isEmpty();
        requestedCacheAreas.clear();

        System.out.println(testName+": update model (1.2, 1.0, 1.45, 1.25)");
        model.updateTable(1.2, 1.0, 1.45, 1.25);
        assertThat(requestedCacheAreas).isEmpty();
        assertThat(removedObjects).isEmpty();
        assertThat(requestedAreas).isEmpty();

        System.out.println(testName+": update model (1.3, 1.0, 1.55, 1.25)");
        model.updateTable(1.3, 1.0, 1.55, 1.25);
        // Model should request the area which was previously cached
        assertThat(requestedAreas).contains(cachedArea);
        assertThat(requestedAreas).hasSize(1);
        assertThat(requestedCacheAreas).isEmpty();
        assertThat(removedObjects).isEmpty();

        System.out.println(testName+": update model (1.4, 1.0, 1.65, 1.25)");
        model.updateTable(1.4, 1.0, 1.65, 1.25);
        assertThat(requestedAreas).hasSize(1);
        assertThat(requestedCacheAreas).isEmpty();
        assertThat(removedObjects).isEmpty();

        System.out.println(testName+": update model (1.5, 1.0, 1.75, 1.25)");
        model.updateTable(1.5, 1.0, 1.75, 1.25);
        assertThat(requestedCacheAreas).isEmpty();
        assertThat(requestedAreas).hasSize(1);
        assertThat(removedObjects).hasSize(4);
        // Check that the removed objects has exactly the objects which were added to the cell.
        assertThat(removedObjects).containsExactlyElementsOf(addObjects);

        System.out.println(testName+": update model (1.6, 1.0, 1.85, 1.25)");
        model.updateTable(1.6, 1.0, 1.85, 1.25);

        System.out.println("FINISHED: "+testName+": Caching and requesting.");
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testModelMoveRight() throws Exception {
        final String testName = "testModelMoveRight";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        final List<MapObject> removedObjects = new ArrayList<>();
        final List<Area> requestedAreas = new ArrayList<>();
        final List<Area> requestedCacheAreas = new ArrayList<>();
        MapTableModel<MapObject> model = initModel(testName, removedObjects, requestedAreas, requestedCacheAreas);
        Area seekArea;

        // After init there is one cell waiting for objects (1.0, 1.0 & 1.5, 1.5)
        // Add objects to the cell
        List<MapObject> addObjects = new ArrayList<>();
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        model.addObjects(1.0, 1.0, addObjects );

        // Remove the area after adding elements to it
        seekArea = new Area(1.0, 1.0, 1.5, 1.5);
        requestedAreas.remove(seekArea);

        // Cell should contain objects
        boolean gridCellEmpty = model.isEmpty(1.0, 1.0);
        assertThat(gridCellEmpty).isEqualTo(false);

        // After adding objects, model should not be waiting for more objects
        boolean waitingForObjects = model.hasGridCellsWaitingForObjects();
        assertThat(waitingForObjects).isEqualTo(false);

        // Lists should not contain any objects
        assertThat(removedObjects).isEmpty();
        assertThat(requestedAreas).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();

        // Update table
        System.out.println(testName+": update model (1.0, 1.1, 1.25, 1.35)");
        model.updateTable(1.0, 1.1, 1.25, 1.35);
        Area cachedArea = new Area(1.0, 1.5, 1.5, 2.0);
        // Latest table update should have requested an area for caching
        assertThat(requestedCacheAreas).contains(cachedArea);
        assertThat(requestedCacheAreas).hasSize(1);
        assertThat(removedObjects).isEmpty();
        assertThat(requestedAreas).isEmpty();
        requestedCacheAreas.clear();

        System.out.println(testName+": update model (1.0, 1.2, 1.25, 1.45)");
        model.updateTable(1.0, 1.2, 1.25, 1.45);
        assertThat(requestedCacheAreas).isEmpty();
        assertThat(removedObjects).isEmpty();
        assertThat(requestedAreas).isEmpty();

        System.out.println(testName+": update model (1.0, 1.3, 1.25, 1.55)");
        model.updateTable(1.0, 1.3, 1.25, 1.55);
        // Model should request the area which was previously cached
        assertThat(requestedAreas).contains(cachedArea);
        assertThat(requestedAreas).hasSize(1);
        assertThat(requestedCacheAreas).isEmpty();
        assertThat(removedObjects).isEmpty();

        System.out.println(testName+": update model (1.0, 1.4, 1.25, 1.65)");
        model.updateTable(1.0, 1.4, 1.25, 1.65);
        assertThat(requestedAreas).hasSize(1);
        assertThat(requestedCacheAreas).isEmpty();
        assertThat(removedObjects).isEmpty();

        System.out.println(testName+": update model (1.0, 1.5, 1.25, 1.75)");
        model.updateTable(1.0, 1.5, 1.25, 1.75);
        assertThat(requestedCacheAreas).isEmpty();
        assertThat(requestedAreas).hasSize(1);
        assertThat(removedObjects).hasSize(4);
        // Check that the removed objects has exactly the objects which were added to the cell.
        assertThat(removedObjects).containsExactlyElementsOf(addObjects);

        System.out.println(testName+": update model (1.0, 1.6, 1.25, 1.85)");
        model.updateTable(1.0, 1.6, 1.25, 1.85);

        System.out.println("FINISHED: "+testName+": Caching and requesting.");
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testModelMoveLeft() throws Exception {
        final String testName = "testModelMoveLeft";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        final List<MapObject> removedObjects = new ArrayList<>();
        final List<Area> requestedAreas = new ArrayList<>();
        final List<Area> requestedCacheAreas = new ArrayList<>();
        MapTableModel<MapObject> model = initModel(testName, removedObjects, requestedAreas, requestedCacheAreas);
        Area seekArea;

        // After init there is one cell waiting for objects (1.0, 1.0 & 1.5, 1.5)
        // Add objects to the cell
        List<MapObject> addObjects = new ArrayList<>();
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        model.addObjects(1.0, 1.0, addObjects );

        // Remove the area after adding elements to it
        seekArea = new Area(1.0, 1.0, 1.5, 1.5);
        requestedAreas.remove(seekArea);

        // Cell should contain objects
        boolean gridCellEmpty = model.isEmpty(1.0, 1.0);
        assertThat(gridCellEmpty).isEqualTo(false);

        // After adding objects, model should not be waiting for more objects
        boolean waitingForObjects = model.hasGridCellsWaitingForObjects();
        assertThat(waitingForObjects).isEqualTo(false);

        // Lists should not contain any objects
        assertThat(removedObjects).isEmpty();
        assertThat(requestedAreas).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();

        // Update table
        System.out.println(testName+": update model (1.0, 0.9, 1.25, 1.15)");
        model.updateTable(1.0, 0.9, 1.25, 1.15);
        Area requestedArea = new Area(1.0, 0.5, 1.5, 1.0);
        // Latest table update should have requested an area instead of caching because it starts
        // already from the edge
        assertThat(requestedAreas).contains(requestedArea);
        assertThat(requestedAreas).hasSize(1);
        assertThat(removedObjects).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();
        requestedAreas.clear();

        System.out.println(testName+": update model (1.0, 0.8, 1.25, 1.05)");
        model.updateTable(1.0, 0.8, 1.25, 1.05);
        assertThat(requestedAreas).isEmpty();
        assertThat(removedObjects).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();

        System.out.println(testName+": update model (1.0, 0.7, 1.25, 0.95)");
        model.updateTable(1.0, 0.7, 1.25, 0.95);
        // Model should try to cache the next cell
        Area cacheArea = new Area(1.0, 0.0, 1.5, 0.5);
        assertThat(requestedCacheAreas).hasSize(1);
        assertThat(requestedCacheAreas).contains(cacheArea);
        assertThat(requestedAreas).isEmpty();
        assertThat(removedObjects).hasSize(4);
        // Check that the removed objects has exactly the objects which were added to the cell.
        assertThat(removedObjects).containsExactlyElementsOf(addObjects);
        removedObjects.clear();
        requestedCacheAreas.clear();

        System.out.println(testName+": update model (1.0, 0.6, 1.25, 0.85)");
        model.updateTable(1.0, 0.6, 1.25, 0.85);
        assertThat(requestedAreas).isEmpty();
        assertThat(removedObjects).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();

        System.out.println(testName+": update model (1.0, 0.5, 1.25, 0.75)");
        model.updateTable(1.0, 0.5, 1.25, 0.75);
        assertThat(requestedAreas).isEmpty();
        assertThat(removedObjects).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();

        System.out.println(testName+": update model (1.0, 0.4, 1.25, 0.65)");
        model.updateTable(1.0, 0.4, 1.25, 0.65);
        // Model should try to get the contents of the previously cached area
        assertThat(requestedAreas).hasSize(1);
        assertThat(requestedAreas).contains(cacheArea);
        assertThat(removedObjects).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();

        System.out.println("FINISHED: "+testName+": Caching and requesting.");
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testModelMoveDown() throws Exception {
        final String testName = "testModelMoveDown";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        final List<MapObject> removedObjects = new ArrayList<>();
        final List<Area> requestedAreas = new ArrayList<>();
        final List<Area> requestedCacheAreas = new ArrayList<>();
        MapTableModel<MapObject> model = initModel(testName, removedObjects, requestedAreas, requestedCacheAreas);
        Area seekArea;

        // After init there is one cell waiting for objects (1.0, 1.0 & 1.5, 1.5)
        // Add objects to the cell
        List<MapObject> addObjects = new ArrayList<>();
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        model.addObjects(1.0, 1.0, addObjects );

        // Remove the area after adding elements to it
        seekArea = new Area(1.0, 1.0, 1.5, 1.5);
        requestedAreas.remove(seekArea);

        // Cell should contain objects
        boolean gridCellEmpty = model.isEmpty(1.0, 1.0);
        assertThat(gridCellEmpty).isEqualTo(false);

        // After adding objects, model should not be waiting for more objects
        boolean waitingForObjects = model.hasGridCellsWaitingForObjects();
        assertThat(waitingForObjects).isEqualTo(false);

        // Lists should not contain any objects
        assertThat(removedObjects).isEmpty();
        assertThat(requestedAreas).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();

        // Update table
        System.out.println(testName+": update model (0.9, 1.0, 1.15, 1.25)");
        model.updateTable(0.9, 1.0, 1.15, 1.25);
        Area requestedArea = new Area(0.5, 1.0, 1.0, 1.5);
        // Latest table update should have requested an area instead of caching because it starts
        // already from the edge
        assertThat(requestedAreas).contains(requestedArea);
        assertThat(requestedAreas).hasSize(1);
        assertThat(removedObjects).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();
        requestedAreas.clear();

        System.out.println(testName+": update model (0.8, 1.0, 1.05, 1.25)");
        model.updateTable(0.8, 1.0, 1.05, 1.25);
        assertThat(requestedAreas).isEmpty();
        assertThat(removedObjects).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();

        System.out.println(testName+": update model (0.7, 1.0, 0.95, 1.25)");
        model.updateTable(0.7, 1.0, 0.95, 1.25);
        // Model should try to cache the next cell
        Area cacheArea = new Area(0.0, 1.0, 0.5, 1.5);
        assertThat(requestedCacheAreas).hasSize(1);
        assertThat(requestedCacheAreas).contains(cacheArea);
        assertThat(requestedAreas).isEmpty();
        assertThat(removedObjects).hasSize(4);
        // Check that the removed objects has exactly the objects which were added to the cell.
        assertThat(removedObjects).containsExactlyElementsOf(addObjects);
        removedObjects.clear();
        requestedCacheAreas.clear();

        System.out.println(testName+": update model (0.6, 1.0, 0.85, 1.25)");
        model.updateTable(0.6, 1.0, 0.85, 1.25);
        assertThat(requestedAreas).isEmpty();
        assertThat(removedObjects).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();

        System.out.println(testName+": update model (0.5, 1.0, 0.75, 1.25)");
        model.updateTable(0.5, 1.0, 0.75, 1.25);
        assertThat(requestedAreas).isEmpty();
        assertThat(removedObjects).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();

        System.out.println(testName+": update model (0.4, 1.0, 0.65, 1.25)");
        model.updateTable(0.4, 1.0, 0.65, 1.25);
        // Model should try to get the contents of the previously cached area
        assertThat(requestedAreas).hasSize(1);
        assertThat(requestedAreas).contains(cacheArea);
        assertThat(removedObjects).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();

        System.out.println("FINISHED: "+testName+": Caching and requesting.");
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testModelJumpToAnotherPlace() throws Exception {
        final String testName = "testModelJumpToAnotherPlace";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        final List<MapObject> removedObjects = new ArrayList<>();
        final List<Area> requestedAreas = new ArrayList<>();
        final List<Area> requestedCacheAreas = new ArrayList<>();
        MapTableModel<MapObject> model = initModel(testName, removedObjects, requestedAreas, requestedCacheAreas);
        Area seekArea;

        // After init there is one cell waiting for objects (1.0, 1.0 & 1.5, 1.5)
        // Add objects to the cell
        List<MapObject> addObjects = new ArrayList<>();
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        addObjects.add( mock(ImmutableMapObject.class) );
        model.addObjects(1.0, 1.0, addObjects );

        // Remove the area after adding elements to it
        seekArea = new Area(1.0, 1.0, 1.5, 1.5);
        requestedAreas.remove(seekArea);

        // Cell should contain objects
        boolean gridCellEmpty = model.isEmpty(1.0, 1.0);
        assertThat(gridCellEmpty).isEqualTo(false);

        // After adding objects, model should not be waiting for more objects
        boolean waitingForObjects = model.hasGridCellsWaitingForObjects();
        assertThat(waitingForObjects).isEqualTo(false);

        // Lists should not contain any objects
        assertThat(removedObjects).isEmpty();
        assertThat(requestedAreas).isEmpty();
        assertThat(requestedCacheAreas).isEmpty();

        // Update table. Jumps to another location which is far away from current location.
        System.out.println(testName+": update model (21.0, 17.0, 21.25, 17.25)");
        model.updateTable(21.0, 17.0, 21.25, 17.25);
        Area requestedArea = new Area(21.0, 17.0, 21.50, 17.50);
        // Latest table update should have requested an area instead of caching because it starts
        // already from the edge
        assertThat(requestedAreas).contains(requestedArea);
        assertThat(requestedAreas).hasSize(1);
        assertThat(removedObjects).hasSize(4);
        // Check that the removed objects has exactly the objects which were added to the cell.
        assertThat(removedObjects).containsExactlyElementsOf(addObjects);
        assertThat(requestedCacheAreas).isEmpty();
        requestedAreas.clear();
        removedObjects.clear();

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testModelAddTwiceToCell() throws Exception {
        final String testName = "testModelAddTwiceToCell";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        final List<MapObject> removedObjects = new ArrayList<>();
        final List<Area> requestedAreas = new ArrayList<>();
        final List<Area> requestedCacheAreas = new ArrayList<>();
        MapTableModel<MapObject> model = initModel(testName, removedObjects, requestedAreas, requestedCacheAreas);

        // Adding twice to same cell should not cause exceptions.
        List<MapObject> addObjects = new ArrayList<>();
        model.addObjects(1.0, 1.0, addObjects );
        model.addObjects(1.0, 1.0, addObjects );

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testModelAddToUnknownToCell() throws Exception {
        final String testName = "testModelAddToUnknownToCell";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        final List<MapObject> removedObjects = new ArrayList<>();
        final List<Area> requestedAreas = new ArrayList<>();
        final List<Area> requestedCacheAreas = new ArrayList<>();
        MapTableModel<MapObject> model = initModel(testName, removedObjects, requestedAreas, requestedCacheAreas);

        // Adding twice to same cell should not cause exceptions.
        List<MapObject> addObjects = new ArrayList<>();
        try {
            model.addObjects(10.0, 10.0, addObjects);
            fail("Method should throw a null pointer exeption!");
        } catch (RuntimeException e) {
            assertThat(e.getClass()).isEqualTo(NullPointerException.class);
        }

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    /**
     * Creates the model and callback listener. Updates the model so that it contains one grid cell
     * which is waiting for objects. Model is at location (1.0, 1.0, 1.25, 1.25) and contains cell
     * (1.0, 1.0, 1.5, 1.5).
     *
     * @param removedObjects
     * @param requestedAreas
     * @param requestedCacheAreas
     * @return Returns the map model
     */
    private MapTableModel<MapObject> initModel(final String testName, final List<MapObject> removedObjects,
                           final List<Area> requestedAreas, final List<Area> requestedCacheAreas) {
        MapTableModel<MapObject> model = new MapTableModel<>(0.5, 0.5);
        model.addListener( new MapTableModelListener<MapObject>() {
            @Override
            public void objectRemoved(MapObject obj) {
                System.out.println( testName +": objectRemoved!");
                removedObjects.add(obj);
            }

            @Override
            public void requestCache(double latGridStart, double lonGridStart, double latGridEnd, double lonGridEnd) {
                System.out.println( testName +": requestCache, start: "+ latGridStart +", "+ lonGridStart +
                        " end: "+ latGridEnd +", "+ lonGridEnd);
                requestedCacheAreas.add(new Area(latGridStart, lonGridStart, latGridEnd, lonGridEnd));
            }

            @Override
            public void requestObjects(double latGridStart, double lonGridStart, double latGridEnd, double lonGridEnd) {
                System.out.println( testName +": requestObjects, start: "+ latGridStart +", "+ lonGridStart +
                        " end: "+ latGridEnd +", "+ lonGridEnd);
                requestedAreas.add(new Area(latGridStart, lonGridStart, latGridEnd, lonGridEnd));
            }
        });

        // Should not be waiting for new objects at the beginning
        boolean waitingForObjects = model.hasGridCellsWaitingForObjects();
        assertThat(waitingForObjects).isEqualTo(false);

        // Grid cell should be empty
        boolean gridCellEmpty = model.isEmpty(1.0, 1.0);
        assertThat(gridCellEmpty).isEqualTo(true);

        // Update table with new area
        model.updateTable(1.0, 1.0, 1.25, 1.25);

        // Model should have requested a new area
        Area seekArea = new Area(1.0, 1.0, 1.5, 1.5);
        assertThat(requestedAreas).contains(seekArea);

        // Cell should still be empty
        gridCellEmpty = model.isEmpty(1.0, 1.0);
        assertThat(gridCellEmpty).isEqualTo(true);

        // After new area is requested a cell should be waiting for map objects
        waitingForObjects = model.hasGridCellsWaitingForObjects();
        assertThat(waitingForObjects).isEqualTo(true);
        return model;
    }
}
