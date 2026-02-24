package tests;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.CategoryPage;
import pages.SubCategoryPage;
import java.time.Duration;
import java.util.List;

public class GlobalDeletionTest extends BaseTest {

    private CategoryPage catPage;
    private SubCategoryPage subPage;
    private WebDriverWait wait;
    private String targetCategory;

    @BeforeClass
    public void setupPages() {
        catPage = new CategoryPage(driver);
        subPage = new SubCategoryPage(driver);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Use last 5 digits of currentTimeMillis for a short, non-spammy name
        String timeStr = String.valueOf(System.currentTimeMillis());
        targetCategory = "Del_" + timeStr.substring(timeStr.length() - 5);
    }

    // =========================================================
    // US2-DEF-01 Task 1: Automate deletion of a Main Category
    // =========================================================
    @Test(priority = 1)
    public void US2_DEF_01_Task1_DeleteMainCategory() {
        // Setup Data
        catPage.navigateToMainCategory();
        catPage.openAddModal();
        catPage.fillForm(targetCategory, "Active");
        catPage.clickCreate();

        catPage.searchFor(targetCategory);
        Assert.assertEquals(catPage.getCategoryNameByIndex(1), targetCategory, "Setup Issue: Category was not created.");

        // Grab reference to the old table before clicking delete
        WebElement oldTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ContentPlaceHolder_Admin_gvCategories")));
        WebElement deleteBtn = oldTable.findElement(By.xpath(".//tr[2]//a[contains(@id, 'lnkDelete')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);

        // Accept "Are you sure?" alert
        wait.until(ExpectedConditions.alertIsPresent()).accept();

        // Accept "Deleted Successfully" alert
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            Alert successAlert = shortWait.until(ExpectedConditions.alertIsPresent());
            successAlert.accept();
        } catch (Exception e) {
            // Ignore if there is no second alert
        }

        // Wait for the table to refresh dynamically (NO Thread.sleep)
        wait.until(ExpectedConditions.stalenessOf(oldTable));

        // Verification
        catPage.clickClear();
        catPage.searchFor(targetCategory);
        Assert.assertNull(catPage.getCategoryNameByIndex(1), "Task 1 Failed: Category is still present in the Main Category table!");

        Reporter.log("Task 1: Main Category deleted successfully and verified absent from main table.");
    }

    // =========================================================
    // US2-DEF-01 Task 2 & 3: Implement global verification
    // =========================================================
    @Test(priority = 2, dependsOnMethods = "US2_DEF_01_Task1_DeleteMainCategory")
    public void US2_DEF_01_Task2_and_3_VerifyGlobalRemoval() {
        subPage.hardResetAndNavigate();

        // Task 2: SubCategory Grid Verification
        // Use the custom fast-check method to avoid 10-second implicit wait delays
        boolean foundInGrid = isCategoryPresentInSubGridFast(targetCategory);
        Assert.assertFalse(foundInGrid, "Task 2 Failed: The deleted Main Category is still lingering inside the SubCategory grid!");
        Reporter.log("Task 2: Verified complete absence in SubCategory data grid (Fast execution).");

        // Task 3: SubCategory Dropdown Verification
        String mainWindow = driver.getWindowHandle();
        subPage.clickAddSubCategory();

        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        List<String> dropdownOptions = subPage.getAddPageDropdownOptions();
        boolean foundInDropdown = dropdownOptions.contains(targetCategory);

        if (driver.getWindowHandles().size() > 1) {
            driver.close();
        }
        driver.switchTo().window(mainWindow);

        Assert.assertFalse(foundInDropdown, "Task 3 Failed: The deleted Main Category is still selectable in the Add Subcategory dropdown!");
        Reporter.log("Task 3: Verified complete absence in SubCategory dropdown options.");
    }

    // =========================================================
    // Cleanup: Remove ALL junk data from past failed test runs
    // =========================================================
    @AfterClass(alwaysRun = true)
    public void cleanupFailedData() {
        try {
            catPage.navigateToMainCategory();
            catPage.clickClear();
            catPage.searchFor("Del_");

            // Loop aggressively until absolutely no matching records remain
            while (true) {
                String name = catPage.getCategoryNameByIndex(1);

                if (name != null && name.contains("Del_")) {
                    WebElement oldTable = driver.findElement(By.id("ContentPlaceHolder_Admin_gvCategories"));
                    WebElement deleteBtn = oldTable.findElement(By.xpath(".//tr[2]//a[contains(@id, 'lnkDelete')]"));

                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
                    wait.until(ExpectedConditions.alertIsPresent()).accept();

                    try {
                        new WebDriverWait(driver, Duration.ofSeconds(3)).until(ExpectedConditions.alertIsPresent()).accept();
                    } catch (Exception e) {}

                    // Wait for the DOM to update instead of using Thread.sleep()
                    wait.until(ExpectedConditions.stalenessOf(oldTable));

                    catPage.clickClear();
                    catPage.searchFor("Del_");
                } else {
                    break; // Exit loop when table is completely clean
                }
            }
            Reporter.log("Cleanup complete: All 'Del_' spam names deleted.");
        } catch (Exception e) {
            System.out.println("Cleanup completed or no remaining records found.");
        }
    }

    // =========================================================
    // HELPER: Ultra-fast pagination check that bypasses implicit wait
    // =========================================================
    private boolean isCategoryPresentInSubGridFast(String targetName) {
        // 1. Temporarily turn OFF implicit wait so missing elements don't cause 10-second delays
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));

        try {
            // Reset to Page 1
            List<WebElement> p1 = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//a[text()='1']"));
            if (!p1.isEmpty()) {
                WebElement oldTable = driver.findElement(By.id("ContentPlaceHolder_Admin_gvSubCategories"));
                p1.get(0).click();
                wait.until(ExpectedConditions.stalenessOf(oldTable));
            }

            while (true) {
                // Check if the text exists anywhere in the current page's table rows
                List<WebElement> matches = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//td[contains(text(), '" + targetName + "') or descendant::*[contains(text(), '" + targetName + "')]]"));
                if (!matches.isEmpty()) {
                    return true;
                }

                // Pagination Logic
                String current = "1";
                try {
                    current = driver.findElement(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//span")).getText();
                } catch (Exception e) { }

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
                    WebElement oldTable = driver.findElement(By.id("ContentPlaceHolder_Admin_gvSubCategories"));
                    next.click();
                    // Wait strictly for DOM update, no Thread.sleep
                    wait.until(ExpectedConditions.stalenessOf(oldTable));
                } else {
                    break;
                }
            }
        } finally {
            // 2. ALWAYS turn implicit wait back ON when done
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        }
        return false;
    }
}