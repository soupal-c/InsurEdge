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
import java.util.Arrays;

public class SubCategoryTest extends BaseTest {

    public String lastCreatedMainCategory = "AutoSync_" + System.currentTimeMillis();
    private String mainWindow;

    // US2-SC-01 - SubCategory Management UI
    //--------------------------------------

    // Task 1 - Verify UI Headers and Table

    // Task 2 -

    // US2-SC-01 - SubCategory Management UI
    //--------------------------------------
// US2-SC-01 - SubCategory Management UI
    //--------------------------------------

    @Test(priority = 1)
    public void US2_SC_01_VerifySubCategoryUI() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Navigation
        WebElement category = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@class,'nav-link')]/span[text()=\"Category\"]")));
        category.click();

        WebElement subCategory = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, 'AdminCreateSubCategory.aspx')]")));
        subCategory.click();

        // Task 1 - Verify UI Headers and Table

        // Validate "Add Subcategory" Button
        WebElement addBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Add Subcategory")));
        Reporter.log("Add Button Displayed: " + addBtn.isDisplayed());

        // Validate "Refresh" Link & Icon Presence
        WebElement refreshLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(@onclick, 'location.reload')]")));
        WebElement refreshText = refreshLink.findElement(By.xpath(".//span[text()='Refresh']"));
        WebElement refreshIcon = refreshLink.findElement(By.className("bi-arrow-clockwise"));
        Reporter.log("Refresh Link Presence: " + refreshLink.isDisplayed());
        Reporter.log("Refresh Icon Displayed: " + refreshIcon.isDisplayed());

        // Check Alignment (Flexbox check)
        WebElement buttonContainer = driver.findElement(By.className("justify-content-between"));
        if(buttonContainer.getCssValue("display").equals("flex")) {
            Reporter.log("UI Alignment Validation: Success.");
        }

        // Validate Table Headers
        List<String> expectedHeaders = Arrays.asList("Main Category", "Subcategory Name", "Status", "Actions");
        List<WebElement> actualHeaders = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//th"));

        for (int i = 0; i < expectedHeaders.size(); i++) {
            String actualHeaderText = actualHeaders.get(i).getText().trim();
            if (actualHeaderText.equals(expectedHeaders.get(i))) {
                Reporter.log("Header Match: " + actualHeaderText);
            }
        }

        // Task 2 - Pagination Automation

        WebElement paginationContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[last()]//td[@colspan='4']")
        ));
        Reporter.log("Pagination UI visible: " + paginationContainer.isDisplayed());

        String[] targetPages = {"2", "3"};
        for (String page : targetPages) {
            WebElement pageLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[last()]//a[text()='" + page + "']")
            ));
            pageLink.click();

            boolean isNavigated = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[last()]//span[text()='" + page + "']")
            )).isDisplayed();

            if (isNavigated) Reporter.log("Navigation Success: Page " + page);
        }
    }
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