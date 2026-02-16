package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import pages.LoginPage;
import java.time.Duration;

public class BaseTest {
    protected WebDriver driver;

    @BeforeClass
    public void setup() {
        // 1. Open Browser
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // 2. Login immediately (Common for all tests in this file)
        driver.get("https://qeaskillhub.cognizant.com/LoginPage");
        LoginPage login = new LoginPage(driver);
        login.doLogin("admin_user", "testadmin");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit(); // Closes the browser after ALL tests are done
        }
    }
}