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

    @Test
    public void US2_SC_02_Task1_VerifyDropdownSync() {
        SubCategoryPage subPage = new SubCategoryPage(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // --- STEP 0: CLEAN SLATE ---
        subPage.hardResetAndNavigate();
        Reporter.log("Step 0: Navigated to Sub-Category page (Clean Slate).");

        // --- STEP 1: CREATE THE DATA (STALE-PROOFED) ---
        // Navigate explicitly
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'AdminCreateMainCategory.aspx')]"))).click();

        // ROBIN FIX: Wait for the 'Add' button to be freshly visible
        WebElement btnAdd = wait.until(ExpectedConditions.refreshed(
                ExpectedConditions.elementToBeClickable(By.id("ContentPlaceHolder_Admin_btnAdd"))
        ));
        btnAdd.click();

        // ROBIN FIX: Wait for the text box to be freshly visible before typing
        WebElement txtName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ContentPlaceHolder_Admin_txtCategoryName")));
        txtName.clear(); // Good practice to clear before typing
        txtName.sendKeys(lastCreatedMainCategory);

        new Select(driver.findElement(By.id("ContentPlaceHolder_Admin_ddlStatus"))).selectByVisibleText("Active");
        driver.findElement(By.id("ContentPlaceHolder_Admin_btnCreate")).click();
        Reporter.log("Step 1: Successfully Created Main Category: " + lastCreatedMainCategory);

        // --- STEP 2: NAVIGATE TO SUB-CATEGORY ---
        subPage.hardResetAndNavigate();
        mainWindow = driver.getWindowHandle();
        Reporter.log("Step 2: Returned to Sub-Category Management.");

        // --- STEP 3: OPEN POPUP (Handle New Tab) ---
        subPage.clickAddSubCategory();

        wait.until(d -> d.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        for (String handle : allWindows) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        Reporter.log("Step 3: Switched focus to 'Add Subcategory' tab.");

        // --- STEP 4: VERIFY SYNC ---
        List<String> options = subPage.getDropdownOptions();
        boolean isFound = options.contains(lastCreatedMainCategory);

        // CRITICAL: Close the tab and return to main window BEFORE assertion
        driver.close();
        driver.switchTo().window(mainWindow);

        Assert.assertTrue(isFound, "FAIL: '" + lastCreatedMainCategory + "' was NOT found in the dropdown!");
        Reporter.log("SUCCESS: Category Sync Verified.");
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment() {
        Reporter.log("Robin: Running Cleanup...");
        CategoryPage catPage = new CategoryPage(driver);

        try {
            if (driver.getWindowHandles().size() > 1 && !driver.getWindowHandle().equals(mainWindow)) {
                driver.switchTo().window(mainWindow);
            }

            driver.findElement(By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']")).click();
            driver.findElement(By.xpath("//a[contains(@href,'AdminCreateMainCategory.aspx')]")).click();

            catPage.deleteMainCategoryByName(lastCreatedMainCategory);
            Reporter.log("CLEANUP SUCCESS: Test data deleted.");
        } catch (Exception e) {
            Reporter.log("CLEANUP SKIPPED: " + e.getMessage());
        }
    }
}