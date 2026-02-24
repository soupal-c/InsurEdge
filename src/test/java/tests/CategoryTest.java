package tests;

import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import pages.CategoryPage;
import pages.SubCategoryPage;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CategoryTest extends BaseTest {

    CategoryPage catPage;

    // Beautifully clean initializations using your new method
    String autoName = nameGenerator("Auto_");
    String preEditName = nameGenerator("PreEdit_");
    String postEditName = nameGenerator("PostEdit_");
    String dupName = nameGenerator("Dup_");
    String deleteName = nameGenerator("Delete_");

    @BeforeClass
    public void setupPage() {
        catPage = new CategoryPage(driver);
    }

    @BeforeMethod
    public void navigate() {
        catPage.navigateToMainCategory();
    }

    @Test(priority = 1)
    public void US2_MC_01_Task1_VerifyUI() {
        Assert.assertEquals(catPage.getPageTitle(), "Create Main Insurance Category");
        Reporter.log("UI Verified");
    }

    @Test(priority = 2)
    public void US2_MC_01_Task2_VerifyRowActionIcons() {
        if (catPage.getCategoryNameByIndex(1) == null) {
            catPage.openAddModal();
            catPage.fillForm(autoName, "Active");
            catPage.clickCreate();
        }
        Assert.assertTrue(catPage.areRowActionIconsVisible(), "Icons missing on first row!");
        Reporter.log("Icons Verified");
    }

    @Test(priority = 3)
    public void US2_MC_02_Task1_SearchFunctionality() {
        String existingName = catPage.getCategoryNameByIndex(1);
        if (existingName == null) {
            catPage.openAddModal();
            catPage.fillForm(autoName, "Active");
            catPage.clickCreate();
            existingName = autoName;
        }

        catPage.searchFor(existingName);
        Assert.assertEquals(catPage.getCategoryNameByIndex(1), existingName);
        catPage.clickClear();
        Reporter.log("Search Verified");
    }

    @Test(priority = 4)
    public void US2_MC_02_Task2_AddCategory() {
        catPage.openAddModal();
        catPage.fillForm(autoName, "Active");
        catPage.clickCreate();

        catPage.searchFor(autoName);
        Assert.assertEquals(catPage.getCategoryNameByIndex(1), autoName);
        Reporter.log("Add Verified");
    }

    @Test(priority = 5)
    public void US2_MC_02_Task3_EditCategory() {
        catPage.openAddModal();
        catPage.fillForm(preEditName, "Active");
        catPage.clickCreate();

        catPage.searchFor(preEditName);
        catPage.editCategoryByIndex(1, postEditName);

        catPage.clickClear();
        catPage.searchFor(postEditName);
        Assert.assertEquals(catPage.getCategoryNameByIndex(1), postEditName);
        Reporter.log("Edit Verified");
    }

    @Test(priority = 6)
    public void US2_MC_02_Task4_DuplicateCheck() {
        try {
            catPage.openAddModal();
            catPage.fillForm(dupName, "Active");
            catPage.clickCreate();

            catPage.openAddModal();
            catPage.fillForm(dupName, "Active");

            catPage.clickCreateFailureExpected();

            String error = catPage.getErrorMessage();
            Assert.assertTrue(error.contains("exists") || error.contains("Duplicate"));
            Reporter.log("Duplicate Verified");
        } finally {
            driver.navigate().refresh();
        }
    }

    @Test(priority = 7)
    public void US2_MC_03_Task1_VerifyDelete() {
        catPage.openAddModal();
        catPage.fillForm(deleteName, "Active");
        catPage.clickCreate();

        catPage.searchFor(deleteName);
        Assert.assertEquals(catPage.getCategoryNameByIndex(1), deleteName);

        catPage.deleteCategoryByIndex(1);

        catPage.clickClear();
        catPage.searchFor(deleteName);
        Assert.assertNull(catPage.getCategoryNameByIndex(1), "Category should be deleted!");
        Reporter.log("Delete Logic Verified");
    }

    @Test(priority = 8)
    public void US2_MC_03_Task2_DeleteCancellation() {
        String cancelName = nameGenerator("Cancel_");
        catPage.openAddModal();
        catPage.fillForm(cancelName, "Active");
        catPage.clickCreate();

        catPage.searchFor(cancelName);

        catPage.deleteCategoryByIndexAndCancel(1);

        catPage.clickClear();
        catPage.searchFor(cancelName);
        Assert.assertEquals(catPage.getCategoryNameByIndex(1), cancelName, "Category should NOT be deleted after cancel!");
        Reporter.log("Delete Cancel Verified");
    }

    @Test(priority = 9)
    public void US2_MC_03_Task2_ValidateDependencyBlocking() {
        String dynamicParent = nameGenerator("Parent_");
        String dynamicChild = nameGenerator("Child_");

        catPage.navigateToMainCategory();
        catPage.openAddModal();
        catPage.fillForm(dynamicParent, "Active");
        catPage.clickCreate();

        SubCategoryPage subPage = new SubCategoryPage(driver);
        subPage.hardResetAndNavigate();
        String mainWindow = driver.getWindowHandle();
        subPage.clickAddSubCategory();

        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        subPage.fillAndSaveAddForm(dynamicChild, dynamicParent, "Active");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        driver.switchTo().window(mainWindow);

        catPage.navigateToMainCategory();
        catPage.searchFor(dynamicParent);
        catPage.deleteCategoryByIndex(1);

        catPage.clickClear();
        catPage.searchFor(dynamicParent);
        String result = catPage.getCategoryNameByIndex(1);

        Assert.assertEquals(result, dynamicParent, "DEFECT: System allowed deletion of '" + dynamicParent + "' even though it had a subcategory link!");
        Reporter.log("Dynamic Dependency Check Verified");

        subPage.hardResetAndNavigate();
        try {
            driver.navigate().refresh();
            WebElement delBtn = driver.findElement(By.xpath("//tr[td[contains(., '" + dynamicChild + "')]]//a[contains(@id, 'lnkDelete')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", delBtn);
            wait.until(ExpectedConditions.alertIsPresent()).accept();
            try { wait.until(ExpectedConditions.alertIsPresent()).accept(); } catch(Exception e) {}
        } catch (Exception e) {}
    }

    @Test(priority = 10)
    public void US2_MC_04_Task1_verifyImportButtonPresence() {
        catPage.navigateToMainCategory();
        WebElement importBtn = catPage.getImportButton();

        boolean isCorrect = importBtn.isDisplayed() && "Import".equalsIgnoreCase(importBtn.getAttribute("value"));
        if (isCorrect) Reporter.log("PASS: Import button is visible with correct label.");
        Assert.assertTrue(isCorrect, "Import button is either missing or has the wrong label!");
    }

    @Test(priority = 11, dependsOnMethods = "US2_MC_04_Task1_verifyImportButtonPresence")
    public void UC2_MC_04_Task2_verifyImportButtonFunctionality() {
        catPage.clickImport();
        Reporter.log("Action: Clicked the Import button.");
        boolean hasResponded = catPage.isImportResponseVisible();

        if (hasResponded) Reporter.log("PASS: Functional response detected.");
        else Reporter.log("FAIL: No UI response detected after clicking Import.");

        Assert.assertTrue(hasResponded, "Import button clicked but no Modal or Message appeared.");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        catPage.navigateToMainCategory();
        catPage.cleanUpAllTestArtifacts();
    }
}