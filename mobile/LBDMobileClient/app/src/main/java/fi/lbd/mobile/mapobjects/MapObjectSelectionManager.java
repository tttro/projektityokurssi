package fi.lbd.mobile.mapobjects;

import fi.lbd.mobile.messaging.messageobjects.MessageObject;

/**
 * Contains the currently selected map object.
 *
 * Created by Tommi on 22.10.2014.
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

    /**
     * Set selected map object.
     * @param selectedMapObject
     */
    public void setSelectedMapObject(MapObject selectedMapObject) {
        this.selectedMapObject = selectedMapObject;
    }

    /**
     * Get selected map object.
     * @return
     */
    public MapObject getSelectedMapObject() {
        return this.selectedMapObject;
    }
}