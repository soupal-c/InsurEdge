package tests;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import pages.MainCategoryPage;
import pages.SubCategoryPage;

public class MainCategoryTest extends BaseTest {

    MainCategoryPage mainCatPage;

    String autoName = nameGenerator("Auto_");
    String preEditName = nameGenerator("PreEdit_");
    String postEditName = nameGenerator("PostEdit_");
    String dupName = nameGenerator("Dup_");
    String deleteName = nameGenerator("Delete_");

    @BeforeClass
    public void setupPage() {
        mainCatPage = new MainCategoryPage(driver);
    }

    @BeforeMethod
    public void navigate() {
        mainCatPage.navigateToMainCategory();
    }

    @Test(priority = 1)
    public void US2_MC_01_Task1_VerifyUI() {
        Assert.assertEquals(mainCatPage.getPageTitle(), "Create Main Insurance Category");
        Reporter.log("Task 1: UI Verified");
    }

    @Test(priority = 2)
    public void US2_MC_01_Task2_VerifyRowActionIcons() {
        if (mainCatPage.getCategoryNameByIndex(1) == null) {
            mainCatPage.openAddModal();
            mainCatPage.fillForm(autoName, "Active");
            mainCatPage.clickCreate();
        }
        Assert.assertTrue(mainCatPage.areRowActionIconsVisible(), "Task 2: Icons missing on first row!");
        Reporter.log("Task 2: Row Action Icons Verified");
    }

    @Test(priority = 3)
    public void US2_MC_02_Task1_SearchFunctionality() {
        String existingName = mainCatPage.getCategoryNameByIndex(1);
        mainCatPage.searchFor(existingName);
        Assert.assertEquals(mainCatPage.getCategoryNameByIndex(1), existingName);
        mainCatPage.clickClear();
        Reporter.log("Task 1: Search Verified");
    }

    @Test(priority = 4)
    public void US2_MC_02_Task2_AddCategory() {
        mainCatPage.openAddModal();
        mainCatPage.fillForm(autoName, "Active");
        mainCatPage.clickCreate();

        mainCatPage.searchFor(autoName);
        Assert.assertEquals(mainCatPage.getCategoryNameByIndex(1), autoName);
        Reporter.log("Task 2: Add Category Verified");
    }

    @Test(priority = 5)
    public void US2_MC_02_Task3_EditCategory() {
        mainCatPage.openAddModal();
        mainCatPage.fillForm(preEditName, "Active");
        mainCatPage.clickCreate();

        mainCatPage.searchFor(preEditName);
        mainCatPage.editCategoryByIndex(1, postEditName);

        mainCatPage.clickClear();
        mainCatPage.searchFor(postEditName);
        Assert.assertEquals(mainCatPage.getCategoryNameByIndex(1), postEditName);
        Reporter.log("Task 3: Edit Category Verified");
    }

    @Test(priority = 6)
    public void US2_MC_02_Task4_DuplicateCheck() {
        try {
            mainCatPage.openAddModal();
            mainCatPage.fillForm(dupName, "Active");
            mainCatPage.clickCreate();

            mainCatPage.openAddModal();
            mainCatPage.fillForm(dupName, "Active");
            mainCatPage.clickCreateFailureExpected();

            String error = mainCatPage.getErrorMessage();
            Assert.assertTrue(error.contains("exists") || error.contains("Duplicate"));
            Reporter.log("Task 4: Duplicate Verification Passed");
        } finally {
            driver.navigate().refresh();
        }
    }

    @Test(priority = 7)
    public void US2_MC_03_Task1_VerifyDelete() {
        mainCatPage.openAddModal();
        mainCatPage.fillForm(deleteName, "Active");
        mainCatPage.clickCreate();

        mainCatPage.searchFor(deleteName);
        mainCatPage.deleteCategoryByIndex(1);

        mainCatPage.clickClear();
        mainCatPage.searchFor(deleteName);
        Assert.assertNull(mainCatPage.getCategoryNameByIndex(1), "Task 1: Category should be deleted!");
        Reporter.log("Task 1: Delete Logic Verified");
    }

    @Test(priority = 8)
    public void US2_MC_03_Task1_DeleteCancellation() {
        String cancelName = nameGenerator("Cancel_");
        mainCatPage.openAddModal();
        mainCatPage.fillForm(cancelName, "Active");
        mainCatPage.clickCreate();

        mainCatPage.searchFor(cancelName);
        mainCatPage.deleteCategoryByIndexAndCancel(1);

        mainCatPage.clickClear();
        mainCatPage.searchFor(cancelName);
        Assert.assertEquals(mainCatPage.getCategoryNameByIndex(1), cancelName, "Category should NOT be deleted after cancel!");
        Reporter.log("Task 1: Delete Cancel Verified");
    }

    @Test(priority = 9)
    public void US2_MC_03_Task2_ValidateDependencyBlocking() {
        String parent = nameGenerator("Parent_");
        String child = nameGenerator("Child_");

        mainCatPage.openAddModal();
        mainCatPage.fillForm(parent, "Active");
        mainCatPage.clickCreate();

        SubCategoryPage subPage = new SubCategoryPage(driver);
        subPage.createSubCategory(parent, child, "Active");

        mainCatPage.navigateToMainCategory();
        mainCatPage.searchFor(parent);
        mainCatPage.deleteCategoryByIndex(1);

        mainCatPage.clickClear();
        mainCatPage.searchFor(parent);

        // This fails properly, catching the system defect you identified
        Assert.assertEquals(mainCatPage.getCategoryNameByIndex(1), parent, "Task 2: System allowed deletion of parent with dependency!");
        Reporter.log("Task 2: Dependency Check Verified");
    }

    @Test(priority = 10)
    public void US2_MC_04_Task1_verifyImportButtonPresence() {
        mainCatPage.navigateToMainCategory();
        WebElement importBtn = mainCatPage.getImportButton();

        // This checks if the button is visible, which it is. So Task 1 passes.
        boolean isCorrect = importBtn.isDisplayed() && "Import".equalsIgnoreCase(importBtn.getAttribute("value"));
        Assert.assertTrue(isCorrect, "Task 1: Import button is missing or has wrong label!");
        Reporter.log("Task 1: Import button is visible");
    }

    @Test(priority = 11, dependsOnMethods = "US2_MC_04_Task1_verifyImportButtonPresence")
    public void UC2_MC_04_Task2_verifyImportButtonFunctionality() {
        mainCatPage.clickImport();
        // This will now accurately return false, failing the test to highlight the broken functionality!
        Assert.assertTrue(mainCatPage.isImportResponseVisible(), "Task 2: No UI response detected after clicking Import.");
        Reporter.log("Task 2: Import functionality triggered successfully.");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        if (driver != null && mainCatPage != null) {
            SubCategoryPage subPage = new SubCategoryPage(driver);
            subPage.cleanUpSubCategoryArtifacts();
            mainCatPage.navigateToMainCategory();
            mainCatPage.cleanUpAllTestArtifacts();
        }
    }
}