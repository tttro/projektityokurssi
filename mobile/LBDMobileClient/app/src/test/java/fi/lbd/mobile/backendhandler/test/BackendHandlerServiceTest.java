package fi.lbd.mobile.backendhandler.test;

import android.util.Pair;

import com.squareup.otto.Subscribe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.ServiceController;

import java.security.cert.Certificate;

import fi.lbd.mobile.CustomRobolectricTestRunner;
import fi.lbd.mobile.backendhandler.AuthProvider;
import fi.lbd.mobile.backendhandler.BackendHandlerService;
import fi.lbd.mobile.backendhandler.BasicUrlReader;
import fi.lbd.mobile.backendhandler.UrlProvider;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.events.CacheObjectsInAreaEvent;
import fi.lbd.mobile.mapobjects.events.RequestMapObjectEvent;
import fi.lbd.mobile.mapobjects.events.RequestNearObjectsEvent;
import fi.lbd.mobile.mapobjects.events.RequestObjectsInAreaEvent;
import fi.lbd.mobile.mapobjects.events.ReturnMapObjectEvent;
import fi.lbd.mobile.mapobjects.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.mapobjects.events.ReturnObjectsInAreaEvent;
import fi.lbd.mobile.utils.DummyAuthProvider;
import fi.lbd.mobile.utils.TestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for the backend handler service.
 *
 * Created by Tommi.
 */
@RunWith(CustomRobolectricTestRunner.class)
public class BackendHandlerServiceTest {

    // http://robolectric.org/activity-lifecycle/
    private ServiceController<BackendHandlerService> serviceCtrlr;
    private UrlProvider urlProvider;
    private AuthProvider authProvider;
    private BasicUrlReader urlreader;

