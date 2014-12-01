package fi.lbd.mobile.backendhandler.test;

import android.util.Log;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLog;

import java.util.HashMap;
import java.util.Map;

import fi.lbd.mobile.CustomRobolectricTestRunner;
import fi.lbd.mobile.backendhandler.BackendHandler;
import fi.lbd.mobile.backendhandler.BasicBackendHandler;
import fi.lbd.mobile.backendhandler.HandlerResponse;
import fi.lbd.mobile.backendhandler.MapObjectParser;
import fi.lbd.mobile.backendhandler.URLReader;
import fi.lbd.mobile.backendhandler.URLResponse;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.ImmutableMapObject;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.utils.TestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Created by Tommi.
 */
@RunWith(CustomRobolectricTestRunner.class)
public class BackendHandlerTest {



//    URLResponse response1 = URLReader.get("http://lbdbackend.ignorelist.com/locationdata/api/Streetlights/inarea/?xbottomleft=23.645&ybottomleft=61.515&ytopright=61.52&xtopright=23.65");
//    Log.e("ASDASDASD", response1.getContents());
    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testValidJsonInArea() throws Exception {
        final String testName = "testValidJsonInArea";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");
        BackendHandler handler = new BasicBackendHandler("", "");

        Robolectric.addPendingHttpResponse(200, TestData.testJson);
        HandlerResponse response = handler.getObjectsInArea(mock(ImmutablePointLocation.class), mock(ImmutablePointLocation.class), false);

        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Succeeded);
        assertThat(response.isOk()).isEqualTo(true);
        assertThat(response.getObjects()).hasSize(2);
        assertThat(response.getObjects().get(0).getAdditionalProperties()).hasSize(5);
        assertThat(response.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.5192743640121, 23.64941278370676));

        System.out.println("Test minimized.");
        Robolectric.addPendingHttpResponse(200, TestData.testJsonMini);
        HandlerResponse responseMini = handler.getObjectsInArea(mock(ImmutablePointLocation.class), mock(ImmutablePointLocation.class), true);

        assertThat(responseMini.getStatus()).isEqualTo(HandlerResponse.Status.Succeeded);
        assertThat(responseMini.isOk()).isEqualTo(true);
        assertThat(responseMini.getObjects()).hasSize(2);
        assertThat(responseMini.getObjects().get(0).getAdditionalProperties()).hasSize(0);
        assertThat(responseMini.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.5192743640121, 23.64941278370676));

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testInvalidJsonInArea() throws Exception {
        final String testName = "testInvalidJsonInArea";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");
        BackendHandler handler = new BasicBackendHandler("", "");

        Robolectric.addPendingHttpResponse(200, TestData.testInvalidJson1);
        HandlerResponse response1 = handler.getObjectsInArea(mock(ImmutablePointLocation.class), mock(ImmutablePointLocation.class), false);

        assertThat(response1.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response1.isOk()).isEqualTo(false);
        assertThat(response1.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(200, TestData.testInvalidJson2);
        HandlerResponse response2 = handler.getObjectsInArea(mock(ImmutablePointLocation.class), mock(ImmutablePointLocation.class), false);

        assertThat(response2.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response2.isOk()).isEqualTo(false);
        assertThat(response2.getObjects()).isNull();

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testValidJsonNear() throws Exception {
        final String testName = "testValidJsonNear";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");
        BackendHandler handler = new BasicBackendHandler("", "");

        Robolectric.addPendingHttpResponse(200, TestData.testJson);
        HandlerResponse response = handler.getObjectsNearLocation(mock(ImmutablePointLocation.class), 0.001, false);

        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Succeeded);
        assertThat(response.isOk()).isEqualTo(true);
        assertThat(response.getObjects()).hasSize(2);
        assertThat(response.getObjects().get(0).getAdditionalProperties()).hasSize(5);
        assertThat(response.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.5192743640121, 23.64941278370676));

        System.out.println("Test minimized.");
        Robolectric.addPendingHttpResponse(200, TestData.testJsonMini);
        HandlerResponse responseMini = handler.getObjectsNearLocation(mock(ImmutablePointLocation.class), 0.001, true);

        assertThat(responseMini.getStatus()).isEqualTo(HandlerResponse.Status.Succeeded);
        assertThat(responseMini.isOk()).isEqualTo(true);
        assertThat(responseMini.getObjects()).hasSize(2);
        assertThat(responseMini.getObjects().get(0).getAdditionalProperties()).hasSize(0);
        assertThat(responseMini.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.5192743640121, 23.64941278370676));

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testInvalidJsonNear() throws Exception {
        final String testName = "testInvalidJsonNear";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");
        BackendHandler handler = new BasicBackendHandler("", "");

        Robolectric.addPendingHttpResponse(200, TestData.testInvalidJson1);
        HandlerResponse response1 = handler.getObjectsNearLocation(mock(ImmutablePointLocation.class), 0.001, false);

        assertThat(response1.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response1.isOk()).isEqualTo(false);
        assertThat(response1.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(200, TestData.testInvalidJson2);
        HandlerResponse response2 = handler.getObjectsNearLocation(mock(ImmutablePointLocation.class), 0.001, false);

        assertThat(response2.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response2.isOk()).isEqualTo(false);
        assertThat(response2.getObjects()).isNull();

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testValidSingle() throws Exception {
        final String testName = "testValidSingle";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");
        BackendHandler handler = new BasicBackendHandler("", "");

        Robolectric.addPendingHttpResponse(200, TestData.testSingleJson);
        HandlerResponse response = handler.getMapObject("");

        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Succeeded);
        assertThat(response.isOk()).isEqualTo(true);
        assertThat(response.getObjects()).hasSize(1);
        assertThat(response.getObjects().get(0).getAdditionalProperties()).hasSize(5);
        assertThat(response.getObjects().get(0).getPointLocation()).isEqualTo(new ImmutablePointLocation(61.519112683582854, 23.643239226767022));

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testInvalidSingle() throws Exception {
        final String testName = "testInvalidSingle";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");
        BackendHandler handler = new BasicBackendHandler("", "");

        Robolectric.addPendingHttpResponse(200, TestData.testInvalidSingleJson1);
        HandlerResponse response1 = handler.getMapObject("");

        assertThat(response1.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response1.isOk()).isEqualTo(false);
        assertThat(response1.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(200, TestData.testInvalidSingleJson2);
        HandlerResponse response2 = handler.getMapObject("");

        assertThat(response2.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response2.isOk()).isEqualTo(false);
        assertThat(response2.getObjects()).isNull();

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }


    @Test
    public void testStatusCodesSingle() throws Exception {
        final String testName = "testStatusCodesSingle";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");
        BackendHandler handler = new BasicBackendHandler("", "");
        HandlerResponse response;

        Robolectric.addPendingHttpResponse(403, TestData.testSingleJson);
        response = handler.getMapObject("");
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(405, TestData.testSingleJson);
        response = handler.getMapObject("");
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(418, TestData.testSingleJson);
        response = handler.getMapObject("");
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(500, TestData.testSingleJson);
        Robolectric.addPendingHttpResponse(500, TestData.testSingleJson);
        response = handler.getMapObject("");
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(500, TestData.testSingleJson);
        Robolectric.addPendingHttpResponse(200, TestData.testSingleJson);
        response = handler.getMapObject("");
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Succeeded);
        assertThat(response.getObjects()).hasSize(1);

        Robolectric.addPendingHttpResponse(404, TestData.testSingleJson);
        Robolectric.addPendingHttpResponse(404, TestData.testSingleJson);
        response = handler.getMapObject("");
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }


    @Test
    public void testStatusCodesArea() throws Exception {
        final String testName = "testStatusCodesArea";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");
        BackendHandler handler = new BasicBackendHandler("", "");
        HandlerResponse response;

        Robolectric.addPendingHttpResponse(403, TestData.testJson);
        response = handler.getObjectsInArea(mock(ImmutablePointLocation.class), mock(ImmutablePointLocation.class), false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(405, TestData.testJson);
        response = handler.getObjectsInArea(mock(ImmutablePointLocation.class), mock(ImmutablePointLocation.class), false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(418, TestData.testJson);
        response = handler.getObjectsInArea(mock(ImmutablePointLocation.class), mock(ImmutablePointLocation.class), false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(500, TestData.testJson);
        Robolectric.addPendingHttpResponse(500, TestData.testJson);
        response = handler.getObjectsInArea(mock(ImmutablePointLocation.class), mock(ImmutablePointLocation.class), false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(500, TestData.testJson);
        Robolectric.addPendingHttpResponse(200, TestData.testJson);
        response = handler.getObjectsInArea(mock(ImmutablePointLocation.class), mock(ImmutablePointLocation.class), false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Succeeded);
        assertThat(response.getObjects()).hasSize(2);

        Robolectric.addPendingHttpResponse(404, TestData.testJson);
        Robolectric.addPendingHttpResponse(404, TestData.testJson);
        response = handler.getObjectsInArea(mock(ImmutablePointLocation.class), mock(ImmutablePointLocation.class), false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }


    @Test
    public void testStatusCodesNear() throws Exception {
        final String testName = "testStatusCodesArea";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");
        BackendHandler handler = new BasicBackendHandler("", "");
        HandlerResponse response;

        Robolectric.addPendingHttpResponse(403, TestData.testJson);
        response = handler.getObjectsNearLocation(mock(ImmutablePointLocation.class), 0.001, false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(405, TestData.testJson);
        response = handler.getObjectsNearLocation(mock(ImmutablePointLocation.class), 0.001, false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(418, TestData.testJson);
        response = handler.getObjectsNearLocation(mock(ImmutablePointLocation.class), 0.001, false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(500, TestData.testJson);
        Robolectric.addPendingHttpResponse(500, TestData.testJson);
        response = handler.getObjectsNearLocation(mock(ImmutablePointLocation.class), 0.001, false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        Robolectric.addPendingHttpResponse(500, TestData.testJson);
        Robolectric.addPendingHttpResponse(200, TestData.testJson);
        response = handler.getObjectsNearLocation(mock(ImmutablePointLocation.class), 0.001, false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Succeeded);
        assertThat(response.getObjects()).hasSize(2);

        Robolectric.addPendingHttpResponse(404, TestData.testJson);
        Robolectric.addPendingHttpResponse(404, TestData.testJson);
        response = handler.getObjectsNearLocation(mock(ImmutablePointLocation.class), 0.001, false);
        assertThat(response.getStatus()).isEqualTo(HandlerResponse.Status.Failed);
        assertThat(response.getObjects()).isNull();

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }

    @Test
    public void testParserNullInputs() throws Exception {
        final String testName = "testInvalidSingle";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        boolean catchedError1 = false;
        try {
            MapObjectParser.parse(null);
        } catch (JSONException e) {
            catchedError1 = true;
        }
        assertThat(catchedError1).isEqualTo(true);

        boolean catchedError2 = false;
        try {
            MapObjectParser.parseCollection(null, false);
        } catch (JSONException e) {
            catchedError2 = true;
        }
        assertThat(catchedError2).isEqualTo(true);

        System.out.println("FINISHED: "+testName);
        System.out.println("_____________________________________________________________________");
    }
}
