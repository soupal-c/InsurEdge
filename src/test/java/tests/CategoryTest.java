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
import org.openqa.selenium.support.ui.Select;

public class CategoryTest extends BaseTest {

    CategoryPage catPage;

    String autoName = "Auto_" + System.currentTimeMillis();
    String preEditName = "PreEdit_" + System.currentTimeMillis();
    String postEditName = "PostEdit_" + System.currentTimeMillis();
    String dupName = "Dup_" + System.currentTimeMillis();
    String deleteName = "Delete_" + System.currentTimeMillis();

    @BeforeClass
    public void setupPage() {
        catPage = new CategoryPage(driver);
    }

    @BeforeMethod
    public void navigate() {
        catPage.navigateToMainCategory();
    }

    // 1. UI Verification
    @Test(priority = 1)
    public void US2_MC_01_Task1_VerifyUI() {
        Assert.assertEquals(catPage.getPageTitle(), "Create Main Insurance Category");
        Reporter.log("UI Verified");
    }

    // 2. Row Icons
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

    // 3. Search
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

    // 4. Add Category
    @Test(priority = 4)
    public void US2_MC_02_Task2_AddCategory() {
        catPage.openAddModal();
        catPage.fillForm(autoName, "Active");
        catPage.clickCreate();

        catPage.searchFor(autoName);
        Assert.assertEquals(catPage.getCategoryNameByIndex(1), autoName);
        Reporter.log("Add Verified");
    }

    // 5. Edit Category
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

    // 6. Duplicate Check (Robust)
    @Test(priority = 6)
    public void US2_MC_02_Task4_DuplicateCheck() {
        try {
            catPage.openAddModal();
            catPage.fillForm(dupName, "Active");
            catPage.clickCreate();

            catPage.openAddModal();
            catPage.fillForm(dupName, "Active");

            // Expect failure (modal stays open)
            catPage.clickCreateFailureExpected();

            String error = catPage.getErrorMessage();
            Assert.assertTrue(error.contains("exists") || error.contains("Duplicate"));
            Reporter.log("Duplicate Verified");
        } finally {
            // ALWAYS refresh to close the stuck modal, so the next test can run
            driver.navigate().refresh();
        }
    }

    // 7. Verify Delete (Happy Path)
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

    // 8. Verify Delete Cancellation (New Test Case)
    @Test(priority = 8)
    public void US2_MC_03_Task2_DeleteCancellation() {
        // Create a category to try and delete
        String cancelName = "Cancel_" + System.currentTimeMillis();
        catPage.openAddModal();
        catPage.fillForm(cancelName, "Active");
        catPage.clickCreate();

        catPage.searchFor(cancelName);

        // Try to delete but click "Cancel" on alert
        catPage.deleteCategoryByIndexAndCancel(1);

        // Verify it still exists
        catPage.clickClear();
        catPage.searchFor(cancelName);
        Assert.assertEquals(catPage.getCategoryNameByIndex(1), cancelName, "Category should NOT be deleted after cancel!");
        Reporter.log("Delete Cancel Verified");
    }

    @Test(priority = 9)
    public void US2_MC_03_Task2_ValidateDependencyBlocking() {
        String dynamicParent = "Parent_" + System.currentTimeMillis();
        String dynamicChild = "Child_" + System.currentTimeMillis();

        // 1. Create the Main Category
        catPage.navigateToMainCategory();
        catPage.openAddModal();
        catPage.fillForm(dynamicParent, "Active");
        catPage.clickCreate();

        // 2. CREATE THE DEPENDENCY (Link Child to Parent)
        SubCategoryPage subPage = new SubCategoryPage(driver);
        subPage.hardResetAndNavigate();
        String mainWindow = driver.getWindowHandle(); // Save current window
        subPage.clickAddSubCategory();

        // Switch to the popup window
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        // Fill the form using the ID from your HTML
        subPage.fillAndSaveAddForm(dynamicChild, dynamicParent, "Active");

        // Handle the success alert
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.alertIsPresent()).accept();

        // Switch back to the main page
        driver.switchTo().window(mainWindow);

        // 3. Go back to Main Category and try to delete the Parent
        catPage.navigateToMainCategory();
        catPage.searchFor(dynamicParent);
        catPage.deleteCategoryByIndex(1);

        // 4. Verification: The Parent should NOT be deleted
        catPage.clickClear();
        catPage.searchFor(dynamicParent);
        String result = catPage.getCategoryNameByIndex(1);

        Assert.assertEquals(result, dynamicParent,
                "DEFECT: System allowed deletion of '" + dynamicParent + "' even though it had a subcategory link!");

        Reporter.log("Dynamic Dependency Check Verified");
    }
    @Test(priority = 10)
    public void US2_MC_04_Task1_verifyImportButtonPresence() {
        catPage.navigateToMainCategory();
        WebElement importBtn = catPage.getImportButton();

        boolean isCorrect = importBtn.isDisplayed() && "Import".equalsIgnoreCase(importBtn.getAttribute("value"));

        if (isCorrect) {
            Reporter.log("PASS: Import button is visible with correct label.");
        }

        Assert.assertTrue(isCorrect, "Import button is either missing or has the wrong label!");
    }

    @Test(priority = 11, dependsOnMethods = "US2_MC_04_Task1_verifyImportButtonPresence")
    public void UC2_MC_04_Task2_verifyImportButtonFunctionality() {
        // 3. Action
        catPage.clickImport();
        Reporter.log("Action: Clicked the Import button.");

        // 4. Functional Assertion
        boolean hasResponded = catPage.isImportResponseVisible();

        if (hasResponded) {
            Reporter.log("PASS: Functional response detected (Modal/Message appeared).");
        }
        else {
            Reporter.log("FAIL: No UI response detected after clicking Import.");
        }

        Assert.assertTrue(hasResponded, "Import button clicked but no Modal or Message appeared.");
    }
    @AfterClass(alwaysRun = true)
    public void cleanup() {
        catPage.navigateToMainCategory();
        catPage.cleanUpAllTestArtifacts();
    }
}