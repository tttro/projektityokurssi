package fi.lbd.mobile.mapobject.test;


import android.util.Log;

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
import static org.mockito.Mockito.*;


@RunWith(CustomRobolectricTestRunner.class)
public class MapTableModelTest {
    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
    }

    // TODO: Saman alueen hakeminen moneen kertaan?
    // TODO: edes takas hakeminen
    // TODO: Antaa null listan => Pitäis tulla poikkeus?

    @Test
    public void testModelBasic() throws Exception {
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: testModelBasic");
        System.out.println("---------------------------------------------------------------------");

        MapTableModel<MapObject> model = new MapTableModel<>(0.5, 0.5);
        final List<MapObject> removedObjects = new ArrayList<>();
        final List<Area> requestedAreas = new ArrayList<>();
        final List<Area> requestedCacheAreas = new ArrayList<>();

        initModel("testModelBasic", model, removedObjects, requestedAreas, requestedCacheAreas);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testModelMoveUp() throws Exception {
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: testModelMoveUp");
        System.out.println("---------------------------------------------------------------------");

        MapTableModel<MapObject> model = new MapTableModel<>(0.5, 0.5);
        final List<MapObject> removedObjects = new ArrayList<>();
        final List<Area> requestedAreas = new ArrayList<>();
        final List<Area> requestedCacheAreas = new ArrayList<>();
        Area seekArea;

        initModel("testModelMoveUp", model, removedObjects, requestedAreas, requestedCacheAreas);
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
        assertThat(requestedCacheAreas).hasSize(2); // Contains 2 cached areas, down and left.

        // Update table
        System.out.println("testModelMoveUp: update model (1.1, 1.0, 1.35, 1.25)");
        model.updateTable(1.1, 1.0, 1.35, 1.25);
        seekArea = new Area(1.5, 1.0, 2.0, 1.5);
        // Latest table update should have requested an area for caching
        assertThat(requestedCacheAreas).contains(seekArea);
        assertThat(requestedCacheAreas).hasSize(3);
        assertThat(removedObjects).isEmpty();
        assertThat(requestedAreas).isEmpty();

        System.out.println("testModelMoveUp: update model (1.2, 1.0, 1.45, 1.25)");
        model.updateTable(1.2, 1.0, 1.45, 1.25);
        assertThat(requestedCacheAreas).hasSize(3);
        assertThat(removedObjects).isEmpty();
        assertThat(requestedAreas).isEmpty();

        System.out.println("testModelMoveUp: update model (1.3, 1.0, 1.55, 1.25)");
        model.updateTable(1.3, 1.0, 1.55, 1.25);
        seekArea = new Area(1.5, 1.0, 2.0, 1.5);
        // Model should request the area which was previously cached
        assertThat(requestedAreas).contains(seekArea);
        assertThat(requestedAreas).hasSize(1);
        assertThat(requestedCacheAreas).hasSize(3);
        assertThat(removedObjects).isEmpty();

        System.out.println("testModelMoveUp: update model (1.4, 1.0, 1.65, 1.25)");
        model.updateTable(1.4, 1.0, 1.65, 1.25);
        assertThat(requestedCacheAreas).hasSize(3);
        assertThat(requestedAreas).hasSize(1);
        assertThat(removedObjects).isEmpty();

        System.out.println("testModelMoveUp: update model (1.5, 1.0, 1.75, 1.25)");
        model.updateTable(1.5, 1.0, 1.75, 1.25);
        assertThat(requestedCacheAreas).hasSize(3);
        assertThat(requestedAreas).hasSize(1);
        assertThat(removedObjects).hasSize(4);

        System.out.println("testModelMoveUp: update model (1.6, 1.0, 1.85, 1.25)");
        model.updateTable(1.6, 1.0, 1.85, 1.25);
        // Lists should not contain any objects
//        assertThat(removedObjects).isEmpty();
//        assertThat(requestedAreas).isEmpty();
//        assertThat(requestedCacheAreas).isEmpty();

        // Update table with new area

//        model.updateTable(1.35, 1.0, 1.6, 1.5);
//
//        model.updateTable(1.45, 1.0, 1.7, 1.5);
//
//        model.updateTable(1.55, 1.0, 1.8, 1.5);
//
//        model.updateTable(1.65, 1.0, 1.9, 1.5);

        System.out.println("_____________________________________________________________________");
    }

    /**
     * Creates the model and callback listener. Updates the model so that it contains one populated
     * grid cell. Model is at location (1.0, 1.0, 1.5, 1.5) and contains cell (1.0, 1.0, 2.0, 2.0)
     * with 4 mocked immutable map objects.
     *
     * @param model
     * @param removedObjects
     * @param requestedAreas
     * @param requestedCacheAreas
     */
    private void initModel(final String testName, MapTableModel<MapObject> model, final List<MapObject> removedObjects,
                           final List<Area> requestedAreas, final List<Area> requestedCacheAreas) {
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

    }
}
