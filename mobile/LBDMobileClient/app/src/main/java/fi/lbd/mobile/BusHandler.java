package fi.lbd.mobile;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Handler for OTTO bus.
 *
 * Created by tommi on 19.10.2014.
 */
public final class BusHandler {
    public static final Bus BUS = new Bus();

    private BusHandler(){};
}
