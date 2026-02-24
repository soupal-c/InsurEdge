package tests;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import pages.SubCategoryPage;
import pages.CategoryPage;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

public class SubCategoryTest extends BaseTest {

    private SubCategoryPage subPage;
    private CategoryPage catPage;

    // Beautifully clean initializations using your new method
    public String lastCreatedMainCategory = nameGenerator("AutoSync_");
    String targetSubCat = nameGenerator("Retire_");
    String updatedSubCat = nameGenerator("RetireUp_");

    private String mainWindow;
    private String deletedRecordName;

    @BeforeClass
    public void setupPage() {
        subPage = new SubCategoryPage(driver);
        catPage = new CategoryPage(driver);
    }

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
    }

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
    }

    @Test(priority = 4)
    public void US2_SC_02_Task2_DuplicateSubCategoryNegative() {
        String subName = nameGenerator("AutoSub-");
        createSubcategoryViaPopup(lastCreatedMainCategory, subName);

        boolean foundFirst = isSubcategoryPresentAcrossPages(subName);
        Assert.assertTrue(foundFirst, "FAIL: Initial subcategory creation not found!");

        createSubcategoryViaPopup(lastCreatedMainCategory, subName);

        int occurrences = countNameOccurrences(subName);
        Assert.assertEquals(occurrences, 1, "BUG: System allowed duplicate subcategory!");
    }

    @Test(priority = 5)
    public void US2_SC_02_Task3_VerifyImmediateReflection() {
        subPage.hardResetAndNavigate();

        String subName = nameGenerator("AutoReflect-");
        String createdName = createSubcategoryViaPopup(lastCreatedMainCategory, subName);

        boolean isNameFound = isSubcategoryPresentAcrossPages(createdName);
        Assert.assertTrue(isNameFound, "FAIL: '" + createdName + "' did not appear in the grid!");
    }

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
        try { wait.until(ExpectedConditions.alertIsPresent()).accept(); } catch (Exception ignored) {}

        if (driver.getWindowHandles().size() > 1) driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test(priority = 7, dependsOnMethods = "US2_SC_03_Task0_SetupData")
    public void US2_SC_03_Task1_VerifyEditVerification() {
        subPage.hardResetAndNavigate();

        boolean found = isSubcategoryPresentAcrossPages(targetSubCat);
        Assert.assertTrue(found, "Data created in Task 0 not found in table!");

        subPage.clickEditForSubCategory(targetSubCat);

        String actualValue = subPage.getEditNameValue();
        Assert.assertEquals(actualValue, targetSubCat, "Edit form field did NOT pre-populate correctly!");
    }

    @Test(priority = 8, dependsOnMethods = "US2_SC_03_Task1_VerifyEditVerification")
    public void US2_SC_03_Task2_VerifyUpdateReflection() {
        subPage.updateSubCategoryDetails(updatedSubCat, "Active");

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.alertIsPresent()).accept();
        } catch (Exception ignored) {}

        boolean isUpdated = isSubcategoryPresentAcrossPages(updatedSubCat);
        Assert.assertTrue(isUpdated, "TASK 2 FAILED: Updated name not reflected in table immediately!");
    }

    @Test(priority = 9)
    public void US2_SC_04_Task1_DeleteAndConfirmPopups() {
        subPage.hardResetAndNavigate();
        createSubcategoryViaPopup(lastCreatedMainCategory, nameGenerator("ToDelete_"));

        deletedRecordName = subPage.getFirstRowSubCategoryName();
        subPage.clickDeleteOnFirstRow();

        String alertText = subPage.handleDualDeleteAlerts();
        Assert.assertTrue(alertText.toLowerCase().contains("successfully"), "Success message mismatch!");
    }

    @Test(priority = 10, dependsOnMethods = "US2_SC_04_Task1_DeleteAndConfirmPopups")
    public void US2_SC_04_Task2_VerifyPersistenceAfterRefresh() {
        subPage.clickRefreshButton();
        boolean isPresent = isSubcategoryPresentAcrossPages(deletedRecordName);
        Assert.assertFalse(isPresent, "The record '" + deletedRecordName + "' was not permanently removed!");
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment() {
        Reporter.log("1. Starting SubCategory specific cleanup to unblock Main Categories...");
        String[] subArtifacts = {"AutoSub-", "AutoReflect-", "Retire_", "RetireUp_", "ToDelete_"};

        for (String artifact : subArtifacts) {
            cleanUpSubCategory(artifact);
        }

        Reporter.log("2. SubCategories wiped. Now safely cleaning parent Main Categories...");
        try { catPage.cleanUpAllTestArtifacts(); } catch (Exception ignored) {}
    }

    private void cleanUpSubCategory(String targetName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        try {
            subPage.hardResetAndNavigate();
            while (true) {
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
                boolean found = isSubcategoryPresentAcrossPages(targetName);
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

                if (found) {
                    WebElement oldTable = driver.findElement(By.id("ContentPlaceHolder_Admin_gvSubCategories"));
                    WebElement deleteBtn = oldTable.findElement(By.xpath(".//tr[td[contains(., '" + targetName + "')]]//a[contains(@id, 'lnkDelete')]"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);

                    wait.until(ExpectedConditions.alertIsPresent()).accept();
                    try { wait.until(ExpectedConditions.alertIsPresent()).accept(); } catch(Exception e) {}

                    wait.until(ExpectedConditions.stalenessOf(oldTable));
                } else {
                    break;
                }
            }
        } catch (Exception e) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        }
    }

    // ===================== HELPERS =====================

    private String createSubcategoryViaPopup(String parentText, String finalName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        String main = driver.getWindowHandle();
        driver.findElement(By.xpath("//a[contains(.,'Add Subcategory')]")).click();

        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String h : driver.getWindowHandles()) {
            if (!h.equals(main)) driver.switchTo().window(h);
        }

        new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ContentPlaceHolder_Admin_ddlMainCategory")))).selectByVisibleText(parentText);

        driver.findElement(By.id("ContentPlaceHolder_Admin_txtSubCategory")).sendKeys(finalName);
        new Select(driver.findElement(By.id("ContentPlaceHolder_Admin_ddlStatus"))).selectByVisibleText("Active");
        driver.findElement(By.id("ContentPlaceHolder_Admin_btnSaveSubCategory")).click();

        try { wait.until(ExpectedConditions.alertIsPresent()).accept(); } catch (Exception ignored) {}

        if (driver.getWindowHandles().size() > 1) driver.close();
        driver.switchTo().window(main);
        driver.navigate().refresh();
        return finalName;
    }

    private boolean isSubcategoryPresentAcrossPages(String targetName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {
            List<WebElement> p1 = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//a[text()='1']"));
            if (!p1.isEmpty()) {
                WebElement oldTable = driver.findElement(By.id("ContentPlaceHolder_Admin_gvSubCategories"));
                p1.get(0).click();
                wait.until(ExpectedConditions.stalenessOf(oldTable));
            }
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
                if (row.getText().contains(targetName)) return true;
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
                WebElement oldTable = driver.findElement(By.id("ContentPlaceHolder_Admin_gvSubCategories"));
                next.click();
                try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch (Exception ignored) {}
            } else { break; }
        }
        return false;
    }

    private int countNameOccurrences(String targetName) {
        int count = 0;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {
            List<WebElement> p1 = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//a[text()='1']"));
            if (!p1.isEmpty()) {
                WebElement oldTable = driver.findElement(By.id("ContentPlaceHolder_Admin_gvSubCategories"));
                p1.get(0).click();
                wait.until(ExpectedConditions.stalenessOf(oldTable));
            }
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
                WebElement oldTable = driver.findElement(By.id("ContentPlaceHolder_Admin_gvSubCategories"));
                next.click();
                try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch (Exception ignored) {}
            } else { break; }
        }
        return count;
    }
}