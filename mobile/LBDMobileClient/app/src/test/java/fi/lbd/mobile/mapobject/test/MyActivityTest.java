package fi.lbd.mobile.mapobject.test;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import fi.lbd.mobile.CustomRobolectricTestRunner;
import fi.lbd.mobile.R;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(CustomRobolectricTestRunner.class)
public class MyActivityTest {

    @Test
    public void shouldHaveHappySmiles() throws Exception {
//        String hello = new ListActivity().getResources().getString(R.string.app_name);
        String hello = Robolectric.application.getString(R.string.app_name);
        assertThat(hello).isEqualTo(("LBDMobileClient"));
    }
}
