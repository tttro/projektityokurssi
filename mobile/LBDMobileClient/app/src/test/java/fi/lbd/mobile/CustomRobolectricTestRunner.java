package fi.lbd.mobile;

import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;
import org.junit.runners.model.InitializationError;


// Tools -> Open Terminal... -> (Oltava projektin juuressa, jos /app/ niin cd ..)gradlew.bat test jacocoTestReport
// file:///D:/Projektit/Projektityokurssi/projektityokurssi/mobile/LBDMobileClient/app/build/test-report/debug/index.html
// file:///D:/Projektit/Projektityokurssi/projektityokurssi/mobile/LBDMobileClient/app/build/reports/jacoco/jacocoTestReport/html/index.html

// http://www.peterfriese.de/android-testing-with-robolectric/
// https://github.com/codepath/android_guides/wiki/Robolectric-Installation-for-Unit-Testing#verify-installation
// https://github.com/square/assertj-android
// https://github.com/robolectric/robolectric/issues/1025
// http://stackoverflow.com/questions/17100830/android-gradle-code-coverage
// http://stackoverflow.com/questions/18358297/android-test-code-coverage-with-jacoco-gradle-plugin/24231246#24231246
public class CustomRobolectricTestRunner extends RobolectricTestRunner {
    private static final int MAX_SDK_SUPPORTED_BY_ROBOLECTRIC = 18;

    public CustomRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        String manifestProperty = System.getProperty("android.manifest");
        if (config.manifest().equals(Config.DEFAULT) && manifestProperty != null) {
            String resProperty = System.getProperty("android.resources");
            String assetsProperty = System.getProperty("android.assets");
            AndroidManifest androidManifest = new AndroidManifest(
                    Fs.fileFromPath(manifestProperty),
                    Fs.fileFromPath(resProperty),
                    Fs.fileFromPath(assetsProperty))  {
                @Override
                public int getTargetSdkVersion() {
                    return MAX_SDK_SUPPORTED_BY_ROBOLECTRIC;
                }
            };
//            androidManifest.setPackageName("fi.lbd");
            return androidManifest;
        }
        return super.getAppManifest(config);
    }
}