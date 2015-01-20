package fi.lbd.mobile;

import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;
import org.junit.runners.model.InitializationError;


/**
 * Custom robolectric test runner.
 * https://github.com/robolectric/robolectric/issues/1025
 */
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