    private boolean bReturnNearObjectsEvent = false;
    private boolean bReturnObjectsInAreaEvent = false;
    private boolean bReturnMapObjectEvent = false;
    private boolean bRequestFailedEvent = false;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        this.serviceCtrlr = Robolectric.buildService(BackendHandlerService.class);
        this.urlProvider = new UrlProvider("", "", "", "");
        this.authProvider = new DummyAuthProvider();
        this.urlreader = new BasicUrlReader();
        this.urlreader.initialize(this.authProvider, new Pair<>("", mock(Certificate.class)));
        BusHandler.getBus().register(this);
    }

    @Subscribe
    public void onEvent(final ReturnNearObjectsEvent event) {
        System.out.println("onEvent: ReturnNearObjectsEvent");
        bReturnNearObjectsEvent = true;
    }
    @Subscribe
    public void onEvent(final ReturnObjectsInAreaEvent event) {
        System.out.println("onEvent: ReturnObjectsInAreaEvent");
        bReturnObjectsInAreaEvent = true;
    }
    @Subscribe
    public void onEvent(final ReturnMapObjectEvent event) {
        System.out.println("onEvent: ReturnMapObjectEvent");
        bReturnMapObjectEvent = true;
    }
    @Subscribe
    public void onEvent(final RequestFailedEvent event) {
        System.out.println("onEvent: RequestFailedEvent: "+ event);
        bRequestFailedEvent = true;
    }

    @Test
    public void testServiceObjectDetails() throws Exception {
        final String testName = "testServiceObjectDetails";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        BackendHandlerService service = this.serviceCtrlr.create().startCommand(0,0).get();
        service.forceUrlReaderAndProvider(urlreader, this.urlProvider);

        this.bReturnNearObjectsEvent = false;
        this.bReturnObjectsInAreaEvent = false;
        this.bReturnMapObjectEvent = false;
        this.bRequestFailedEvent = false;
        Robolectric.addPendingHttpResponse(200, TestData.testSingleJson);
        BusHandler.getBus().post(new RequestMapObjectEvent("TestEvent"));
        service.shutdown(); // Order service to stop executors
        service.awaitTermination(); // Wait for the executors to stop processing their current tasks.
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.

        assertThat(this.bReturnNearObjectsEvent).isEqualTo(false);
        assertThat(this.bReturnObjectsInAreaEvent).isEqualTo(false);
        assertThat(this.bReturnMapObjectEvent).isEqualTo(true);
        assertThat(this.bRequestFailedEvent).isEqualTo(false);
        assertThat(Robolectric.getFakeHttpLayer().hasPendingResponses()).isEqualTo(false);

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testServiceObjectDetailsFail() throws Exception {
        final String testName = "testServiceObjectDetailsFail";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        BackendHandlerService service = this.serviceCtrlr.create().startCommand(0,0).get();
        service.forceUrlReaderAndProvider(urlreader, this.urlProvider);

        this.bReturnNearObjectsEvent = false;
        this.bReturnObjectsInAreaEvent = false;
        this.bReturnMapObjectEvent = false;
        this.bRequestFailedEvent = false;
        Robolectric.addPendingHttpResponse(405, TestData.testSingleJson);
        BusHandler.getBus().post(new RequestMapObjectEvent("TestEvent"));
        service.shutdown(); // Order service to stop executors
        service.awaitTermination(); // Wait for the executors to stop processing their current tasks.
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.

        assertThat(this.bReturnNearObjectsEvent).isEqualTo(false);
        assertThat(this.bReturnObjectsInAreaEvent).isEqualTo(false);
        assertThat(this.bReturnMapObjectEvent).isEqualTo(false);
        assertThat(this.bRequestFailedEvent).isEqualTo(true);
        assertThat(Robolectric.getFakeHttpLayer().hasPendingResponses()).isEqualTo(false);

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testServiceObjectsInArea() throws Exception {
        final String testName = "testServiceObjectsInArea";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        BackendHandlerService service = this.serviceCtrlr.create().startCommand(0,0).get();
        service.forceUrlReaderAndProvider(urlreader, this.urlProvider);

        this.bReturnNearObjectsEvent = false;
        this.bReturnObjectsInAreaEvent = false;
        this.bReturnMapObjectEvent = false;
        this.bRequestFailedEvent = false;
        Robolectric.addPendingHttpResponse(200, TestData.testJsonMini);
        BusHandler.getBus().post(new RequestObjectsInAreaEvent(mock(ImmutablePointLocation.class),
                mock(ImmutablePointLocation.class)));
        service.shutdown(); // Order service to stop executors
        service.awaitTermination(); // Wait for the executors to stop processing their current tasks.
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.

        assertThat(this.bReturnNearObjectsEvent).isEqualTo(false);
        assertThat(this.bReturnObjectsInAreaEvent).isEqualTo(true);
        assertThat(this.bReturnMapObjectEvent).isEqualTo(false);
        assertThat(this.bRequestFailedEvent).isEqualTo(false);
        assertThat(Robolectric.getFakeHttpLayer().hasPendingResponses()).isEqualTo(false);

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testServiceObjectsInAreaFail() throws Exception {
        final String testName = "testServiceObjectsInAreaFail";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        BackendHandlerService service = this.serviceCtrlr.create().startCommand(0,0).get();
        service.forceUrlReaderAndProvider(urlreader, this.urlProvider);

        this.bReturnNearObjectsEvent = false;
        this.bReturnObjectsInAreaEvent = false;
        this.bReturnMapObjectEvent = false;
        this.bRequestFailedEvent = false;
        Robolectric.addPendingHttpResponse(405, TestData.testJsonMini);
        BusHandler.getBus().post(new RequestObjectsInAreaEvent(mock(ImmutablePointLocation.class),
                mock(ImmutablePointLocation.class)));
        service.shutdown(); // Order service to stop executors
        service.awaitTermination(); // Wait for the executors to stop processing their current tasks.
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.

        assertThat(this.bReturnNearObjectsEvent).isEqualTo(false);
        assertThat(this.bReturnObjectsInAreaEvent).isEqualTo(false);
        assertThat(this.bReturnMapObjectEvent).isEqualTo(false);
        assertThat(this.bRequestFailedEvent).isEqualTo(true);
        assertThat(Robolectric.getFakeHttpLayer().hasPendingResponses()).isEqualTo(false);

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }



    @Test
    public void testServiceCacheObjectsInArea() throws Exception {
        final String testName = "testServiceObjectsInArea";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        BackendHandlerService service = this.serviceCtrlr.create().startCommand(0,0).get();
        service.forceUrlReaderAndProvider(urlreader, this.urlProvider);

        this.bReturnNearObjectsEvent = false;
        this.bReturnObjectsInAreaEvent = false;
        this.bReturnMapObjectEvent = false;
        this.bRequestFailedEvent = false;
        Robolectric.addPendingHttpResponse(200, TestData.testJsonMini);
        BusHandler.getBus().post(new CacheObjectsInAreaEvent(mock(ImmutablePointLocation.class),
                mock(ImmutablePointLocation.class)));
        service.shutdown(); // Order service to stop executors
        service.awaitTermination(); // Wait for the executors to stop processing their current tasks.
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.

        assertThat(this.bReturnNearObjectsEvent).isEqualTo(false);
        assertThat(this.bReturnObjectsInAreaEvent).isEqualTo(false);
        assertThat(this.bReturnMapObjectEvent).isEqualTo(false);
        assertThat(this.bRequestFailedEvent).isEqualTo(false);
        assertThat(Robolectric.getFakeHttpLayer().hasPendingResponses()).isEqualTo(false);

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testServiceCacheObjectsInAreaFail() throws Exception {
        final String testName = "testServiceCacheObjectsInAreaFail";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        BackendHandlerService service = this.serviceCtrlr.create().startCommand(0,0).get();
        service.forceUrlReaderAndProvider(urlreader, this.urlProvider);

        this.bReturnNearObjectsEvent = false;
        this.bReturnObjectsInAreaEvent = false;
        this.bReturnMapObjectEvent = false;
        this.bRequestFailedEvent = false;
        Robolectric.addPendingHttpResponse(200, TestData.testInvalidJson1);
        BusHandler.getBus().post(new CacheObjectsInAreaEvent(mock(ImmutablePointLocation.class),
                mock(ImmutablePointLocation.class)));
        service.shutdown(); // Order service to stop executors
        service.awaitTermination(); // Wait for the executors to stop processing their current tasks.
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.

        assertThat(this.bReturnNearObjectsEvent).isEqualTo(false);
        assertThat(this.bReturnObjectsInAreaEvent).isEqualTo(false);
        assertThat(this.bReturnMapObjectEvent).isEqualTo(false);
        assertThat(this.bRequestFailedEvent).isEqualTo(true);
        assertThat(Robolectric.getFakeHttpLayer().hasPendingResponses()).isEqualTo(false);

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testServiceObjectsNearPoint() throws Exception {
        final String testName = "testServiceObjectsNearPoint";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        BackendHandlerService service = this.serviceCtrlr.create().startCommand(0,0).get();
        service.forceUrlReaderAndProvider(urlreader, this.urlProvider);

        this.bReturnNearObjectsEvent = false;
        this.bReturnObjectsInAreaEvent = false;
        this.bReturnMapObjectEvent = false;
        this.bRequestFailedEvent = false;
        Robolectric.addPendingHttpResponse(200, TestData.testJsonMini);
        BusHandler.getBus().post(new RequestNearObjectsEvent(mock(ImmutablePointLocation.class), 0.001, true));
        service.shutdown(); // Order service to stop executors
        service.awaitTermination(); // Wait for the executors to stop processing their current tasks.
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.

        assertThat(this.bReturnNearObjectsEvent).isEqualTo(true);
        assertThat(this.bReturnObjectsInAreaEvent).isEqualTo(false);
        assertThat(this.bReturnMapObjectEvent).isEqualTo(false);
        assertThat(this.bRequestFailedEvent).isEqualTo(false);
        assertThat(Robolectric.getFakeHttpLayer().hasPendingResponses()).isEqualTo(false);

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testServiceObjectsNearPointFail() throws Exception {
        final String testName = "testServiceObjectsNearPointFail";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        BackendHandlerService service = this.serviceCtrlr.create().startCommand(0,0).get();
        service.forceUrlReaderAndProvider(urlreader, this.urlProvider);

        this.bReturnNearObjectsEvent = false;
        this.bReturnObjectsInAreaEvent = false;
        this.bReturnMapObjectEvent = false;
        this.bRequestFailedEvent = false;
        Robolectric.addPendingHttpResponse(405, TestData.testJsonMini);
        BusHandler.getBus().post(new RequestNearObjectsEvent(mock(ImmutablePointLocation.class), 0.001, true));
        service.shutdown(); // Order service to stop executors
        service.awaitTermination(); // Wait for the executors to stop processing their current tasks.
        Robolectric.runUiThreadTasks(); // Wait for the UI thread to stop. Processes OTTO-events.

        assertThat(this.bReturnNearObjectsEvent).isEqualTo(false);
        assertThat(this.bReturnObjectsInAreaEvent).isEqualTo(false);
        assertThat(this.bReturnMapObjectEvent).isEqualTo(false);
        assertThat(this.bRequestFailedEvent).isEqualTo(true);
        assertThat(Robolectric.getFakeHttpLayer().hasPendingResponses()).isEqualTo(false);

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @After
    public void tearDown() {
        Robolectric.getFakeHttpLayer().clearPendingHttpResponses();
        this.serviceCtrlr.destroy();
        BusHandler.getBus().unregister(this);
    }
}
