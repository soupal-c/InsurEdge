package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;

public class LoginTest extends BaseTest { // Inherits 'driver' from BaseTest

    @Test
    public void verifyValidLogin() {
        // 1. Initialize the Page Object
        LoginPage loginPage = new LoginPage(driver);

        // 2. Perform the Action (Using your credentials)
        loginPage.doLogin("admin_user", "testadmin");

        // 3. Validation (Did we actually log in?)
        String currentUrl = driver.getCurrentUrl();
        String pageTitle = driver.getTitle();

        System.out.println("Current URL: " + currentUrl);
        System.out.println("Page Title: " + pageTitle);

        // Simple check: If URL or Title changed, we are in!
        // Adjust "Dashboard" to whatever word appears on your home page
        // Assert.assertTrue(pageTitle.contains("Dashboard"), "Login failed!");
    }
}