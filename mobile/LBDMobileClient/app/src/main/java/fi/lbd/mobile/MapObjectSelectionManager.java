package fi.lbd.mobile;

import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.messageobjects.MessageObject;

/**
 *
 * TODO: Ennemmin dependency injectionilla? Dagger tms.
 * Created by tommi on 22.10.2014.
 */
public final class MapObjectSelectionManager {
    private static MapObjectSelectionManager singleton;
    private MapObjectSelectionManager() {}
    private MapObject selectedMapObject;
    private MessageObject selectedMessageObject;

    public static void initialize() {
        if (MapObjectSelectionManager.singleton == null) {
            MapObjectSelectionManager.singleton = new MapObjectSelectionManager();
        }
    }
    public static MapObjectSelectionManager get() {
        return MapObjectSelectionManager.singleton;
    }
    public void setSelectedMapObject(MapObject selectedMapObject) {
        this.selectedMapObject = selectedMapObject;
    }
    public MapObject getSelectedMapObject() {
        return this.selectedMapObject;
    }
}