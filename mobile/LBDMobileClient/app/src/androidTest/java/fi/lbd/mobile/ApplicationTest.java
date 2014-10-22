package fi.lbd.mobile;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 *
 * http://robolectric.org/
 * http://tech.pristine.io/android-unit-test-idioms/
 * http://www.bignerdranch.com/blog/testing-the-android-way/
 * https://github.com/roboguice/roboguice/tree/master/astroboy
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }
}