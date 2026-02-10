package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

// Always extend BaseTest to handle browser open/close
public class Template_Test extends BaseTest {

    @Test(priority = 1, description = "Verify valid login flow")
    public void testLoginFunctionality() {
        // 1. Initialize Page Object
        Template_Page page = new Template_Page(driver);

        // 2. Execute Actions
        page.enterUsername("admin");
        page.clickSubmit();

        // 3. Validate Result (Assertions)
        String pageTitle = driver.getTitle();
        Assert.assertEquals(pageTitle, "Dashboard", "Login failed: Title mismatch!");
        
        // Example: Validate an element is displayed
        // Assert.assertTrue(driver.findElement(By.id("welcome")).isDisplayed());
    }
}