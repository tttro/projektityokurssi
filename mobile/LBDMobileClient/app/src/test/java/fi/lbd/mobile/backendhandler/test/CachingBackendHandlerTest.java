package fi.lbd.mobile.backendhandler.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowLog;

import fi.lbd.mobile.CustomRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
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

    }
}
