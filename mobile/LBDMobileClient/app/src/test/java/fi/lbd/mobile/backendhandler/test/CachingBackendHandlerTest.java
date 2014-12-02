package fi.lbd.mobile.backendhandler.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLog;

import fi.lbd.mobile.CustomRobolectricTestRunner;
import fi.lbd.mobile.backendhandler.BackendHandler;
import fi.lbd.mobile.backendhandler.BasicBackendHandler;
import fi.lbd.mobile.backendhandler.CachingBackendHandler;
import fi.lbd.mobile.backendhandler.HandlerResponse;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.location.PointLocation;
import fi.lbd.mobile.utils.TestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for the CachingBackendHandler.
 *
 * Created by Tommi.
 */
@RunWith(CustomRobolectricTestRunner.class)
public class CachingBackendHandlerTest {
    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testCaching() throws Exception {
        final String testName = "testCaching";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");
        CachingBackendHandler handler = new CachingBackendHandler("", "", 100);

        PointLocation point1 = new ImmutablePointLocation(10, 10);
        PointLocation point2 = new ImmutablePointLocation(20, 20);

        Robolectric.addPendingHttpResponse(200, TestData.testJson);
        HandlerResponse response;

        response = handler.getObjectsInArea(point1, point2, false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Succeeded);
        assertThat(response.isOk()).isEqualTo(true);
        assertThat(response.getObjects()).hasSize(2);
        assertThat(response.getObjects().get(0).getAdditionalProperties()).hasSize(5);
        assertThat(response.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.5192743640121, 23.64941278370676));

        response = handler.getObjectsInArea(point1, point2, false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Cached);
        assertThat(response.isOk()).isEqualTo(true);
        assertThat(response.getObjects()).hasSize(2);
        assertThat(response.getObjects().get(0).getAdditionalProperties()).hasSize(5);
        assertThat(response.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.5192743640121, 23.64941278370676));

        Thread.sleep(150);
        handler.checkForOutdatedCaches();

        Robolectric.addPendingHttpResponse(200, TestData.testJson);
        response = handler.getObjectsInArea(point1, point2, false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Succeeded);
        assertThat(response.isOk()).isEqualTo(true);
        assertThat(response.getObjects()).hasSize(2);
        assertThat(response.getObjects().get(0).getAdditionalProperties()).hasSize(5);
        assertThat(response.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.5192743640121, 23.64941278370676));

        Thread.sleep(50);
        handler.checkForOutdatedCaches();

        response = handler.getObjectsInArea(point1, point2, false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Cached);
        assertThat(response.isOk()).isEqualTo(true);
        assertThat(response.getObjects()).hasSize(2);
        assertThat(response.getObjects().get(0).getAdditionalProperties()).hasSize(5);
        assertThat(response.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.5192743640121, 23.64941278370676));

        handler.checkForOutdatedCaches();

        response = handler.getObjectsInArea(point1, point2, false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Cached);
        assertThat(response.isOk()).isEqualTo(true);
        assertThat(response.getObjects()).hasSize(2);
        assertThat(response.getObjects().get(0).getAdditionalProperties()).hasSize(5);
        assertThat(response.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.5192743640121, 23.64941278370676));

        assertThat(Robolectric.getFakeHttpLayer().hasPendingResponses()).isEqualTo(false);

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");

    }

    @Test
    public void testCachingMinimized() throws Exception {
        final String testName = "testCachingMinimized";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");
        CachingBackendHandler handler = new CachingBackendHandler("", "", 100);

        PointLocation point1 = new ImmutablePointLocation(10, 10);
        PointLocation point2 = new ImmutablePointLocation(20, 20);

        Robolectric.addPendingHttpResponse(200, TestData.testJson);
        HandlerResponse response;

        response = handler.getObjectsInArea(point1, point2, false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Succeeded);
        assertThat(response.isOk()).isEqualTo(true);
        assertThat(response.getObjects()).hasSize(2);
        assertThat(response.getObjects().get(0).getAdditionalProperties()).hasSize(5);
        assertThat(response.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.5192743640121, 23.64941278370676));

        Robolectric.addPendingHttpResponse(200, TestData.testJsonMini);
        response = handler.getObjectsInArea(point1, point2, true);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Succeeded);
        assertThat(response.isOk()).isEqualTo(true);
        assertThat(response.getObjects()).hasSize(2);
        assertThat(response.getObjects().get(0).getAdditionalProperties()).hasSize(0);
        assertThat(response.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.5192743640121, 23.64941278370676));

        response = handler.getObjectsInArea(point1, point2, false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Cached);
        assertThat(response.isOk()).isEqualTo(true);
        assertThat(response.getObjects()).hasSize(2);
        assertThat(response.getObjects().get(0).getAdditionalProperties()).hasSize(5);
        assertThat(response.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.5192743640121, 23.64941278370676));

        response = handler.getObjectsInArea(point1, point2, true);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Cached);
        assertThat(response.isOk()).isEqualTo(true);
        assertThat(response.getObjects()).hasSize(2);
        assertThat(response.getObjects().get(0).getAdditionalProperties()).hasSize(0);
        assertThat(response.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.5192743640121, 23.64941278370676));

        assertThat(Robolectric.getFakeHttpLayer().hasPendingResponses()).isEqualTo(false);

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");

    }

    @After
    public void tearDown() {
        Robolectric.getFakeHttpLayer().clearPendingHttpResponses();
    }
}
