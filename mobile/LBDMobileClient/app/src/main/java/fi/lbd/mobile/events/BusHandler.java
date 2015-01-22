package fi.lbd.mobile.events;

/**
 * Handler for OTTO bus.
 *
 * Created by tommi on 19.10.2014.
 */
public final class BusHandler {
    private static WrappedEventBus bus;

    private BusHandler(){};

    public static void initialize() {
        BusHandler.bus = new WrappedEventBus();
    }

    public static WrappedEventBus getBus() {
        return BusHandler.bus;
    }
}
