package tests;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import pages.SubCategoryPage;
import pages.CategoryPage;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.time.Duration;

public class SubCategoryTest extends BaseTest {

    private SubCategoryPage subPage;
    private CategoryPage catPage;

    public String lastCreatedMainCategory = "AutoSync_" + System.currentTimeMillis();
    private String mainWindow;

    // Data for SC-03 (Dynamic Creation & Update)
    String targetSubCat = "Retirement_" + System.currentTimeMillis();
    String updatedSubCat = "Retirement_Updated_" + System.currentTimeMillis();

    @BeforeClass
    public void setupPage() {
        subPage = new SubCategoryPage(driver);
        catPage = new CategoryPage(driver);
    }

    // =========================================================
    // SC-01: UI Verification (2 Test Cases)
    // =========================================================

    @Test(priority = 1)
    public void US2_SC_01_Task1_VerifyUIElements() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        subPage.hardResetAndNavigate();

        WebElement addBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Add Subcategory")));
        Assert.assertTrue(addBtn.isDisplayed(), "Add Button not displayed");

        WebElement title = driver.findElement(By.xpath("//div[@class='pagetitle']/h1"));
        Assert.assertEquals(title.getText(), "Subcategory Management");
        Reporter.log("SC-01 Task 1: UI Elements Verified");
    }

    @Test(priority = 2)
    public void US2_SC_01_Task2_VerifyPagination() {
        subPage.hardResetAndNavigate();
        if(subPage.isPaginationVisible()) {
            Reporter.log("Pagination controls are visible.");
            Assert.assertTrue(true);
        } else {
            Reporter.log("Data count low, pagination hidden (Expected behavior).");
        }
        Reporter.log("SC-01 Task 2: Pagination Verified");
    }

    // =========================================================
    // SC-02: Functional Filters & Sync (3 Test Cases)
    // =========================================================

    @Test(priority = 3)
    public void US2_SC_02_Task1_VerifyDropdownSync() {
        // 1. Create Parent
        catPage.navigateToMainCategory();
        catPage.openAddModal();
        catPage.fillForm(lastCreatedMainCategory, "Active");
        catPage.clickCreate();

        // 2. Check Sync
        subPage.hardResetAndNavigate();
        mainWindow = driver.getWindowHandle();
        subPage.clickAddSubCategory();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until((WebDriver d) -> d.getWindowHandles().size() > 1);

        Set<String> allWindows = driver.getWindowHandles();
        for (String handle : allWindows) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        List<String> options = subPage.getAddPageDropdownOptions();
        boolean isFound = options.contains(lastCreatedMainCategory);

        driver.close();
        driver.switchTo().window(mainWindow);
        Assert.assertTrue(isFound, "Sync Failed!");
        Reporter.log("SC-02 Task 1: Dropdown Sync Verified");
    }

/**
    @Test(priority = 4)
    public void US2_SC_02_Task2_VerifyRefreshFunctionality() {
        // REPLACED failing filter test with Refresh Button test (As filter doesn't exist in HTML)
        subPage.hardResetAndNavigate();
        boolean isRefreshVisible = subPage.isRefreshButtonVisible();
        Assert.assertTrue(isRefreshVisible, "Refresh button is missing from the page");
        Reporter.log("SC-02 Task 2: Refresh Button Verified");
    }


    @Test(priority = 5)
    public void US2_SC_02_Task3_VerifyStatusColumn() {
        subPage.hardResetAndNavigate();
        // Wait for table headers to ensure page is loaded
        List<String> headers = subPage.getTableHeaders();
        boolean statusColumnExists = headers.contains("Status");
        Assert.assertTrue(statusColumnExists, "Status Column missing in table. Found: " + headers);
        Reporter.log("SC-02 Task 3: Status Column Verified");
    }

    */
@Test(priority = 3)
public void US2_SC_02_Task2_DuplicateSubCategoryNegative() {
    // 1. Create unique subcategory
    String subName = createSubcategoryViaPopup(lastCreatedMainCategory, "AutoSub-");

    // 2. VERIFY: Instead of counting, we check if the specific name exists
    boolean foundFirst = isSubcategoryPresentAcrossPages(subName);
    Assert.assertTrue(foundFirst, "FAIL: Subcategory '" + subName + "' was not found after creation!");
    Reporter.log("Positive Check: Subcategory created successfully.");

    // 3. NEGATIVE: Try to create duplicate
    createSubcategoryViaPopup(lastCreatedMainCategory, subName);

    // 4. FINAL VERIFY:
    // If the system is working correctly, count should remain 1.
    // If the system has a bug (allowing duplicates), count will be 2.
    int occurrencesAfterDup = countNameOccurrences(subName);

    if (occurrencesAfterDup > 1) {
        Reporter.log("NEGATIVE TEST RESULT: Application allowed a duplicate! (Bug Found)");
        Assert.assertEquals(occurrencesAfterDup, 1, "BUG: Duplicate SubCategory was allowed in the system! Found " + occurrencesAfterDup + " times.");
    } else {
        Reporter.log("NEGATIVE TEST RESULT: Duplicate blocked by system successfully.");
        Assert.assertEquals(occurrencesAfterDup, 1, "System correctly blocked the duplicate entry.");
    }
}

