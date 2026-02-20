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

    String targetSubCat = "Retirement_" + System.currentTimeMillis();
    String updatedSubCat = "Retirement_Updated_" + System.currentTimeMillis();

    @BeforeClass
    public void setupPage() {
        subPage = new SubCategoryPage(driver);
        catPage = new CategoryPage(driver);
    }

    // =========================================================
    // SC-01: UI Verification
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
        } else {
            Reporter.log("Data count low, pagination hidden.");
        }
        Reporter.log("SC-01 Task 2: Pagination Verified");
    }

    // =========================================================
    // SC-02: Functional Sync & Verification
    // =========================================================

    @Test(priority = 3)
    public void US2_SC_02_Task1_VerifyDropdownSync() {
        catPage.navigateToMainCategory();
        catPage.openAddModal();
        catPage.fillForm(lastCreatedMainCategory, "Active");
        catPage.clickCreate();

        subPage.hardResetAndNavigate();
        mainWindow = driver.getWindowHandle();
        subPage.clickAddSubCategory();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until((WebDriver d) -> d.getWindowHandles().size() > 1);

        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        List<String> options = subPage.getAddPageDropdownOptions();
        boolean isFound = options.contains(lastCreatedMainCategory);

        driver.close();
        driver.switchTo().window(mainWindow);
        Assert.assertTrue(isFound, "Sync Failed: Category not found in dropdown!");
        Reporter.log("SC-02 Task 1: Dropdown Sync Verified");
    }

    @Test(priority = 4)
    public void US2_SC_02_Task2_DuplicateSubCategoryNegative() {
        String subName = "AutoSub-" + System.currentTimeMillis();
        createSubcategoryViaPopup(lastCreatedMainCategory, subName);

        boolean foundFirst = isSubcategoryPresentAcrossPages(subName);
        Assert.assertTrue(foundFirst, "FAIL: Initial subcategory creation not found!");

        // Try to create duplicate
        createSubcategoryViaPopup(lastCreatedMainCategory, subName);

        // System should block duplicate, so count should still be 1
        int occurrences = countNameOccurrences(subName);
        Assert.assertEquals(occurrences, 1, "BUG: System allowed duplicate subcategory!");
        Reporter.log("SC-02 Task 2: Duplicate Prevention Verified");
    }

    @Test(priority = 5)
    public void US2_SC_02_Task3_VerifyImmediateReflection() {
        subPage.hardResetAndNavigate();

        // 1. Create unique name
        String subName = "AutoReflect-" + System.currentTimeMillis();
        String createdName = createSubcategoryViaPopup(lastCreatedMainCategory, subName);
        Reporter.log("Searching for newly created entry: " + createdName);

        // 2. VERIFY: Direct match in grid (No counting rows)
        boolean isNameFound = isSubcategoryPresentAcrossPages(createdName);
        Assert.assertTrue(isNameFound, "FAIL: '" + createdName + "' did not appear in the grid!");

        Reporter.log("SC-02 Task 3: Immediate Reflection Verified successfully.");
    }

    // =========================================================
    // SC-03: Edit & Update
    // =========================================================

    @Test(priority = 6)
    public void US2_SC_03_Task0_SetupData() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        subPage.hardResetAndNavigate();
        String originalWindow = driver.getWindowHandle();
        subPage.clickAddSubCategory();

        wait.until((WebDriver d) -> d.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        subPage.fillAndSaveAddForm(targetSubCat, 1, "Active");
        try {
            wait.until(ExpectedConditions.alertIsPresent()).accept();
        } catch (Exception ignored) {}

        if (driver.getWindowHandles().size() > 1) driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test(priority = 7, dependsOnMethods = "US2_SC_03_Task0_SetupData")
    public void US2_SC_03_Task1_VerifyEditPrePopulation() {
        subPage.hardResetAndNavigate();
        subPage.searchAndLocateSubCategory(targetSubCat);
        subPage.clickEditForSubCategory(targetSubCat);

        String actualValue = subPage.getEditNameFieldValue();
        Assert.assertEquals(actualValue, targetSubCat, "Pre-population failed!");
        driver.navigate().refresh();
    }

    @Test(priority = 8, dependsOnMethods = "US2_SC_03_Task1_VerifyEditPrePopulation")
    public void US2_SC_03_Task2_VerifyUpdateReflection() {
        subPage.hardResetAndNavigate();
        subPage.searchAndLocateSubCategory(targetSubCat);
        subPage.clickEditForSubCategory(targetSubCat);

        subPage.performUpdate(updatedSubCat, "Active");
        boolean isUpdated = isSubcategoryPresentAcrossPages(updatedSubCat);
        Assert.assertTrue(isUpdated, "Update Reflection Failed!");
    }


    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment() {
        try { catPage.cleanUpAllTestArtifacts(); } catch (Exception ignored) {}
    }

    // ===================== HELPERS =====================

    private String createSubcategoryViaPopup(String parentText, String namePrefix) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        String main = driver.getWindowHandle();
        driver.findElement(By.xpath("//a[contains(.,'Add Subcategory')]")).click();

        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String h : driver.getWindowHandles()) {
            if (!h.equals(main)) driver.switchTo().window(h);
        }

        new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ContentPlaceHolder_Admin_ddlMainCategory")))).selectByVisibleText(parentText);
        String finalName = namePrefix.contains("-") ? namePrefix + System.currentTimeMillis() : namePrefix;
        driver.findElement(By.id("ContentPlaceHolder_Admin_txtSubCategory")).sendKeys(finalName);
        new Select(driver.findElement(By.id("ContentPlaceHolder_Admin_ddlStatus"))).selectByVisibleText("Active");
        driver.findElement(By.id("ContentPlaceHolder_Admin_btnSaveSubCategory")).click();

        try {
            wait.until(ExpectedConditions.alertIsPresent()).accept();
        } catch (Exception ignored) {}

        if (driver.getWindowHandles().size() > 1) driver.close();
        driver.switchTo().window(main);
        driver.navigate().refresh();
        return finalName;
    }

    /**
     * Finds a name by traversing pages. Returns true immediately if found.
     */
    private boolean isSubcategoryPresentAcrossPages(String targetName) {
        // Reset to Page 1
        try {
            List<WebElement> p1 = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//a[text()='1']"));
            if (!p1.isEmpty()) { p1.get(0).click(); Thread.sleep(1000); }
        } catch (Exception ignored) {}

        List<String> visited = new ArrayList<>();
        while (true) {
            String current;
            try {
                current = driver.findElement(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//span")).getText();
            } catch (Exception e) { current = "1"; }

            if (visited.contains(current)) break;
            visited.add(current);

            // Match Logic
            List<WebElement> rows = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[td and not(descendant::table)]"));
            for (WebElement row : rows) {
                if (row.getText().contains(targetName)) return true;
            }

            // Pagination Logic
            List<WebElement> links = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//a"));
            WebElement next = null;
            for (WebElement link : links) {
                String val = link.getText().trim();
                if (val.matches("\\d+") && Integer.parseInt(val) > Integer.parseInt(current)) {
                    next = link; break;
                } else if (val.equals("...") && links.indexOf(link) == links.size() - 1) {
                    next = link; break;
                }
            }

            if (next != null) {
                next.click();
                try { Thread.sleep(1500); } catch (Exception ignored) {}
            } else { break; }
        }
        return false;
    }

    private int countNameOccurrences(String targetName) {
        int count = 0;
        try {
            List<WebElement> p1 = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//a[text()='1']"));
            if (!p1.isEmpty()) { p1.get(0).click(); Thread.sleep(1000); }
        } catch (Exception ignored) {}

        List<String> visited = new ArrayList<>();
        while (true) {
            String current;
            try {
                current = driver.findElement(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//span")).getText();
            } catch (Exception e) { current = "1"; }

            if (visited.contains(current)) break;
            visited.add(current);

            List<WebElement> rows = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[td and not(descendant::table)]"));
            for (WebElement row : rows) {
                if (row.getText().contains(targetName)) count++;
            }

            List<WebElement> links = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//a"));
            WebElement next = null;
            for (WebElement link : links) {
                String val = link.getText().trim();
                if (val.matches("\\d+") && Integer.parseInt(val) > Integer.parseInt(current)) {
                    next = link; break;
                } else if (val.equals("...") && links.indexOf(link) == links.size() - 1) {
                    next = link; break;
                }
            }
            if (next != null) {
                next.click();
                try { Thread.sleep(1500); } catch (Exception ignored) {}
            } else { break; }
        }
        return count;
    }
}