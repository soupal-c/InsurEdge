package tests;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import pages.SubCategoryPage;
import pages.CategoryPage;
import java.util.Set;
import java.util.List;
import java.time.Duration;

public class SubCategoryTest extends BaseTest {

    public String lastCreatedMainCategory = "AutoSync_" + System.currentTimeMillis();
    private String mainWindow;

    // US2-SC-01 - SubCategory Management UI
    //--------------------------------------

    // Task 1 - Verify UI Headers and Table

    // Task 2 -


    // US2-SC-02 - SubCategory Functional Flows
    //-----------------------------------------

    // Task 1 - Verify Dropdown Synchronization
    @Test(priority = 2)
    public void US2_SC_02_Task1_VerifyDropdownSync() {
        SubCategoryPage subPage = new SubCategoryPage(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Setup: Clean & Create Parent Data
        subPage.hardResetAndNavigate();

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'AdminCreateMainCategory.aspx')]"))).click();

        WebElement btnAdd = wait.until(ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(By.id("ContentPlaceHolder_Admin_btnAdd"))));
        btnAdd.click();

        WebElement txtName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ContentPlaceHolder_Admin_txtCategoryName")));
        txtName.clear();
        txtName.sendKeys(lastCreatedMainCategory);
        new Select(driver.findElement(By.id("ContentPlaceHolder_Admin_ddlStatus"))).selectByVisibleText("Active");
        driver.findElement(By.id("ContentPlaceHolder_Admin_btnCreate")).click();
        Reporter.log("Created: " + lastCreatedMainCategory);

        // Action: Check Sync
        subPage.hardResetAndNavigate();
        mainWindow = driver.getWindowHandle();
        subPage.clickAddSubCategory();

        wait.until(d -> d.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        for (String handle : allWindows) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        List<String> options = subPage.getDropdownOptions();
        boolean isFound = options.contains(lastCreatedMainCategory);

        driver.close();
        driver.switchTo().window(mainWindow);

        Assert.assertTrue(isFound, "Sync Failed!");
        Reporter.log("Sync Verified");
    }

    // Task 2 - Add SubCategory
    // @Test(priority = 3)


    // Task 3 - Edit SubCategory
    // @Test(priority = 4)



    // US2-SC-03 -
    //------------------------------------

    // Task 1 -

    // Task 2 -


    // US2-SC-04 -
    //-----------------------------

    // Task 1 -

    // Task 2 -



    // DEFECT FIXES


    // CLEANUP

    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment() {
        CategoryPage catPage = new CategoryPage(driver);
        try {
            if (driver.getWindowHandles().size() > 1 && !driver.getWindowHandle().equals(mainWindow)) {
                driver.switchTo().window(mainWindow);
            }
            driver.findElement(By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']")).click();
            driver.findElement(By.xpath("//a[contains(@href,'AdminCreateMainCategory.aspx')]")).click();
            catPage.deleteMainCategoryByName(lastCreatedMainCategory);
        } catch (Exception e) { /* Ignore */ }
    }
}