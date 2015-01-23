ANDROID CLIENT
==========

Used components
-----------------
* Google Maps API https://developers.google.com/maps/documentation/android/
* AssertJ (Apache 2.0) https://github.com/joel-costigliola/assertj-core
* Guava (Apache 2.0) https://code.google.com/p/guava-libraries/
* Hamcrest (New BSD License) https://code.google.com/p/hamcrest/
* Jackson JSON (Apache 2.0) http://jackson.codehaus.org/
* JaCoCo (Eclipse 1.0) http://www.eclemma.org/jacoco/trunk/
* JUnit (Eclipse 1.0) http://junit.org/
* Mockito (MIT License) https://github.com/mockito/mockito
* Otto bus (Apache 2.0) https://github.com/square/otto
* Roboelectric (MIT License) https://github.com/robolectric/robolectric

Installing
-----------------
To compile the project, download newest Android Studio IDE and Android SDK tools from http://developer.android.com/sdk/index.html


Running tests
-----------------
1. Open terminal at the root path of the project (not in the /app/ folder).
2. Execute command: gradlew.bat test jacocoTestReport
3. Test report: /mobile/LBDMobileClient/app/build/test-report/index.html
4. Test coverage report: /mobile/LBDMobileClient/app/build/reports/jacoco/jacocoTestReport/html/index.html