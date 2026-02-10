package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import java.time.Duration;

public class BaseTest {
    protected WebDriver driver;

    // This runs automatically BEFORE every single test case
    @BeforeMethod
    public void setup() {
        // 1. Open Chrome
        driver = new ChromeDriver();
        
        // 2. Maximize the window
        driver.manage().window().maximize();
        
        // 3. Set a global wait time (10 seconds) for elements to load
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        // 4. Navigate to the Insurance Application URL
        driver.get("https://qeaskillhub.cognizant.com/LoginPage");
    }

    // This runs automatically AFTER every single test case
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit(); // Closes the browser window
        }
    }
}