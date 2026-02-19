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
import java.util.Arrays;

public class SubCategoryTest extends BaseTest {

    public String lastCreatedMainCategory = "AutoSync_" + System.currentTimeMillis();
    private String mainWindow;

    @Test(priority = 1)
    public void US2_SC_01_VerifySubCategoryUI() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@class,'nav-link')]/span[text()=\"Category\"]"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, 'AdminCreateSubCategory.aspx')]"))).click();

        WebElement addBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Add Subcategory")));
        Reporter.log("Add Button Displayed: " + addBtn.isDisplayed());

        List<String> expectedHeaders = Arrays.asList("Main Category", "Subcategory Name", "Status", "Actions");
        List<WebElement> actualHeaders = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//th"));
        for (int i = 0; i < expectedHeaders.size(); i++) {
            if (actualHeaders.get(i).getText().trim().equals(expectedHeaders.get(i))) {
                Reporter.log("Header Match: " + expectedHeaders.get(i));
            }
        }
    }

    @Test(priority = 2)
    public void US2_SC_02_Task1_VerifyDropdownSync() {
        SubCategoryPage subPage = new SubCategoryPage(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        subPage.hardResetAndNavigate();

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'AdminCreateMainCategory.aspx')]"))).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("ContentPlaceHolder_Admin_btnAdd"))).click();
        WebElement txtName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ContentPlaceHolder_Admin_txtCategoryName")));
        txtName.sendKeys(lastCreatedMainCategory);
        new Select(driver.findElement(By.id("ContentPlaceHolder_Admin_ddlStatus"))).selectByVisibleText("Active");
        driver.findElement(By.id("ContentPlaceHolder_Admin_btnCreate")).click();

        subPage.hardResetAndNavigate();
        mainWindow = driver.getWindowHandle();
        subPage.clickAddSubCategory();

        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(mainWindow)) driver.switchTo().window(handle);
        }

        boolean isFound = subPage.getDropdownOptions().contains(lastCreatedMainCategory);
        driver.close();
        driver.switchTo().window(mainWindow);
        Assert.assertTrue(isFound, "Sync Failed!");
    }

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

    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment() {
        CategoryPage catPage = new CategoryPage(driver);
        try {
            driver.switchTo().window(mainWindow);
            driver.findElement(By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']")).click();
            driver.findElement(By.xpath("//a[contains(@href,'AdminCreateMainCategory.aspx')]")).click();
            catPage.deleteMainCategoryByName(lastCreatedMainCategory);
        } catch (Exception ignored) {}
    }

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
}