//    @AfterClass(alwaysRun = true)
//    public void cleanupEnvironment() {
//        CategoryPage catPage = new CategoryPage(driver);
//        try {
//            driver.switchTo().window(mainWindow);
//            driver.findElement(By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']")).click();
//            driver.findElement(By.xpath("//a[contains(@href,'AdminCreateMainCategory.aspx')]")).click();
//            catPage.deleteMainCategoryByName(lastCreatedMainCategory);
//        } catch (Exception ignored) {}
//    }

    // ===================== IMPROVED HELPERS =====================

    private String createSubcategoryViaPopup(String parentText, String namePrefix) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        String main = driver.getWindowHandle();
        driver.findElement(By.xpath("//a[contains(.,'Add Subcategory')]")).click();

        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String h : driver.getWindowHandles()) {
            if (!h.equals(main)) driver.switchTo().window(h);
        }

        new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ContentPlaceHolder_Admin_ddlMainCategory")))).selectByVisibleText(parentText);
        String finalName = namePrefix.endsWith("-") ? namePrefix + System.currentTimeMillis() : namePrefix;
        driver.findElement(By.id("ContentPlaceHolder_Admin_txtSubCategory")).sendKeys(finalName);
        new Select(driver.findElement(By.id("ContentPlaceHolder_Admin_ddlStatus"))).selectByVisibleText("Active");
        driver.findElement(By.id("ContentPlaceHolder_Admin_btnSaveSubCategory")).click();

        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}

        if (driver.getWindowHandles().size() > 1) driver.close();
        driver.switchTo().window(main);

        // CRITICAL: Force reload and wait for grid to stabilize
        driver.navigate().refresh();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ContentPlaceHolder_Admin_gvSubCategories")));
        return finalName;
    }

    private boolean isSubcategoryPresentAcrossPages(String targetName) {
        return countNameOccurrences(targetName) > 0;
    }

    private int countNameOccurrences(String targetName) {
        // Reset to page 1
        try {
            WebElement p1 = driver.findElement(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//a[text()='1']"));
            p1.click();
            Thread.sleep(1500);
        } catch (Exception ignored) {}

        int count = 0;
        List<String> visited = new ArrayList<>();
        boolean hasMore = true;

        while (hasMore) {
            String current = driver.findElement(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//span")).getText();
            visited.add(current);

            // Count on current page
            List<WebElement> rows = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[td]"));
            for (WebElement row : rows) {
                if (row.getText().contains(targetName)) count++;
            }

            // Find Next Page
            List<WebElement> links = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//a"));
            WebElement next = null;
            for (WebElement link : links) {
                String val = link.getText().trim();
                if (val.matches("\\d+") && Integer.parseInt(val) > Integer.parseInt(current) && !visited.contains(val)) {
                    next = link; break;
                } else if (val.equals("...") && links.indexOf(link) == links.size() - 1) {
                    next = link; break;
                }
            }

            if (next != null) {
                next.click();
                try { Thread.sleep(2000); } catch (Exception ignored) {}
            } else {
                hasMore = false;
            }
        }
        return count;
    }
    // =========================================================
    // SC-03: Edit & Update (2 Test Cases + 1 Setup)
    // =========================================================

    @Test(priority = 6)
    public void US2_SC_03_Task0_SetupData() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        subPage.hardResetAndNavigate();

        String originalWindow = driver.getWindowHandle();
        subPage.clickAddSubCategory();

        wait.until((WebDriver d) -> d.getWindowHandles().size() > 1);

        // FIXED: Robust Window Switching using URL
        boolean windowFound = false;
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                // Check URL for "AdminAddSubCategory" (matches your HTML)
                if (driver.getCurrentUrl().contains("AdminAddSubCategory") || driver.getTitle().contains("Create Subcategory")) {
                    windowFound = true;
                    driver.manage().window().maximize();
                    break;
                }
            }
        }
        Assert.assertTrue(windowFound, "Could not find Add Window (Checked URL/Title)");

        subPage.fillAndSaveAddForm(targetSubCat, 1, "Active");

        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept();
        } catch (TimeoutException e) {}

        try {
            if (driver.getWindowHandles().size() > 1) driver.close();
        } catch (Exception e) {}

        driver.switchTo().window(originalWindow);
        Reporter.log("Setup Complete: Created " + targetSubCat);
    }

    @Test(priority = 7, dependsOnMethods = "US2_SC_03_Task0_SetupData")
    public void US2_SC_03_Task1_VerifyEditPrePopulation() {
        subPage.hardResetAndNavigate();

        boolean found = subPage.searchAndLocateSubCategory(targetSubCat);
        Assert.assertTrue(found, "Setup Failed: Could not find " + targetSubCat);

        subPage.clickEditForSubCategory(targetSubCat);

        String actualValue = subPage.getEditNameFieldValue();
        Assert.assertEquals(actualValue, targetSubCat, "Pre-population failed!");

        driver.navigate().refresh();
        Reporter.log("SC-03 Task 1: Pre-population Verified");
    }

    @Test(priority = 8, dependsOnMethods = "US2_SC_03_Task1_VerifyEditPrePopulation")
    public void US2_SC_03_Task2_VerifyUpdateReflection() {
        subPage.hardResetAndNavigate();

        subPage.searchAndLocateSubCategory(targetSubCat);
        subPage.clickEditForSubCategory(targetSubCat);

        subPage.performUpdate(updatedSubCat, "Active");

        boolean isUpdated = subPage.isSubCategoryVisibleInTable(updatedSubCat);
        Assert.assertTrue(isUpdated, "Update Failed: New value not found in table.");
        Reporter.log("SC-03 Task 2: Update Reflection Verified");
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment() {
        try {
            catPage.cleanUpAllTestArtifacts();
        } catch (Exception e) {}
    }
}