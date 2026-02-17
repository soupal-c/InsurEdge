package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import pages.LoginPage;
import java.time.Duration;

public class BaseTest {
    public static WebDriver driver;

    @BeforeSuite
    public void setupSuite() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        driver.get("https://qeaskillhub.cognizant.com/LoginPage");
        LoginPage login = new LoginPage(driver);
        login.doLogin("admin_user", "testadmin");
    }

    // THIS IS THE MISSING PIECE
    @AfterMethod
    public void cleanupAfterTest(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            System.out.println("Robin: Hard reset for: " + result.getName());
            // Instead of just refreshing, let's force go back to the starting URL
            // to break any stuck JS loops or modals
            driver.get("https://qeaskillhub.cognizant.com/AdminDashboard"); // Or your main admin page
        }
    }

    @AfterSuite
    public void tearDownSuite() {
        if (driver != null) {
            driver.quit();
        }
    }
}