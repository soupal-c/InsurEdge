package tests;

import org.testng.annotations.Test;

public class SmokeTest extends BaseTest { // Extends BaseTest to use 'driver'

    @Test
    public void testBrowserLaunch() {
        System.out.println(">>> SMOKE TEST STARTED: Opening Browser...");

        // This validates if the driver from BaseTest is alive
        if (driver != null) {
            System.out.println(">>> SUCCESS: Browser opened!");
        } else {
            System.out.println(">>> FAILURE: Driver is null.");
        }
    }
}