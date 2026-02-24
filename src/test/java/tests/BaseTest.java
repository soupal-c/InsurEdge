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

    @AfterMethod
    public void cleanupAfterTest(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            System.out.println("Robin: Hard reset for: " + result.getName());
            driver.get("https://qeaskillhub.cognizant.com/AdminDashboard");
        }
    }

    @AfterSuite
    public void tearDownSuite() {
        if (driver != null) {
            driver.quit();
        }
    }

    public String nameGenerator(String prefix) {
        String timeStr = String.valueOf(System.currentTimeMillis());
        // Appends the 5-digit slice to whatever prefix you pass in
        return prefix + timeStr.substring(timeStr.length() - 5);
    }
}