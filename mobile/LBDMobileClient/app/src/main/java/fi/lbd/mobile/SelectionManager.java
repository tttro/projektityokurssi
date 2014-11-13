package fi.lbd.mobile;

import fi.lbd.mobile.mapobjects.MapObject;

/**
 *
 * TODO: Ennemmin dependency injectionilla? Dagger tms.
 * Created by tommi on 22.10.2014.
 */
public final class SelectionManager {
    private static SelectionManager singleton;
    private SelectionManager() {}
    private MapObject selectedObject;

    public static void initialize() {
        if (SelectionManager.singleton == null) {
            SelectionManager.singleton = new SelectionManager();
        }
    }
    public static SelectionManager get() {
        return SelectionManager.singleton;
    }
    public void setSelection(MapObject selectedObject) {
        this.selectedObject = selectedObject;
    }
    public MapObject getSelectedObject() {
        return this.selectedObject;
    }
}