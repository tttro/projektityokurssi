package fi.lbd.mobile;

/**
 * Created by Tommi on 3.1.2015.
 */
public class ApplicationDetails {
    private static ApplicationDetails singleton;

    private String userId;
    private String currentCategory;

    public static void initialize() {
        if (ApplicationDetails.singleton == null) {
            ApplicationDetails.singleton = new ApplicationDetails();
        }
    }

    public static ApplicationDetails get() {
        return ApplicationDetails.singleton;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getCurrentCategory() {
        return currentCategory;
    }
    public void setCurrentCategory(String currentCategory) {
        this.currentCategory = currentCategory;
    }


}
