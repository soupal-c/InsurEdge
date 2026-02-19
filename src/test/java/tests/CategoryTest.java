package tests;

import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import pages.CategoryPage;

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

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        catPage.navigateToMainCategory();
        catPage.cleanUpAllTestArtifacts();
    }
}